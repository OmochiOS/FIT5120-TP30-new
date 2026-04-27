import sourceRegisterJson from '../../../data/source-register.json';
import type { CrowdLevel, Location } from '@/types/domain';

interface RawSourceRegisterFile {
  sources: Array<{
    datasetId: string;
    accessPattern: string;
  }>;
}

interface Coordinate {
  lat: number;
  lon: number;
}

interface NoiseReading {
  coordinates: Coordinate;
  observedAt: string | null;
  noise: number;
}

interface RecordsEnvelope {
  results?: unknown[];
}

const sourceRegister = sourceRegisterJson as RawSourceRegisterFile;
const microclimateEndpoint =
  sourceRegister.sources.find((source) => source.datasetId === 'microclimate-sensor-readings')
    ?.accessPattern ??
  'https://data.melbourne.vic.gov.au/api/explore/v2.1/catalog/datasets/microclimate-sensors-data/records?limit=100';

function buildMicroclimateEndpoint(baseEndpoint: string): string {
  if (baseEndpoint.includes('limit=')) {
    return baseEndpoint.replace(/limit=\d+/i, 'limit=100');
  }

  return `${baseEndpoint}${baseEndpoint.includes('?') ? '&' : '?'}limit=100`;
}

function coerceNumber(value: unknown): number | null {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value;
  }

  if (typeof value === 'string' && value.trim() !== '') {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
  }

  return null;
}

function extractObservedAt(record: Record<string, unknown>): string | null {
  const timeKeys = ['timestamp', 'datetime', 'observed', 'measured', 'date', 'time'];

  for (const [key, value] of Object.entries(record)) {
    const normalizedKey = key.toLowerCase();

    if (!timeKeys.some((candidate) => normalizedKey.includes(candidate))) {
      continue;
    }

    if (typeof value === 'string' && value.trim() !== '' && !Number.isNaN(Date.parse(value))) {
      return value;
    }
  }

  return null;
}

function extractCoordinates(record: Record<string, unknown>): Coordinate | null {
  const latitudeKey = Object.keys(record).find((key) => key.toLowerCase().includes('lat'));
  const longitudeKey = Object.keys(record).find((key) => key.toLowerCase().includes('lon'));

  if (latitudeKey && longitudeKey) {
    const lat = coerceNumber(record[latitudeKey]);
    const lon = coerceNumber(record[longitudeKey]);

    if (lat !== null && lon !== null) {
      return { lat, lon };
    }
  }

  for (const value of Object.values(record)) {
    if (!value || typeof value !== 'object') {
      continue;
    }

    const nestedRecord = value as Record<string, unknown>;
    const lat = coerceNumber(nestedRecord.lat ?? nestedRecord.latitude);
    const lon = coerceNumber(nestedRecord.lon ?? nestedRecord.lng ?? nestedRecord.longitude);

    if (lat !== null && lon !== null) {
      return { lat, lon };
    }

    if (Array.isArray(value) && value.length >= 2) {
      const [first, second] = value;
      const arrayLat = coerceNumber(first);
      const arrayLon = coerceNumber(second);

      if (arrayLat !== null && arrayLon !== null) {
        return { lat: arrayLat, lon: arrayLon };
      }
    }
  }

  return null;
}

function extractNoise(record: Record<string, unknown>): number | null {
  for (const [key, value] of Object.entries(record)) {
    if (!key.toLowerCase().includes('noise')) {
      continue;
    }

    const noiseValue = coerceNumber(value);

    if (noiseValue !== null) {
      return noiseValue;
    }
  }

  return null;
}

function toNoiseReadings(payload: unknown): NoiseReading[] {
  const records = Array.isArray(payload)
    ? payload
    : Array.isArray((payload as RecordsEnvelope | null)?.results)
      ? (payload as RecordsEnvelope).results ?? []
      : [];

  return records
    .map((entry) => {
      if (!entry || typeof entry !== 'object') {
        return null;
      }

      const record = entry as Record<string, unknown>;
      const coordinates = extractCoordinates(record);
      const noise = extractNoise(record);

      if (!coordinates || noise === null) {
        return null;
      }

      return {
        coordinates,
        noise,
        observedAt: extractObservedAt(record),
      };
    })
    .filter((reading): reading is NoiseReading => Boolean(reading));
}

function distanceInKilometres(left: Coordinate, right: Coordinate): number {
  const earthRadiusKm = 6371;
  const deltaLat = ((right.lat - left.lat) * Math.PI) / 180;
  const deltaLon = ((right.lon - left.lon) * Math.PI) / 180;
  const leftLatRadians = (left.lat * Math.PI) / 180;
  const rightLatRadians = (right.lat * Math.PI) / 180;

  const haversine =
    Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
    Math.cos(leftLatRadians) *
      Math.cos(rightLatRadians) *
      Math.sin(deltaLon / 2) *
      Math.sin(deltaLon / 2);

  return 2 * earthRadiusKm * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
}

function deriveNoiseLevel(noise: number): CrowdLevel {
  if (noise <= 55) {
    return 'low';
  }

  if (noise <= 68) {
    return 'moderate';
  }

  return 'high';
}

function supportsAmbientNoiseRefresh(location: Location): boolean {
  if (location.dataSource === 'backend') {
    return false;
  }

  return location.category === 'Park' || location.locationId === 'federation-square';
}

export async function fetchAutomatedProfileRefresh(
  locations: Location[],
  fetchImpl: typeof fetch = fetch,
): Promise<Location[]> {
  const response = await fetchImpl(buildMicroclimateEndpoint(microclimateEndpoint));

  if (!response.ok) {
    throw new Error(`Unable to fetch microclimate sensor readings: ${response.status}`);
  }

  const payload = await response.json();
  const noiseReadings = toNoiseReadings(payload);

  if (!noiseReadings.length) {
    return locations;
  }

  return locations.map((location) => {
    if (!supportsAmbientNoiseRefresh(location)) {
      return location;
    }

    const nearestReading = noiseReadings
      .map((reading) => ({
        reading,
        distanceKm: distanceInKilometres(location.coordinates, reading.coordinates),
      }))
      .sort((left, right) => left.distanceKm - right.distanceKm)[0];

    if (!nearestReading || nearestReading.distanceKm > 2) {
      return location;
    }

    const noiseLevel = deriveNoiseLevel(nearestReading.reading.noise);
    const roundedNoise = Math.round(nearestReading.reading.noise);
    const roundedDistance = Math.round(nearestReading.distanceKm * 1000);

    return {
      ...location,
      sensoryProfile: {
        ...location.sensoryProfile,
        noiseLevel,
        signalSources: {
          ...location.sensoryProfile.signalSources,
          noiseLevel: {
            source: 'official',
            observedAt: nearestReading.reading.observedAt,
            note: `Official ambient noise reading nearby: ${roundedNoise} dBA (${roundedDistance} m away).`,
          },
        },
      },
    };
  });
}
