<template>
  <section class="glass-panel-strong space-y-4 p-6">
    <p class="text-sm font-semibold uppercase tracking-[0.16em] text-sense-teal/70 dark:text-sense-mint">
      Page not found
    </p>
    <h1 class="font-display text-4xl text-sense-ink dark:text-white">
      This page isn't available.
    </h1>
    <p class="max-w-2xl text-base leading-7 text-sense-ink/72 dark:text-slate-100">
      Use Map, Prediction, or About from the main navigation, or return to the landing page.
    </p>
    <RouterLink
      to="/"
      class="inline-flex rounded-full bg-sense-teal px-5 py-3 text-sm font-semibold text-white transition hover:bg-sense-ink dark:bg-sense-mint dark:text-sense-night dark:hover:bg-white"
    >
      Go home
    </RouterLink>
  </section>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>
