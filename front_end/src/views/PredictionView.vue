<template>
  <section class="prediction-page pb-4 pt-7 md:pt-8">
    <div class="mx-auto max-w-[700px] px-4 md:px-6">
      <div class="space-y-4">
        <div class="space-y-2">
          <h1 class="text-[22px] font-semibold tracking-[-0.02em] text-sense-ink dark:text-white md:text-[24px]">
            Crowd Level Prediction
          </h1>
          <p class="prediction-subtle text-[13px] leading-6 text-sense-ink/54 dark:text-slate-100">
            Search a location or postcode to see predicted crowd levels
          </p>
        </div>

        <div class="flex flex-col gap-2.5 sm:flex-row sm:items-center">
          <input
            v-model="query"
            type="search"
            placeholder="Enter location or postcode"
            class="prediction-input h-10 flex-1 rounded-[8px] border border-sense-teal/12 bg-white px-4 text-sm text-sense-ink outline-none transition placeholder:text-sense-ink/38 focus:border-sense-teal focus:ring-2 focus:ring-sense-teal/10 dark:border-white/10 dark:bg-white/5 dark:text-white dark:placeholder:text-slate-300"
            @keydown.enter.prevent="handleSearch"
          >
          <button
            type="button"
            class="prediction-action h-10 rounded-[6px] bg-sense-teal px-7 text-sm font-semibold text-white transition hover:bg-sense-ink dark:bg-sense-mint dark:text-sense-night dark:hover:bg-white"
            @click="handleSearch"
          >
            Search
          </button>
        </div>

      <div class="flex flex-wrap items-center gap-2 text-[12px]">
        <span class="prediction-subtle font-medium text-sense-ink/42 dark:text-slate-200">Popular:</span>
        <button
          v-for="pill in predictionPopularSearches"
            :key="pill.label"
            type="button"
            class="prediction-pill rounded-full border border-sense-teal/14 bg-white px-4 py-1.5 text-[12px] font-medium text-sense-ink/74 transition hover:border-sense-teal/30 dark:border-white/10 dark:bg-white/5 dark:text-slate-200"
            @click="applyPopularSearch(pill.query)"
          >
            {{ pill.label }}
          </button>
        </div>

        <div
          v-if="searchFeedback"
          class="prediction-note rounded-[14px] border border-amber-200 bg-amber-50 px-4 py-3 text-[12px] text-amber-800 dark:border-amber-400/30 dark:bg-amber-500/10 dark:text-amber-100"
        >
          {{ searchFeedback }}
        </div>

        <div
          v-if="!selectedLocation"
          class="prediction-empty flex min-h-[220px] flex-col items-center justify-center gap-4 px-4 pb-4 pt-14 text-center"
        >
          <div class="prediction-empty-icon flex h-12 w-12 items-center justify-center rounded-full border border-sense-teal/14 text-sense-ink/34 dark:border-white/10 dark:text-slate-200">
            <svg viewBox="0 0 24 24" fill="none" class="h-8 w-8" aria-hidden="true">
              <circle cx="11" cy="11" r="6.5" stroke="currentColor" stroke-width="1.7" />
              <path d="M16 16L20 20" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
            </svg>
          </div>
          <p class="prediction-subtle text-[14px] leading-7 text-sense-ink/56 dark:text-slate-100">
            Enter a location name or postcode to see crowd predictions
          </p>
        </div>

        <template v-else>
          <section class="prediction-surface rounded-[16px] border border-sense-teal/10 bg-white px-5 py-4 dark:border-white/8 dark:bg-white/5">
            <div class="flex items-start justify-between gap-4">
              <div>
                <h2 class="text-[21px] font-semibold tracking-[-0.02em] text-sense-ink dark:text-white">
                  {{ selectedLocation.name }}
                </h2>
                <p class="prediction-subtle mt-1 text-[13px] text-sense-ink/54 dark:text-slate-100">
                  {{ selectedLocation.displayType }}
                </p>
              </div>

              <div
                class="flex h-11 w-11 items-center justify-center rounded-full text-sm font-bold text-white"
                :class="scoreTone(selectedLocation.sensoryScore)"
              >
                {{ selectedLocation.sensoryScore }}
              </div>
            </div>
          </section>

          <div
            v-if="predictionMessage || predictionTimestampLabel"
            class="prediction-note space-y-1 rounded-[14px] border border-sense-teal/10 bg-[#f8f4ee] px-4 py-3 text-[12px] text-sense-ink/62 dark:border-[#425a66] dark:bg-[#142631] dark:text-slate-100"
          >
            <p v-if="predictionMessage">
              {{ predictionMessage }}
            </p>
            <p v-if="predictionTimestampLabel">
              {{ predictionTimestampLabel }}
            </p>
          </div>

          <div
            v-if="predictionStatus === 'loading' && !prediction"
            class="prediction-loading rounded-[16px] border border-dashed border-sense-teal/14 bg-white px-4 py-10 text-center text-[13px] text-sense-ink/54 dark:border-white/10 dark:bg-white/5 dark:text-slate-100"
          >
            Loading historical pedestrian counts...
          </div>

          <section
            v-if="prediction"
            class="prediction-surface overflow-hidden rounded-[16px] border border-sense-teal/10 bg-white dark:border-white/8 dark:bg-white/5"
          >
            <div class="prediction-table-head grid grid-cols-[1.05fr_1fr_1fr_1.1fr] bg-[#f3efe9] text-[12px] font-semibold text-sense-ink/72 dark:bg-[#172833] dark:text-slate-50">
              <div class="px-4 py-3">Time</div>
              <div class="px-4 py-3">Predicted Level</div>
              <div class="px-4 py-3">Crowd Estimate</div>
              <div class="px-4 py-3">Recommendation</div>
            </div>

            <div
              v-for="(row, index) in predictionRows"
              :key="row.time"
              class="prediction-table-row grid min-h-[42px] grid-cols-[1.05fr_1fr_1fr_1.1fr] items-center text-[12px]"
              :class="index % 2 === 0 ? 'bg-white dark:bg-transparent' : 'bg-[#faf8f4] dark:bg-white/[0.03]'"
            >
              <div class="prediction-table-primary px-4 py-3 font-medium text-sense-ink dark:text-white">
                {{ row.time }}
              </div>
              <div class="prediction-table-copy flex items-center gap-2 px-4 py-3 text-sense-ink/74 dark:text-slate-100">
                <span class="h-2 w-2 rounded-full" :class="dotTone(row.level)"></span>
                <span>{{ levelLabel(row.level) }}</span>
              </div>
              <div class="prediction-table-copy px-4 py-3 text-sense-ink/68 dark:text-slate-100">
                ~{{ row.estimate }}
              </div>
              <div class="prediction-table-copy px-4 py-3 text-sense-ink/68 dark:text-slate-100">
                {{ row.recommendation }}
              </div>
            </div>
          </section>

          <section class="prediction-surface rounded-[16px] border border-sense-teal/10 bg-white px-4 py-4 dark:border-white/8 dark:bg-white/5">
            <div class="flex h-8 overflow-hidden rounded-full border border-sense-teal/10 dark:border-white/8">
              <div
                v-for="segment in predictionTimeBar"
                :key="segment.time"
                class="flex min-w-0 flex-1 items-center justify-center px-1 text-[10px] font-semibold"
                :class="toneBar(segment.level)"
              >
                {{ segment.time }}
              </div>
            </div>
          </section>
        </template>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

