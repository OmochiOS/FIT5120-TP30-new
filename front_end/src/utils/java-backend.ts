import type {
  ActivityTag,
  Category,
  CrowdLevel,
  GoodToKnowItem,
  LightingType,
  Location,
  MapQuickFilters,
} from '@/types/domain';
import { getCrowdLabel, getCrowdSummary } from '@/utils/crowd';

type BackendPlaceType = 'OUTDOOR' | 'CULTURAL' | 'SOCIAL' | 'ACTIVE' | 'ANY' | null;
type BackendCrowdLevel = 'LOW' | 'MODERATE' | 'HIGH' | null;

interface BackendLocationSummary {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
  crowdLevel: BackendCrowdLevel;
  statusLabel: string | null;
  placeType: BackendPlaceType;
  matchesPreferences: boolean;
}

interface BackendSensoryProfile {
  noiseDb: number | null;
  noiseLevel: string | null;
  noiseDescription: string | null;
  lightingLevel: string | null;
  lightingDescription: string | null;
  predictabilityLevel: string | null;
  predictabilityDescription: string | null;
  quietSpaceAvailable: boolean | null;
  quietSpaceDescription: string | null;
  overallSensoryScore: string | null;
  bestTimesToVisit: string | null;
  goodToKnowTips: string | null;
  lastUpdated: string | null;
}

interface BackendLocationDetail extends BackendLocationSummary {
  sensoryProfile: BackendSensoryProfile | null;
}

interface BackendFilterRequest {
  lowCrowd: boolean;
  lowNoise: boolean;
  lowLighting: boolean;
  quietSpaceRequired: boolean;
  predictableEnvironment: boolean;
  placeType: Exclude<BackendPlaceType, null>;
}

const BACKEND_BASE_URL = '/api/locations';

const DEFAULT_ADDRESS = 'Melbourne CBD sensor area';
const DEFAULT_BEST_TIMES = 'Weekday mornings are usually the calmest starting point.';
const DEFAULT_TIPS = 'Use quieter edges where possible and keep a simple exit route in mind.';
const DEFAULT_TRANSPORT = 'Check nearby tram, train, or street exits before entering busy areas.';
const DEFAULT_AMENITIES = 'Check venue signage or staff guidance for toilets and amenities before settling in.';
const DEFAULT_ESCAPE = 'Plan a nearby open space or quieter side street as a reset point.';
const DEFAULT_SUMMARY = 'Current comfort guidance is estimated from crowd, place type and sensor context.';

