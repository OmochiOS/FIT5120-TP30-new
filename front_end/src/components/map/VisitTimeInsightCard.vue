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

    <!-- Loading state -->
    <div v-if="isLoading" class="flex items-center justify-center py-6 text-[12px] text-sense-ink/40 dark:text-slate-500">
      Loading insight…
    </div>

    <div v-else class="space-y-2">
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

      <!-- Calmer alternative (only when data available) -->
      <div
        v-if="insight?.dataAvailable"
        class="rounded-[12px] border border-[#c4e3d3]/50 bg-[#edf8f0]/80 px-3 py-2.5 dark:border-[#224b3c] dark:bg-[#133127]/50"
      >
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
import { computed, ref, watch } from 'vue';

import type { Location } from '@/types/domain';
import { usePreferencesStore } from '@/stores/preferences';
import { fetchCrowdInsight, timeSlotToHour, type CrowdInsightResult } from '@/utils/java-backend';

const props = defineProps<{
  location: Location;
}>();

const preferencesStore = usePreferencesStore();

const insight = ref<CrowdInsightResult | null>(null);
const isLoading = ref(false);

// ─── Determine the hour to query ─────────────────────────────────────────────

const resolvedHour = computed<number>(() => {
  const slot = preferencesStore.predictionTime;
  if (slot && slot !== 'any') {
    const h = timeSlotToHour(slot);
    if (h !== null) return h;
  }
  // Fall back to current hour clamped to 7–21
  const now = new Date().getHours();
  return Math.min(21, Math.max(7, now));
});

// ─── Fetch whenever location or hour changes ──────────────────────────────────

async function loadInsight(locationId: string, visitHour: number) {
  isLoading.value = true;
  insight.value = null;
  try {
    insight.value = await fetchCrowdInsight(locationId, visitHour);
  } catch {
    insight.value = null;
  } finally {
    isLoading.value = false;
  }
}

watch(
  [() => props.location?.locationId, resolvedHour],
  ([id, hour]) => {
    if (id) loadInsight(id, hour);
  },
  { immediate: true },
);

// ─── Display helpers ──────────────────────────────────────────────────────────

const intendedVisitTimeDisplay = computed(() => {
  const slot = preferencesStore.predictionTime;
  if (slot && slot !== 'any') return slot;
  return insight.value?.visitTimeLabel ?? 'Current time';
});

const crowdLevel = computed(() => {
  // Map Predicted crowd level directly to backend's predictabilityLevel
  const predictability = props.location.sensoryProfile.crowdPredictability;
  if (predictability === 'stable') return 'LOW'; // Calm
  if (predictability === 'event-sensitive') return 'HIGH'; // Busy
  return 'MODERATE'; // Moderate
});

const predictedLevelDisplay = computed(() => {
  if (crowdLevel.value === 'LOW') return 'Calm';
  if (crowdLevel.value === 'MODERATE') return 'Moderate';
  if (crowdLevel.value === 'HIGH') return 'Busy';
  return 'Unknown';
});

const crowdLevelClass = computed(() => {
  if (crowdLevel.value === 'LOW') return 'bg-[#edf8f0] text-[#1f6b46] dark:bg-[#133127] dark:text-[#98f5bf]';
  if (crowdLevel.value === 'MODERATE') return 'bg-[#fbf2e4] text-[#9f5a0c] dark:bg-[#352712] dark:text-[#ffd56f]';
  return 'bg-rose-100 text-rose-800 dark:bg-rose-900/40 dark:text-rose-200';
});

const crowdDotClass = computed(() => {
  if (crowdLevel.value === 'LOW') return 'bg-[#2c7753]';
  if (crowdLevel.value === 'MODERATE') return 'bg-[#c27c24]';
  return 'bg-rose-600';
});

const suitabilityClass = computed(() => {
  if (crowdLevel.value === 'LOW' || crowdLevel.value === 'MODERATE')
    return 'bg-[#edf8f0] text-[#1f6b46] dark:bg-[#133127] dark:text-[#98f5bf]';
  return 'bg-rose-100 text-rose-800 dark:bg-rose-900/40 dark:text-rose-200';
});

const suitabilityLabel = computed(() => insight.value?.suitabilityLabel ?? 'Manageable');

const suitabilityDescription = computed(() => {
  if (crowdLevel.value === 'LOW') return 'This time is typically calm and highly recommended.';
  if (crowdLevel.value === 'MODERATE') return 'This time may feel manageable for many users.';
  if (crowdLevel.value === 'HIGH') return 'This time is typically busy and may be overwhelming.';
  return 'Crowd data is unavailable for this time slot.';
});

const calmerAlternativeDisplay = computed(() => {
  if (!insight.value?.dataAvailable) return null;
  if (crowdLevel.value === 'LOW') return 'Already quiet';
  return insight.value.alternativeSuggestion ? 'Calmer time available' : 'No calmer time found';
});

const calmerAlternativeDescription = computed(() => {
  if (!insight.value?.dataAvailable) return null;
  if (crowdLevel.value === 'LOW') return 'You have selected a great time to visit.';
  return insight.value?.alternativeSuggestion ?? 'Consider visiting during quieter periods instead.';
});
</script>
