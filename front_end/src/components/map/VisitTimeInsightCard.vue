<template>
  <section class="mt-3.5 rounded-[22px] border border-sense-teal/10 bg-white/78 px-[14px] py-[14px] dark:border-[#49626e] dark:bg-[#142a35]">
    <div class="mb-3 flex items-center justify-between gap-3">
      <p class="text-[12px] font-semibold uppercase tracking-[0.14em] text-sense-ink/58 dark:text-slate-200">
        Visit time insight
      </p>
      <span class="shrink-0 rounded-full bg-[#edf8f0] px-2.5 py-1 text-[10px] font-semibold text-[#1f6b46] dark:bg-[#133127] dark:text-[#98f5bf]">
        Historical data
      </span>
    </div>

    <div class="space-y-2">
      <!-- Intended visit time -->
      <div class="flex items-center justify-between rounded-[12px] bg-white/50 px-3 py-2.5 dark:bg-[#203844]/50">
        <span class="text-[12px] text-sense-ink/66 dark:text-slate-300">Intended visit time</span>
        <span class="text-[13px] font-bold text-sense-ink dark:text-slate-50">{{ intendedVisitTimeDisplay }}</span>
      </div>

      <!-- Predicted crowd level -->
      <div class="flex items-center justify-between rounded-[12px] bg-white/50 px-3 py-2.5 dark:bg-[#203844]/50">
        <span class="text-[12px] text-sense-ink/66 dark:text-slate-300">Predicted crowd level</span>
        <span :class="['flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-[11px] font-bold', crowdLevelClass]">
          <span class="h-2 w-2 rounded-full" :class="crowdDotClass"></span>
          {{ predictedLevelDisplay }}
        </span>
      </div>

      <!-- Suitability -->
      <div class="rounded-[12px] bg-white/50 px-3 py-2.5 dark:bg-[#203844]/50">
        <div class="mb-1 flex items-center justify-between">
          <span class="text-[12px] text-sense-ink/66 dark:text-slate-300">Suitability</span>
          <span :class="['flex items-center gap-1 rounded-full px-2.5 py-0.5 text-[11px] font-bold', suitabilityClass]">
            <svg class="h-3 w-3" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
            </svg>
            {{ suitabilityLabel }}
          </span>
        </div>
        <p class="text-[11px] leading-snug text-sense-ink/60 dark:text-slate-400">
          {{ suitabilityDescription }}
        </p>
      </div>

      <!-- Calmer alternative -->
      <div v-if="calmerAlternativeDisplay" class="rounded-[12px] border border-[#c4e3d3]/50 bg-[#edf8f0]/80 px-3 py-2.5 dark:border-[#224b3c] dark:bg-[#133127]/50">
        <div class="mb-1 flex items-center justify-between">
          <span class="text-[12px] text-[#2c7753] dark:text-[#a5e0be]">Calmer alternative</span>
          <span class="text-[12px] font-bold text-sense-ink dark:text-slate-50">{{ calmerAlternativeDisplay }}</span>
        </div>
        <p class="text-[11px] leading-snug text-[#3e8a64] dark:text-[#88c9a6]">
          {{ calmerAlternativeDescription }}
        </p>
      </div>
    </div>
    
    <p class="mt-3 flex items-start gap-1 text-[10px] text-sense-ink/40 dark:text-slate-500">
      <svg class="mt-0.5 h-3 w-3 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      Estimate based on historical pedestrian patterns
    </p>
  </section>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';

import type { Location } from '@/types/domain';
import { useLocationsStore } from '@/stores/locations';
import { usePreferencesStore } from '@/stores/preferences';

const props = defineProps<{
  location: Location;
}>();

const locationsStore = useLocationsStore();
const preferencesStore = usePreferencesStore();

watch(
  () => props.location?.locationId,
  (newId) => {
    if (newId) {
      locationsStore.hydratePrediction(newId);
    }
  },
  { immediate: true }
);

const currentPredictionTime = computed(() => preferencesStore.predictionTime);

const prediction = computed(() => {
  return locationsStore.getPredictionByLocationId(props.location.locationId);
});