function sanitizeText(value: string | null | undefined): string {
  if (!value) {
    return '';
  }

  return value
    .replace(/[^\x20-\x7E]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

function isUnavailableText(value: string | null | undefined): boolean {
  const normalized = sanitizeText(value).toLowerCase();

  return (
    !normalized ||
    normalized === 'data unavailable' ||
    normalized.includes('not available') ||
    normalized.includes('currently unavailable') ||
    normalized.includes('unavailable')
  );
}

function displayText(value: string | null | undefined): string {
  return isUnavailableText(value) ? '' : sanitizeText(value);
}

function toCrowdLevel(value: BackendCrowdLevel): CrowdLevel {
  if (value === 'HIGH') {
    return 'high';
  }

  if (value === 'MODERATE') {
    return 'moderate';
  }

  return 'low';
}

function scoreFromCrowdLevel(value: BackendCrowdLevel): Location['sensoryScore'] {
  if (value === 'HIGH') {
    return 4;
  }

  if (value === 'MODERATE') {
    return 3;
  }

  return 2;
}

function scoreFromOverallScore(value: string | null | undefined, fallback: Location['sensoryScore']) {
  const normalized = sanitizeText(value);
  const match = normalized.match(/\b([1-4])\b/);

  if (!match) {
    return fallback;
  }

  const parsed = Number(match[1]);

  return parsed >= 1 && parsed <= 4 ? (parsed as Location['sensoryScore']) : fallback;
}

function inferCategory(name: string, placeType: BackendPlaceType): Exclude<Category, 'All'> {
  const normalizedName = name.toLowerCase();

  if (normalizedName.includes('library')) {
    return 'Library';
  }

  if (normalizedName.includes('museum')) {
    return 'Museum';
  }

  if (placeType === 'OUTDOOR' || placeType === 'ACTIVE') {
    return 'Park';
  }

  if (placeType === 'CULTURAL') {
    return normalizedName.includes('gallery') ? 'Museum' : 'Library';
  }

  return 'Community Centre';
}

function inferActivityTags(placeType: BackendPlaceType, category: Exclude<Category, 'All'>): ActivityTag[] {
  if (placeType === 'OUTDOOR') {
    return ['outdoor'];
  }

  if (placeType === 'CULTURAL') {
    return ['cultural'];
  }

  if (placeType === 'SOCIAL') {
    return ['social'];
  }

  if (placeType === 'ACTIVE') {
    return ['active'];
  }

  if (category === 'Park') {
    return ['outdoor'];
  }

  if (category === 'Library' || category === 'Museum') {
    return ['cultural'];
  }

  return ['social'];
}

function toDisplayType(category: Exclude<Category, 'All'>): string {
  return category;
}

function toLightingType(level: string | null, placeType: BackendPlaceType): LightingType {
  const normalized = sanitizeText(level).toLowerCase();

  if (placeType === 'OUTDOOR') {
    return normalized === 'high' ? 'natural' : 'mixed';
  }

  if (normalized === 'low') {
    return 'controlled-indoor';
  }

  if (normalized === 'moderate') {
    return 'mixed';
  }

  return 'bright-artificial';
}

function toNoiseLevel(level: string | null): CrowdLevel {
  const normalized = sanitizeText(level).toLowerCase();

  if (normalized === 'high' || normalized === 'busy' || normalized === 'overstimulating') {
    return 'high';
  }

  if (normalized === 'moderate') {
    return 'moderate';
  }

  return 'low';
}

function toCrowdPredictability(level: string | null): Location['sensoryProfile']['crowdPredictability'] {
  const normalized = sanitizeText(level).toLowerCase();

  if (normalized === 'high') {
    return 'stable';
  }

  if (normalized === 'moderate') {
    return 'variable';
  }

  return 'event-sensitive';
}

function splitTips(value: string): string[] {
  return sanitizeText(value)
    .split(/\s*\|\s*|\s*;\s*/g)
    .map((item) => item.trim())
    .filter(Boolean);
}

function buildGoodToKnowItems(value: string | null | undefined): GoodToKnowItem[] {
  const tips = splitTips(displayText(value));

  if (!tips.length) {
    return [
      { kind: 'quiet-area', label: 'Quiet area', value: DEFAULT_TIPS },
      { kind: 'transport', label: 'Transport', value: DEFAULT_TRANSPORT },
      { kind: 'toilets', label: 'Toilets', value: DEFAULT_AMENITIES },
    ];
  }

  const kinds: GoodToKnowItem['kind'][] = ['quiet-area', 'transport', 'toilets'];

  return tips.slice(0, 3).map((tip, index) => ({
    kind: kinds[index] ?? 'quiet-area',
    label: kinds[index] === 'transport' ? 'Transport' : kinds[index] === 'toilets' ? 'Toilets' : 'Quiet area',
    value: tip,
  }));
}

function createDefaultPreparationGuide(locationId: string, quietSpaceDescription: string, goodToKnowTips: string) {
  return {
    locationId,
    sensoryToolsToBring: ['Noise support', 'Water', 'Flexible exit plan'],
    arrivalTip:
      goodToKnowTips ||
      'Arrive with a short route plan and choose a calmer edge before entering the busiest area.',
    escapeRoute: quietSpaceDescription || DEFAULT_ESCAPE,
    recoveryOption: quietSpaceDescription || 'Use a quieter edge or open area nearby for a short reset.',
    visitStyle: 'Plan a short first visit and extend only if conditions feel manageable.',
  };
}

function buildSourceReferences(locationId: string): Location['sourceReferences'] {
  return [
    {
      type: 'backend',
      label: 'AmitySpace backend location detail',
      url: `${BACKEND_BASE_URL}/${locationId}`,
      notes: 'Mapped from the project backend.',
    },
  ];
}

function buildSummaryLocation(summary: BackendLocationSummary): Location {
  const category = inferCategory(summary.name, summary.placeType);
  const crowdLevel = toCrowdLevel(summary.crowdLevel);
  const scoreFromCrowd = scoreFromCrowdLevel(summary.crowdLevel);
  const locationId = String(summary.id);

  return {
    locationId,
    name: displayText(summary.name) || `Location ${summary.id}`,
    category,
    displayType: toDisplayType(category),
    matchesPreferences: summary.matchesPreferences,
    summaryCrowdLevel: crowdLevel,
    summaryStatusLabel: displayText(summary.statusLabel) || getCrowdSummary(crowdLevel),
    coordinates: {
      lat: summary.latitude,
      lon: summary.longitude,
    },
    sensorIds: [summary.id],
    addressOrArea: DEFAULT_ADDRESS,
    priority: scoreFromCrowd,
    verificationStatus: 'backend-summary',
    sourceReferences: buildSourceReferences(locationId),
    crowdSnapshot: {
      sensorId: summary.id,
      observedAt: new Date().toISOString(),
      rawCount: null,
      crowdLevel,
      status: 'fresh',
      label: getCrowdLabel(crowdLevel),
      shortLabel: displayText(summary.statusLabel) || 'Current backend summary',
      summary: displayText(summary.statusLabel) || getCrowdSummary(crowdLevel),
    },
    sensoryProfile: {
      locationId,
      noiseLevel: crowdLevel,
      lightingType: summary.placeType === 'OUTDOOR' ? 'natural' : 'mixed',
      crowdPredictability: crowdLevel === 'high' ? 'event-sensitive' : crowdLevel === 'moderate' ? 'variable' : 'stable',
      hasQuietArea: false,
      escapeRouteNotes: DEFAULT_ESCAPE,
      sensoryFriendlyNotes: DEFAULT_SUMMARY,
      sourceReferences: buildSourceReferences(locationId),
      lastReviewedAt: new Date().toISOString(),
      verificationStatus: 'backend-summary',
    },
    preparationGuide: createDefaultPreparationGuide(locationId, '', ''),
    mapPoint: { x: 0, y: 0 },
    sensoryScore: scoreFromCrowd,
    activityTags: inferActivityTags(summary.placeType, category),
    dataSource: 'backend',
    detailHydrated: false,
    goodToKnowItems: buildGoodToKnowItems(null),
    bestTimesSummary: DEFAULT_BEST_TIMES,
  };
}

function mergeDetailLocation(summaryLocation: Location, detail: BackendLocationDetail): Location {
  const sensoryProfile = detail.sensoryProfile;
  const fallbackScore = scoreFromCrowdLevel(detail.crowdLevel);
  const sensoryScore = scoreFromOverallScore(sensoryProfile?.overallSensoryScore, fallbackScore);
  const category = inferCategory(detail.name, detail.placeType);
  const goodToKnowTips = displayText(sensoryProfile?.goodToKnowTips);
  const quietSpaceDescription = displayText(sensoryProfile?.quietSpaceDescription);
  const noiseDescription = displayText(sensoryProfile?.noiseDescription);
  const lightingDescription = displayText(sensoryProfile?.lightingDescription);
  const predictabilityDescription = displayText(sensoryProfile?.predictabilityDescription);
  const locationId = String(detail.id);

  return {
    ...summaryLocation,
    name: displayText(detail.name) || summaryLocation.name,
    category,
    displayType: toDisplayType(category),
    matchesPreferences: detail.matchesPreferences,
    summaryCrowdLevel: toCrowdLevel(detail.crowdLevel),
    summaryStatusLabel:
      displayText(detail.statusLabel) ||
      getCrowdSummary(toCrowdLevel(detail.crowdLevel)),
    coordinates: {
      lat: detail.latitude,
      lon: detail.longitude,
    },
    priority: sensoryScore,
    sourceReferences: buildSourceReferences(locationId),
    crowdSnapshot: {
      ...summaryLocation.crowdSnapshot,
      sensorId: detail.id,
      crowdLevel: toCrowdLevel(detail.crowdLevel),
      label: getCrowdLabel(toCrowdLevel(detail.crowdLevel)),
      shortLabel: displayText(detail.statusLabel) || summaryLocation.crowdSnapshot.shortLabel,
      summary: displayText(detail.statusLabel) || summaryLocation.crowdSnapshot.summary,
    },
    sensoryProfile: {
      locationId,
      noiseLevel: toNoiseLevel(sensoryProfile?.noiseLevel ?? null),
      lightingType: toLightingType(sensoryProfile?.lightingLevel ?? null, detail.placeType),
      crowdPredictability: toCrowdPredictability(sensoryProfile?.predictabilityLevel ?? null),
      hasQuietArea: Boolean(sensoryProfile?.quietSpaceAvailable),
      escapeRouteNotes: quietSpaceDescription || DEFAULT_ESCAPE,
      sensoryFriendlyNotes:
        [noiseDescription, lightingDescription, predictabilityDescription]
          .filter(Boolean)
          .join(' ') || DEFAULT_SUMMARY,
      sourceReferences: buildSourceReferences(locationId),
      lastReviewedAt: displayText(sensoryProfile?.lastUpdated) || new Date().toISOString(),
      verificationStatus: 'backend-detail',
    },
    preparationGuide: createDefaultPreparationGuide(
      locationId,
      quietSpaceDescription,
      goodToKnowTips || noiseDescription,
    ),
    sensoryScore,
    activityTags: inferActivityTags(detail.placeType, category),
    dataSource: 'backend',
    detailHydrated: true,
    goodToKnowItems: buildGoodToKnowItems(goodToKnowTips),
    bestTimesSummary: displayText(sensoryProfile?.bestTimesToVisit) || DEFAULT_BEST_TIMES,
  };
}

function buildFilterRequest(filters: MapQuickFilters): BackendFilterRequest | null {
  const placeType = filters.activity === 'any'
    ? 'ANY'
    : filters.activity === 'outdoor'
      ? 'OUTDOOR'
      : filters.activity === 'cultural'
        ? 'CULTURAL'
        : filters.activity === 'social'
          ? 'SOCIAL'
          : 'ACTIVE';

  if (
    !filters.noise &&
    !filters.lighting &&
    !filters.crowds &&
    !filters.unpredictability &&
    placeType === 'ANY'
  ) {
    return null;
  }

  return {
    lowCrowd: filters.crowds,
    lowNoise: filters.noise,
    lowLighting: filters.lighting,
    quietSpaceRequired: false,
    predictableEnvironment: filters.unpredictability,
    placeType,
  };
}

async function fetchJson<T>(path: string, options?: RequestInit, fetchImpl: typeof fetch = fetch): Promise<T> {
  const response = await fetchImpl(path, {
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      ...(options?.headers ?? {}),
    },
    ...options,
  });

  if (!response.ok) {
    throw new Error(`Backend request failed: ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export async function fetchBackendLocations(fetchImpl: typeof fetch = fetch): Promise<Location[]> {
  const locations = await fetchJson<BackendLocationSummary[]>(BACKEND_BASE_URL, undefined, fetchImpl);

  return locations.map(buildSummaryLocation);
}

export async function fetchBackendLocationDetail(
  summaryLocation: Location,
  fetchImpl: typeof fetch = fetch,
): Promise<Location> {
  const detail = await fetchJson<BackendLocationDetail>(
    `${BACKEND_BASE_URL}/${summaryLocation.locationId}`,
    undefined,
    fetchImpl,
  );

  return mergeDetailLocation(summaryLocation, detail);
}

export async function fetchBackendFilteredLocationIds(
  filters: MapQuickFilters,
  fetchImpl: typeof fetch = fetch,
): Promise<string[] | null> {
  const payload = buildFilterRequest(filters);

  if (!payload) {
    return null;
  }

  const filtered = await fetchJson<BackendLocationSummary[]>(
    `${BACKEND_BASE_URL}/filter`,
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    fetchImpl,
  );

  return filtered.map((location) => String(location.id));
}
