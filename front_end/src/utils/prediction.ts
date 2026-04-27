import type {
  CrowdLevel,
  DayLabel,
  Location,
  LocationPrediction,
  PredictionDayPlan,
  PredictionRow,
  PredictionSnapshot,
} from '@/types/domain';

const HISTORICAL_DATASET_URL =
  'https://data.melbourne.vic.gov.au/api/explore/v2.1/catalog/datasets/pedestrian-counting-system-monthly-counts-per-hour/records';
const HISTORICAL_PAGE_LIMIT = 100;
const HISTORICAL_MAX_RECORDS = 300;

const BUCKETS = [
  { label: '7-9am', startHour: 7, endHour: 9 },
  { label: '9-11am', startHour: 9, endHour: 11 },
  { label: '11am-1pm', startHour: 11, endHour: 13 },
  { label: '1-3pm', startHour: 13, endHour: 15 },
  { label: '3-5pm', startHour: 15, endHour: 17 },
  { label: '5-7pm', startHour: 17, endHour: 19 },
  { label: '7-9pm', startHour: 19, endHour: 21 },
  { label: '9-11pm', startHour: 21, endHour: 23 },
] as const;

const DAY_ORDER: DayLabel[] = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

const BASE_ROWS: PredictionRow[] = [
  { time: '7-9am', level: 'low', estimate: 20, recommendation: 'Great' },
  { time: '9-11am', level: 'low', estimate: 45, recommendation: 'Good' },
  { time: '11am-1pm', level: 'moderate', estimate: 120, recommendation: 'Manageable' },
  { time: '1-3pm', level: 'moderate', estimate: 150, recommendation: 'Some activity' },
  { time: '3-5pm', level: 'high', estimate: 280, recommendation: 'May overwhelm' },
  { time: '5-7pm', level: 'high', estimate: 310, recommendation: 'Peak avoid' },
  { time: '7-9pm', level: 'moderate', estimate: 130, recommendation: 'Winding down' },
  { time: '9-11pm', level: 'low', estimate: 30, recommendation: 'Quiet' },
];

const ESTIMATE_MULTIPLIER: Record<string, number> = {
  'flagstaff-gardens': 0.55,
  'state-library-victoria': 0.72,
  'fitzroy-gardens': 0.64,
  'federation-square': 1.08,
  'melbourne-central': 1.18,
  'carlton-gardens': 0.6,
  'docklands-library': 0.68,
  'treasury-gardens': 0.58,
};

const QUIETEST_NOTES: Record<string, string> = {
  'flagstaff-gardens': 'Quietest: weekday mornings',
  'state-library-victoria': 'Quietest: early weekday study hours',
  'fitzroy-gardens': 'Quietest: weekday mornings',
  'federation-square': 'Quietest: early mornings before events',
  'melbourne-central': 'Quietest: short morning windows only',
  'carlton-gardens': 'Quietest: weekday mornings',
  'docklands-library': 'Quietest: late morning before school finish',
  'treasury-gardens': 'Quietest: weekday mornings',
};

const SEARCH_ALIASES: Record<string, string[]> = {
  'flagstaff-gardens': ['flagstaff', 'west melbourne'],
  'state-library-victoria': ['state library', 'library', 'melbourne cbd', '3000', 'swanston'],
  'fitzroy-gardens': ['fitzroy', 'east melbourne'],
  'federation-square': ['federation', 'fed square', 'southbank', 'flinders'],
  'melbourne-central': ['melbourne central', 'la trobe', '3000'],
  'carlton-gardens': ['carlton', '3053', 'museum'],
  'docklands-library': ['docklands', 'library at the dock', 'victoria harbour'],
  'treasury-gardens': ['treasury', 'wellington parade'],
  // backend numeric IDs
  '3': ['melbourne central', 'cbd', 'melbourne cbd'],
  '28': ['the arts centre', 'arts centre', 'southbank', 'south bank'],
  '31': ['lygon st', 'lygon street', 'carlton'],
};

