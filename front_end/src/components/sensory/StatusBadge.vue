<template>
  <span
    class="inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-[0.14em]"
    :class="classes"
  >
    {{ label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';

import type { CrowdLevel, DataStatus } from '@/types/domain';
import { getCrowdToneClass } from '@/utils/crowd';

const props = defineProps<{
  label: string;
  level?: CrowdLevel;
  status?: DataStatus;
}>();

const classes = computed(() => {
  if (props.level) {
    return getCrowdToneClass(props.level);
  }

  if (props.status === 'stale') {
    return 'bg-amber-100 text-amber-800 dark:bg-amber-500/20 dark:text-amber-100';
  }

  if (props.status === 'error' || props.status === 'empty') {
    return 'bg-rose-100 text-rose-800 dark:bg-rose-500/20 dark:text-rose-100';
  }

  return 'bg-slate-100 text-slate-700 dark:bg-white/10 dark:text-slate-200';
});
</script>
