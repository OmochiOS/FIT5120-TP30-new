<template>
  <div class="flex flex-wrap gap-2">
    <button
      v-for="option in options"
      :key="option.value"
      type="button"
      class="h-8 rounded-full border px-4 text-sm font-semibold transition"
      :class="modelValue === option.value
        ? 'border-[#5f8b84] bg-[#5f8b84] text-white dark:border-sense-mint dark:bg-sense-mint dark:text-sense-night'
        : 'border-[#d8ded8] bg-white text-sense-ink/78 hover:border-[#93aca6] hover:text-sense-ink dark:border-white/10 dark:bg-white/5 dark:text-slate-200 dark:hover:border-sense-mint/35'"
      @click="$emit('update:modelValue', option.value)"
    >
      {{ option.label }}
    </button>
  </div>
</template>

<script setup lang="ts">
import type { FilterOption } from '@/types/domain';

defineProps<{
  modelValue: FilterOption['value'];
  options: FilterOption[];
}>();

defineEmits<{
  'update:modelValue': [value: FilterOption['value']];
}>();
</script>
