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

      <div class="flex items-center gap-3">
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

        <!-- Quick View link -->
        <RouterLink
          to="/quick-view"
          class="inline-flex items-center gap-1.5 rounded-full border px-3.5 py-[7px] text-[14px] font-semibold leading-none tracking-[-0.01em] transition"
          :class="isActive('/quick-view')
            ? 'border-sense-teal/30 bg-sense-teal/8 text-sense-teal dark:border-sense-mint/30 dark:bg-sense-mint/10 dark:text-sense-mint'
            : 'border-sense-teal/18 bg-white/55 text-sense-teal/80 hover:border-sense-teal/30 hover:bg-sense-teal/6 hover:text-sense-teal dark:border-white/12 dark:bg-white/[0.04] dark:text-sense-mint/75 dark:hover:border-sense-mint/28 dark:hover:text-sense-mint'"
        >
          <svg viewBox="0 0 16 16" fill="none" class="h-3.5 w-3.5 shrink-0" aria-hidden="true">
            <circle cx="6.5" cy="6.5" r="4" stroke="currentColor" stroke-width="1.5" />
            <path d="M10 10 13.5 13.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
          </svg>
          Quick View
        </RouterLink>

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
