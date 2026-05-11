<template>
  <section class="pb-4" :class="preferencesStore.quickViewModeActive ? 'quick-view-page space-y-4 pt-1 page-intro' : 'space-y-5'">
    
    <!-- Quick View Header -->
    <div v-if="preferencesStore.quickViewModeActive" class="figma-panel px-4 py-3 md:px-5 flex items-center justify-between gap-4">
      <SearchField v-model="searchText" class="flex-1 max-w-[400px]" />
      <button 
        type="button" 
        @click="preferencesStore.setQuickViewMode(false)" 
        class="inline-flex shrink-0 items-center gap-1.5 rounded-full border border-sense-ink/15 bg-white/50 px-3.5 py-[5px] text-[13px] font-semibold text-sense-ink/80 transition hover:border-sense-ink/30 hover:bg-white dark:border-white/15 dark:bg-white/5 dark:text-slate-200 dark:hover:border-white/30 dark:hover:bg-white/10"
      >
        <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" class="h-3.5 w-3.5">
          <path d="M11.5 4.5L4.5 11.5" />
          <path d="M4.5 4.5l7 7" />
        </svg>
        Exit
      </button>
    </div>

    <!-- Regular Map Header -->
    <section v-show="!preferencesStore.quickViewModeActive" class="figma-panel p-4 md:p-5">
      <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div class="flex flex-col gap-2 md:flex-row md:items-center">
          <div data-guide="search">
            <SearchField
              v-model="searchText"
              show-dropdown
              :results="topLocations"
              @select="onListSelect"
            />
          </div>
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
            data-guide="filter"
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
      </div>
    </section>

    <!-- Empty State for Quick View Mode -->
    <template v-if="preferencesStore.quickViewModeActive && displayedLocations.length === 0 && !isLoading">
      <div
        v-if="searchText"
        class="figma-panel-soft rounded-[22px] px-6 py-8 text-center text-[14px] text-sense-ink/56 dark:text-slate-300"
      >
        No locations match "<strong class="font-semibold text-sense-ink dark:text-white">{{ searchText }}</strong>".
      </div>
      <div
        v-else
        class="figma-panel-soft rounded-[22px] px-6 py-10 text-center space-y-2"
      >
        <p class="text-[18px] font-semibold text-sense-ink dark:text-white">Search to explore</p>
        <p class="text-[13px] text-sense-ink/55 dark:text-slate-300">
          Type a location name above to see its sensory score and practical tips.
        </p>
      </div>
    </template>

    <!-- Unified Map Container -->
    <div
      v-show="!preferencesStore.quickViewModeActive || (preferencesStore.quickViewModeActive && (displayedLocations.length || isLoading))"
      :class="preferencesStore.quickViewModeActive ? 'figma-panel overflow-hidden p-0' : 'mt-6'"
    >
      <div :class="preferencesStore.quickViewModeActive ? 'grid md:grid-cols-[minmax(0,1.55fr)_minmax(0,1fr)]' : 'relative'">
        <MapCanvas
          :class="preferencesStore.quickViewModeActive ? '' : 'md:h-[620px] w-full'"
          :locations="displayedLocations"
          :all-locations="allLocations"
          :external-selected-id="externalSelectedId"
          :compact="preferencesStore.quickViewModeActive"
          @clear-external="externalSelectedId = null"
          @location-clicked="(id) => { if (preferencesStore.quickViewModeActive && id) focusedLocationId = id }"
        />
        
        <QuickViewSidebar v-if="preferencesStore.quickViewModeActive" :location-id="focusedLocationId" />
        
        <!-- Floating LocationList on top-left in regular mode -->
        <div v-else class="pointer-events-none absolute left-3 top-3 z-[400] w-full max-w-[320px] md:left-4 md:top-4">
          <div class="pointer-events-auto flex flex-col gap-2">
            <button
              type="button"
              class="flex w-fit items-center gap-2 rounded-full border border-sense-teal/15 bg-white px-4 py-2 text-[13px] font-semibold text-sense-ink shadow-sm transition hover:border-sense-teal/30 dark:border-white/10 dark:bg-[#142631] dark:text-slate-100 dark:hover:border-white/30"
              @click="showLocations = !showLocations"
            >
              <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" class="h-4 w-4 transition-transform" :class="showLocations ? 'rotate-180' : ''">
                <path d="M4 7l6 6 6-6" />
              </svg>
              {{ showLocations ? 'Hide locations' : 'Show locations' }}
            </button>
            
            <Transition name="fade-slide">
              <div v-show="showLocations" class="max-h-[460px] overflow-hidden rounded-[22px] shadow-[0_12px_24px_-10px_rgba(16,74,85,0.2)]">
                <LocationList
                  class="h-full max-h-[460px]"
                  :locations="topLocations"
                  :limit="3"
                  @select="onListSelect"
                />
              </div>
            </Transition>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick View Extra Sections (Best Times, Good to Know) -->
    <template v-if="preferencesStore.quickViewModeActive && focusedLocationId">
      <QuickViewExtras :location-id="focusedLocationId" />
    </template>

    <PreferencesDrawer v-model="drawerOpen" />

    <!-- Onboarding guide -->
    <MapGuide
      v-if="!preferencesStore.quickViewModeActive"
      :first-location-id="firstLocationId"
      @select-location="onGuideSelectLocation"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';

