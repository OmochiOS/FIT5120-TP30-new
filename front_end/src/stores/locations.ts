import { defineStore } from 'pinia';

import type {
  Category,
  DataStatus,
  Location,
  PredictionSnapshot,
} from '@/types/domain';
import { usePreferencesStore } from '@/stores/preferences';
import { searchAndSortLocations } from '@/utils/filters';
import { fetchBackendFilteredLocationIds, fetchBackendLocationDetail, fetchBackendLocations } from '@/utils/java-backend';
import { seedLocations, statusRules } from '@/utils/adapters';
import { fetchLiveLocationRefresh } from '@/utils/live-data';
import { fetchAutomatedProfileRefresh } from '@/utils/profile-enrichment';
import { fetchLocationPrediction, getLocationPrediction } from '@/utils/prediction';

function minutesSince(observedAt: string, now = Date.now()): number {
  const observedAtMs = Date.parse(observedAt);

  if (Number.isNaN(observedAtMs)) {
    return 0;
  }

  return Math.max(0, Math.round((now - observedAtMs) / 60_000));
}

function formatLastSyncLabel(status: DataStatus, observedAt: string): string {
  const ageInMinutes = minutesSince(observedAt);

  if (status === 'stale') {
    return `Last sync ${ageInMinutes} min ago - Stale`;
  }

  return `Last sync ${ageInMinutes} min ago`;
}

function formatHistoricalSyncLabel(observedAt: string): string {
  const date = new Date(observedAt);

  if (Number.isNaN(date.getTime())) {
    return '';
  }

  return `Historical data through ${date.toLocaleDateString('en-AU', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })}`;
}

function isBackendDrivenLocation(location: Location): boolean {
  return location.dataSource === 'backend';
}

