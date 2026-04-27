import sourceRegisterJson from '../../../data/source-register.json';
import type { CrowdPredictability, DataStatus, LiveCrowdSnapshot, Location } from '@/types/domain';
import { getCrowdLabel, getCrowdSummary } from '@/utils/crowd';

interface RawSourceRegisterFile {
  sources: Array<{
    datasetId: string;
    accessPattern: string;
  }>;
}

interface PastHourCountRecord {
  location_id: number | string;
  sensing_datetime: string;
  total_of_directions: number | string;
}

interface LatestSensorReading {
  sensorId: number;
  observedAt: string;
  rawCount: number;
}

interface SensorVariation {
  value: CrowdPredictability;
  coefficient: number;
}

interface CrowdCalibration {
  score1Max: number;
  score2Max: number;
  score3Max: number;
}

export interface LiveLocationRefreshResult {
  locations: Location[];
  dataStatus: DataStatus;
  syncedAt: string | null;
  source?: 'backend' | 'direct';
}

const sourceRegister = sourceRegisterJson as RawSourceRegisterFile;
const pastHourEndpoint =
  sourceRegister.sources.find(
    (source) => source.datasetId === 'pedestrian-counting-system-past-hour-counts-per-minute',
  )?.accessPattern ??
  'https://data.melbourne.vic.gov.au/api/explore/v2.1/catalog/datasets/pedestrian-counting-system-past-hour-counts-per-minute/exports/json?lang=en&timezone=Australia%2FMelbourne';

function coerceNumber(value: number | string | null | undefined): number | null {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value;
  }

  if (typeof value === 'string' && value.trim() !== '') {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
  }

  return null;
}

function toLatestSensorReadings(records: PastHourCountRecord[]): Map<number, LatestSensorReading> {
  const latestBySensor = new Map<number, LatestSensorReading>();

  for (const record of records) {
    const sensorId = coerceNumber(record.location_id);
    const rawCount = coerceNumber(record.total_of_directions);

    if (sensorId === null || rawCount === null || !record.sensing_datetime) {
      continue;
    }

    const current = latestBySensor.get(sensorId);

    if (!current || Date.parse(record.sensing_datetime) > Date.parse(current.observedAt)) {
      latestBySensor.set(sensorId, {
        sensorId,
        observedAt: record.sensing_datetime,
        rawCount,
      });
    }
  }

  return latestBySensor;
}

function toSensorSeries(records: PastHourCountRecord[]): Map<number, number[]> {
  const seriesBySensor = new Map<number, number[]>();

  for (const record of records) {
    const sensorId = coerceNumber(record.location_id);
    const rawCount = coerceNumber(record.total_of_directions);

    if (sensorId === null || rawCount === null) {
      continue;
    }

    const currentSeries = seriesBySensor.get(sensorId) ?? [];
    currentSeries.push(rawCount);
    seriesBySensor.set(sensorId, currentSeries);
  }

  return seriesBySensor;
}

function percentile(sortedValues: number[], ratio: number): number {
  if (!sortedValues.length) {
    return 0;
  }

  if (sortedValues.length === 1) {
    return sortedValues[0];
  }

  const clampedRatio = Math.min(Math.max(ratio, 0), 1);
  const index = (sortedValues.length - 1) * clampedRatio;
  const lowerIndex = Math.floor(index);
  const upperIndex = Math.ceil(index);
  const weight = index - lowerIndex;

  if (lowerIndex === upperIndex) {
    return sortedValues[lowerIndex];
  }

  return sortedValues[lowerIndex] + (sortedValues[upperIndex] - sortedValues[lowerIndex]) * weight;
}

function buildCalibration(readings: LatestSensorReading[]): CrowdCalibration {
  const sortedCounts = readings
    .map((reading) => reading.rawCount)
    .filter((count) => Number.isFinite(count))
    .sort((left, right) => left - right);

  return {
    // Use percentile-style fallback heuristics until historical calibration exists.
    score1Max: percentile(sortedCounts, 0.2),
    score2Max: percentile(sortedCounts, 0.4),
    score3Max: percentile(sortedCounts, 0.8),
  };
}

function deriveSensoryScore(rawCount: number, calibration: CrowdCalibration): Location['sensoryScore'] {
  if (rawCount <= calibration.score1Max) {
    return 1;
  }

  if (rawCount <= calibration.score2Max) {
    return 2;
  }

  if (rawCount <= calibration.score3Max) {
    return 3;
  }

  return 4;
}

function deriveCrowdLevel(score: Location['sensoryScore']): LiveCrowdSnapshot['crowdLevel'] {
  if (score <= 2) {
    return 'low';
  }

  if (score === 3) {
    return 'moderate';
  }

  return 'high';
}

function minutesSince(observedAt: string, now = Date.now()): number {
  const observedAtMs = Date.parse(observedAt);

  if (Number.isNaN(observedAtMs)) {
    return 0;
  }

  return Math.max(0, Math.round((now - observedAtMs) / 60_000));
}

