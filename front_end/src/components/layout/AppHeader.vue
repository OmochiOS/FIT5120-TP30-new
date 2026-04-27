<template>
  <header class="sticky top-0 z-30 border-b border-sense-teal/10 bg-white/88 px-4 backdrop-blur-md dark:border-white/10 dark:bg-sense-night/88 md:px-6">
    <div class="mx-auto flex max-w-[1440px] items-center justify-between gap-6 py-4">
      <RouterLink
        to="/"
        class="inline-flex shrink-0 leading-none"
      >
        <span class="font-display text-[1.9rem] tracking-[0.01em] text-sense-teal dark:text-sense-mint">
          AmitySpace
        </span>
      </RouterLink>

      <nav
        aria-label="Primary"
        class="hidden items-center gap-8 md:flex"
      >
        <RouterLink
          v-for="item in navigationItems"
          :key="item.to"
          :to="item.to"
          class="text-[15px] font-medium transition"
          :class="isActive(item.to)
            ? 'text-sense-teal dark:text-sense-mint'
            : 'text-sense-ink/66 hover:text-sense-ink dark:text-slate-100 dark:hover:text-white'"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="flex items-center gap-4">
        <nav
          aria-label="Primary mobile"
          class="flex items-center gap-4 md:hidden"
        >
          <RouterLink
            v-for="item in navigationItems"
            :key="item.to"
            :to="item.to"
            class="text-[14px] font-medium transition"
            :class="isActive(item.to)
              ? 'text-sense-teal dark:text-sense-mint'
              : 'text-sense-ink/66 hover:text-sense-ink dark:text-slate-100 dark:hover:text-white'"
          >
            {{ item.label }}
          </RouterLink>
        </nav>

        <ThemeToggle />
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router';

import ThemeToggle from '@/components/layout/ThemeToggle.vue';

const route = useRoute();

const navigationItems = [
  { label: 'Home', to: '/' },
  { label: 'Map', to: '/map' },
  { label: 'Prediction', to: '/prediction' },
  { label: 'About', to: '/about' },
];

function isActive(targetPath: string): boolean {
  if (targetPath === '/') {
    return route.path === '/';
  }

  if (targetPath === '/map') {
    return route.path === '/map' || route.path === '/locations' || route.path.startsWith('/location/');
  }

  return route.path === targetPath;
}
</script>