import MapGuide from '@/components/guide/MapGuide.vue';
import LocationList from '@/components/map/LocationList.vue';
import MapCanvas from '@/components/map/MapCanvas.vue';
import SearchField from '@/components/map/SearchField.vue';
import QuickViewSidebar from '@/components/map/QuickViewSidebar.vue';
import QuickViewExtras from '@/components/map/QuickViewExtras.vue';
import PreferencesDrawer from '@/components/preferences/PreferencesDrawer.vue';
import { useLocationsStore } from '@/stores/locations';
import { usePreferencesStore } from '@/stores/preferences';
import type { Location } from '@/types/domain';

const locationsStore = useLocationsStore();
const preferencesStore = usePreferencesStore();
const drawerOpen = ref(false);
const showLocations = ref(true);
const externalSelectedId = ref<string | null>(null);
const focusedLocationId = ref<string | null>(null);

const isLoading = computed(() => !locationsStore.locationsHydrated);
const allLocations = computed(() => locationsStore.locations);
const filteredLocations = computed(() => locationsStore.filteredLocations);

const displayedLocations = computed(() => {
  if (preferencesStore.quickViewModeActive) {
    return [...locationsStore.filteredLocations].sort((a, b) => a.sensoryScore - b.sensoryScore);
  }
  return filteredLocations.value;
});

const topLocations = computed(() =>
  [...filteredLocations.value].sort((a, b) => a.sensoryScore - b.sensoryScore),
);

const liveDataMessage = computed(() => locationsStore.liveDataMessage);
const liveDataTimestampLabel = computed(() => locationsStore.liveDataTimestampLabel);
const liveDataRefreshing = computed(() => locationsStore.liveDataRefreshing);

const searchText = computed({
  get: () => locationsStore.searchText,
  set: (value: string) => locationsStore.setSearchText(value),
});

// Auto-select first location when list changes in Quick View Mode
watch(
  [displayedLocations, () => preferencesStore.quickViewModeActive],
  ([nextList, isQuickView]) => {
    if (!isQuickView) return;
    
    if (nextList.length && (!focusedLocationId.value || !nextList.some((l) => l.locationId === focusedLocationId.value))) {
      focusedLocationId.value = nextList[0]!.locationId;
      externalSelectedId.value = focusedLocationId.value;
    }
    if (!nextList.length) {
      focusedLocationId.value = null;
    }
  },
  { immediate: true },
);

// When map marker clicked in MapView → sync Quick View focused location
watch(externalSelectedId, (id) => {
  if (id) focusedLocationId.value = id;
});

function onListSelect(location: Location) {
  externalSelectedId.value = location.locationId;
}

/** Used by MapGuide to auto-select the first result for step 4 */
const firstLocationId = computed(() => topLocations.value[0]?.locationId ?? null);

function onGuideSelectLocation(id: string) {
  externalSelectedId.value = id;
}

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

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
  transform-origin: top left;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: scale(0.96) translateY(-8px);
}
</style>
