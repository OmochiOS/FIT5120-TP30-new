<template>
  <div class="relative block min-w-[200px] flex-1">
    <label class="block">
      <span class="sr-only">Search locations</span>
      <input
        :value="modelValue"
        type="search"
        placeholder="Search locations..."
        class="h-9 w-full rounded-full border border-[#d8dfd8] bg-white px-4 text-sm text-sense-ink/86 outline-none transition placeholder:text-sense-ink/42 focus:border-[#88a29b] focus:ring-2 focus:ring-[#88a29b]/15 dark:border-white/10 dark:bg-white/5 dark:text-white dark:placeholder:text-slate-300"
        @input="onInput"
        @focus="isFocused = true"
        @blur="onBlur"
      />
    </label>

    <Transition name="fade-dropdown">
      <div
        v-if="showDropdown && isFocused && modelValue.length > 0"
        class="absolute left-0 top-[calc(100%+8px)] z-[5000] w-full max-w-[400px] overflow-hidden rounded-[22px] border border-[#ebe5dd] bg-white shadow-[0_12px_24px_-10px_rgba(16,74,85,0.2)] dark:border-white/10 dark:bg-[#0f2029]"
      >
        <div v-if="results && results.length > 0" class="max-h-[300px] overflow-y-auto py-2">
          <button
            v-for="location in results.slice(0, 8)"
            :key="location.locationId"
            type="button"
            class="group grid w-full grid-cols-[auto_minmax(0,1fr)] items-center gap-3 px-5 py-2.5 text-left transition hover:bg-[#faf8f4] dark:hover:bg-white/[0.045]"
            @mousedown.prevent="selectLocation(location)"
          >
            <span
              class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-[0.72rem] font-bold text-white shadow-[0_6px_12px_rgba(79,90,83,0.11)]"
              :class="priorityTone(location.sensoryScore)"
            >
              {{ location.sensoryScore }}
            </span>
            <div class="min-w-0">
              <p class="truncate font-semibold text-sense-ink group-hover:text-[#5f8b84] dark:text-white dark:group-hover:text-sense-mint">
                {{ location.name }}
              </p>
              <p class="text-xs text-sense-ink/58 dark:text-slate-200">
                {{ location.displayType }}
              </p>
            </div>
          </button>
        </div>
        <div v-else class="px-5 py-6 text-center text-sm text-sense-ink/60 dark:text-slate-400">
          No locations match "{{ modelValue }}"
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { Location } from '@/types/domain';

defineProps<{
  modelValue: string;
  results?: Location[];
  showDropdown?: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: string];
  select: [location: Location];
}>();

const isFocused = ref(false);

function onInput(event: Event) {
  emit('update:modelValue', (event.target as HTMLInputElement).value);
}

function onBlur() {
  // Small delay to allow click event on dropdown items to fire
  setTimeout(() => {
    isFocused.value = false;
  }, 150);
}

function selectLocation(location: Location) {
  emit('select', location);
  isFocused.value = false;
}

function priorityTone(score: Location['sensoryScore']): string {
  if (score <= 2) return 'bg-[#98c39b]';
  if (score === 3) return 'bg-[#d39b57]';
  return 'bg-[#cb8b79]';
}
</script>

<style scoped>
.fade-dropdown-enter-active,
.fade-dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.fade-dropdown-enter-from,
.fade-dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
