<template>
  <Teleport to="body">
    <Transition name="pref-drawer">
      <div
        v-if="modelValue"
        class="fixed inset-0 z-[9999]"
      >
        <!-- Backdrop -->
        <button
          type="button"
          class="absolute inset-0 bg-sense-night/30"
          aria-label="Close sensory preferences"
          @click="$emit('update:modelValue', false)"
        />

        <!-- Panel: right-side on desktop, bottom-sheet on mobile -->
        <aside class="pref-drawer-panel absolute inset-y-0 right-0 flex w-full max-w-[340px] flex-col border-l border-sense-teal/10 bg-[#f8f6f3] px-5 py-5 shadow-2xl dark:border-[#38515d] dark:bg-[#0b1720]">
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-sm font-semibold uppercase tracking-[0.16em] text-sense-teal/70 dark:text-sense-mint">
                Sensory Preferences
              </p>
              <p class="mt-2 text-sm leading-6 text-sense-ink/62 dark:text-slate-50">
                Apply quick filters to narrow the map to calmer, easier-to-read places.
              </p>
              <p class="mt-2 text-[11px] leading-5 text-sense-ink/50 dark:text-slate-200">
                Preferences stay on this device. Clearing browser cache resets them.
              </p>
            </div>

            <button
              type="button"
              class="shrink-0 rounded-full border border-sense-teal/12 px-3 py-1.5 text-xs font-semibold text-sense-ink/72 transition hover:border-sense-teal/28 dark:border-[#425a66] dark:bg-[#142631] dark:text-slate-50 dark:hover:border-slate-200"
              @click="$emit('update:modelValue', false)"
            >
              Close
            </button>
          </div>

          <div class="mt-6 grid gap-3">
            <button
              v-for="option in filterOptions"
              :key="option.key"
              type="button"
              class="flex items-center justify-between rounded-full border px-4 py-3 text-left text-sm font-semibold transition"
              :class="draft[option.key]
                ? 'border-sense-teal bg-sense-teal text-white dark:border-sense-mint dark:bg-sense-mint dark:text-sense-night'
                : 'border-sense-teal/15 bg-white text-sense-ink/80 hover:border-sense-teal/35 dark:border-[#425a66] dark:bg-[#142631] dark:text-slate-50 dark:hover:border-slate-200'"
              @click="toggleFilter(option.key)"
            >
              <span>{{ option.label }}</span>
              <!-- Icon: ON = semantic active icon, OFF = muted/disabled variant -->
              <span class="flex h-5 w-5 shrink-0 items-center justify-center opacity-90" aria-hidden="true" v-html="draft[option.key] ? filterIcons[option.key].on : filterIcons[option.key].off" />
            </button>
          </div>

          <div class="mt-6">
            <p class="text-xs font-semibold uppercase tracking-[0.18em] text-sense-teal/65 dark:text-sense-mint">
              Activity
            </p>

            <div class="mt-3 flex flex-wrap gap-2">
              <button
                v-for="option in activityOptions"
                :key="option.value"
                type="button"
                class="rounded-[16px] border px-4 py-3 text-sm font-semibold transition"
                :class="draft.activity === option.value
                  ? 'border-sense-teal bg-sense-teal text-white dark:border-sense-mint dark:bg-sense-mint dark:text-sense-night'
                  : 'border-sense-teal/15 bg-white text-sense-ink/78 hover:border-sense-teal/35 dark:border-[#425a66] dark:bg-[#142631] dark:text-slate-50 dark:hover:border-slate-200'"
                @click="draft.activity = option.value"
              >
                {{ option.label }}
              </button>
            </div>
          </div>

          <div class="mt-auto flex items-center justify-between gap-3 pt-8">
            <button
              type="button"
              class="text-sm font-semibold text-sense-ink/62 underline decoration-sense-teal/25 underline-offset-4 transition hover:text-sense-ink dark:text-slate-100 dark:hover:text-white"
              @click="clearFilters"
            >
              Clear
            </button>

            <button
              type="button"
              class="rounded-full bg-sense-teal px-5 py-3 text-sm font-semibold text-white transition hover:bg-sense-ink dark:bg-sense-mint dark:text-sense-night dark:hover:bg-white"
              @click="applyFilters"
            >
              Apply
            </button>
          </div>
        </aside>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* ── Desktop: right-side drawer ───────────────────────── */
