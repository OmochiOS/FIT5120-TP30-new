<template>
  <section class="space-y-3.5">
    <p class="text-[20px] font-semibold tracking-tight text-sense-ink dark:text-white">
      Best Times to Visit
    </p>

    <div class="detail-surface detail-best-times rounded-[12px] border border-sense-teal/10 bg-white px-4 py-4 shadow-[0_12px_24px_-20px_rgba(16,74,85,0.35)] dark:border-white/8 dark:bg-[#16252f]/90 md:px-4.5">

      <!-- Loading skeleton -->
      <div v-if="loading" class="space-y-3">
        <div class="h-6 w-full animate-pulse rounded-[6px] bg-sense-teal/8 dark:bg-white/8" />
        <div class="h-4 w-36 animate-pulse rounded bg-sense-teal/6 dark:bg-white/6" />
      </div>

      <template v-else>
        <!-- Time bar -->
        <div>
          <div class="flex h-6 overflow-hidden rounded-[6px] border border-sense-teal/8 dark:border-white/8">
            <div
              v-for="segment in segments"
              :key="segment.time"
              class="min-w-0 flex-1"
              :class="toneClass(segment.level)"
              :title="segment.time"
            />
          </div>
          <div class="detail-axis mt-1.5 flex items-center justify-between text-[11px] font-medium text-sense-ink/50 dark:text-slate-200">
            <span>7am</span>
            <span>9pm</span>
          </div>
        </div>

        <!-- Legend -->
        <div class="mt-3 flex justify-start">
          <MapLegend variant="detail" />
        </div>

        <!-- Quietest note -->
        <p v-if="quietestNote" class="detail-copy mt-4 text-[14px] font-medium leading-relaxed text-sense-ink/70 dark:text-slate-100">
          {{ quietestNote }}
        </p>
      </template>
    </div>
  </section>
</template>

<script setup lang="ts">
import MapLegend from '@/components/map/MapLegend.vue';

defineProps<{
  segments: { time: string; level: 'low' | 'moderate' | 'high' }[];
  quietestNote: string;
  loading: boolean;
}>();

function toneClass(level: 'low' | 'moderate' | 'high'): string {
  if (level === 'low') return 'bg-[#95c894] dark:bg-emerald-500/40';
  if (level === 'moderate') return 'bg-[#e0b16f] dark:bg-amber-500/40';
  return 'bg-[#efc1c8] dark:bg-rose-500/40';
}
</script>
