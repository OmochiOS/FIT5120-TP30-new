<template>
  <Teleport to="body">
    <Transition name="guide-fade">
      <div v-if="active" class="guide-root" aria-modal="true" role="dialog" aria-label="Map page guide">
        <!-- Spotlight backdrop: box-shadow creates the darkened surround -->
        <div
          class="guide-spotlight"
          :style="spotlightStyle"
        />

        <!-- Tooltip bubble -->
        <Transition name="guide-bubble" mode="out-in">
          <div
            :key="currentStep"
            class="guide-bubble"
            :style="bubbleStyle"
          >
            <!-- Step counter -->
            <p class="guide-bubble__counter">Step {{ currentStep + 1 }} of {{ steps.length }}</p>

            <!-- Icon -->
            <div class="guide-bubble__icon" aria-hidden="true">
              <component :is="steps[currentStep].icon" />
            </div>

            <!-- Text -->
            <p class="guide-bubble__title">{{ steps[currentStep].title }}</p>
            <p class="guide-bubble__body">{{ steps[currentStep].body }}</p>

            <!-- Actions -->
            <div class="guide-bubble__actions">
              <button type="button" class="guide-bubble__skip" @click="close">Skip</button>
              <div class="guide-bubble__nav">
                <button
                  v-if="currentStep > 0"
                  type="button"
                  class="guide-bubble__prev"
                  @click="prev"
                >← Back</button>
                <button
                  type="button"
                  class="guide-bubble__next"
                  @click="next"
                >
                  {{ currentStep === steps.length - 1 ? 'Done' : 'Next →' }}
                </button>
              </div>
            </div>

            <!-- Arrow pointer -->
            <div class="guide-bubble__arrow" :class="`guide-bubble__arrow--${steps[currentStep].arrowDir}`" />
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { FunctionalComponent, computed, h, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import { useLocationsStore } from '@/stores/locations';

const props = defineProps<{
  /** Passed from MapView so the guide can auto-select a location for step 4 */
  firstLocationId?: string | null;
}>();

const emit = defineEmits<{
  /** Ask MapView to select this location (opens the summary popup) */
  'select-location': [id: string];
}>();

const route = useRoute();
const locationsStore = useLocationsStore();

// ─── Step definitions ────────────────────────────────────────────────────────

type ArrowDir = 'top' | 'bottom' | 'left' | 'right' | 'none';

type GuideStep = {
  /** CSS data-guide attribute value of the target element */
  target: string;
  title: string;
  body: string;
  arrowDir: ArrowDir;
  /** Which side of the target the bubble appears on */
  placement: 'below' | 'above' | 'left' | 'right';
  icon: FunctionalComponent;
};

const SearchIcon: FunctionalComponent = () =>
  h('svg', { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.75', class: 'h-5 w-5' }, [
    h('circle', { cx: '8.8', cy: '8.8', r: '5.2', 'stroke-linecap': 'round' }),
    h('path', { d: 'M12.6 12.6 16 16', 'stroke-linecap': 'round' }),
  ]);

const FilterIcon: FunctionalComponent = () =>
  h('svg', { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.75', class: 'h-5 w-5' }, [
    h('path', { d: 'M3 5h14M6 10h8M9 15h2', 'stroke-linecap': 'round' }),
  ]);

const ListIcon: FunctionalComponent = () =>
  h('svg', { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.75', class: 'h-5 w-5' }, [
    h('rect', { x: '3', y: '4', width: '14', height: '12', rx: '3' }),
    h('path', { d: 'M7 8h6M7 12h4', 'stroke-linecap': 'round' }),
  ]);

const ProfileIcon: FunctionalComponent = () =>
  h('svg', { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.75', class: 'h-5 w-5' }, [
    h('path', { d: 'M4 10h12M10 4l6 6-6 6', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }),
  ]);

const steps: GuideStep[] = [
  {
    target: 'search',
    title: 'Search for a place',
    body: 'Type a location name or suburb to find places near you.',
    placement: 'below',
    arrowDir: 'top',
    icon: SearchIcon,
  },
  {
    target: 'filter',
    title: 'Filter by your needs',
    body: 'Set your sensory preferences and place type to narrow down results.',
    placement: 'below',
    arrowDir: 'top',
    icon: FilterIcon,
  },
  {
    target: 'results',
    title: 'Browse your results',
    body: 'These are your top-matched locations. Tap any row to see a quick summary.',
    placement: 'left',
    arrowDir: 'right',
    icon: ListIcon,
  },
  {
    target: 'view-profile',
    title: 'View the full profile',
    body: 'Tap here for the full sensory breakdown — noise, lighting, crowd levels and more.',
    placement: 'above',
    arrowDir: 'bottom',
    icon: ProfileIcon,
  },
];

// ─── State ───────────────────────────────────────────────────────────────────

const active = ref(false);
const currentStep = ref(0);

type SpotlightRect = { top: number; left: number; width: number; height: number };
const spotRect = ref<SpotlightRect>({ top: 0, left: 0, width: 0, height: 0 });
const bubblePos = ref({ top: 'auto', left: 'auto', bottom: 'auto', right: 'auto' });

// ─── Computed styles ─────────────────────────────────────────────────────────

const PADDING = 10; // px around the target element

const spotlightStyle = computed(() => {
  const r = spotRect.value;
  const x = r.left - PADDING;
  const y = r.top - PADDING;
  const w = r.width + PADDING * 2;
  const hh = r.height + PADDING * 2;
  const radius = 14;

  return {
    top: `${y}px`,
    left: `${x}px`,
    width: `${w}px`,
    height: `${hh}px`,
    borderRadius: `${radius}px`,
    boxShadow: `0 0 0 9999px rgba(0,0,0,0.54)`,
  };
});

const bubbleStyle = computed(() => ({
  top: bubblePos.value.top,
  left: bubblePos.value.left,
  bottom: bubblePos.value.bottom,
  right: bubblePos.value.right,
}));

// ─── Helpers ─────────────────────────────────────────────────────────────────

function getTargetEl(selector: string): Element | null {
  return document.querySelector(`[data-guide="${selector}"]`);
}

function positionForStep(step: number) {
  const s = steps[step];
  const el = getTargetEl(s.target);
  if (!el) return;

  const r = el.getBoundingClientRect();
  spotRect.value = { top: r.top, left: r.left, width: r.width, height: r.height };

  const GAP = 18;
  const bubbleW = 290;

  const pos: typeof bubblePos.value = { top: 'auto', left: 'auto', bottom: 'auto', right: 'auto' };

  if (s.placement === 'below') {
    pos.top = `${r.bottom + GAP}px`;
    // Centre horizontally on target, clamped to viewport
    const ideal = r.left + r.width / 2 - bubbleW / 2;
    pos.left = `${Math.max(12, Math.min(ideal, window.innerWidth - bubbleW - 12))}px`;
  } else if (s.placement === 'above') {
    pos.bottom = `${window.innerHeight - r.top + GAP}px`;
    const ideal = r.left + r.width / 2 - bubbleW / 2;
    pos.left = `${Math.max(12, Math.min(ideal, window.innerWidth - bubbleW - 12))}px`;
  } else if (s.placement === 'left') {
    pos.top = `${Math.max(12, r.top)}px`;
    pos.left = `${Math.max(12, r.left - bubbleW - GAP)}px`;
  } else if (s.placement === 'right') {
    pos.top = `${Math.max(12, r.top)}px`;
    pos.left = `${r.right + GAP}px`;
  }

  bubblePos.value = pos;
}

let rafId = 0;
function trackTarget() {
  positionForStep(currentStep.value);
  rafId = requestAnimationFrame(trackTarget);
}

// ─── Step transitions ─────────────────────────────────────────────────────────

async function goToStep(n: number) {
  // Step 4 (index 3): auto-select first location so popup appears
  if (n === 3) {
    const firstId = props.firstLocationId ?? locationsStore.filteredLocations[0]?.locationId ?? null;
    if (firstId) {
      emit('select-location', firstId);
      // Wait for popup DOM to render
      await new Promise((r) => setTimeout(r, 350));
    }
  }
  currentStep.value = n;
  await nextTick();
  positionForStep(n);
}

async function next() {
  if (currentStep.value === steps.length - 1) {
    close();
    return;
  }
  await goToStep(currentStep.value + 1);
}

async function prev() {
  if (currentStep.value === 0) return;
  await goToStep(currentStep.value - 1);
}

function close() {
  active.value = false;
  cancelAnimationFrame(rafId);
}

// ─── Lifecycle ────────────────────────────────────────────────────────────────

onMounted(async () => {
  if (route.query.guide === '1') {
    // Small delay to let the page finish rendering
    await new Promise((r) => setTimeout(r, 600));
    active.value = true;
    await nextTick();
    positionForStep(0);
    trackTarget();
  }
});

onBeforeUnmount(() => {
  cancelAnimationFrame(rafId);
});
</script>

<style scoped>
/* ─── Root overlay ─────────────────────────────────── */
.guide-root {
  position: fixed;
  inset: 0;
  z-index: 9000;
  pointer-events: none; /* let spotlight + bubble handle their own events */
}

/* Spotlight hole */
.guide-spotlight {
  position: fixed;
  pointer-events: none;
  z-index: 9001;
  transition: top 250ms ease, left 250ms ease, width 250ms ease, height 250ms ease;
}

/* ─── Bubble ───────────────────────────────────────── */
.guide-bubble {
  position: fixed;
  z-index: 9002;
  pointer-events: all;
  width: 290px;
  border-radius: 20px;
  background: #ffffff;
  border: 1.5px solid rgba(95, 139, 132, 0.28);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.22), 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1.2rem 1.3rem 1.1rem;
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
}

