<template>
  <section
    v-if="location"
    class="detail-page mx-auto max-w-[860px] space-y-6 pb-6 pt-1"
  >
    <RouterLink
      to="/map"
      class="inline-flex w-fit items-center gap-1.5 text-[14px] md:text-[14px] font-medium text-sense-teal underline decoration-sense-teal/25 underline-offset-4 transition hover:text-sense-ink dark:text-sense-mint dark:hover:text-white"
    >
      <svg viewBox="0 0 16 16" fill="none" class="h-3.5 w-3.5" aria-hidden="true">
        <path d="M9.5 3.5 5 8l4.5 4.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
      Back to Map
    </RouterLink>

    <section class="grid gap-6 lg:grid-cols-[minmax(0,1fr)_80px] lg:items-start">
      <div class="space-y-3">
        <div class="space-y-2.5">
          <h1 class="text-[32px] font-semibold tracking-tight leading-[1.05] text-sense-ink dark:text-white md:text-[40px]">
            {{ location.name }}
          </h1>
          <div class="flex flex-wrap items-center gap-3">
            <span class="rounded-full border border-sense-teal/15 bg-white px-3 py-1 text-[12px] font-medium text-sense-teal dark:border-white/10 dark:bg-white/5 dark:text-sense-mint">
              {{ location.category }}
            </span>
            <p class="detail-meta text-[15px] md:text-[16px] text-sense-ink/60 dark:text-slate-100">
              {{ location.addressOrArea }}
            </p>
          </div>
        </div>
        <p class="detail-summary max-w-[620px] text-[16px] md:text-[17px] leading-relaxed text-sense-ink/80 dark:text-slate-50">
          {{ location.crowdSnapshot.summary }}
        </p>
        <p v-if="location.crowdSnapshot.shortLabel.toLowerCase() !== location.crowdSnapshot.summary.toLowerCase()" class="detail-note text-[14px] md:text-[15px] font-medium text-sense-ink/50 dark:text-slate-100">
          {{ location.crowdSnapshot.shortLabel }}
        </p>
      </div>

      <div class="flex flex-col items-center gap-1 pt-1">
        <div
          class="flex h-[80px] w-[80px] items-center justify-center rounded-full text-center shadow-sm"
          :class="scoreBubbleClass"
        >
          <span class="text-[32px] font-bold leading-none">
            {{ location.sensoryScore }}
          </span>
        </div>
        <span class="detail-meta mt-1.5 text-[14px] font-semibold text-sense-ink/60 dark:text-slate-100">
          {{ toSentenceCase(location.crowdSnapshot.label) }}
        </span>
      </div>
    </section>

    <SensoryProfileCard :profile="location.sensoryProfile" />

    <section class="space-y-2">
      <div
        v-if="predictionMessage || predictionTimestampLabel"
        class="detail-meta space-y-1 text-[14px] text-sense-ink/54 dark:text-slate-100"
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
        class="detail-surface rounded-[18px] border border-dashed border-sense-teal/14 bg-white px-5 py-8 text-[15px] text-sense-ink/56 dark:border-white/10 dark:bg-[#16252f]/90 dark:text-slate-100"
      >
        Loading historical pedestrian counts for best-time guidance...
      </div>

      <BestTimesCard
        v-if="prediction"
        :prediction="prediction"
      />
    </section>

    <PreparationGuideCard :items="goodToKnowItems" />
  </section>

  <section
    v-else-if="isLoadingLocation"
    class="glass-panel-strong space-y-4 p-6"
  >
    <p class="text-sm font-semibold uppercase tracking-[0.16em] text-sense-teal/70 dark:text-sense-mint">
      Loading location
    </p>
    <h1 class="font-display text-4xl text-sense-ink dark:text-white">
      Preparing this location view.
    </h1>
    <p class="max-w-2xl text-base leading-7 text-sense-ink/72 dark:text-slate-100">
      We are loading the location summary and sensory details now.
    </p>
  </section>

  <section
    v-else
    class="glass-panel-strong space-y-4 p-6"
  >
    <p class="text-sm font-semibold uppercase tracking-[0.16em] text-sense-teal/70 dark:text-sense-mint">
      Location not found
    </p>
    <h1 class="font-display text-4xl text-sense-ink dark:text-white">
      We couldn't find that location.
    </h1>
    <p class="max-w-2xl text-base leading-7 text-sense-ink/72 dark:text-slate-100">
      Return to the map and choose another place to continue exploring.
    </p>
    <RouterLink
      to="/map"
      class="inline-flex rounded-full bg-sense-teal px-5 py-3 text-sm font-semibold text-white transition hover:bg-sense-ink dark:bg-sense-mint dark:text-sense-night dark:hover:bg-white"
    >
      Back to map
    </RouterLink>
  </section>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import { RouterLink, useRoute } from 'vue-router';

import BestTimesCard from '@/components/sensory/BestTimesCard.vue';
import PreparationGuideCard from '@/components/sensory/PreparationGuideCard.vue';
import SensoryProfileCard from '@/components/sensory/SensoryProfileCard.vue';
import type { GoodToKnowItem } from '@/types/domain';
import { useLocationsStore } from '@/stores/locations';
import { getGoodToKnow } from '@/utils/location-details';

const route = useRoute();
const locationsStore = useLocationsStore();
const currentLocationId = computed(() => String(route.params.locationId ?? ''));

const location = computed(() => locationsStore.getLocationById(currentLocationId.value));
const predictionSnapshot = computed(() =>
  location.value ? locationsStore.getPredictionSnapshot(location.value.locationId) : null,
);
const prediction = computed(() => predictionSnapshot.value?.prediction ?? null);
const predictionStatus = computed(() => predictionSnapshot.value?.status ?? 'loading');
const predictionMessage = computed(() => predictionSnapshot.value?.note ?? '');
const predictionTimestampLabel = computed(() =>
  location.value ? locationsStore.getPredictionTimestampLabel(location.value.locationId) : '',
);
const goodToKnowItems = computed<GoodToKnowItem[]>(() =>
  location.value ? getGoodToKnow(location.value) : [],
);
const isLoadingLocation = computed(() =>
  !locationsStore.locationsHydrated || locationsStore.detailRefreshing[currentLocationId.value],
);

watch(
  () => location.value?.locationId,
  (nextLocationId) => {
    if (!nextLocationId) {
      return;
    }

    void locationsStore.hydrateLocationDetail(nextLocationId);
    void locationsStore.hydratePrediction(nextLocationId);
  },
  { immediate: true },
);

const scoreBubbleClass = computed(() => {
  if (!location.value) {
    return '';
  }

  if (location.value.sensoryScore <= 2) {
    return 'bg-[#a9d3a6] text-white dark:bg-emerald-400/60 dark:text-sense-night';
  }

  if (location.value.sensoryScore === 3) {
    return 'bg-[#d7b078] text-white dark:bg-amber-400/60 dark:text-sense-night';
  }

  return 'bg-[#db7480] text-white dark:bg-rose-400/60 dark:text-sense-night';
});

function toSentenceCase(value: string): string {
  const normalized = value.trim().toLowerCase();

  return normalized ? normalized[0].toUpperCase() + normalized.slice(1) : value;
}
</script>