import { useLocationsStore } from '@/stores/locations';
import { findPredictionLocation, predictionPopularSearches } from '@/utils/prediction';

const query = ref('');
const selectedLocationId = ref<string | null>(null);
const searchFeedback = ref('');
const locationsStore = useLocationsStore();
const locations = computed(() => locationsStore.locations);

const selectedLocation = computed(() => {
  if (!selectedLocationId.value) {
    return null;
  }

  return locations.value.find((location) => location.locationId === selectedLocationId.value) ?? null;
});

const predictionSnapshot = computed(() =>
  selectedLocationId.value ? locationsStore.getPredictionSnapshot(selectedLocationId.value) : null,
);
const prediction = computed(() => predictionSnapshot.value?.prediction ?? null);
const predictionStatus = computed(() => predictionSnapshot.value?.status ?? 'loading');
const predictionMessage = computed(() => predictionSnapshot.value?.note ?? '');
const predictionTimestampLabel = computed(() =>
  selectedLocationId.value ? locationsStore.getPredictionTimestampLabel(selectedLocationId.value) : '',
);
const predictionRows = computed(() => prediction.value?.rows ?? []);
const predictionTimeBar = computed(() => prediction.value?.timeBar ?? []);

async function handleSearch() {
  const normalizedQuery = query.value.trim();

  if (!normalizedQuery) {
    selectedLocationId.value = null;
    searchFeedback.value = 'Enter a location name or postcode to load a prediction.';
    return;
  }

  const match = findPredictionLocation(locations.value, query.value);

  if (match) {
    searchFeedback.value = '';
    selectedLocationId.value = match.locationId;
    await locationsStore.hydratePrediction(match.locationId);
    return;
  }

  selectedLocationId.value = null;
  searchFeedback.value = `No supported prediction location matched "${normalizedQuery}". Try a map location like Melbourne Central, Southbank, Carlton, or Docklands.`;
}

async function applyPopularSearch(nextQuery: string) {
  query.value = nextQuery;
  await handleSearch();
}

function levelLabel(level: 'low' | 'moderate' | 'high') {
  return level === 'low' ? 'Low' : level === 'moderate' ? 'Moderate' : 'High';
}

function dotTone(level: 'low' | 'moderate' | 'high') {
  return level === 'low' ? 'bg-emerald-500' : level === 'moderate' ? 'bg-amber-500' : 'bg-rose-500';
}

function toneBar(level: 'low' | 'moderate' | 'high') {
  if (level === 'low') {
    return 'bg-emerald-100 text-emerald-800 dark:bg-emerald-500/20 dark:text-emerald-100';
  }

  if (level === 'moderate') {
    return 'bg-amber-100 text-amber-800 dark:bg-amber-500/20 dark:text-amber-100';
  }

  return 'bg-rose-100 text-rose-800 dark:bg-rose-500/20 dark:text-rose-100';
}

function scoreTone(score: number) {
  if (score <= 2) {
    return 'bg-emerald-500';
  }

  if (score === 3) {
    return 'bg-amber-500';
  }

  return 'bg-rose-500';
}
</script>