.dark .guide-bubble {
  background: #1a2e2a;
  border-color: rgba(95, 160, 140, 0.35);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.48);
}

/* Counter */
.guide-bubble__counter {
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #7aa08c;
}

/* Icon row */
.guide-bubble__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: #eef5ef;
  color: #5f8b84;
}

.dark .guide-bubble__icon {
  background: #243e38;
  color: #7dcfb6;
}

/* Text */
.guide-bubble__title {
  font-size: 1rem;
  font-weight: 700;
  color: #1a3530;
  line-height: 1.25;
  margin-top: 0.1rem;
}

.dark .guide-bubble__title {
  color: #e8f5f0;
}

.guide-bubble__body {
  font-size: 0.88rem;
  font-weight: 400;
  color: #4e6f68;
  line-height: 1.55;
}

.dark .guide-bubble__body {
  color: #a8c8c0;
}

/* Actions */
.guide-bubble__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 0.4rem;
  gap: 0.5rem;
}

.guide-bubble__nav {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.guide-bubble__skip {
  font-size: 0.82rem;
  font-weight: 500;
  color: #7a9990;
  padding: 0.25rem 0;
  cursor: pointer;
  transition: color 120ms;
  background: none;
  border: none;
}

.guide-bubble__skip:hover {
  color: #4e6f68;
}

.dark .guide-bubble__skip {
  color: #6aada0;
}

.guide-bubble__prev {
  font-size: 0.82rem;
  font-weight: 500;
  color: #5f8b84;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.35rem 0.6rem;
  border-radius: 99px;
  transition: background 120ms;
}

.guide-bubble__prev:hover {
  background: #eef5ef;
}

.dark .guide-bubble__prev {
  color: #7dcfb6;
}

.guide-bubble__next {
  font-size: 0.85rem;
  font-weight: 600;
  color: #ffffff;
  background: #5f8b84;
  border: none;
  cursor: pointer;
  padding: 0.42rem 1rem;
  border-radius: 99px;
  transition: background 140ms ease;
}

.guide-bubble__next:hover {
  background: #4f7771;
}

.dark .guide-bubble__next {
  background: #4dbaaa;
  color: #0d1f1c;
}

.dark .guide-bubble__next:hover {
  background: #3da898;
}

/* ─── Arrow pointer ────────────────────────────────── */
.guide-bubble__arrow {
  position: absolute;
  width: 0;
  height: 0;
  pointer-events: none;
}

/* Arrow pointing UP (bubble is below target) */
.guide-bubble__arrow--top {
  top: -9px;
  left: 50%;
  transform: translateX(-50%);
  border-left: 9px solid transparent;
  border-right: 9px solid transparent;
  border-bottom: 9px solid #ffffff;
  filter: drop-shadow(0 -2px 2px rgba(0,0,0,0.08));
}

.dark .guide-bubble__arrow--top {
  border-bottom-color: #1a2e2a;
}

/* Arrow pointing DOWN (bubble is above target) */
.guide-bubble__arrow--bottom {
  bottom: -9px;
  left: 50%;
  transform: translateX(-50%);
  border-left: 9px solid transparent;
  border-right: 9px solid transparent;
  border-top: 9px solid #ffffff;
  filter: drop-shadow(0 2px 2px rgba(0,0,0,0.08));
}

.dark .guide-bubble__arrow--bottom {
  border-top-color: #1a2e2a;
}

/* Arrow pointing RIGHT (bubble is to the left of target) */
.guide-bubble__arrow--right {
  top: 22px;
  right: -9px;
  border-top: 9px solid transparent;
  border-bottom: 9px solid transparent;
  border-left: 9px solid #ffffff;
}

.dark .guide-bubble__arrow--right {
  border-left-color: #1a2e2a;
}

/* Arrow pointing LEFT (bubble is to the right of target) */
.guide-bubble__arrow--left {
  top: 22px;
  left: -9px;
  border-top: 9px solid transparent;
  border-bottom: 9px solid transparent;
  border-right: 9px solid #ffffff;
}

.dark .guide-bubble__arrow--left {
  border-right-color: #1a2e2a;
}

/* No arrow */
.guide-bubble__arrow--none {
  display: none;
}

/* ─── Transitions ──────────────────────────────────── */
.guide-fade-enter-active {
  transition: opacity 300ms ease;
}
.guide-fade-leave-active {
  transition: opacity 200ms ease;
}
.guide-fade-enter-from,
.guide-fade-leave-to {
  opacity: 0;
}

.guide-bubble-enter-active {
  transition: opacity 180ms ease, transform 180ms ease;
}
.guide-bubble-leave-active {
  transition: opacity 120ms ease, transform 120ms ease;
}
.guide-bubble-enter-from {
  opacity: 0;
  transform: translateY(6px) scale(0.97);
}
.guide-bubble-leave-to {
  opacity: 0;
  transform: translateY(-4px) scale(0.98);
}
</style>