const targetRow = computed(() => {
  const pred = prediction.value;
  if (!pred) return null;
  const activeDayPlans = pred.dayPlans?.[pred.activeDay];
  const rows = activeDayPlans?.rows || pred.rows;
  
  if (currentPredictionTime.value && currentPredictionTime.value !== 'any') {
    const found = rows.find(r => r.time === currentPredictionTime.value);
    if (found) return found;
  }
  
  const hour = new Date().getHours();
  const bucket = [
    { label: '7-9am', startHour: 7, endHour: 9 },
    { label: '9-11am', startHour: 9, endHour: 11 },
    { label: '11am-1pm', startHour: 11, endHour: 13 },
    { label: '1-3pm', startHour: 13, endHour: 15 },
    { label: '3-5pm', startHour: 15, endHour: 17 },
    { label: '5-7pm', startHour: 17, endHour: 19 },
    { label: '7-9pm', startHour: 19, endHour: 21 },
    { label: '9-11pm', startHour: 21, endHour: 23 },
  ].find(b => hour >= b.startHour && hour < b.endHour);
  
  if (bucket) {
    const found = rows.find(r => r.time === bucket.label);
    if (found) return found;
  }
  return rows[0];
});

const intendedVisitTimeDisplay = computed(() => {
  if (currentPredictionTime.value && currentPredictionTime.value !== 'any') {
    return currentPredictionTime.value;
  }
  return targetRow.value?.time || 'Current time';
});

const predictedLevelDisplay = computed(() => {
  if (!targetRow.value) return 'Unknown';
  const level = targetRow.value.level;
  if (level === 'low') return 'Calm';
  if (level === 'moderate') return 'Moderate';
  return 'Busy';
});

const crowdLevelClass = computed(() => {
  const level = targetRow.value?.level;
  if (level === 'low') return 'bg-[#edf8f0] text-[#1f6b46] dark:bg-[#133127] dark:text-[#98f5bf]';
  if (level === 'moderate') return 'bg-[#fbf2e4] text-[#9f5a0c] dark:bg-[#352712] dark:text-[#ffd56f]';
  return 'bg-rose-100 text-rose-800 dark:bg-rose-900/40 dark:text-rose-200';
});

const crowdDotClass = computed(() => {
  const level = targetRow.value?.level;
  if (level === 'low') return 'bg-[#2c7753]';
  if (level === 'moderate') return 'bg-[#c27c24]';
  return 'bg-rose-600';
});

const suitabilityClass = computed(() => {
  const level = targetRow.value?.level;
  if (level === 'low' || level === 'moderate') return 'bg-[#edf8f0] text-[#1f6b46] dark:bg-[#133127] dark:text-[#98f5bf]';
  return 'bg-rose-100 text-rose-800 dark:bg-rose-900/40 dark:text-rose-200';
});

const suitabilityLabel = computed(() => {
  return targetRow.value?.recommendation || 'Manageable';
});

const suitabilityDescription = computed(() => {
  const level = targetRow.value?.level;
  if (level === 'low') return 'This time is typically calm and highly recommended.';
  if (level === 'moderate') return 'This time may feel manageable for many users.';
  return 'This time is typically busy and may be overwhelming.';
});

const calmerAlternativeDisplay = computed(() => {
  const pred = prediction.value;
  if (!pred) return null;
  const level = targetRow.value?.level;
  if (level === 'low') return 'Already quiet';

  const activeDayPlans = pred.dayPlans?.[pred.activeDay];
  const rows = activeDayPlans?.rows || pred.rows;
  
  const targetIndex = rows.findIndex(r => r.time === targetRow.value?.time);
  if (targetIndex >= 0) {
    const laterRows = rows.slice(targetIndex + 1);
    const calmer = laterRows.find(r => r.level === 'low');
    if (calmer) {
      return calmer.time;
    }
  }
  
  return pred.quietestNote; 
});

const calmerAlternativeDescription = computed(() => {
  const pred = prediction.value;
  if (!pred) return null;
  const level = targetRow.value?.level;
  if (level === 'low') return 'You have selected a great time to visit.';
  
  const activeDayPlans = pred.dayPlans?.[pred.activeDay];
  const rows = activeDayPlans?.rows || pred.rows;
  const targetIndex = rows.findIndex(r => r.time === targetRow.value?.time);
  if (targetIndex >= 0) {
    const laterRows = rows.slice(targetIndex + 1);
    const calmer = laterRows.find(r => r.level === 'low');
    if (calmer) {
      return 'Later in the day may feel a little calmer.';
    }
  }
  return 'Consider visiting during the quietest times instead.';
});

</script>
