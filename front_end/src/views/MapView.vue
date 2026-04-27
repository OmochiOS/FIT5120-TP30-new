<template>
  <section class="space-y-5 pb-4">
    <section class="figma-panel p-4 md:p-5">
      <div class="flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
        <div class="flex flex-col gap-3 xl:flex-row xl:items-center">
          <SearchField v-model="searchText" />
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <button
            type="button"
            class="h-9 rounded-full border border-[#d7ddd8] bg-white px-4 text-sm font-semibold text-sense-ink/86 transition hover:border-[#7e9993] hover:text-[#5f8b84] disabled:cursor-not-allowed disabled:opacity-60 dark:border-white/10 dark:bg-white/5 dark:text-white dark:hover:border-sense-mint dark:hover:text-sense-mint"
            :disabled="liveDataRefreshing"
            @click="refreshData"
          >
            {{ liveDataRefreshing ? 'Refreshing...' : 'Refresh Data' }}
          </button>

          <button
            type="button"
            class="h-9 rounded-full bg-[#5f8b84] px-5 text-sm font-semibold text-white transition hover:bg-[#4f7771] dark:bg-sense-mint dark:text-sense-night dark:hover:bg-white"
            @click="drawerOpen = true"
          >
            Filter by My Needs
          </button>
        </div>
      </div>

      <div class="mt-3 space-y-1.5">
        <p
          v-if="liveDataMessage"
          class="text-[11px] font-medium tracking-[0.02em] text-sense-ink/56 dark:text-slate-100"
        >
          {{ liveDataMessage }}
        </p>
        <p
          v-if="liveDataTimestampLabel"
          class="text-[11px] font-medium tracking-[0.02em] text-sense-ink/52 dark:text-slate-200"
        >
          {{ liveDataTimestampLabel }}
        </p>
        <p
          v-if="searchCoverageMessage"
          class="text-[11px] font-medium tracking-[0.02em] text-sense-ink/52 dark:text-slate-200"
        >
          {{ searchCoverageMessage }}
        </p>
      </div>

      <div class="mt-6 grid gap-6 xl:grid-cols-[minmax(0,1.63fr)_minmax(320px,0.98fr)]">
        <MapCanvas
          :locations="filteredLocations"
          :all-locations="allLocations"
        />
        <LocationList
          :locations="filteredLocations"
          :coverage-results="expandedSearchResults"
          :limit="10"
        />
      </div>
    </section>

    <PreferencesDrawer v-model="drawerOpen" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';

import LocationList from '@/components/map/LocationList.vue';
import MapCanvas from '@/components/map/MapCanvas.vue';
import SearchField from '@/components/map/SearchField.vue';
import PreferencesDrawer from '@/components/preferences/PreferencesDrawer.vue';
import { useLocationsStore } from '@/stores/locations';
import { usePreferencesStore } from '@/stores/preferences';

const locationsStore = useLocationsStore();
const preferencesStore = usePreferencesStore();
const drawerOpen = ref(false);

const allLocations = computed(() => locationsStore.locations);
const filteredLocations = computed(() => locationsStore.filteredLocations);
const expandedSearchResults = computed(() => locationsStore.expandedSearchResults);
const liveDataMessage = computed(() => locationsStore.liveDataMessage);
const liveDataTimestampLabel = computed(() => locationsStore.liveDataTimestampLabel);
const liveDataRefreshing = computed(() => locationsStore.liveDataRefreshing);
const searchCoverageMessage = computed(() => locationsStore.searchCoverageMessage);

const searchText = computed({
  get: () => locationsStore.searchText,
  set: (value: string) => locationsStore.setSearchText(value),
});

watch(
  () => preferencesStore.currentFilters,
  () => {
    void locationsStore.applyBackendFilter();
  },
  { deep: true, immediate: true },
);

onMounted(() => {
  locationsStore.setCategory('All');
});

function refreshData() {
  void locationsStore.refreshLiveData();
}
</script>
