<template>
  <section class="space-y-3.5">
    <div class="flex items-center justify-between gap-3">
      <div class="flex items-baseline gap-2.5">
        <p class="text-[20px] font-semibold tracking-tight text-sense-ink dark:text-white">
          Sensory Profile
        </p>
        <span v-if="lastUpdatedLabel" class="text-[12px] text-sense-ink/45 dark:text-slate-400">
          {{ lastUpdatedLabel }}
        </span>
      </div>
      <RouterLink
        to="/about"
        class="inline-flex items-center gap-1 text-[13px] font-medium text-sense-teal/78 transition hover:text-sense-teal dark:text-sense-mint/82 dark:hover:text-sense-mint"
      >
        <span>Learn more</span>
        <svg
          viewBox="0 0 16 16"
          fill="none"
          class="h-3.5 w-3.5"
          aria-hidden="true"
        >
          <path
            d="M6 3.5 10.5 8 6 12.5"
            stroke="currentColor"
            stroke-width="1.6"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </RouterLink>
    </div>

    <div class="grid gap-3 md:grid-cols-2">
      <article
        v-for="item in cards"
        :key="item.label"
        class="detail-surface detail-profile-item rounded-[12px] border border-sense-teal/10 bg-white px-4 py-4 shadow-[0_12px_24px_-20px_rgba(16,74,85,0.35)] dark:border-white/8 dark:bg-[#16252f]/90"
      >
        <div class="flex items-start gap-3">
          <div class="detail-chip flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-sense-mint/34 text-[13px] font-bold uppercase text-sense-teal dark:bg-white/10 dark:text-sense-mint">
            <component :is="item.icon" class="h-4 w-4" />
          </div>
          <div class="min-w-0 flex-1">
            <div class="flex flex-wrap items-center justify-between gap-2">
              <p class="detail-card-title text-[16px] md:text-[17px] font-semibold leading-tight text-sense-ink dark:text-white">
                {{ item.label }}
              </p>
              <span class="detail-badge rounded-md bg-emerald-100 px-2.5 py-0.5 text-[11px] font-bold uppercase tracking-[0.08em] text-emerald-800 dark:bg-emerald-500/20 dark:text-emerald-100">
                {{ item.value }}
              </span>
            </div>
            <p class="detail-copy mt-1.5 text-[14px] md:text-[15px] leading-relaxed text-sense-ink/70 dark:text-slate-100">
              {{ item.detail }}
            </p>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, FunctionalComponent, h } from 'vue';
import { RouterLink } from 'vue-router';

import type { SensoryProfile } from '@/types/domain';

const props = defineProps<{
  profile: SensoryProfile;
}>();

const SoundIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...props },
    [
      h('path', { d: 'M4 12h3l4 3V5L7 8H4Z', 'stroke-linejoin': 'round' }),
      h('path', { d: 'M14 7.5a4 4 0 0 1 0 5', 'stroke-linecap': 'round' }),
      h('path', { d: 'M15.8 5.4a6.8 6.8 0 0 1 0 9.2', 'stroke-linecap': 'round' }),
    ],
  );

const SunIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...props },
    [
      h('circle', { cx: '10', cy: '10', r: '3' }),
      h('path', { d: 'M10 2.5v2.1', 'stroke-linecap': 'round' }),
      h('path', { d: 'M10 15.4v2.1', 'stroke-linecap': 'round' }),
      h('path', { d: 'M2.5 10h2.1', 'stroke-linecap': 'round' }),
      h('path', { d: 'M15.4 10h2.1', 'stroke-linecap': 'round' }),
      h('path', { d: 'm4.8 4.8 1.5 1.5', 'stroke-linecap': 'round' }),
      h('path', { d: 'm13.7 13.7 1.5 1.5', 'stroke-linecap': 'round' }),
      h('path', { d: 'm13.7 6.3 1.5-1.5', 'stroke-linecap': 'round' }),
      h('path', { d: 'm4.8 15.2 1.5-1.5', 'stroke-linecap': 'round' }),
    ],
  );

const UnpredictabilityIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...props },
    [
      h('path', { d: 'M3 13.8 7.2 9.6l2.6 2.6 5.2-5.2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }),
      h('path', { d: 'M12 7h3v3', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }),
    ],
  );

const QuietSpacesIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...props },
    [
      h('path', { d: 'M10 17s5-4.4 5-8.2A5 5 0 0 0 5 8.8C5 12.6 10 17 10 17Z', 'stroke-linejoin': 'round' }),
      h('circle', { cx: '10', cy: '8.5', r: '1.8' }),
    ],
  );

function noiseLabel(value: SensoryProfile['noiseLevel']): string {
  return value === 'low' ? 'Quiet' : value === 'moderate' ? 'Moderate' : 'Busy';
}

function lightingLabel(value: SensoryProfile['lightingType']): string {
  if (value === 'natural') {
    return 'Natural';
  }

  if (value === 'controlled-indoor') {
    return 'Steady';
  }

  if (value === 'mixed') {
    return 'Mixed';
  }

  return 'Bright';
}

function unpredictabilityLabel(value: SensoryProfile['crowdPredictability']): string {
  return value === 'stable' ? 'Low' : value === 'variable' ? 'Moderate' : 'High';
}

const lastUpdatedLabel = computed(() => {
  const raw = props.profile.lastReviewedAt;
  if (!raw) return null;
  const date = new Date(raw);
  if (isNaN(date.getTime())) return null;
  return `Updated ${date.toLocaleDateString('en-AU', { day: 'numeric', month: 'short', year: 'numeric' })}`;
});

const cards = computed(() => [
  {
    icon: SoundIcon,
    label: 'Noise',
    value: props.profile.noiseDb != null ? `${props.profile.noiseDb} dB` : noiseLabel(props.profile.noiseLevel),
    detail: props.profile.noiseDescription ?? (props.profile.noiseLevel === 'low'
      ? 'Generally quieter and easier to regulate.'
      : props.profile.noiseLevel === 'moderate'
        ? 'Some background sound is likely through the visit.'
        : 'Sound levels can rise quickly in active periods.'),
  },
  {
    icon: SunIcon,
    label: 'Lighting',
    value: props.profile.lightingLevel ?? lightingLabel(props.profile.lightingType),
    detail: props.profile.lightingDescription ?? (props.profile.lightingType === 'natural'
      ? 'Mostly natural light with fewer harsh changes.'
      : props.profile.lightingType === 'controlled-indoor'
        ? 'Indoor lighting is relatively steady and predictable.'
        : props.profile.lightingType === 'mixed'
          ? 'Lighting conditions shift between spaces.'
          : 'Bright artificial lighting may feel intense.'),
  },
  {
    icon: UnpredictabilityIcon,
    label: 'Unpredictability',
    value: unpredictabilityLabel(props.profile.crowdPredictability),
    detail: props.profile.crowdPredictability === 'stable'
      ? 'Movement patterns are usually easy to read.'
      : props.profile.crowdPredictability === 'variable'
        ? 'Foot traffic changes through the day.'
        : 'Events can alter sensory load quickly.',
  },
  {
    icon: QuietSpacesIcon,
    label: 'Quiet Spaces',
    value: props.profile.hasQuietArea ? 'Available' : 'Limited',
    detail: props.profile.hasQuietArea
      ? 'A clearer retreat option is available nearby if needed.'
      : 'A dedicated quiet retreat is not clearly confirmed.',
  },
]);
</script>
