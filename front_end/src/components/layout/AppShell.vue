<template>
  <div class="min-h-screen">
    <a
      href="#main-content"
      class="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-full focus:bg-sense-teal focus:px-4 focus:py-2 focus:text-white"
    >
      Skip to content
    </a>

    <AppHeader />

    <main
      id="main-content"
      class="mx-auto flex w-full max-w-[1440px] flex-1 flex-col px-4 pt-6 md:px-6"
    >
      <slot />
    </main>

    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import AppFooter from '@/components/layout/AppFooter.vue';
import AppHeader from '@/components/layout/AppHeader.vue';
</script>
