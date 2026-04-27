export type Category = 'All' | 'Park' | 'Library' | 'Museum' | 'Community Centre';
export type CrowdLevel = 'low' | 'moderate' | 'high';
export type DataStatus = 'loading' | 'fresh' | 'stale' | 'error' | 'empty';
export type LightingType = 'natural' | 'mixed' | 'controlled-indoor' | 'bright-artificial';
export type CrowdPredictability = 'stable' | 'variable' | 'event-sensitive';
export type ActivityTag = 'outdoor' | 'cultural' | 'social' | 'active';
export type ActivityFilter = ActivityTag | 'any';
export type SensoryMetricKey = 'noiseLevel' | 'lightingType' | 'crowdPredictability';
export type SensorySignalSource = 'official' | 'derived';
export type DayLabel = 'Mon' | 'Tue' | 'Wed' | 'Thu' | 'Fri' | 'Sat' | 'Sun';

export interface SourceReference {
  type: string;
  label: string;
  url: string | null;
  notes: string;
}

export interface SensorySignal {
  source: SensorySignalSource;
  observedAt: string | null;
  note: string;
}

export interface SensoryProfile {
  locationId: string;
  noiseLevel: CrowdLevel;
  lightingType: LightingType;
  crowdPredictability: CrowdPredictability;
  hasQuietArea: boolean;
  escapeRouteNotes: string;
  sensoryFriendlyNotes: string;
  sourceReferences: SourceReference[];
  lastReviewedAt: string;
  verificationStatus: string;
  signalSources?: Partial<Record<SensoryMetricKey, SensorySignal>>;
}

export interface PreparationGuide {
  locationId: string;
  sensoryToolsToBring: string[];
  arrivalTip: string;
  escapeRoute: string;
  recoveryOption: string;
  visitStyle: string;
}

export interface MapPoint {
  x: number;
  y: number;
}

export interface LiveCrowdSnapshot {
  sensorId: number | null;
  observedAt: string;
  rawCount: number | null;
  crowdLevel: CrowdLevel;
  status: DataStatus;
  label: string;
  shortLabel: string;
  summary: string;
}

export interface Location {
  locationId: string;
  name: string;
  category: Exclude<Category, 'All'>;
  displayType: string;
  matchesPreferences?: boolean;
  summaryCrowdLevel?: CrowdLevel;
  summaryStatusLabel?: string;
  coordinates: {
    lat: number;
    lon: number;
  };
  sensorIds: number[];
  addressOrArea: string;
  priority: number;
  verificationStatus: string;
  sourceReferences: SourceReference[];
  crowdSnapshot: LiveCrowdSnapshot;
  sensoryProfile: SensoryProfile;
  preparationGuide: PreparationGuide;
  mapPoint: MapPoint;
  sensoryScore: 1 | 2 | 3 | 4;
  activityTags: ActivityTag[];
  dataSource?: 'seed' | 'backend';
  detailHydrated?: boolean;
  goodToKnowItems?: GoodToKnowItem[];
  bestTimesSummary?: string | null;
}

export interface MapQuickFilters {
  noise: boolean;
  lighting: boolean;
  crowds: boolean;
  unpredictability: boolean;
  activity: ActivityFilter;
}

export interface UserPreference extends MapQuickFilters {
  darkModeEnabled: boolean;
}

export interface FilterOption {
  value: Category;
  label: string;
}

export interface AttributionContent {
  footerAttribution: string;
  privacyNotice: string;
  derivedDataNotice: string;
  accessibilityNotice: string;
  license: {
    name: string;
    url: string;
  };
}

export interface FilterState {
  searchText: string;
  category: Category;
  preferences: MapQuickFilters;
}

export interface PredictionRow {
  time: string;
  level: CrowdLevel;
  estimate: number;
  recommendation: string;
}

export interface TimeBarSegment {
  time: string;
  level: CrowdLevel;
}

export interface PredictionDayPlan {
  rows: PredictionRow[];
  timeBar: TimeBarSegment[];
  quietestNote: string;
}

export interface LocationPrediction {
  rows: PredictionRow[];
  timeBar: TimeBarSegment[];
  quietestNote: string;
  activeDay: DayLabel;
  dayPlans?: Partial<Record<DayLabel, PredictionDayPlan>>;
}

export type PredictionSource = 'official' | 'fallback';

export interface PredictionSnapshot {
  prediction: LocationPrediction | null;
  status: DataStatus;
  source: PredictionSource;
  syncedAt: string | null;
  note: string;
}

export interface GoodToKnowItem {
  kind: 'quiet-area' | 'transport' | 'toilets';
  label: string;
  value: string;
}

export interface SearchCoveragePlace {
  name: string;
  theme: string;
  subTheme: string;
  coordinates: {
    lat: number;
    lon: number;
  } | null;
}
