<template>
  <footer class="px-4 pb-4 pt-6 md:px-6 md:pb-5">
    <div class="mx-auto max-w-[1120px] px-3 text-center">
      <p class="text-[9px] leading-5 text-sense-ink/46 dark:text-slate-200">
        &copy; 2026 AmitySpace. {{ attribution.footerAttribution }}
      </p>
      <p class="mt-1 text-[9px] leading-5 text-sense-ink/42 dark:text-slate-200">
        Preferences are saved only on this device. Clearing browser data resets them.
      </p>
      <p class="mt-1 text-[9px] leading-5 text-sense-ink/42 dark:text-slate-200">
        Sensor coverage is strongest in central Melbourne and may be limited outside current monitored areas.
      </p>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { attribution } from '@/utils/adapters';
</script>
