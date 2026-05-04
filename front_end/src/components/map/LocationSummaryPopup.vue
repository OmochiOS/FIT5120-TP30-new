<template>
  <Transition name="map-summary">
    <aside
      v-if="location"
      data-testid="summary-shell"
      class="summary-shell pointer-events-auto fixed bottom-0 left-3 w-[calc(100vw-1.5rem)] z-[9990] md:bottom-auto md:left-4 md:right-auto md:top-4 md:w-[364px]"
      aria-label="Selected location summary"
      :style="panelStyle"
    >
      <div
        data-testid="summary-panel"
        class="map-summary-panel figma-panel-soft overflow-hidden border border-white/55 bg-white/96 p-[15px] backdrop-blur-xl dark:border-[#6f8792] dark:bg-[#0f2029] dark:shadow-[0_22px_44px_rgba(0,0,0,0.42)]"
        :class="isDragging ? 'cursor-grabbing select-none' : 'cursor-grab'"
        @mousedown="startDrag"
        @touchstart="startDrag"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="min-w-0 space-y-1.5">
            <p class="text-[10px] font-semibold uppercase tracking-[0.18em] text-sense-teal/68 dark:text-slate-200">
              Location summary
            </p>
            <h3 class="text-[21px] font-semibold leading-[1.15] text-sense-ink dark:text-slate-50">
              {{ location.name }}
            </h3>
          </div>

          <button
            type="button"
            data-testid="summary-close"
            class="inline-flex h-8 w-8 shrink-0 cursor-pointer items-center justify-center rounded-full border border-sense-teal/12 bg-white/80 text-sense-ink/60 transition hover:border-sense-teal/24 hover:text-sense-ink dark:border-[#6f8792] dark:bg-[#1b323e] dark:text-slate-50 dark:hover:border-slate-200 dark:hover:bg-[#263f4c] dark:hover:text-white"
            aria-label="Close summary"
            @click="$emit('close')"
          >
            <svg viewBox="0 0 16 16" fill="none" class="h-3.5 w-3.5" aria-hidden="true">
              <path d="M4 4l8 8M12 4 4 12" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
            </svg>
          </button>
        </div>

        <div class="mt-2.5 flex flex-col gap-2 sm:flex-row sm:items-center">
          <span
            data-testid="summary-display-type"
            class="w-fit rounded-full border border-sense-teal/12 bg-white px-3 py-1 text-[10px] font-semibold text-sense-teal dark:border-[#7e98a4] dark:bg-[#203844] dark:text-slate-50"
          >
            {{ location.displayType }}
          </span>

          <div
            data-testid="summary-crowd-bar"
            class="flex min-w-0 flex-1 items-center justify-between gap-2 rounded-[13px] border px-3 py-2"
            :class="crowdBarClass(location)"
          >
            <span class="text-[10px] font-semibold uppercase tracking-[0.14em]">
              Crowd level
            </span>
            <span class="shrink-0 text-right text-[13px] font-semibold">
              {{ crowdLabel(location) }}
            </span>
          </div>
        </div>

        <section
          class="mt-3.5 rounded-[22px] border border-sense-teal/10 bg-white/78 px-[14px] py-[14px] dark:border-[#49626e] dark:bg-[#142a35]"
        >
          <div class="flex items-center justify-between gap-3">
            <p class="text-[12px] font-semibold uppercase tracking-[0.14em] text-sense-ink/58 dark:text-slate-200">
              Sensory score
            </p>
            <span
              class="shrink-0 rounded-full px-2.5 py-1 text-[10px] font-semibold uppercase tracking-[0.12em]"
              :class="gaugePillClass(location.sensoryScore)"
            >
              {{ gaugeDescriptor(location.sensoryScore) }}
            </span>
          </div>

          <div class="mt-3 flex justify-center">
            <svg
              data-testid="summary-gauge"
              class="w-full max-w-[234px]"
              viewBox="0 0 220 140"
              role="img"
              :aria-label="`Sensory score ${location.sensoryScore} out of 4`"
            >
              <path
                d="M38 112 A72 72 0 0 1 182 112"
                fill="none"
                stroke="currentColor"
                stroke-width="10"
                stroke-linecap="round"
                class="text-[#dde5e1] dark:text-white/10"
              />
              <path
                :d="gaugeProgressPath(location.sensoryScore)"
                fill="none"
                :stroke="gaugeAccentColor(location.sensoryScore)"
                stroke-width="10"
                stroke-linecap="round"
              />

              <g>
                <line
                  v-for="angle in gaugeTickAngles"
                  :key="angle"
                  :x1="tickLine(angle, 56).x"
                  :y1="tickLine(angle, 56).y"
                  :x2="tickLine(angle, 68).x"
                  :y2="tickLine(angle, 68).y"
                  stroke="currentColor"
                  stroke-width="2.4"
                  stroke-linecap="round"
                  class="text-sense-ink/42 dark:text-slate-300"
                />
                <text
                  v-for="(angle, index) in gaugeTickAngles"
                  :key="'label-' + angle"
                  :x="tickLine(angle, 88).x"
                  :y="tickLine(angle, 88).y"
                  fill="currentColor"
                  text-anchor="middle"
                  dominant-baseline="central"
                  class="text-[13px] font-bold text-sense-ink/50 dark:text-slate-300/80"
                >
                  {{ index + 1 }}
                </text>
              </g>

              <line
                x1="110"
                y1="112"
                :x2="needlePoint(location.sensoryScore).x"
                :y2="needlePoint(location.sensoryScore).y"
                :stroke="gaugeAccentColor(location.sensoryScore)"
                stroke-width="4.5"
                stroke-linecap="round"
              />
              <circle cx="110" cy="112" r="8.5" :fill="gaugeAccentColor(location.sensoryScore)" />
              <circle cx="110" cy="112" r="3.4" fill="#ffffff" fill-opacity="0.92" />
            </svg>
          </div>
        </section>

        <section
          data-testid="summary-description"
          class="mt-3.5 rounded-[20px] border border-sense-teal/10 bg-white/78 px-[14px] py-[14px] dark:border-[#49626e] dark:bg-[#142a35]"
        >
          <p class="text-[12px] font-semibold uppercase tracking-[0.14em] text-sense-ink/58 dark:text-slate-200">
            Description
          </p>
          <p class="mt-2 text-[13px] leading-6 text-sense-ink/66 dark:text-slate-50">
            {{ location.description || 'Description coming soon. Backend detail for this location will appear here once available.' }}
          </p>
        </section>

        <button
          type="button"
          data-testid="summary-view-profile"
          data-guide="view-profile"
          class="mt-3.5 inline-flex w-full cursor-pointer items-center justify-center rounded-full bg-[#5f8b84] px-4 py-[11px] text-sm font-semibold text-white transition hover:bg-[#4f7771] dark:bg-sense-mint dark:text-sense-night dark:hover:bg-white"
          @click="$emit('view-profile')"
        >
          View full profile
        </button>
      </div>
    </aside>
  </Transition>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';

