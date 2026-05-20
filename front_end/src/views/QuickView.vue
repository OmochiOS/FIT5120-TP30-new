<template>
  <section class="quick-view-page space-y-4 pb-8 pt-1 page-intro">

    <!-- ── Search bar ──────────────────────────────────────────────────────── -->
    <div class="figma-panel px-4 py-3 md:px-5">
      <SearchField v-model="searchText" />
    </div>

    <!-- Map + Sensory panel -->
    <div
      v-if="displayedLocations.length || isLoading"
      class="figma-panel overflow-hidden p-0"
    >
      <div class="grid md:grid-cols-[minmax(0,1.55fr)_minmax(0,1fr)]">

        <!-- Left: Map — no height constraint; MapCanvas sizes itself -->
        <MapCanvas
          :locations="displayedLocations"
          :all-locations="allLocations"
          :external-selected-id="externalSelectedId"
          @clear-external="externalSelectedId = null"
          @location-clicked="(id) => { if (id) focusedLocationId = id }"
        />

        <!-- Right: sensory panel — h-full fills same grid row height as map -->
        <div class="flex h-full flex-col border-t border-sense-teal/8 md:border-t-0 md:border-l dark:border-white/8">

          <!-- Gauge block — natural height, won't compress -->
          <div class="shrink-0 px-4 py-4">
            <div class="flex items-center justify-between gap-2">
              <p class="text-[11px] font-semibold uppercase tracking-[0.16em] text-sense-ink/56 dark:text-slate-300">
                Sensory Score
              </p>
              <span
                v-if="focusedLocation"
                class="shrink-0 rounded-full px-2.5 py-0.5 text-[10px] font-semibold uppercase tracking-[0.1em]"
                :class="gaugePillClass(focusedLocation.sensoryScore)"
              >
                {{ gaugeDescriptor(focusedLocation.sensoryScore) }}
              </span>
            </div>

            <div class="mt-2 flex justify-center">
              <svg
                v-if="focusedLocation"
                class="w-full max-w-[200px]"
                viewBox="0 0 220 140"
                role="img"
                :aria-label="`Sensory score ${focusedLocation.sensoryScore} out of 4`"
              >
                <path d="M38 112 A72 72 0 0 1 182 112" fill="none" stroke="currentColor"
                  stroke-width="10" stroke-linecap="round" class="text-[#dde5e1] dark:text-white/10" />
                <path :d="gaugeProgressPath(focusedLocation.sensoryScore)" fill="none"
                  :stroke="gaugeAccentColor(focusedLocation.sensoryScore)" stroke-width="10" stroke-linecap="round" />
                <g>
                  <line v-for="angle in gaugeTickAngles" :key="angle"
                    :x1="tickLine(angle, 56).x" :y1="tickLine(angle, 56).y"
                    :x2="tickLine(angle, 68).x" :y2="tickLine(angle, 68).y"
                    stroke="currentColor" stroke-width="2.4" stroke-linecap="round"
                    class="text-sense-ink/42 dark:text-slate-300" />
                  <text v-for="(angle, index) in gaugeTickAngles" :key="'lbl-' + angle"
                    :x="tickLine(angle, 88).x" :y="tickLine(angle, 88).y"
                    fill="currentColor" text-anchor="middle" dominant-baseline="central"
                    font-size="13" font-weight="700" class="text-sense-ink/50 dark:text-slate-300/80">{{ index + 1 }}</text>
                </g>
                <line x1="110" y1="112"
                  :x2="needlePoint(focusedLocation.sensoryScore).x"
                  :y2="needlePoint(focusedLocation.sensoryScore).y"
                  :stroke="gaugeAccentColor(focusedLocation.sensoryScore)"
                  stroke-width="4.5" stroke-linecap="round" />
                <circle cx="110" cy="112" r="8.5" :fill="gaugeAccentColor(focusedLocation.sensoryScore)" />
                <circle cx="110" cy="112" r="3.4" fill="#ffffff" fill-opacity="0.92" />
              </svg>
              <span v-else class="flex py-8 items-center text-[12px] text-sense-ink/40 dark:text-slate-400">Select a location</span>
            </div>

            <p v-if="focusedLocation"
              class="mt-1 text-center text-[12px] font-medium text-sense-ink/55 dark:text-slate-300 truncate">
              {{ focusedLocation.name }}
            </p>
          </div>

          <!-- 4 cards — flex-1 so they fill the remaining height -->
          <div class="flex-1 overflow-hidden border-t border-sense-teal/8 dark:border-white/8">
            <div class="grid h-full grid-cols-2 grid-rows-2">
              <template v-if="focusedLocation">
                <div
                  v-for="(card, index) in profileCards"
                  :key="card.label"
                  class="flex flex-col justify-center gap-3 px-5 py-4 lg:px-6 lg:py-5"
                  :class="{
                    'border-r border-sense-teal/8 dark:border-white/8': index % 2 === 0,
                    'border-t border-sense-teal/8 dark:border-white/8': index >= 2,
                  }"
                >
                  <div class="flex items-center gap-2.5">
                    <div class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-sense-mint/34 text-sense-teal dark:bg-white/10 dark:text-sense-mint">
                      <component :is="card.icon" class="h-4 w-4" />
                    </div>
                    <span class="text-[14px] lg:text-[15px] font-semibold text-sense-ink dark:text-white">{{ card.label }}</span>
                  </div>
                  <span class="self-start rounded bg-emerald-100 px-2.5 py-1 text-[11px] lg:text-[12px] font-bold uppercase tracking-[0.07em] text-emerald-800 dark:bg-emerald-500/20 dark:text-emerald-100">
                    {{ card.value }}
                  </span>
                </div>
              </template>
              <template v-else>
                <div
                  v-for="n in 4" :key="n"
                  class="flex flex-col justify-center gap-3 px-5 py-4 lg:px-6 lg:py-5"
                  :class="{
                    'border-r border-sense-teal/8 dark:border-white/8': (n - 1) % 2 === 0,
                    'border-t border-sense-teal/8 dark:border-white/8': n >= 3,
                  }"
                >
                  <div class="flex items-center gap-2.5">
                    <div class="h-7 w-7 shrink-0 animate-pulse rounded-full bg-sense-teal/8 dark:bg-white/8" />
                    <div class="h-4 w-20 animate-pulse rounded bg-sense-teal/8 dark:bg-white/8" />
                  </div>
                  <div class="h-6 w-16 animate-pulse rounded bg-sense-teal/5 dark:bg-white/5" />
                </div>
              </template>
            </div>
          </div>

        </div>
      </div>
    </div>
    <!-- Empty search result state -->
    <div
      v-else-if="searchText"
      class="figma-panel-soft rounded-[22px] px-6 py-8 text-center text-[14px] text-sense-ink/56 dark:text-slate-300"
    >
      No locations match "<strong class="font-semibold text-sense-ink dark:text-white">{{ searchText }}</strong>".
    </div>

    <!-- Initial empty state -->
    <div
      v-else
      class="figma-panel-soft rounded-[22px] px-6 py-10 text-center space-y-2"
    >
      <p class="text-[18px] font-semibold text-sense-ink dark:text-white">Search to explore</p>
      <p class="text-[13px] text-sense-ink/55 dark:text-slate-300">
        Type a location name above to see its sensory score and practical tips.
      </p>
    </div>

    <div
      v-if="focusedLocation"
      class="flex flex-wrap items-baseline gap-x-3 gap-y-1 px-1"
    >
      <p class="text-[17px] font-semibold text-sense-ink dark:text-white">
        Best Times to Visit
      </p>
      <p
        v-if="focusedLocation.bestTimesSummary"
        class="text-[14px] text-sense-ink/70 dark:text-slate-200"
      >
        {{ focusedLocation.bestTimesSummary }}
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

  </section>
