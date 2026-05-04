<template>
  <section class="flex flex-col items-center justify-center py-32 px-6 text-center page-intro">
    <h1 class="text-7xl font-bold text-sense-ink dark:text-white mb-4">404</h1>
    <h2 class="text-xl font-medium text-sense-ink/80 dark:text-slate-200 mb-8">
      Page not found
    </h2>
    <RouterLink
      to="/"
      class="inline-flex items-center justify-center rounded-full bg-sense-teal px-6 py-2.5 text-sm font-semibold text-white transition hover:bg-sense-ink dark:bg-sense-mint dark:text-sense-night dark:hover:bg-white"
    >
      Return to Home
    </RouterLink>
  </section>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>