const AREA_ALIAS_RULES = [
  {
    aliases: ['melbourne cbd', 'cbd', 'city'],
    keywords: [
      'bourke',
      'collins',
      'elizabeth',
      'swanston',
      'flinders',
      'qv',
      'town hall',
      'melbourne central',
      'city square',
      'city library',
    ],
  },
  {
    aliases: ['southbank'],
    keywords: [
      'princes bridge',
      'arts centre',
      'sandridge bridge',
      'boyd',
      'birrarung marr',
      'queens bridge',
      'acmi',
      'hammer hall',
    ],
  },
  {
    aliases: ['carlton'],
    keywords: ['lygon', 'pelham', 'faraday', 'tin alley', 'lincoln', 'queensberry', 'rmit', 'city baths'],
  },
  {
    aliases: ['docklands'],
    keywords: ['new quay', 'waterfront city', 'victoria point', 'harbour esplanade', 'library at the dock', 'docklands'],
  },
  {
    aliases: ['north melbourne'],
    keywords: ['north melbourne', 'macaulay', 'errol', 'kensington', 'southern cross'],
  },
  {
    aliases: ['fitzroy', 'east melbourne'],
    keywords: ['fitzroy garden', 'fitzroy gardens', 'east melbourne'],
  },
] as const;

type HistoricalRecord = Record<string, unknown>;

type BucketAggregate = {
  sum: number;
  count: number;
};

type ParsedHistoricalRecord = {
  count: number;
  hour: number;
  dayShort: string | null;
  observedAt: string | null;
};

export const predictionPopularSearches = [
  { label: 'Melbourne CBD', query: 'Melbourne Central' },
  { label: 'Southbank', query: 'The Arts Centre' },
  { label: 'Carlton', query: 'Lygon St' },
];

export function getLocationPrediction(location: Location): LocationPrediction {
  const multiplier = ESTIMATE_MULTIPLIER[location.locationId] ?? 1;
  const rows = BASE_ROWS.map((row) => ({
    ...row,
    estimate: Math.max(10, Math.round(row.estimate * multiplier)),
  }));
  const fallbackQuietestNote =
    QUIETEST_NOTES[location.locationId] ?? 'Quietest: weekday mornings';
  const fallbackDayPlans = Object.fromEntries(
    DAY_ORDER.map((day) => [
      day,
      {
        rows: rows.map((row) => ({ ...row })),
        timeBar: rows.map((row) => ({
          time: row.time,
          level: row.level,
        })),
        quietestNote: fallbackQuietestNote,
      },
    ]),
  ) as LocationPrediction['dayPlans'];

  return {
    rows,
    timeBar: rows.map((row) => ({
      time: row.time,
      level: row.level,
    })),
    quietestNote: fallbackQuietestNote,
    activeDay: 'Tue',
    dayPlans: fallbackDayPlans,
  };
}

export async function fetchLocationPrediction(
  location: Location,
  fetchImpl: typeof fetch = fetch,
): Promise<PredictionSnapshot> {
  const fallbackPrediction = getLocationPrediction(location);

  try {
    const records = await fetchHistoricalRecords(location.sensorIds, fetchImpl);
    const parsedRecords = records
      .map(parseHistoricalRecord)
      .filter((record): record is ParsedHistoricalRecord => record !== null);

    if (!parsedRecords.length) {
      return {
        prediction: fallbackPrediction,
        status: 'empty',
        source: 'fallback',
        syncedAt: null,
        note: 'Not enough official count history matched this place yet. Showing an estimated visit pattern for now.',
      };
    }

    const dynamicPrediction = buildPredictionFromRecords(parsedRecords, fallbackPrediction);
    const latestObservedAt = getLatestObservedAt(parsedRecords);

    return {
      prediction: dynamicPrediction,
      status: 'fresh',
      source: 'official',
      syncedAt: latestObservedAt,
      note: 'Based on historical pedestrian counts from City of Melbourne open data.',
    };
  } catch (error) {
    console.error('Historical prediction refresh failed.', error);

    return {
      prediction: fallbackPrediction,
      status: 'error',
      source: 'fallback',
      syncedAt: null,
      note: 'Official count history is temporarily unavailable. Showing an estimated visit pattern for now.',
    };
  }
}

export function findPredictionLocation(locations: Location[], query: string): Location | undefined {
  const normalized = normalizeSearchText(query);

  if (!normalized) {
    return undefined;
  }

  const rankedLocations = locations
    .map((location) => ({
      location,
      score: scorePredictionLocationMatch(location, normalized),
    }))
    .filter((candidate) => candidate.score > 0)
    .sort((left, right) => right.score - left.score);

  return rankedLocations[0]?.location;
}

