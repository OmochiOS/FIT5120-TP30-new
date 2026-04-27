<template>
  <button
    type="button"
    class="rounded-full border border-sense-teal/15 bg-white/75 px-4 py-2 text-sm font-semibold text-sense-ink transition hover:border-sense-teal/35 hover:text-sense-teal dark:border-white/10 dark:bg-white/5 dark:text-slate-100 dark:hover:border-sense-mint/40"
    @click="preferencesStore.toggleTheme()"
  >
    {{ preferencesStore.darkModeEnabled ? 'Light theme' : 'Dark theme' }}
  </button>
</template>

<script setup lang="ts">
import { usePreferencesStore } from '@/stores/preferences';

const preferencesStore = usePreferencesStore();
</script>
