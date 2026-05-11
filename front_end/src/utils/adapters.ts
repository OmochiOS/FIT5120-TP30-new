import attributionJson from '../../../data/attribution.json';
import businessRulesJson from '../../../data/business-rules.json';
import filterOptionsJson from '../../../data/filter-options.json';
import locationsJson from '../../../data/locations.json';
import preparationGuidesJson from '../../../data/preparation-guides.json';
import sensoryProfilesJson from '../../../data/sensory-profiles.json';
import type {
  ActivityTag,
  AttributionContent,
  FilterOption,
  LiveCrowdSnapshot,
  Location,
  MapPoint,
  SensoryProfile,
  UserPreference,
} from '@/types/domain';
import { getCrowdLabel, getCrowdSummary } from '@/utils/crowd';

interface RawLocationsFile {
  locations: Array<Omit<Location, 'crowdSnapshot' | 'sensoryProfile' | 'preparationGuide' | 'mapPoint' | 'sensoryScore' | 'activityTags'>>;
}

interface RawSensoryProfilesFile {
  profiles: Omit<SensoryProfile, 'noiseDb' | 'noiseDescription' | 'lightingLevel' | 'lightingDescription'>[];
}

interface RawPreparationGuidesFile {
  guides: Location['preparationGuide'][];
}

interface RawFilterOptionsFile {
  categoryFilters: FilterOption[];
}

interface RawBusinessRulesFile {
  statusRules: {
    refreshIntervalMinutes: number;
    freshWithinMinutes: number;
    fallbackCopy: Record<string, string>;
  };
}

const rawLocations = locationsJson as RawLocationsFile;
const rawProfiles = sensoryProfilesJson as RawSensoryProfilesFile;
const rawGuides = preparationGuidesJson as RawPreparationGuidesFile;
const rawFilters = filterOptionsJson as RawFilterOptionsFile;
const rawBusinessRules = businessRulesJson as RawBusinessRulesFile;

const MAP_POINTS: Record<string, MapPoint> = {
  'flagstaff-gardens': { x: 19, y: 29 },
  'state-library-victoria': { x: 55, y: 31 },
  'carlton-gardens': { x: 66, y: 20 },
  'treasury-gardens': { x: 83, y: 63 },
  'docklands-library': { x: 14, y: 59 },
  'fitzroy-gardens': { x: 88, y: 36 },
  'federation-square': { x: 70, y: 52 },
  'melbourne-central': { x: 63, y: 45 },
};

const SENSORY_SCORES: Record<string, 1 | 2 | 3 | 4> = {
  'flagstaff-gardens': 1,
  'state-library-victoria': 1,
  'carlton-gardens': 1,
  'treasury-gardens': 1,
  'docklands-library': 2,
  'fitzroy-gardens': 2,
  'federation-square': 3,
  'melbourne-central': 4,
};

const LIST_ORDER: Record<string, number> = {
  'flagstaff-gardens': 1,
  'state-library-victoria': 2,
  'fitzroy-gardens': 3,
  'federation-square': 4,
  'melbourne-central': 5,
  'carlton-gardens': 6,
  'docklands-library': 7,
  'treasury-gardens': 8,
};

const ACTIVITY_TAGS: Record<string, ActivityTag[]> = {
  'flagstaff-gardens': ['outdoor', 'active', 'social'],
  'state-library-victoria': ['cultural', 'social'],
  'carlton-gardens': ['outdoor', 'active', 'cultural'],
  'treasury-gardens': ['outdoor', 'active'],
  'docklands-library': ['cultural', 'social'],
  'fitzroy-gardens': ['outdoor', 'active'],
  'federation-square': ['cultural', 'social'],
  'melbourne-central': ['social', 'active', 'cultural'],
};

const SNAPSHOT_SEEDS: Record<
  string,
  {
    crowdLevel: LiveCrowdSnapshot['crowdLevel'];
    rawCount: number;
    minutesAgo: number;
  }
> = {
  'flagstaff-gardens': { crowdLevel: 'low', rawCount: 11, minutesAgo: 6 },
  'state-library-victoria': { crowdLevel: 'low', rawCount: 18, minutesAgo: 8 },
  'carlton-gardens': { crowdLevel: 'low', rawCount: 14, minutesAgo: 16 },
  'treasury-gardens': { crowdLevel: 'low', rawCount: 12, minutesAgo: 12 },
  'docklands-library': { crowdLevel: 'moderate', rawCount: 28, minutesAgo: 42 },
  'fitzroy-gardens': { crowdLevel: 'low', rawCount: 16, minutesAgo: 14 },
  'federation-square': { crowdLevel: 'moderate', rawCount: 54, minutesAgo: 18 },
  'melbourne-central': { crowdLevel: 'high', rawCount: 87, minutesAgo: 9 },
};

const profileById = new Map(rawProfiles.profiles.map((profile) => [profile.locationId, profile]));
const guideById = new Map(rawGuides.guides.map((guide) => [guide.locationId, guide]));

function buildSnapshot(locationId: string, sensorIds: number[]): LiveCrowdSnapshot {
  const seed = SNAPSHOT_SEEDS[locationId];
  const observedAt = new Date(Date.now() - seed.minutesAgo * 60_000).toISOString();
  const status =
    seed.minutesAgo > rawBusinessRules.statusRules.freshWithinMinutes ? 'stale' : 'fresh';
  const shortLabel =
    status === 'stale'
      ? `Updated ${seed.minutesAgo} min ago - Stale`
      : `Updated ${seed.minutesAgo} min ago`;

  return {
    sensorId: sensorIds[0] ?? null,
    observedAt,
    rawCount: seed.rawCount,
    crowdLevel: seed.crowdLevel,
    status,
    label: getCrowdLabel(seed.crowdLevel),
    shortLabel,
    summary: getCrowdSummary(seed.crowdLevel),
  };
}

function assertLookup<T>(value: T | undefined, locationId: string, label: string): T {
  if (!value) {
    throw new Error(`Missing ${label} for ${locationId}`);
  }

  return value;
}

export const categoryFilters = rawFilters.categoryFilters;
export const attribution = attributionJson as AttributionContent;
export const statusRules = rawBusinessRules.statusRules;
export const defaultPreferences: UserPreference = {
  noise: false,
  lighting: false,
  crowds: false,
  unpredictability: false,
  activity: 'any',
  predictionTime: 'any',
  darkModeEnabled: false,
};

export const seedLocations: Location[] = rawLocations.locations.map((location) => {
  const sensoryProfile = assertLookup(profileById.get(location.locationId), location.locationId, 'sensory profile');
  const preparationGuide = assertLookup(guideById.get(location.locationId), location.locationId, 'preparation guide');

  return {
    ...location,
    priority: LIST_ORDER[location.locationId] ?? location.priority,
    sensoryProfile: { ...sensoryProfile, noiseDb: null, noiseDescription: null, lightingLevel: null, lightingDescription: null },
    preparationGuide,
    crowdSnapshot: buildSnapshot(location.locationId, location.sensorIds),
    mapPoint: MAP_POINTS[location.locationId] ?? { x: 50, y: 50 },
    sensoryScore: SENSORY_SCORES[location.locationId] ?? 2,
    activityTags: ACTIVITY_TAGS[location.locationId] ?? ['social'],
  };
});

export function findLocationById(locationId: string): Location | undefined {
  return seedLocations.find((location) => location.locationId === locationId);
}