export const useLocationsStore = defineStore('locations', {
  state: () => ({
    locations: [] as Location[],
    locationsHydrated: false,
    searchText: '',
    activeCategory: 'All' as Category,
    liveDataStatus: 'loading' as DataStatus,
    liveDataSyncedAt: null as string | null,
    liveDataRefreshing: false,
    predictionSnapshots: {} as Record<string, PredictionSnapshot | undefined>,
    predictionRefreshing: {} as Record<string, boolean>,
    detailRefreshing: {} as Record<string, boolean>,
    backendFilterIds: null as string[] | null,
  }),
  getters: {
    filteredLocations(state) {
      const baseSet = state.backendFilterIds ? new Set(state.backendFilterIds) : null;
      let locations = (baseSet
        ? state.locations.filter((location) => baseSet.has(location.locationId))
        : state.locations
      ).filter((location) => state.activeCategory === 'All' || location.category === state.activeCategory);

      const preferencesStore = usePreferencesStore();
      const timeFilter = preferencesStore.predictionTime;
      
      if (timeFilter && timeFilter !== 'any') {
        locations = locations.filter(location => {
          const pred = state.predictionSnapshots[location.locationId]?.prediction ?? getLocationPrediction(location);
          const activeDayPlans = pred.dayPlans?.[pred.activeDay];
          const rows = activeDayPlans?.rows || pred.rows;
          const targetRow = rows.find(r => r.time === timeFilter);
          return targetRow && (targetRow.level === 'low' || targetRow.level === 'moderate');
        });
      }

      return searchAndSortLocations(locations, state.searchText);
    },
    getLocationById: (state) => {
      return (locationId: string) =>
        state.locations.find((location) => location.locationId === locationId);
    },
    getPredictionSnapshot: (state) => {
      return (locationId: string) => state.predictionSnapshots[locationId] ?? null;
    },
    getPredictionByLocationId() {
      return (locationId: string) => this.getPredictionSnapshot(locationId)?.prediction ?? null;
    },
    getPredictionMessage() {
      return (locationId: string) => this.getPredictionSnapshot(locationId)?.note ?? '';
    },
    getPredictionTimestampLabel() {
      return (locationId: string) => {
        const snapshot = this.getPredictionSnapshot(locationId);

        if (!snapshot?.syncedAt) {
          return '';
        }

        return formatHistoricalSyncLabel(snapshot.syncedAt);
      };
    },
    liveDataMessage(state): string {
      if (state.liveDataRefreshing) {
        return 'Refreshing live pedestrian counts...';
      }

      if (state.liveDataStatus === 'loading') {
        return 'Checking live pedestrian counts...';
      }

      if (state.liveDataStatus === 'error') {
        return 'Live pedestrian data is unavailable. Showing the last verified local snapshot.';
      }

      if (state.liveDataStatus === 'empty') {
        return 'No current live readings matched the supported locations.';
      }

      if (state.liveDataStatus === 'stale') {
        return 'Live pedestrian data is connected, but the latest readings are older than expected.';
      }

      return '';
    },
    liveDataTimestampLabel(state): string {
      if (!state.liveDataSyncedAt) {
        return '';
      }

      return formatLastSyncLabel(state.liveDataStatus, state.liveDataSyncedAt);
    },
  },
  actions: {
    async hydrateLocations() {
      try {
        this.locations = await fetchBackendLocations();
      } catch (error) {
        console.error('Backend location load failed. Falling back to local seed data.', error);
        this.locations = seedLocations;
      } finally {
        this.locationsHydrated = true;
      }

      await this.applyBackendFilter();
    },
    async hydrateLocationDetail(locationId: string) {
      const location = this.getLocationById(locationId);

      if (!location || !isBackendDrivenLocation(location) || location.detailHydrated || this.detailRefreshing[locationId]) {
        return;
      }

      this.detailRefreshing[locationId] = true;

      try {
        const detailedLocation = await fetchBackendLocationDetail(location);

        this.locations = this.locations.map((candidate) =>
          candidate.locationId === locationId ? detailedLocation : candidate,
        );
      } catch (error) {
        console.error(`Backend location detail load failed for ${locationId}.`, error);
      } finally {
        this.detailRefreshing[locationId] = false;
      }
    },
    async applyBackendFilter() {
      const preferencesStore = usePreferencesStore();

      if (!this.locations.length || !this.locations.every(isBackendDrivenLocation)) {
        this.backendFilterIds = null;
        return;
      }

      try {
        this.backendFilterIds = await fetchBackendFilteredLocationIds(preferencesStore.currentFilters);
      } catch (error) {
        console.error('Backend filter failed. Using local category/search only.', error);
        this.backendFilterIds = null;
      }
    },
    async refreshLiveData() {
      if (this.liveDataRefreshing || !this.locations.length) {
        return;
      }

      this.liveDataRefreshing = true;

      try {
        const nextState = await fetchLiveLocationRefresh(
          this.locations,
          statusRules.freshWithinMinutes,
        );
        let nextLocations = nextState.locations;

        if (!this.locations.every(isBackendDrivenLocation)) {
          try {
            nextLocations = await fetchAutomatedProfileRefresh(nextLocations);
          } catch (error) {
            console.error('Automated profile refresh failed.', error);
          }
        }

        this.locations = nextLocations;
        this.liveDataStatus = nextState.dataStatus;
        this.liveDataSyncedAt = nextState.syncedAt;
      } catch (error) {
        console.error('Live data refresh failed.', error);

        if (this.liveDataSyncedAt) {
          this.liveDataStatus = 'stale';
          return;
        }

        this.liveDataStatus = 'error';
      } finally {
        this.liveDataRefreshing = false;
      }
    },
    async hydrateLiveSnapshots() {
      await this.refreshLiveData();
    },
    async hydratePrediction(locationId: string, force = false) {
      if (this.predictionRefreshing[locationId]) {
        return;
      }

      const location = this.getLocationById(locationId);

      if (!location) {
        return;
      }

      const existingSnapshot = this.predictionSnapshots[locationId];

      if (!force && existingSnapshot?.status === 'fresh' && existingSnapshot.source === 'official') {
        return;
      }

      this.predictionRefreshing[locationId] = true;
      this.predictionSnapshots[locationId] = {
        prediction: existingSnapshot?.prediction ?? null,
        status: 'loading',
        source: existingSnapshot?.source ?? 'fallback',
        syncedAt: existingSnapshot?.syncedAt ?? null,
        note: 'Loading historical pedestrian counts...',
      };

      try {
        this.predictionSnapshots[locationId] = await fetchLocationPrediction(location);
      } finally {
        this.predictionRefreshing[locationId] = false;
      }
    },
    setSearchText(value: string) {
      this.searchText = value;
    },
    setCategory(value: Category) {
      this.activeCategory = value;
    },
    clearSearch() {
      this.searchText = '';
    },
  },
});