function scorePredictionLocationMatch(location: Location, normalizedQuery: string): number {
  const searchTerms = buildPredictionSearchTerms(location);
  let bestScore = 0;

  for (const term of searchTerms) {
    if (term === normalizedQuery) {
      bestScore = Math.max(bestScore, 120);
      continue;
    }

    if (term.startsWith(normalizedQuery)) {
      bestScore = Math.max(bestScore, 90);
      continue;
    }

    if (term.includes(normalizedQuery)) {
      bestScore = Math.max(bestScore, 70);
      continue;
    }

    if (normalizedQuery.includes(term) && term.length >= 4) {
      bestScore = Math.max(bestScore, 50);
    }
  }

  return bestScore;
}

function buildPredictionSearchTerms(location: Location): string[] {
  const normalizedName = normalizeSearchText(location.name);
  const terms = new Set<string>([
    normalizedName,
    normalizeSearchText(location.addressOrArea),
    normalizeSearchText(location.displayType),
    ...(SEARCH_ALIASES[location.locationId] ?? []).map(normalizeSearchText),
  ]);

  for (const rule of AREA_ALIAS_RULES) {
    if (rule.keywords.some((keyword) => normalizedName.includes(keyword))) {
      for (const alias of rule.aliases) {
        terms.add(alias);
      }
    }
  }

  return [...terms].filter(Boolean);
}

function normalizeSearchText(value: string): string {
  return value
    .trim()
    .toLowerCase()
    .replace(/[^\w\s-]/g, ' ')
    .replace(/\s+/g, ' ');
}

function buildHistoricalDatasetUrl(sensorIds: number[]): string {
  const params = new URLSearchParams({
    limit: String(HISTORICAL_PAGE_LIMIT),
    lang: 'en',
    timezone: 'Australia/Melbourne',
  });

  if (sensorIds.length) {
    params.set('where', `location_id in (${sensorIds.join(',')})`);
  }

  return `${HISTORICAL_DATASET_URL}?${params.toString()}`;
}