</template>

<script setup lang="ts">
import { computed, h, ref, watch } from 'vue';
import type { FunctionalComponent } from 'vue';

import MapCanvas from '@/components/map/MapCanvas.vue';
import SearchField from '@/components/map/SearchField.vue';
import type { GoodToKnowItem, Location } from '@/types/domain';
import { useLocationsStore } from '@/stores/locations';
import { getGoodToKnow } from '@/utils/location-details';

// ─── Store ────────────────────────────────────────────────────────────────────
const locationsStore = useLocationsStore();

const externalSelectedId = ref<string | null>(null);
const focusedLocationId = ref<string | null>(null);
const isLoading = computed(() => !locationsStore.locationsHydrated);

const allLocations = computed(() => locationsStore.locations);
const searchText = computed({
  get: () => locationsStore.searchText,
  set: (v: string) => locationsStore.setSearchText(v),
});
const displayedLocations = computed(() =>
  [...locationsStore.filteredLocations].sort((a, b) => a.sensoryScore - b.sensoryScore),
);

// Auto-select first location when list changes
watch(
  displayedLocations,
  (next) => {
    if (next.length && (!focusedLocationId.value || !next.some((l) => l.locationId === focusedLocationId.value))) {
      focusedLocationId.value = next[0]!.locationId;
      externalSelectedId.value = focusedLocationId.value;
    }
    if (!next.length) {
      focusedLocationId.value = null;
    }
  },
  { immediate: true },
);

