import { defineStore } from 'pinia';

import type {
  Category,
  DataStatus,
  Location,
} from '@/types/domain';
import { usePreferencesStore } from '@/stores/preferences';
import { searchAndSortLocations } from '@/utils/filters';
import {
  fetchBackendFilteredLocationIds,
  fetchBackendLocationDetail,
  fetchBackendLocations,
  fetchCrowdInsightBatch,
  timeSlotToHour,
  type CrowdInsightResult,
} from '@/utils/java-backend';
import { seedLocations, statusRules } from '@/utils/adapters';
import { fetchLiveLocationRefresh } from '@/utils/live-data';
import { fetchAutomatedProfileRefresh } from '@/utils/profile-enrichment';

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
    /** Cache of crowd-insight results keyed by `"${locationId}:${visitHour}"` */
    crowdInsightCache: {} as Record<string, CrowdInsightResult>,
    crowdInsightFetching: false,
    detailRefreshing: {} as Record<string, boolean>,
    backendFilterIds: null as string[] | null,
  }),
  getters: {
    filteredLocations(state) {
      // Ignore current filters and search across all locations if a search query is provided
      if (state.searchText.trim() !== '') {
        return searchAndSortLocations(state.locations, state.searchText);
      }

      const baseSet = state.backendFilterIds ? new Set(state.backendFilterIds) : null;
      let locations = (baseSet
        ? state.locations.filter((location) => baseSet.has(location.locationId))
        : state.locations
      ).filter((location) => state.activeCategory === 'All' || location.category === state.activeCategory);

      const preferencesStore = usePreferencesStore();
      const timeFilter = preferencesStore.predictionTime;

      if (timeFilter && timeFilter !== 'any') {
        const hour = timeSlotToHour(timeFilter);
        locations = locations.filter((location) => {
          if (hour === null) return true;
          const cached = state.crowdInsightCache[`${location.locationId}:${hour}`];
          if (!cached) return true; // not yet fetched → show optimistically
          return cached.predictedCrowdLevel === 'LOW' || cached.predictedCrowdLevel === 'MODERATE';
        });
      }

      return locations;
    },
    getLocationById: (state) => {
      return (locationId: string) =>
        state.locations.find((location) => location.locationId === locationId);
    },
    getCrowdInsight: (state) => {
      return (locationId: string, visitHour: number) =>
        state.crowdInsightCache[`${locationId}:${visitHour}`] ?? null;
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
        
        // Asynchronously fetch details for all locations so we have predictabilityLevel
        // and full sensory profiles without modifying the backend summary API
        Promise.allSettled(
          this.locations.map((loc) => this.hydrateLocationDetail(loc.locationId))
        ).catch(console.error);

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
    /**
     * Batch-fetch crowd-insight for all current locations at the given hour
     * and store results in the cache. Called when the user selects a time slot.
     */
    async applyTimeFilter(timeSlot: string) {
      if (timeSlot === 'any' || this.crowdInsightFetching) return;
      const hour = timeSlotToHour(timeSlot);
      if (hour === null) return;

      this.crowdInsightFetching = true;
      try {
        const ids = this.locations.map((l) => l.locationId);
        const results = await fetchCrowdInsightBatch(ids, hour);
        for (const [id, result] of results) {
          this.crowdInsightCache[`${id}:${hour}`] = result;
        }
      } catch (error) {
        console.error('Crowd insight batch fetch failed.', error);
      } finally {
        this.crowdInsightFetching = false;
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