.pref-drawer-enter-active,
.pref-drawer-leave-active {
  transition: opacity 220ms ease;
}
.pref-drawer-enter-active .pref-drawer-panel,
.pref-drawer-leave-active .pref-drawer-panel {
  transition: transform 260ms cubic-bezier(0.32, 0, 0.1, 1);
}
.pref-drawer-enter-from .pref-drawer-panel,
.pref-drawer-leave-to .pref-drawer-panel {
  transform: translateX(100%);
}
.pref-drawer-enter-from,
.pref-drawer-leave-to {
  opacity: 0;
}

/* ── Mobile: bottom sheet ─────────────────────────────── */
@media (max-width: 767px) {
  .pref-drawer-panel {
    /* Override right-side drawer → bottom sheet */
    inset: auto 0 0 0 !important;
    top: auto !important;
    right: 0 !important;
    left: 0 !important;
    width: 100% !important;
    max-width: 100% !important;
    max-height: 85vh;
    overflow-y: auto;
    border-left: none !important;
    border-top: 1.5px solid rgba(95, 139, 132, 0.18);
    border-radius: 24px 24px 0 0;
    /* Safe area for phones with home bar */
    padding-bottom: calc(1.25rem + env(safe-area-inset-bottom, 0px));
  }

  /* Bottom sheet slides up instead of from right */
  .pref-drawer-enter-from .pref-drawer-panel,
  .pref-drawer-leave-to .pref-drawer-panel {
    transform: translateY(100%);
  }
}
</style>


<script setup lang="ts">
import { reactive, watch } from 'vue';

import type { MapQuickFilters } from '@/types/domain';
import { useLocationsStore } from '@/stores/locations';
import { usePreferencesStore } from '@/stores/preferences';

const props = defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
}>();

const preferencesStore = usePreferencesStore();
const locationsStore = useLocationsStore();

const draft = reactive<MapQuickFilters>({
  noise: false,
  lighting: false,
  crowds: false,
  unpredictability: false,
  activity: 'any',
});

const filterOptions = [
  { key: 'noise', label: 'Noise' },
  { key: 'lighting', label: 'Lighting' },
  { key: 'crowds', label: 'Crowds' },
  { key: 'unpredictability', label: 'Unpredictability' },
] as const;

