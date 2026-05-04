<template>
  <button
    type="button"
    class="inline-flex h-11 w-11 items-center justify-center rounded-full border border-sense-teal/15 bg-white/75 text-sense-ink transition hover:border-sense-teal/35 hover:text-sense-teal dark:border-white/10 dark:bg-white/5 dark:text-slate-100 dark:hover:border-sense-mint/40"
    :aria-label="themeLabel"
    :title="themeLabel"
    @click="preferencesStore.toggleTheme()"
  >
    <span class="sr-only">{{ themeLabel }}</span>

    <svg
      v-if="preferencesStore.darkModeEnabled"
      aria-hidden="true"
      viewBox="0 0 24 24"
      class="h-5 w-5"
      fill="none"
      stroke="currentColor"
      stroke-linecap="round"
      stroke-linejoin="round"
      stroke-width="1.9"
    >
      <circle
        cx="12"
        cy="12"
        r="4.25"
      />
      <path d="M12 2.75v2.1" />
      <path d="M12 19.15v2.1" />
      <path d="m5.46 5.46 1.48 1.48" />
      <path d="m17.06 17.06 1.48 1.48" />
      <path d="M2.75 12h2.1" />
      <path d="M19.15 12h2.1" />
      <path d="m5.46 18.54 1.48-1.48" />
      <path d="m17.06 6.94 1.48-1.48" />
    </svg>

    <svg
      v-else
      aria-hidden="true"
      viewBox="0 0 24 24"
      class="h-5 w-5"
      fill="none"
      stroke="currentColor"
      stroke-linecap="round"
      stroke-linejoin="round"
      stroke-width="1.9"
    >
      <path d="M20.25 14.6A8.85 8.85 0 1 1 9.4 3.75a7.35 7.35 0 1 0 10.85 10.85Z" />
    </svg>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue';

import { usePreferencesStore } from '@/stores/preferences';

const preferencesStore = usePreferencesStore();
const themeLabel = computed(() => (preferencesStore.darkModeEnabled ? 'Switch to light theme' : 'Switch to dark theme'));
</script>
