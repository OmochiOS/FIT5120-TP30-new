<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="fixed inset-0 z-50"
    >
      <button
        type="button"
        class="absolute inset-0 bg-sense-night/30"
        aria-label="Close sensory preferences"
        @click="$emit('update:modelValue', false)"
      ></button>

      <aside class="absolute inset-y-0 right-0 flex w-full max-w-[340px] flex-col border-l border-sense-teal/10 bg-[#f8f6f3] px-5 py-5 shadow-2xl dark:border-[#38515d] dark:bg-[#0b1720]">
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
            class="rounded-full border border-sense-teal/12 px-3 py-1.5 text-xs font-semibold text-sense-ink/72 transition hover:border-sense-teal/28 dark:border-[#425a66] dark:bg-[#142631] dark:text-slate-50 dark:hover:border-slate-200"
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
            <span class="text-xs uppercase tracking-[0.14em] text-current opacity-90">
              {{ draft[option.key] ? 'On' : 'Off' }}
            </span>
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
  </Teleport>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue';

import type { MapQuickFilters } from '@/types/domain';
import { usePreferencesStore } from '@/stores/preferences';

const props = defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
}>();

const preferencesStore = usePreferencesStore();

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
  emit('update:modelValue', false);
}
</script>
