<template>
  <section class="figma-panel-soft h-full overflow-hidden">
    <div class="flex items-center justify-between gap-3 border-b border-[#ebe5dd] px-5 py-4 dark:border-white/8">
      <h2 class="text-base font-semibold text-sense-ink dark:text-white">
        Locations
      </h2>
      <RouterLink
        to="/locations"
        class="shrink-0 rounded-full border border-sense-teal/12 bg-white/74 px-3 py-1 text-xs font-semibold text-sense-teal transition hover:border-sense-teal/28 hover:bg-[#f5faf7] dark:border-white/10 dark:bg-white/5 dark:text-sense-mint dark:hover:border-sense-mint/40 dark:hover:bg-white/10"
      >
        More+
      </RouterLink>
    </div>

    <div
      v-if="displayedLocations.length"
      class="divide-y divide-[#ebe5dd] dark:divide-white/8"
    >
      <RouterLink
        v-for="location in displayedLocations"
        :key="location.locationId"
        :to="`/location/${location.locationId}`"
        class="group grid min-h-[52px] grid-cols-[auto_minmax(0,1fr)] items-center gap-3 px-5 py-3 transition hover:bg-[#faf8f4] dark:hover:bg-white/[0.045]"
      >
        <span
          class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-[0.72rem] font-bold text-white shadow-[0_6px_12px_rgba(79,90,83,0.11)]"
          :class="priorityTone(location.sensoryScore)"
        >
          {{ location.sensoryScore }}
        </span>

        <div class="min-w-0">
          <p class="truncate font-semibold text-sense-ink group-hover:text-[#5f8b84] dark:text-white dark:group-hover:text-sense-mint">
            {{ location.name }}
          </p>
          <p class="text-xs text-sense-ink/58 dark:text-slate-200">
            {{ location.displayType }}
          </p>
        </div>
      </RouterLink>
    </div>

    <div
      v-if="coverageResults.length"
      class="border-t border-[#ebe5dd] dark:border-white/8"
    >
      <div class="px-5 py-3">
        <p class="text-[11px] font-semibold uppercase tracking-[0.16em] text-sense-ink/48 dark:text-slate-200">
          More Melbourne Places
        </p>
      </div>

      <div class="divide-y divide-[#ebe5dd] dark:divide-white/8">
        <div
          v-for="place in coverageResults"
          :key="`${place.name}-${place.subTheme}`"
          class="grid min-h-[58px] grid-cols-[auto_minmax(0,1fr)] items-center gap-3 px-5 py-3"
        >
          <span class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full border border-[#dbe1db] bg-[#f6f4ef] text-[0.72rem] font-bold text-[#6d8f88] dark:border-white/10 dark:bg-white/8 dark:text-sense-mint">
            +
          </span>

          <div class="min-w-0">
            <p class="truncate font-semibold text-sense-ink dark:text-white">
              {{ place.name }}
            </p>
            <p class="truncate text-xs text-sense-ink/58 dark:text-slate-200">
              {{ place.subTheme }}
            </p>
            <p class="mt-1 text-[11px] text-sense-ink/46 dark:text-slate-200">
              Official place match from the Melbourne places dataset.
            </p>
          </div>
        </div>
      </div>
    </div>

    <div
      v-else-if="!displayedLocations.length"
      class="m-5 rounded-[22px] border border-dashed border-[#d9dfd8] bg-[#faf8f4] px-4 py-6 text-sm leading-6 text-sense-ink/70 dark:border-white/10 dark:bg-white/5 dark:text-slate-100"
    >
      No locations match your current filters. Try widening your category or sensory needs.
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink } from 'vue-router';

import type { Location, SearchCoveragePlace } from '@/types/domain';

const props = withDefaults(defineProps<{
  locations: Location[];
  coverageResults?: SearchCoveragePlace[];
  limit?: number;
}>(), {
  limit: 10,
  coverageResults: () => [],
});

const displayedLocations = computed(() => props.locations.slice(0, props.limit));

function priorityTone(score: Location['sensoryScore']): string {
  if (score <= 2) {
    return 'bg-[#98c39b]';
  }

  if (score === 3) {
    return 'bg-[#d39b57]';
  }

  return 'bg-[#cb8b79]';
}
</script>