// When map marker clicked → update focused location
watch(externalSelectedId, (id) => {
  if (id) focusedLocationId.value = id;
});

const focusedLocation = computed(() =>
  focusedLocationId.value ? (locationsStore.getLocationById(focusedLocationId.value) ?? null) : null,
);

// Hydrate detail for the focused location
watch(
  () => focusedLocation.value?.locationId,
  (id) => {
    if (id) {
      void locationsStore.hydrateLocationDetail(id);
    }
  },
  { immediate: true },
);

// ─── Good to Know ─────────────────────────────────────────────────────────────
const goodToKnowItems = computed<GoodToKnowItem[]>(() =>
  focusedLocation.value ? getGoodToKnow(focusedLocation.value) : [],
);

// ─── Sensory profile mini-cards ───────────────────────────────────────────────
const SoundIcon: FunctionalComponent = (p) =>
  h('svg', { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...p }, [
    h('path', { d: 'M4 12h3l4 3V5L7 8H4Z', 'stroke-linejoin': 'round' }),
    h('path', { d: 'M14 7.5a4 4 0 0 1 0 5', 'stroke-linecap': 'round' }),
    h('path', { d: 'M15.8 5.4a6.8 6.8 0 0 1 0 9.2', 'stroke-linecap': 'round' }),
  ]);

const SunIcon: FunctionalComponent = (p) =>
  h('svg', { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...p }, [
    h('circle', { cx: '10', cy: '10', r: '3' }),
    h('path', { d: 'M10 2.5v2.1', 'stroke-linecap': 'round' }),
    h('path', { d: 'M10 15.4v2.1', 'stroke-linecap': 'round' }),
    h('path', { d: 'M2.5 10h2.1', 'stroke-linecap': 'round' }),
    h('path', { d: 'M15.4 10h2.1', 'stroke-linecap': 'round' }),
    h('path', { d: 'm4.8 4.8 1.5 1.5', 'stroke-linecap': 'round' }),
    h('path', { d: 'm13.7 13.7 1.5 1.5', 'stroke-linecap': 'round' }),
    h('path', { d: 'm13.7 6.3 1.5-1.5', 'stroke-linecap': 'round' }),
    h('path', { d: 'm4.8 15.2 1.5-1.5', 'stroke-linecap': 'round' }),
  ]);