function derivePredictabilityFromSeries(values: number[]): SensorVariation | null {
  if (values.length < 3) {
    return null;
  }

  const average = values.reduce((sum, value) => sum + value, 0) / values.length;

  if (!Number.isFinite(average) || average <= 0) {
    return null;
  }

  const variance =
    values.reduce((sum, value) => sum + (value - average) * (value - average), 0) / values.length;
  const standardDeviation = Math.sqrt(variance);
  const coefficient = standardDeviation / average;

  if (coefficient <= 0.2) {
    return { value: 'stable', coefficient };
  }

  if (coefficient <= 0.45) {
    return { value: 'variable', coefficient };
  }

  return { value: 'event-sensitive', coefficient };
}

function formatSnapshotShortLabel(status: DataStatus, observedAt: string, now = Date.now()): string {
  const minutesAgo = minutesSince(observedAt, now);

  if (status === 'error') {
    return 'Using last local reading';
  }

  if (status === 'empty') {
    return 'No live reading found';
  }

  if (status === 'stale') {
    return `Updated ${minutesAgo} min ago - Stale`;
  }

  return `Updated ${minutesAgo} min ago`;
}

function mergeLocationWithLiveReading(
  location: Location,
  latestBySensor: Map<number, LatestSensorReading>,
  seriesBySensor: Map<number, number[]>,
  calibration: CrowdCalibration,
  freshWithinMinutes: number,
  now = Date.now(),
): Location {
  const readings = location.sensorIds
    .map((sensorId) => latestBySensor.get(sensorId))
    .filter((reading): reading is LatestSensorReading => Boolean(reading));

  if (!readings.length) {
    return location;
  }

  const rawCount = Math.round(
    readings.reduce((total, reading) => total + reading.rawCount, 0) / readings.length,
  );
  const observedAt = readings
    .map((reading) => reading.observedAt)
    .sort((left, right) => Date.parse(right) - Date.parse(left))[0];
  const freshnessMinutes = minutesSince(observedAt, now);
  const status: DataStatus = freshnessMinutes > freshWithinMinutes ? 'stale' : 'fresh';
  const sensoryScore = deriveSensoryScore(rawCount, calibration);
  const crowdLevel = deriveCrowdLevel(sensoryScore);
  const primarySensorId = location.sensorIds.find((sensorId) => latestBySensor.has(sensorId)) ?? null;
  const predictabilitySignals = location.sensorIds
    .map((sensorId) => derivePredictabilityFromSeries(seriesBySensor.get(sensorId) ?? []))
    .filter((signal): signal is SensorVariation => Boolean(signal));
  const averageVariation =
    predictabilitySignals.reduce((sum, signal) => sum + signal.coefficient, 0) /
    Math.max(predictabilitySignals.length, 1);
  const crowdPredictability =
    averageVariation <= 0.2
      ? 'stable'
      : averageVariation <= 0.45
        ? 'variable'
        : 'event-sensitive';

  return {
    ...location,
    sensoryProfile: {
      ...location.sensoryProfile,
      crowdPredictability: predictabilitySignals.length
        ? crowdPredictability
        : location.sensoryProfile.crowdPredictability,
      signalSources: predictabilitySignals.length
        ? {
            ...location.sensoryProfile.signalSources,
            crowdPredictability: {
              source: 'derived',
              observedAt,
              note: 'Derived from official pedestrian count variability across the last hour.',
            },
          }
        : location.sensoryProfile.signalSources,
    },
    sensoryScore,
    crowdSnapshot: {
      sensorId: primarySensorId,
      observedAt,
      rawCount,
      crowdLevel,
      status,
      label: getCrowdLabel(crowdLevel),
      shortLabel: formatSnapshotShortLabel(status, observedAt, now),
      summary: getCrowdSummary(crowdLevel),
    },
  };
}

export async function fetchLiveLocationRefresh(
  locations: Location[],
  freshWithinMinutes: number,
  fetchImpl: typeof fetch = fetch,
): Promise<LiveLocationRefreshResult> {
  const response = await fetchImpl(pastHourEndpoint);

  if (!response.ok) {
    throw new Error(`Unable to fetch live counts: ${response.status}`);
  }

  const payload = (await response.json()) as PastHourCountRecord[];
  const latestBySensor = toLatestSensorReadings(Array.isArray(payload) ? payload : []);
  const seriesBySensor = toSensorSeries(Array.isArray(payload) ? payload : []);
  const latestReadings = [...latestBySensor.values()];

  if (!latestReadings.length) {
    return {
      locations,
      dataStatus: 'empty',
      syncedAt: null,
      source: 'direct',
    };
  }

  const calibration = buildCalibration(latestReadings);
  const now = Date.now();
  const mergedLocations = locations.map((location) =>
    mergeLocationWithLiveReading(
      location,
      latestBySensor,
      seriesBySensor,
      calibration,
      freshWithinMinutes,
      now,
    ),
  );
  const updatedLocations = mergedLocations.filter(
    (location) => latestBySensor.has(location.crowdSnapshot.sensorId ?? -1),
  );

  if (!updatedLocations.length) {
    return {
      locations,
      dataStatus: 'empty',
      syncedAt: null,
      source: 'direct',
    };
  }

  const syncedAt = updatedLocations
    .map((location) => location.crowdSnapshot.observedAt)
    .sort((left, right) => Date.parse(right) - Date.parse(left))[0];
  const hasFreshReading = updatedLocations.some((location) => location.crowdSnapshot.status === 'fresh');

  return {
    locations: mergedLocations,
    dataStatus: hasFreshReading ? 'fresh' : 'stale',
    syncedAt,
    source: 'direct',
  };
}