async function fetchHistoricalRecords(
  sensorIds: number[],
  fetchImpl: typeof fetch,
): Promise<HistoricalRecord[]> {
  const records: HistoricalRecord[] = [];

  for (let offset = 0; offset < HISTORICAL_MAX_RECORDS; offset += HISTORICAL_PAGE_LIMIT) {
    const response = await fetchImpl(`${buildHistoricalDatasetUrl(sensorIds)}&offset=${offset}`, {
      headers: {
        Accept: 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`Historical counts request failed with ${response.status}.`);
    }

    const payload = (await response.json()) as { results?: HistoricalRecord[] };
    const pageResults = Array.isArray(payload.results) ? payload.results : [];

    records.push(...pageResults);

    if (pageResults.length < HISTORICAL_PAGE_LIMIT) {
      break;
    }
  }

  return records;
}

function parseHistoricalRecord(record: HistoricalRecord): ParsedHistoricalRecord | null {
  const count = pickNumber(record, [
    'pedestriancount',
    'hourly_counts',
    'hourly_count',
    'count',
    'count_per_hour',
    'pedestrian_count',
  ]);

  if (count === null) {
    return null;
  }

  const directHour = pickHour(record, ['hourday', 'hour', 'hour_of_day', 'hour_beginning_hour', 'time']);
  const observedAtBase =
    pickString(record, [
      'sensing_date',
      'hour_beginning',
      'date_time',
      'datetime',
      'timestamp',
      'date',
      'local_time',
    ]) ?? null;
  const localHourFromObservedAt = observedAtBase ? parseHourValue(observedAtBase) : null;

  const derivedDate = observedAtBase ? new Date(observedAtBase) : null;
  const derivedHour =
    directHour ??
    localHourFromObservedAt ??
    (derivedDate && !Number.isNaN(derivedDate.getTime()) ? derivedDate.getHours() : null);

  if (derivedHour === null) {
    return null;
  }

  const dayShort =
    normalizeDayLabel(
      pickString(record, ['day', 'day_of_week', 'weekday']) ??
        (derivedDate && !Number.isNaN(derivedDate.getTime())
          ? derivedDate.toLocaleDateString('en-AU', { weekday: 'short' })
          : null),
    ) ?? null;

  const observedAt =
    observedAtBase && derivedHour !== null
      ? `${observedAtBase}${observedAtBase.includes('T') ? '' : `T${String(derivedHour).padStart(2, '0')}:00:00`}`
      : observedAtBase;

  return {
    count,
    hour: derivedHour,
    dayShort,
    observedAt,
  };
}

function buildPredictionFromRecords(
  records: ParsedHistoricalRecord[],
  fallbackPrediction: LocationPrediction,
): LocationPrediction {
  const activeDay = getPreferredDay(records) ?? fallbackPrediction.activeDay;
  const bucketAveragesByDay = buildBucketAveragesByDay(records);
  const bucketAveragesAllDays = buildBucketAverages(records);
  const fallbackDayPlans = fallbackPrediction.dayPlans ?? {};
  const dayPlans = Object.fromEntries(
    DAY_ORDER.map((day) => [
      day,
      buildDayPlan(
        day,
        fallbackPrediction.rows,
        bucketAveragesByDay.get(day),
        bucketAveragesAllDays,
        fallbackDayPlans[day],
        fallbackPrediction.quietestNote,
      ),
    ]),
  ) as Record<DayLabel, PredictionDayPlan>;
  const activeDayPlan = dayPlans[activeDay];

  return {
    rows: activeDayPlan.rows,
    timeBar: activeDayPlan.timeBar,
    quietestNote: activeDayPlan.quietestNote,
    activeDay,
    dayPlans,
  };
}

function buildDayPlan(
  day: DayLabel,
  fallbackRows: PredictionRow[],
  dayAverages: Map<string, number> | undefined,
  allDayAverages: Map<string, number>,
  fallbackDayPlan: PredictionDayPlan | undefined,
  fallbackQuietestNote: string,
): PredictionDayPlan {
  const rows = fallbackRows.map((fallbackRow, index) => {
    const dayFallbackRow = fallbackDayPlan?.rows[index];
    const preferredEstimate =
      dayAverages?.get(fallbackRow.time) ??
      allDayAverages.get(fallbackRow.time) ??
      dayFallbackRow?.estimate ??
      fallbackRow.estimate;

    return {
      time: fallbackRow.time,
      estimate: Math.max(10, Math.round(preferredEstimate)),
      level: 'low' as CrowdLevel,
      recommendation: '',
    };
  });

  const thresholds = getLevelThresholds(rows.map((row) => row.estimate));

  for (const row of rows) {
    row.level = mapEstimateToLevel(row.estimate, thresholds.lowMax, thresholds.moderateMax);
    row.recommendation = getRecommendation(row.level);
  }

  const quietestRow = [...rows].sort((left, right) => left.estimate - right.estimate)[0];

  return {
    rows,
    timeBar: rows.map((row) => ({
      time: row.time,
      level: row.level,
    })),
    quietestNote: quietestRow
      ? `Quietest: ${day} ${quietestRow.time.toLowerCase()}`
      : fallbackDayPlan?.quietestNote ?? fallbackQuietestNote,
  };
}

function buildBucketAveragesByDay(records: ParsedHistoricalRecord[]): Map<string, Map<string, number>> {
  const aggregates = new Map<string, Map<string, BucketAggregate>>();

  for (const record of records) {
    const bucket = BUCKETS.find(
      (candidate) => record.hour >= candidate.startHour && record.hour < candidate.endHour,
    );

    if (!bucket || !record.dayShort) {
      continue;
    }

    if (!aggregates.has(record.dayShort)) {
      aggregates.set(record.dayShort, new Map());
    }

    const dayAggregates = aggregates.get(record.dayShort)!;
    const current = dayAggregates.get(bucket.label) ?? { sum: 0, count: 0 };
    current.sum += record.count;
    current.count += 1;
    dayAggregates.set(bucket.label, current);
  }

  return new Map(
    [...aggregates.entries()].map(([day, bucketAggregates]) => [
      day,
      new Map(
        [...bucketAggregates.entries()].map(([label, aggregate]) => [
          label,
          aggregate.count ? aggregate.sum / aggregate.count : 0,
        ]),
      ),
    ]),
  );
}

function buildBucketAverages(records: ParsedHistoricalRecord[]): Map<string, number> {
  const aggregates = new Map<string, BucketAggregate>();

  for (const record of records) {
    const bucket = BUCKETS.find(
      (candidate) => record.hour >= candidate.startHour && record.hour < candidate.endHour,
    );

    if (!bucket) {
      continue;
    }

    const current = aggregates.get(bucket.label) ?? { sum: 0, count: 0 };
    current.sum += record.count;
    current.count += 1;
    aggregates.set(bucket.label, current);
  }

  return new Map(
    [...aggregates.entries()].map(([label, aggregate]) => [
      label,
      aggregate.count ? aggregate.sum / aggregate.count : 0,
    ]),
  );
}

function getPreferredDay(records: ParsedHistoricalRecord[]): DayLabel | null {
  const today = new Date().toLocaleDateString('en-AU', { weekday: 'short' });
  const normalizedToday = normalizeDayLabel(today);

  if (normalizedToday && records.some((record) => record.dayShort === normalizedToday)) {
    return normalizedToday;
  }

  const countsByDay = new Map<string, number>();

  for (const record of records) {
    if (!record.dayShort) {
      continue;
    }

    countsByDay.set(record.dayShort, (countsByDay.get(record.dayShort) ?? 0) + 1);
  }

  return ([...countsByDay.entries()].sort((left, right) => right[1] - left[1])[0]?.[0] as DayLabel | undefined) ?? null;
}

function getLatestObservedAt(records: ParsedHistoricalRecord[]): string | null {
  const observedAtValues = records
    .map((record) => record.observedAt)
    .filter((value): value is string => Boolean(value))
    .filter((value) => !Number.isNaN(Date.parse(value)));

  if (!observedAtValues.length) {
    return null;
  }

  return observedAtValues.sort((left, right) => Date.parse(right) - Date.parse(left))[0];
}

function getLevelThresholds(estimates: number[]): { lowMax: number; moderateMax: number } {
  const sorted = [...estimates].sort((left, right) => left - right);

  if (!sorted.length) {
    return { lowMax: 0, moderateMax: 0 };
  }

  const lowMax = sorted[Math.min(sorted.length - 1, Math.floor(sorted.length * 0.33))];
  const moderateMax = sorted[Math.min(sorted.length - 1, Math.floor(sorted.length * 0.66))];

  return {
    lowMax,
    moderateMax: Math.max(lowMax + 1, moderateMax),
  };
}

function mapEstimateToLevel(estimate: number, lowMax: number, moderateMax: number): CrowdLevel {
  if (estimate <= lowMax) {
    return 'low';
  }

  if (estimate <= moderateMax) {
    return 'moderate';
  }

  return 'high';
}

function getRecommendation(level: CrowdLevel): string {
  if (level === 'low') {
    return 'Recommended';
  }

  if (level === 'moderate') {
    return 'Manageable';
  }

  return 'Peak avoid';
}

function normalizeDayLabel(value: string | null): DayLabel | null {
  if (!value) {
    return null;
  }

  const normalized = value.trim().slice(0, 3).toLowerCase();

  if (!normalized) {
    return null;
  }

  const formatted = normalized[0].toUpperCase() + normalized.slice(1);

  return DAY_ORDER.includes(formatted as DayLabel) ? (formatted as DayLabel) : null;
}

function pickString(record: HistoricalRecord, keys: string[]): string | null {
  for (const key of keys) {
    const value = record[key];

    if (typeof value === 'string' && value.trim()) {
      return value.trim();
    }
  }

  return null;
}

function pickNumber(record: HistoricalRecord, keys: string[]): number | null {
  for (const key of keys) {
    const value = record[key];

    if (typeof value === 'number' && Number.isFinite(value)) {
      return value;
    }

    if (typeof value === 'string' && value.trim()) {
      const parsed = Number(value);

      if (Number.isFinite(parsed)) {
        return parsed;
      }
    }
  }

  return null;
}

function pickHour(record: HistoricalRecord, keys: string[]): number | null {
  for (const key of keys) {
    const value = record[key];
    const parsed = parseHourValue(value);

    if (parsed !== null) {
      return parsed;
    }
  }

  return null;
}

function parseHourValue(value: unknown): number | null {
  if (typeof value === 'number' && Number.isFinite(value) && value >= 0 && value <= 23) {
    return value;
  }

  if (typeof value !== 'string') {
    return null;
  }

  const normalized = value.trim();

  if (!normalized) {
    return null;
  }

  const isoMatch = normalized.match(/T(\d{2}):\d{2}/);

  if (isoMatch) {
    const parsedIsoHour = Number(isoMatch[1]);

    return Number.isFinite(parsedIsoHour) && parsedIsoHour >= 0 && parsedIsoHour <= 23
      ? parsedIsoHour
      : null;
  }

  const timeMatch = normalized.match(/^(\d{1,2})(?::\d{2})?/);

  if (!timeMatch) {
    return null;
  }

  const parsed = Number(timeMatch[1]);

  return Number.isFinite(parsed) && parsed >= 0 && parsed <= 23 ? parsed : null;
}