/** SVG icon pairs for each filter – ON = active, OFF = muted/disabled */
const filterIcons: Record<string, { on: string; off: string }> = {
  // Noise: speaker with waves (on) / speaker muted with X (off)
  noise: {
    on: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" width="20" height="20">
      <path d="M4 7.5H2a1 1 0 0 0-1 1v3a1 1 0 0 0 1 1h2l4.5 3.5V4L4 7.5z"/>
      <path d="M14 7a3 3 0 0 1 0 6"/>
      <path d="M16.5 4.5a7 7 0 0 1 0 11"/>
    </svg>`,
    off: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" width="20" height="20">
      <path d="M4 7.5H2a1 1 0 0 0-1 1v3a1 1 0 0 0 1 1h2l4.5 3.5V4L4 7.5z"/>
      <line x1="13" y1="7" x2="19" y2="13"/>
      <line x1="19" y1="7" x2="13" y2="13"/>
    </svg>`,
  },
  // Lighting: sun (on) / sun with line through (off)
  lighting: {
    on: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" width="20" height="20">
      <circle cx="10" cy="10" r="3.5"/>
      <line x1="10" y1="1" x2="10" y2="3"/>
      <line x1="10" y1="17" x2="10" y2="19"/>
      <line x1="1" y1="10" x2="3" y2="10"/>
      <line x1="17" y1="10" x2="19" y2="10"/>
      <line x1="3.5" y1="3.5" x2="5" y2="5"/>
      <line x1="15" y1="15" x2="16.5" y2="16.5"/>
      <line x1="16.5" y1="3.5" x2="15" y2="5"/>
      <line x1="5" y1="15" x2="3.5" y2="16.5"/>
    </svg>`,
    off: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" width="20" height="20">
      <circle cx="10" cy="10" r="3.5" stroke-opacity="0.45"/>
      <line x1="10" y1="1" x2="10" y2="3" stroke-opacity="0.45"/>
      <line x1="10" y1="17" x2="10" y2="19" stroke-opacity="0.45"/>
      <line x1="1" y1="10" x2="3" y2="10" stroke-opacity="0.45"/>
      <line x1="17" y1="10" x2="19" y2="10" stroke-opacity="0.45"/>
      <line x1="3.5" y1="3.5" x2="5" y2="5" stroke-opacity="0.45"/>
      <line x1="15" y1="15" x2="16.5" y2="16.5" stroke-opacity="0.45"/>
      <line x1="16.5" y1="3.5" x2="15" y2="5" stroke-opacity="0.45"/>
      <line x1="5" y1="15" x2="3.5" y2="16.5" stroke-opacity="0.45"/>
      <line x1="3" y1="17" x2="17" y2="3" stroke-width="1.8"/>
    </svg>`,
  },
  // Crowds: two people silhouette (on) / people with X (off)
  crowds: {
    on: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" width="20" height="20">
      <circle cx="7" cy="6" r="2.5"/>
      <path d="M1 17c0-3.314 2.686-5 6-5s6 1.686 6 5"/>
      <circle cx="14.5" cy="6" r="2"/>
      <path d="M13 12.2c.48-.13.98-.2 1.5-.2 2.76 0 4.5 1.343 4.5 4"/>
    </svg>`,
    off: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" width="20" height="20">
      <circle cx="7" cy="6" r="2.5" stroke-opacity="0.45"/>
      <path d="M1 17c0-3.314 2.686-5 6-5s6 1.686 6 5" stroke-opacity="0.45"/>
      <circle cx="14.5" cy="6" r="2" stroke-opacity="0.45"/>
      <path d="M13 12.2c.48-.13.98-.2 1.5-.2 2.76 0 4.5 1.343 4.5 4" stroke-opacity="0.45"/>
      <line x1="3" y1="17" x2="17" y2="3" stroke-width="1.8"/>
    </svg>`,
  },
  // Unpredictability: shuffle arrows (on) / straight flat line (off)
  unpredictability: {
    on: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" width="20" height="20">
      <polyline points="16,3 19,3 19,6"/>
      <path d="M1 17c2-4 4-6 8-6s6-4 10-8"/>
      <polyline points="16,17 19,17 19,14"/>
      <path d="M1 3c2 4 4 6 8 6"/>
    </svg>`,
    off: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" width="20" height="20">
      <line x1="1" y1="10" x2="19" y2="10" stroke-opacity="0.5"/>
      <line x1="3" y1="17" x2="17" y2="3" stroke-width="1.8"/>
    </svg>`,
  },
};

const activityOptions = [
  { label: 'Outdoor', value: 'outdoor' as const },
  { label: 'Cultural', value: 'cultural' as const },
  { label: 'Social', value: 'social' as const },
  { label: 'Active', value: 'active' as const },
  { label: 'Any', value: 'any' as const },
];

watch(
  () => props.modelValue,
  (isOpen) => {
    if (!isOpen) {
      return;
    }

    Object.assign(draft, preferencesStore.currentFilters);
  },
  { immediate: true },
);

function toggleFilter(key: keyof Omit<MapQuickFilters, 'activity'>) {
  draft[key] = !draft[key];
}

function clearFilters() {
  Object.assign(draft, {
    noise: false,
    lighting: false,
    crowds: false,
    unpredictability: false,
    activity: 'any',
  } satisfies MapQuickFilters);
}

function applyFilters() {
  preferencesStore.replaceFilters({ ...draft });
  void locationsStore.applyBackendFilter();
  emit('update:modelValue', false);
}
</script>
