<template>
  <section data-guide="results" class="figma-panel-soft flex h-full min-h-0 flex-col overflow-hidden">
    <div class="flex items-center justify-between gap-3 border-b border-[#ebe5dd] px-5 py-4 dark:border-white/8">
      <h2 class="text-base font-semibold text-sense-ink dark:text-white">
        Locations
      </h2>
    </div>

    <div class="min-h-0 flex-1 overflow-y-auto">
      <div
        v-if="displayedLocations.length"
        class="divide-y divide-[#ebe5dd] dark:divide-white/8"
      >
        <button
          v-for="location in displayedLocations"
          :key="location.locationId"
          type="button"
          class="group grid min-h-[52px] w-full grid-cols-[auto_minmax(0,1fr)] items-center gap-3 px-5 py-3 text-left transition hover:bg-[#faf8f4] dark:hover:bg-white/[0.045]"
          @click="emit('select', location)"
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
        </button>
      </div>

      <div
        v-else-if="!displayedLocations.length"
        class="m-5 rounded-[22px] border border-dashed border-[#d9dfd8] bg-[#faf8f4] px-4 py-6 text-sm leading-6 text-sense-ink/70 dark:border-white/10 dark:bg-white/5 dark:text-slate-100"
      >
        No locations match your current filters. Try widening your category or sensory needs.
      </div>
    </div>

    <div
      v-if="hasMoreLocations"
      class="border-t border-[#ebe5dd] px-5 py-4 dark:border-white/8"
    >
      <button
        type="button"
        class="flex h-9 w-full items-center justify-center rounded-full border border-sense-teal/12 bg-white/74 px-4 text-sm font-semibold text-sense-teal transition hover:border-sense-teal/28 hover:bg-[#f5faf7] dark:border-white/10 dark:bg-white/5 dark:text-sense-mint dark:hover:border-sense-mint/40 dark:hover:bg-white/10"
        @click="showMoreLocations"
      >
        More+
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import type { Location } from '@/types/domain';

const props = withDefaults(defineProps<{
  locations: Location[];
  limit?: number;
  increment?: number;
}>(), {
  limit: 10,
  increment: 8,
});

const emit = defineEmits<{
  select: [location: Location];
}>();

const visibleCount = ref(props.limit);
const displayedLocations = computed(() => props.locations.slice(0, visibleCount.value));
const hasMoreLocations = computed(() => visibleCount.value < props.locations.length);

watch(
  () => [props.limit, props.locations.map((location) => location.locationId).join('|')],
  () => {
    visibleCount.value = props.limit;
  },
  { immediate: true },
);

function showMoreLocations() {
  visibleCount.value += props.increment;
}

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