import type { CrowdLevel, Location } from '@/types/domain';
import { getCrowdLabel } from '@/utils/crowd';

const props = defineProps<{
  location: Location | null;
}>();

defineEmits<{
  close: [];
  'view-profile': [];
}>();

type GaugePoint = {
  x: number;
  y: number;
};

type DragPoint = {
  x: number;
  y: number;
};

const gaugeScoreAngles: Record<Location['sensoryScore'], number> = {
  1: 180,
  2: 240,
  3: 300,
  4: 360,
};

const gaugeTickAngles = Object.values(gaugeScoreAngles);
const dragOffset = ref<DragPoint>({ x: 0, y: 0 });
const isDragging = ref(false);

let dragPointerOrigin: DragPoint | null = null;
let dragOffsetOrigin: DragPoint = { x: 0, y: 0 };

const panelStyle = computed(() => ({
  transform: `translate(${dragOffset.value.x}px, ${dragOffset.value.y}px)`,
}));

function isInteractiveTarget(target: EventTarget | null): boolean {
  return target instanceof Element && Boolean(target.closest('button, a'));
}

function readDragPoint(event: MouseEvent | TouchEvent): DragPoint | null {
  if ('touches' in event) {
    const touch = event.touches[0] ?? event.changedTouches[0];

    if (!touch) {
      return null;
    }

    return {
      x: touch.clientX,
      y: touch.clientY,
    };
  }

  return {
    x: event.clientX,
    y: event.clientY,
  };
}

function removeDragListeners() {
  if (typeof window === 'undefined') {
    return;
  }

  window.removeEventListener('mousemove', handleDragMove);
  window.removeEventListener('mouseup', stopDrag);
  window.removeEventListener('touchmove', handleDragMove);
  window.removeEventListener('touchend', stopDrag);
  window.removeEventListener('touchcancel', stopDrag);
}

function startDrag(event: MouseEvent | TouchEvent) {
  if (!props.location || isInteractiveTarget(event.target)) {
    return;
  }

  if ('button' in event && event.button !== 0) {
    return;
  }

  if (typeof window !== 'undefined' && window.innerWidth < 768) {
    return;
  }

  const point = readDragPoint(event);

  if (!point || typeof window === 'undefined') {
    return;
  }

  isDragging.value = true;
  dragPointerOrigin = point;
  dragOffsetOrigin = { ...dragOffset.value };

  window.addEventListener('mousemove', handleDragMove);
  window.addEventListener('mouseup', stopDrag);
  window.addEventListener('touchmove', handleDragMove, { passive: false });
  window.addEventListener('touchend', stopDrag);
  window.addEventListener('touchcancel', stopDrag);
}