const UnpredictabilityIcon: FunctionalComponent = (p) =>
  h('svg', { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...p }, [
    h('path', { d: 'M3 13.8 7.2 9.6l2.6 2.6 5.2-5.2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }),
    h('path', { d: 'M12 7h3v3', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }),
  ]);

const QuietSpacesIcon: FunctionalComponent = (p) =>
  h('svg', { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...p }, [
    h('path', { d: 'M10 17s5-4.4 5-8.2A5 5 0 0 0 5 8.8C5 12.6 10 17 10 17Z', 'stroke-linejoin': 'round' }),
    h('circle', { cx: '10', cy: '8.5', r: '1.8' }),
  ]);

function noiseLabel(v: Location['sensoryProfile']['noiseLevel']): string {
  return v === 'low' ? 'Low' : v === 'moderate' ? 'Moderate' : 'Busy';
}
function lightingLabel(v: Location['sensoryProfile']['lightingType']): string {
  if (v === 'natural') return 'Natural';
  if (v === 'controlled-indoor') return 'Steady';
  if (v === 'mixed') return 'Mixed';
  return 'Bright';
}
function unpredictabilityLabel(v: Location['sensoryProfile']['crowdPredictability']): string {
  return v === 'stable' ? 'Low' : v === 'variable' ? 'Moderate' : 'High';
}

const profileCards = computed(() => {
  if (!focusedLocation.value) return [];
  const p = focusedLocation.value.sensoryProfile;
  return [
    {
      icon: SoundIcon,
      label: 'Noise',
      value: p.noiseDb != null ? `${p.noiseDb} dB` : noiseLabel(p.noiseLevel),
    },
    {
      icon: SunIcon,
      label: 'Lighting',
      value: p.lightingLevel ?? lightingLabel(p.lightingType),
    },
    {
      icon: UnpredictabilityIcon,
      label: 'Unpredictability',
      value: unpredictabilityLabel(p.crowdPredictability),
    },
    {
      icon: QuietSpacesIcon,
      label: 'Quiet Spaces',
      value: p.hasQuietArea ? 'Available' : 'Limited',
    },
  ];
});

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

// ─── Gauge helpers (from LocationSummaryPopup) ────────────────────────────────
type GaugePoint = { x: number; y: number };

const gaugeScoreAngles: Record<Location['sensoryScore'], number> = { 1: 180, 2: 240, 3: 300, 4: 360 };
const gaugeTickAngles = Object.values(gaugeScoreAngles);

function pointOnGauge(angle: number, radius: number): GaugePoint {
  const rad = (angle * Math.PI) / 180;
  return { x: 110 + radius * Math.cos(rad), y: 112 + radius * Math.sin(rad) };
}

function needlePoint(score: Location['sensoryScore']): GaugePoint {
  return pointOnGauge(gaugeScoreAngles[score], 54);
}

function tickLine(angle: number, radius: number): GaugePoint {
  return pointOnGauge(angle, radius);
}

function gaugeProgressPath(score: Location['sensoryScore']): string {
  const start = pointOnGauge(180, 72);
  const end = pointOnGauge(gaugeScoreAngles[score], 72);
  const largeArc = gaugeScoreAngles[score] - 180 > 180 ? 1 : 0;
  if (score === 1) return `M ${start.x} ${start.y} L ${start.x} ${start.y}`;
  return `M ${start.x} ${start.y} A 72 72 0 ${largeArc} 1 ${end.x} ${end.y}`;
}

function gaugeAccentColor(score: Location['sensoryScore']): string {
  if (score <= 2) return score === 1 ? '#6aa07c' : '#5f8b84';
  if (score === 3) return '#d39b57';
  return '#cb8b79';
}

function gaugeDescriptor(score: Location['sensoryScore']): string {
  if (score === 1) return 'Very calm';
  if (score === 2) return 'Calmer';
  if (score === 3) return 'Manageable';
  return 'Higher';
}

function gaugePillClass(score: Location['sensoryScore']): string {
  if (score <= 2) return 'bg-[#edf8f0] text-[#1f6b46] dark:bg-[#133127] dark:text-[#98f5bf]';
  if (score === 3) return 'bg-[#fbf2e4] text-[#9f5a0c] dark:bg-[#352712] dark:text-[#ffd56f]';
  return 'bg-[#fbecee] text-[#ae425b] dark:bg-[#3a1822] dark:text-[#ffabc0]';
}
</script>
