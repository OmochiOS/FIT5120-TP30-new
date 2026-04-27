<template>
  <label class="relative block min-w-[200px] flex-1">
    <span class="sr-only">Search locations</span>
    <input
      :value="modelValue"
      type="search"
      placeholder="Search locations..."
      class="h-9 w-full rounded-full border border-[#d8dfd8] bg-white px-4 text-sm text-sense-ink/86 outline-none transition placeholder:text-sense-ink/42 focus:border-[#88a29b] focus:ring-2 focus:ring-[#88a29b]/15 dark:border-white/10 dark:bg-white/5 dark:text-white dark:placeholder:text-slate-300"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    />
  </label>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: string;
}>();

defineEmits<{
  'update:modelValue': [value: string];
}>();
</script>