function handleDragMove(event: MouseEvent | TouchEvent) {
  if (!isDragging.value || !dragPointerOrigin) {
    return;
  }

  const point = readDragPoint(event);

  if (!point) {
    return;
  }

  if ('cancelable' in event && event.cancelable) {
    event.preventDefault();
  }

  dragOffset.value = {
    x: dragOffsetOrigin.x + point.x - dragPointerOrigin.x,
    y: dragOffsetOrigin.y + point.y - dragPointerOrigin.y,
  };
}

function stopDrag() {
  if (!isDragging.value) {
    return;
  }

  isDragging.value = false;
  dragPointerOrigin = null;
  removeDragListeners();
}

function crowdLevel(location: Location): CrowdLevel {
  return location.summaryCrowdLevel ?? location.crowdSnapshot.crowdLevel;
}

function crowdLabel(location: Location): string {
  return location.summaryStatusLabel?.trim() || location.crowdSnapshot.label || getCrowdLabel(crowdLevel(location));
}

function crowdBarClass(location: Location): string {
  const level = crowdLevel(location);

  if (level === 'high') {
    return 'border-rose-200 bg-rose-50 text-rose-700 dark:border-[#f0a0a0] dark:bg-[#5a1f2b] dark:text-rose-50';
  }

  if (level === 'moderate') {
    return 'border-amber-200 bg-amber-50 text-amber-700 dark:border-[#f1c96b] dark:bg-[#5a4215] dark:text-amber-50';
  }

  return 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-[#87d8a8] dark:bg-[#174633] dark:text-emerald-50';
}

function gaugeAccentColor(score: Location['sensoryScore']): string {
  if (score <= 2) {
    return score === 1 ? '#6aa07c' : '#5f8b84';
  }

  if (score === 3) {
    return '#d39b57';
  }

  return '#cb8b79';
}

function gaugeDescriptor(score: Location['sensoryScore']): string {
  if (score === 1) {
    return 'Very calm';
  }

  if (score === 2) {
    return 'Calmer';
  }

  if (score === 3) {
    return 'Manageable';
  }

  return 'Higher';
}

function gaugePillClass(score: Location['sensoryScore']): string {
  if (score <= 2) {
    return 'bg-[#edf8f0] text-[#1f6b46] dark:bg-[#133127] dark:text-[#98f5bf]';
  }

  if (score === 3) {
    return 'bg-[#fbf2e4] text-[#9f5a0c] dark:bg-[#352712] dark:text-[#ffd56f]';
  }

  return 'bg-[#fbecee] text-[#ae425b] dark:bg-[#3a1822] dark:text-[#ffabc0]';
}

function scoreAngle(score: Location['sensoryScore']): number {
  return gaugeScoreAngles[score];
}

function pointOnGauge(angle: number, radius: number): GaugePoint {
  const radians = (angle * Math.PI) / 180;

  return {
    x: 110 + radius * Math.cos(radians),
    y: 112 + radius * Math.sin(radians),
  };
}

function needlePoint(score: Location['sensoryScore']): GaugePoint {
  return pointOnGauge(scoreAngle(score), 54);
}

function tickLine(angle: number, radius: number): GaugePoint {
  return pointOnGauge(angle, radius);
}

function gaugeProgressPath(score: Location['sensoryScore']): string {
  const start = pointOnGauge(180, 72);
  const end = pointOnGauge(scoreAngle(score), 72);
  const largeArcFlag = scoreAngle(score) - 180 > 180 ? 1 : 0;

  if (score === 1) {
    return `M ${start.x} ${start.y} L ${start.x} ${start.y}`;
  }

  return `M ${start.x} ${start.y} A 72 72 0 ${largeArcFlag} 1 ${end.x} ${end.y}`;
}

watch(
  () => props.location?.locationId ?? null,
  () => {
    stopDrag();
    dragOffset.value = { x: 0, y: 0 };
  },
);

onBeforeUnmount(() => {
  removeDragListeners();
});
</script>

<style scoped>
.map-summary-panel {
  touch-action: none;
}

.map-summary-enter-active,
.map-summary-leave-active {
  transition:
    opacity 180ms ease,
    transform 180ms ease;
}

.map-summary-enter-from,
.map-summary-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}

/* ── Mobile: bottom sheet ──────────────────────────────── */
@media (max-width: 767px) {
  .summary-shell {
    transform: none !important; /* disable drag on mobile */
    border-radius: 20px 20px 20px 20px;
    padding-bottom: env(safe-area-inset-bottom, 0px);
    max-height: 82vh;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
  }

  .summary-shell .map-summary-panel {
    border-radius: 20px;
    touch-action: pan-y;
    cursor: default;
  }

  /* Slide up from bottom on mobile */
  .map-summary-enter-from,
  .map-summary-leave-to {
    transform: translateY(120%) !important;
    opacity: 0;
  }
}
</style>
