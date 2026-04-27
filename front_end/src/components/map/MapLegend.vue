<template>
  <div
    class="inline-flex flex-wrap items-center rounded-full font-bold uppercase tracking-[0.1em]"
    :class="wrapperClass"
  >
    <div
      v-for="item in legendItems"
      :key="item.label"
      class="flex items-center gap-2 rounded-full"
      :class="[item.pillTone, itemClass]"
    >
      <span
        class="h-2.5 w-2.5 rounded-full"
        :class="item.tone"
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
  }>(),
  {
    variant: 'map',
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

const wrapperClass = computed(() =>
  props.variant === 'detail'
    ? 'gap-2.5 border border-[#e4ddd1] bg-[#f9f6ef] px-2.5 py-2 text-[10px] shadow-none dark:border-white/10 dark:bg-[#13222c]'
    : 'gap-2 border border-[#d7d4cd] bg-white px-3 py-2 text-[11px] shadow-[0_12px_28px_rgba(32,45,41,0.18)] dark:border-white/12 dark:bg-[#0d1b24]',
);

const itemClass = computed(() => (props.variant === 'detail' ? 'px-2 py-[5px]' : 'px-2.5 py-1'));
</script>
