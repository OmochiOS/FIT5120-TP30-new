<template>
  <section class="space-y-3.5">
    <p class="text-[18px] font-semibold text-sense-ink dark:text-white">
      Best Times to Visit
    </p>

    <div class="detail-surface detail-best-times rounded-[12px] border border-sense-teal/10 bg-white px-4 py-4 shadow-[0_12px_24px_-20px_rgba(16,74,85,0.35)] dark:border-white/8 dark:bg-[#16252f]/92 md:px-4.5">
      <div class="flex flex-wrap gap-2">
        <button
          v-for="day in days"
          :key="day"
          type="button"
          class="h-8 min-w-[54px] rounded-[8px] border px-3 text-[11px] font-medium transition"
          :class="selectedDay === day
            ? 'border-sense-teal bg-sense-teal text-white dark:border-sense-mint dark:bg-sense-mint dark:text-sense-night'
            : 'detail-day-button border-sense-teal/12 bg-white text-sense-ink/72 dark:border-white/10 dark:bg-[#1b2b35] dark:text-slate-100'"
          @click="selectedDay = day"
        >
          {{ day }}
        </button>
      </div>

      <div class="mt-4">
        <div class="flex h-6 overflow-hidden rounded-[6px] border border-sense-teal/8 dark:border-white/8">
          <div
            v-for="segment in selectedPlan.timeBar"
            :key="segment.time"
            class="min-w-0 flex-1"
            :class="toneClass(segment.level)"
          />
        </div>
        <div class="detail-axis mt-1 flex items-center justify-between text-[10px] text-sense-ink/44 dark:text-slate-200">
          <span>7am</span>
          <span>9pm</span>
        </div>
      </div>

      <div class="mt-3 flex justify-start">
        <MapLegend variant="detail" />
      </div>

      <p class="detail-copy mt-4 text-[13px] font-medium text-sense-ink/66 dark:text-slate-100">
        {{ selectedPlan.quietestNote }}
      </p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import MapLegend from '@/components/map/MapLegend.vue';
import type { LocationPrediction, CrowdLevel, DayLabel } from '@/types/domain';

const props = defineProps<{
  prediction: LocationPrediction;
}>();

const days: DayLabel[] = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
const selectedDay = ref<DayLabel>('Tue');

watch(
  () => props.prediction.activeDay,
  (nextDay) => {
    selectedDay.value = days.includes(nextDay) ? nextDay : 'Tue';
  },
  { immediate: true },
);

const selectedPlan = computed(() => {
  const dayPlan = props.prediction.dayPlans?.[selectedDay.value];

  if (dayPlan) {
    return dayPlan;
  }

  return {
    rows: props.prediction.rows,
    timeBar: props.prediction.timeBar,
    quietestNote: props.prediction.quietestNote,
  };
});

function toneClass(level: CrowdLevel): string {
  if (level === 'low') {
    return 'bg-[#95c894] dark:bg-emerald-500/40';
  }

  if (level === 'moderate') {
    return 'bg-[#e0b16f] dark:bg-amber-500/40';
  }

  return 'bg-[#efc1c8] dark:bg-rose-500/40';
}
</script>
