<template>
  <div class="space-y-4">
    <!-- ── Best Times to Visit (inline text) ───────────────────────────────── -->
    <div
      v-if="focusedLocation"
      class="flex flex-wrap items-baseline gap-x-3 gap-y-1 px-1"
    >
      <p class="text-[17px] font-semibold text-sense-ink dark:text-white">
        Best Times to Visit
      </p>
      <p
        v-if="quietestNote"
        class="text-[14px] text-sense-ink/70 dark:text-slate-200"
      >
        <span class="font-medium text-sense-ink dark:text-white">Quietest:</span>
        {{ quietestNote }}
      </p>
      <p
        v-else
        class="text-[13px] text-sense-ink/45 dark:text-slate-400 italic"
      >
        Loading…
      </p>
    </div>

    <!-- ── Good to Know ────────────────────────────────────────────────────── -->
    <div v-if="focusedLocation && goodToKnowItems.length" class="space-y-3">
      <p class="px-1 text-[17px] font-semibold text-sense-ink dark:text-white">
        Good to Know
      </p>

      <div class="overflow-hidden rounded-[16px] border border-sense-teal/10 bg-white shadow-[0_12px_24px_-20px_rgba(16,74,85,0.28)] dark:border-white/8 dark:bg-[#16252f]/90">
        <article
          v-for="item in goodToKnowItems"
          :key="item.kind"
          class="flex items-center gap-3 border-b border-sense-teal/8 px-5 py-4 last:border-b-0 dark:border-white/8"
        >
          <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-sense-mint/30 text-sense-teal dark:bg-white/10 dark:text-sense-mint">
            <component :is="iconFor(item.kind)" class="h-4 w-4" />
          </div>
          <p class="text-[14px] leading-6 text-sense-ink/80 dark:text-slate-100">
            {{ item.value }}
          </p>
        </article>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h } from 'vue';
import type { FunctionalComponent } from 'vue';

import { useLocationsStore } from '@/stores/locations';
import type { GoodToKnowItem } from '@/types/domain';
import { getGoodToKnow } from '@/utils/location-details';

const props = defineProps<{
  locationId: string | null;
}>();

const locationsStore = useLocationsStore();

const focusedLocation = computed(() =>
  props.locationId ? (locationsStore.getLocationById(props.locationId) ?? null) : null,
);

const focusedPrediction = computed(() =>
  focusedLocation.value
    ? (locationsStore.getPredictionSnapshot(focusedLocation.value.locationId)?.prediction ?? null)
    : null,
);

const quietestNote = computed(() => {
  const note = focusedPrediction.value?.quietestNote;
  if (!note) return '';
  return note.replace(/^quietest:\s*/i, '').trim();
});

const goodToKnowItems = computed<GoodToKnowItem[]>(() =>
  focusedLocation.value ? getGoodToKnow(focusedLocation.value) : [],
);

// ─── Good to Know icons ───────────────────────────────────────────────────────
const QuietAreaIcon: FunctionalComponent = (p) =>
  h('svg', { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...p }, [
    h('path', { d: 'M10 17s5-4.4 5-8.2A5 5 0 0 0 5 8.8C5 12.6 10 17 10 17Z', 'stroke-linejoin': 'round' }),
    h('circle', { cx: '10', cy: '8.5', r: '1.8' }),
  ]);

const TransportIcon: FunctionalComponent = (p) =>
  h('svg', { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...p }, [
    h('rect', { x: '4', y: '5.5', width: '12', height: '8', rx: '2', 'stroke-linejoin': 'round' }),
    h('path', { d: 'M7 13.5v2', 'stroke-linecap': 'round' }),
    h('path', { d: 'M13 13.5v2', 'stroke-linecap': 'round' }),
    h('path', { d: 'M4 9.5h12', 'stroke-linecap': 'round' }),
    h('circle', { cx: '7', cy: '15.5', r: '0.8', fill: 'currentColor', stroke: 'none' }),
    h('circle', { cx: '13', cy: '15.5', r: '0.8', fill: 'currentColor', stroke: 'none' }),
  ]);

const ToiletsIcon: FunctionalComponent = (p) =>
  h('svg', { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...p }, [
    h('circle', { cx: '10', cy: '4.3', r: '1.5' }),
    h('path', { d: 'M10 6.2v4.2', 'stroke-linecap': 'round' }),
    h('path', { d: 'M7.2 8.2h5.6', 'stroke-linecap': 'round' }),
    h('path', { d: 'm8.1 15.8 1-5.4', 'stroke-linecap': 'round' }),
    h('path', { d: 'm11.9 15.8-1-5.4', 'stroke-linecap': 'round' }),
    h('path', { d: 'M6.2 11.2a3.2 3.2 0 1 0 6.4 0', 'stroke-linecap': 'round' }),
  ]);

function iconFor(kind: GoodToKnowItem['kind']): FunctionalComponent {
  if (kind === 'quiet-area') return QuietAreaIcon;
  if (kind === 'transport') return TransportIcon;
  return ToiletsIcon;
}
</script>
