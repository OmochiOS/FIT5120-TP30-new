<template>
  <div
    class="inline-flex flex-wrap items-center rounded-full font-bold uppercase tracking-[0.1em]"
    :class="wrapperClass"
  >
    <div
      v-for="item in legendItems"
      :key="item.label"
      class="flex items-center rounded-full"
      :class="[item.pillTone, itemClass]"
    >
      <span
        class="rounded-full shrink-0"
        :class="[item.tone, dotClass]"
      ></span>
      <span :class="item.labelTone" class="leading-none">
        {{ item.label }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    variant?: 'map' | 'detail';
    compactMobile?: boolean;
  }>(),
  {
    variant: 'map',
    compactMobile: false,
  },
);

const legendItems = [
  {
    label: 'Low',
    tone: 'bg-[#98c39b]',
    labelTone: 'text-[#1f6b46] dark:text-[#98f5bf]',
    pillTone: 'bg-[#edf8f0] dark:bg-[#133127]',
  },
  {
    label: 'Moderate',
    tone: 'bg-[#d39b57]',
    labelTone: 'text-[#9f5a0c] dark:text-[#ffd56f]',
    pillTone: 'bg-[#fbf2e4] dark:bg-[#352712]',
  },
  {
    label: 'High',
    tone: 'bg-[#cb8b79]',
    labelTone: 'text-[#ae425b] dark:text-[#ffabc0]',
    pillTone: 'bg-[#fbecee] dark:bg-[#3a1822]',
  },
];

const wrapperClass = computed(() => {
  if (props.variant === 'detail') {
    return 'gap-2.5 border border-[#e4ddd1] bg-[#f9f6ef] px-2.5 py-2 text-[10px] shadow-none dark:border-white/10 dark:bg-[#13222c]';
  }
  if (props.compactMobile) {
    return 'gap-1 border border-[#d7d4cd] bg-white px-2 py-1 text-[9px] shadow-[0_12px_28px_rgba(32,45,41,0.18)] dark:border-white/12 dark:bg-[#0d1b24] md:gap-2 md:px-3 md:py-2 md:text-[11px]';
  }
  return 'gap-2 border border-[#d7d4cd] bg-white px-3 py-2 text-[11px] shadow-[0_12px_28px_rgba(32,45,41,0.18)] dark:border-white/12 dark:bg-[#0d1b24]';
});

const itemClass = computed(() => {
  if (props.variant === 'detail') return 'px-2 py-[5px] gap-2';
  if (props.compactMobile) return 'px-1.5 py-[3px] gap-1 md:px-2.5 md:py-1 md:gap-2';
  return 'px-2.5 py-1 gap-2';
});

const dotClass = computed(() => {
  if (props.compactMobile) return 'h-2 w-2 md:h-2.5 md:w-2.5';
  return 'h-2.5 w-2.5';
});
</script>
