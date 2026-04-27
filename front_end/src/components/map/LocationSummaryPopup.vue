<template>
  <Transition name="map-summary">
    <aside
      v-if="location"
      class="pointer-events-auto absolute inset-x-3 top-3 z-[460] md:inset-x-auto md:left-4 md:top-4 md:w-[340px]"
      aria-label="Selected location summary"
    >
      <div class="figma-panel-soft overflow-hidden border border-white/55 bg-white/96 p-4 backdrop-blur-xl dark:border-[#6f8792] dark:bg-[#0f2029] dark:shadow-[0_22px_44px_rgba(0,0,0,0.42)]">
        <div class="flex items-start justify-between gap-3">
          <div class="min-w-0 space-y-1">
            <p class="text-[10px] font-semibold uppercase tracking-[0.18em] text-sense-teal/68 dark:text-slate-200">
              Location summary
            </p>
            <h3 class="text-[22px] font-semibold leading-[1.15] text-sense-ink dark:text-slate-50">
              {{ location.name }}
            </h3>
          </div>

          <button
            type="button"
            data-testid="summary-close"
            class="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-full border border-sense-teal/12 bg-white/80 text-sense-ink/60 transition hover:border-sense-teal/24 hover:text-sense-ink dark:border-[#6f8792] dark:bg-[#1b323e] dark:text-slate-50 dark:hover:border-slate-200 dark:hover:bg-[#263f4c] dark:hover:text-white"
            aria-label="Close summary"
            @click="$emit('close')"
          >
            <svg viewBox="0 0 16 16" fill="none" class="h-3.5 w-3.5" aria-hidden="true">
              <path d="M4 4l8 8M12 4 4 12" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
            </svg>
          </button>
        </div>

        <div class="mt-3 flex flex-wrap items-center gap-2">
          <span
            class="rounded-full border px-3 py-1 text-[11px] font-semibold"
            :class="displayTypeChipClass"
          >
            {{ location.displayType }}
          </span>
          <span
            class="rounded-full border px-3 py-1 text-[11px] font-semibold"
            :class="statusChipClass(location)"
          >
            {{ location.summaryStatusLabel ?? location.crowdSnapshot.shortLabel }}
          </span>
          <span
            v-if="location.matchesPreferences"
            class="rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-[11px] font-semibold text-emerald-700 dark:border-emerald-400/24 dark:bg-emerald-400/14 dark:text-emerald-200"
          >
            Matches your filters
          </span>
        </div>

        <p class="mt-3 text-[13px] leading-6 text-sense-ink/68 dark:text-slate-50">
          Quick backend snapshot for this map point before opening the full sensory profile.
        </p>

        <dl class="mt-4 grid grid-cols-2 gap-2.5">
          <div class="rounded-[18px] border border-sense-teal/10 bg-white/78 px-3 py-3 dark:border-[#49626e] dark:bg-[#142a35]">
            <dt class="text-[10px] font-semibold uppercase tracking-[0.12em] text-sense-ink/45 dark:text-slate-200">
              Location ID
            </dt>
            <dd class="mt-1 text-[14px] font-semibold text-sense-ink dark:text-slate-50">
              #{{ location.locationId }}
            </dd>
          </div>

          <div class="rounded-[18px] border border-sense-teal/10 bg-white/78 px-3 py-3 dark:border-[#49626e] dark:bg-[#142a35]">
            <dt class="text-[10px] font-semibold uppercase tracking-[0.12em] text-sense-ink/45 dark:text-slate-200">
              Sensory score
            </dt>
            <dd class="mt-1 text-[14px] font-semibold text-sense-ink dark:text-slate-50">
              {{ location.sensoryScore }}/4
            </dd>
          </div>

          <div class="col-span-2 rounded-[18px] border border-sense-teal/10 bg-white/78 px-3 py-3 dark:border-[#49626e] dark:bg-[#142a35]">
            <dt class="text-[10px] font-semibold uppercase tracking-[0.12em] text-sense-ink/45 dark:text-slate-200">
              Crowd level
            </dt>
            <dd class="mt-1 text-[14px] font-semibold text-sense-ink dark:text-slate-50">
              {{ toSentenceCase(location.summaryCrowdLevel ?? location.crowdSnapshot.label) }}
            </dd>
          </div>

          <div class="col-span-2 rounded-[18px] border border-sense-teal/10 bg-white/78 px-3 py-3 dark:border-[#49626e] dark:bg-[#142a35]">
            <dt class="text-[10px] font-semibold uppercase tracking-[0.12em] text-sense-ink/45 dark:text-slate-200">
              Coordinates
            </dt>
            <dd class="mt-1 text-[14px] font-semibold text-sense-ink dark:text-slate-50">
              {{ formatCoordinate(location.coordinates.lat) }}, {{ formatCoordinate(location.coordinates.lon) }}
            </dd>
          </div>
        </dl>

        <button
          type="button"
          data-testid="summary-view-profile"
          class="mt-4 inline-flex w-full items-center justify-center rounded-full bg-[#5f8b84] px-4 py-3 text-sm font-semibold text-white transition hover:bg-[#4f7771] dark:bg-sense-mint dark:text-sense-night dark:hover:bg-white"
          @click="$emit('view-profile')"
        >
          View full profile
        </button>
      </div>
    </aside>
  </Transition>
</template>

<script setup lang="ts">
import type { Location } from '@/types/domain';

defineProps<{
  location: Location | null;
}>();

defineEmits<{
  close: [];
  'view-profile': [];
}>();

const displayTypeChipClass =
  'border-sense-teal/12 bg-white text-sense-teal dark:border-[#7e98a4] dark:bg-[#203844] dark:text-slate-50';

function statusChipClass(location: Location): string {
  const crowdLevel = location.summaryCrowdLevel ?? location.crowdSnapshot.crowdLevel;

  if (crowdLevel === 'high') {
    return 'border-rose-200 bg-rose-50 text-rose-700 dark:border-[#f0a0a0] dark:bg-[#5a1f2b] dark:text-rose-50';
  }

  if (crowdLevel === 'moderate') {
    return 'border-amber-200 bg-amber-50 text-amber-700 dark:border-[#f1c96b] dark:bg-[#5a4215] dark:text-amber-50';
  }

  return 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-[#87d8a8] dark:bg-[#174633] dark:text-emerald-50';
}

function toSentenceCase(value: string): string {
  const normalized = value.trim().toLowerCase();

  return normalized ? normalized[0].toUpperCase() + normalized.slice(1) : value;
}

function formatCoordinate(value: number): string {
  return value.toFixed(4);
}
</script>

<style scoped>
.map-summary-enter-active,
.map-summary-leave-active {
  transition:
    opacity 180ms ease,
    transform 180ms ease;
}

.map-summary-enter-from,
.map-summary-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}
</style>
