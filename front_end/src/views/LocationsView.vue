<template>
  <section class="mx-auto w-full max-w-[1080px] space-y-5 pb-8">
    <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
      <div class="space-y-2">
        <RouterLink
          to="/map"
          class="inline-flex w-fit items-center gap-2 text-[13px] font-medium text-sense-teal underline decoration-sense-teal/25 underline-offset-4 dark:text-sense-mint"
        >
          <svg viewBox="0 0 16 16" fill="none" class="h-3.5 w-3.5" aria-hidden="true">
            <path d="M9.5 3.5 5 8l4.5 4.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          Back to Map
        </RouterLink>
        <div>
          <p class="text-[12px] font-semibold uppercase tracking-[0.18em] text-sense-teal/68 dark:text-sense-mint">
            Current backend locations
          </p>
          <h1 class="mt-2 text-[34px] font-semibold leading-tight text-sense-ink dark:text-white md:text-[44px]">
            All Locations
          </h1>
        </div>
      </div>

      <p class="rounded-full border border-sense-teal/12 bg-white/76 px-4 py-2 text-sm font-semibold text-sense-ink/72 dark:border-white/10 dark:bg-white/5 dark:text-slate-100">
        {{ locations.length }} locations
      </p>
    </div>

    <section class="figma-panel-soft overflow-hidden">
      <div class="grid grid-cols-[64px_minmax(0,1fr)_120px_120px] gap-3 border-b border-[#ebe5dd] px-5 py-3 text-[11px] font-semibold uppercase tracking-[0.14em] text-sense-ink/45 dark:border-white/8 dark:text-slate-200 max-sm:hidden">
        <span>Score</span>
        <span>Name</span>
        <span>Type</span>
        <span>Status</span>
      </div>

      <div
        v-if="locations.length"
        class="divide-y divide-[#ebe5dd] dark:divide-white/8"
      >
        <RouterLink
          v-for="location in locations"
          :key="location.locationId"
          :to="`/location/${location.locationId}`"
          class="group grid gap-3 px-5 py-4 transition hover:bg-[#faf8f4] dark:hover:bg-white/[0.045] sm:grid-cols-[64px_minmax(0,1fr)_120px_120px] sm:items-center"
        >
          <span
            class="flex h-8 w-8 items-center justify-center rounded-full text-[0.78rem] font-bold text-white shadow-[0_6px_12px_rgba(79,90,83,0.11)]"
            :class="priorityTone(location.sensoryScore)"
          >
            {{ location.sensoryScore }}
          </span>

          <div class="min-w-0">
            <p class="truncate text-[16px] font-semibold text-sense-ink group-hover:text-[#5f8b84] dark:text-white dark:group-hover:text-sense-mint">
              {{ location.name }}
            </p>
            <p class="mt-1 text-xs text-sense-ink/52 dark:text-slate-200">
              ID #{{ location.locationId }}
            </p>
          </div>

          <span class="w-fit rounded-full border border-sense-teal/10 bg-white/78 px-3 py-1 text-xs font-semibold text-sense-teal dark:border-white/10 dark:bg-white/5 dark:text-sense-mint">
            {{ location.displayType }}
          </span>

          <span
            class="w-fit rounded-full border px-3 py-1 text-xs font-semibold"
            :class="statusTone(location.summaryCrowdLevel ?? location.crowdSnapshot.crowdLevel)"
          >
            {{ location.summaryStatusLabel ?? location.crowdSnapshot.shortLabel }}
          </span>
        </RouterLink>
      </div>

      <div
        v-else
        class="px-5 py-10 text-sm leading-6 text-sense-ink/62 dark:text-slate-100"
      >
        Locations are still loading. If this persists, return to the map and refresh data.
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink } from 'vue-router';

import { useLocationsStore } from '@/stores/locations';
import type { CrowdLevel, Location } from '@/types/domain';

const locationsStore = useLocationsStore();
const locations = computed(() => locationsStore.locations);

function priorityTone(score: Location['sensoryScore']): string {
  if (score <= 2) {
    return 'bg-[#98c39b]';
  }

  if (score === 3) {
    return 'bg-[#d39b57]';
  }

  return 'bg-[#cb8b79]';
}

function statusTone(level: CrowdLevel): string {
  if (level === 'high') {
    return 'border-rose-200 bg-rose-50 text-rose-700 dark:border-[#f0a0a0] dark:bg-[#5a1f2b] dark:text-rose-50';
  }

  if (level === 'moderate') {
    return 'border-amber-200 bg-amber-50 text-amber-700 dark:border-[#f1c96b] dark:bg-[#5a4215] dark:text-amber-50';
  }

  return 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-[#87d8a8] dark:bg-[#174633] dark:text-emerald-50';
}
</script>
