<template>
  <header class="sticky top-0 z-30 border-b border-sense-teal/10 bg-white/88 px-4 backdrop-blur-md dark:border-white/10 dark:bg-sense-night/88 md:px-6">
    <div class="mx-auto flex max-w-[1440px] items-center justify-between gap-2 py-4 md:gap-6">
      <RouterLink
        to="/"
        class="inline-flex shrink-0 leading-none"
      >
        <span class="font-display text-[1.5rem] tracking-[0.01em] text-sense-teal dark:text-sense-mint md:text-[1.9rem]">
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

      <div class="flex items-center gap-1.5 sm:gap-3">
        <nav
          aria-label="Primary mobile"
          class="flex items-center gap-2.5 md:hidden sm:gap-4"
        >
          <RouterLink
            v-for="item in navigationItems"
            :key="item.to"
            :to="item.to"
            class="text-[13px] font-medium transition sm:text-[14px]"
            :class="isActive(item.to)
              ? 'text-sense-teal dark:text-sense-mint'
              : 'text-sense-ink/66 hover:text-sense-ink dark:text-slate-100 dark:hover:text-white'"
          >
            {{ item.label }}
          </RouterLink>
        </nav>

        <!-- Quick View toggle -->
        <button
          type="button"
          data-guide="quick-view"
          @click="handleQuickViewToggle"
          class="inline-flex items-center justify-center gap-1.5 rounded-full border p-2 transition md:px-3.5 md:py-[7px] text-[14px] font-semibold leading-none tracking-[-0.01em]"
          :class="preferencesStore.quickViewModeActive
            ? 'border-sense-teal/30 bg-sense-teal/8 text-sense-teal dark:border-sense-mint/30 dark:bg-sense-mint/10 dark:text-sense-mint'
            : 'border-sense-teal/18 bg-white/55 text-sense-teal/80 hover:border-sense-teal/30 hover:bg-sense-teal/6 hover:text-sense-teal dark:border-white/12 dark:bg-white/[0.04] dark:text-sense-mint/75 dark:hover:border-sense-mint/28 dark:hover:text-sense-mint'"
        >
          <svg viewBox="0 0 16 16" fill="none" class="h-4 w-4 shrink-0 md:h-3.5 md:w-3.5" aria-hidden="true">
            <circle cx="6.5" cy="6.5" r="4" stroke="currentColor" stroke-width="1.5" />
            <path d="M10 10 13.5 13.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
          </svg>
          <span class="hidden md:inline">Quick View Mode</span>
        </button>

        <ThemeToggle />
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { RouterLink, useRoute, useRouter } from 'vue-router';

import ThemeToggle from '@/components/layout/ThemeToggle.vue';
import { usePreferencesStore } from '@/stores/preferences';

const route = useRoute();
const router = useRouter();
const preferencesStore = usePreferencesStore();

function handleQuickViewToggle() {
  preferencesStore.toggleQuickViewMode();
  if (preferencesStore.quickViewModeActive && route.path !== '/map') {
    router.push('/map');
  }
}

const navigationItems = [
  { label: 'Home', to: '/' },
  { label: 'Map', to: '/map' },
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
