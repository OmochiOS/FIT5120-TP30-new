<template>
  <section class="about-page mx-auto w-full max-w-[1280px] pb-6 pt-2 dark:text-slate-100">
    <section class="border-b border-sense-teal/10 px-5 py-16 dark:border-white/8 md:px-12 md:py-20">
      <div class="mx-auto max-w-[700px] space-y-4">
        <h1 class="text-[31px] font-semibold tracking-[-0.03em] text-sense-ink dark:text-white md:text-[36px]">
          What is Autism?
        </h1>
        <p class="about-copy text-[15px] leading-8 text-sense-ink/60 dark:text-slate-100 md:text-[16px]">
          Autism is a neurodevelopmental condition that affects how people experience the world, particularly through senses, social interaction, and communication. It is not an illness. Every autistic person is unique.
        </p>
      </div>
    </section>

    <section class="border-b border-sense-teal/10 px-5 py-16 dark:border-white/8 md:px-12 md:py-16">
      <div class="mx-auto max-w-[960px] space-y-7">
        <h2 class="text-[24px] font-semibold tracking-[-0.03em] text-sense-ink dark:text-white md:text-[26px]">
          Sensory Sensitivity
        </h2>

        <div class="grid gap-5 md:grid-cols-3">
          <article
            v-for="card in sensitivityCards"
            :key="card.title"
            class="about-card rounded-[10px] border border-sense-teal/10 bg-white px-6 py-8 text-center shadow-[0_16px_36px_-28px_rgba(16,74,85,0.35)] dark:border-white/10 dark:bg-[#1a2b35]/96"
          >
            <div class="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-sense-mint/18 text-sense-teal dark:bg-white/10 dark:text-sense-mint">
              <component :is="card.icon" class="h-5 w-5" />
            </div>
            <p class="about-card-title mt-4 text-[16px] font-semibold text-sense-ink dark:text-white md:text-[17px]">
              {{ card.title }}
            </p>
            <p class="about-card-copy mx-auto mt-3 max-w-[220px] text-[13px] leading-6 text-sense-ink/56 dark:text-slate-100">
              {{ card.copy }}
            </p>
          </article>
        </div>
      </div>
    </section>

    <section class="border-b border-sense-teal/10 px-5 py-14 text-center dark:border-white/8 md:px-12 md:py-16">
      <div class="font-display text-[56px] leading-none text-sense-teal md:text-[66px]">
        1 in 40
      </div>
      <p class="about-stat-copy mt-3 text-[16px] text-sense-ink/84 dark:text-slate-50 md:text-[18px]">
        Australians are on the autism spectrum
      </p>
    </section>

    <section class="border-b border-sense-teal/10 px-5 py-14 dark:border-white/8 md:px-12 md:py-16">
      <div class="mx-auto max-w-[860px] space-y-8">
        <h2 class="text-[24px] font-semibold tracking-[-0.03em] text-sense-ink dark:text-white md:text-[26px]">
          Why It Matters
        </h2>

        <div class="grid gap-8 md:grid-cols-2">
          <article class="space-y-2">
            <div class="font-display text-[42px] leading-none text-[#ca7d6c] md:text-[50px]">
              51%
            </div>
            <p class="about-support-copy max-w-[280px] text-[15px] leading-7 text-sense-ink/82 dark:text-slate-50 md:text-[16px]">
              of autistic young adults avoid social situations due to sensory barriers
            </p>
          </article>

          <article class="space-y-2">
            <div class="font-display text-[42px] leading-none text-sense-teal md:text-[50px]">
              94%
            </div>
            <p class="about-support-copy max-w-[300px] text-[15px] leading-7 text-sense-ink/82 dark:text-slate-50 md:text-[16px]">
              of autistic individuals have extreme sensitivity in at least one sense
            </p>
          </article>
        </div>
      </div>
    </section>

    <section class="border-b border-sense-teal/10 px-5 py-14 dark:border-white/8 md:px-12 md:py-16">
      <div class="mx-auto max-w-[860px] space-y-7">
        <h2 class="text-[24px] font-semibold tracking-[-0.03em] text-sense-ink dark:text-white md:text-[26px]">
          The Accessibility Gap
        </h2>

        <div class="grid gap-4 md:grid-cols-2">
          <article class="about-gap-card about-gap-card--positive rounded-[12px] border border-[#9fe1cf] bg-[#effbf6] px-5 py-5 dark:border-emerald-300/22 dark:bg-emerald-500/14">
            <p class="about-gap-title about-gap-title--positive text-[13px] font-semibold uppercase tracking-[0.18em] text-sense-teal dark:text-emerald-100">
              Physical Access
            </p>
            <ul class="mt-4 space-y-2.5">
              <li
                v-for="item in physicalAccessItems"
                :key="item"
                class="about-gap-copy flex items-start gap-3 text-[14px] leading-6 text-sense-ink/82 dark:text-slate-50"
              >
                <CheckIcon class="about-gap-icon about-gap-icon--positive mt-[3px] h-4 w-4 shrink-0 text-sense-teal dark:text-emerald-200" />
                <span>{{ item }}</span>
              </li>
            </ul>
          </article>

          <article class="about-gap-card about-gap-card--negative rounded-[12px] border border-[#f2c8ce] bg-[#fff2f4] px-5 py-5 dark:border-rose-300/22 dark:bg-rose-500/14">
            <p class="about-gap-title about-gap-title--negative text-[13px] font-semibold uppercase tracking-[0.18em] text-[#b64b5c] dark:text-rose-100">
              Sensory Access
            </p>
            <ul class="mt-4 space-y-2.5">
              <li
                v-for="item in sensoryAccessItems"
                :key="item"
                class="about-gap-copy flex items-start gap-3 text-[14px] leading-6 text-sense-ink/82 dark:text-slate-50"
              >
                <CrossIcon class="about-gap-icon about-gap-icon--negative mt-[3px] h-4 w-4 shrink-0 text-[#b64b5c] dark:text-rose-200" />
                <span>{{ item }}</span>
              </li>
            </ul>
          </article>
        </div>
      </div>
    </section>

    <section class="px-5 py-16 md:px-12 md:py-18">
      <div class="mx-auto flex max-w-[760px] flex-col items-center gap-7 text-center">
        <div class="space-y-4">
          <h2 class="text-[24px] font-semibold tracking-[-0.03em] text-sense-ink dark:text-white md:text-[26px]">
            How You Can Help
          </h2>
          <div class="space-y-3">
            <p
              v-for="tip in helpTips"
              :key="tip"
              class="about-copy text-[15px] leading-7 text-sense-ink/64 dark:text-slate-100 md:text-[16px]"
            >
              {{ tip }}
            </p>
          </div>
        </div>

        <RouterLink
          to="/"
          class="inline-flex items-center rounded-full bg-sense-teal px-7 py-3 text-[13px] font-semibold text-white transition hover:bg-sense-ink dark:bg-sense-mint dark:text-sense-night dark:hover:bg-white"
        >
          Explore the Sensory Map
        </RouterLink>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { FunctionalComponent, h } from 'vue';
import { RouterLink } from 'vue-router';

const EarIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...props },
    [
      h('path', { d: 'M11.6 4.2a4 4 0 1 0-5.1 6.1c1 .8 1.7 1.5 1.7 2.8 0 1.5 1.1 2.7 2.6 2.7 1.6 0 2.8-1.2 2.8-2.8' }),
      h('path', { d: 'M10 8.2a1.8 1.8 0 0 1 1.8 1.8c0 1.2-.9 1.7-1.7 2.2' }),
    ],
  );

const EyeIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...props },
    [
      h('path', { d: 'M2 10s2.8-4 8-4 8 4 8 4-2.8 4-8 4-8-4-8-4Z' }),
      h('circle', { cx: '10', cy: '10', r: '2.2' }),
    ],
  );

const CrowdIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...props },
    [
      h('circle', { cx: '7', cy: '8', r: '2.2' }),
      h('circle', { cx: '13.5', cy: '7.5', r: '1.8' }),
      h('path', { d: 'M3.8 15c.6-2.1 2.2-3.3 4.3-3.3s3.7 1.2 4.3 3.3' }),
      h('path', { d: 'M11.6 14.6c.4-1.5 1.6-2.5 3.2-2.5 1.2 0 2.1.5 2.8 1.7' }),
    ],
  );

const CheckIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props },
    [h('path', { d: 'M4.5 10.5 8 14l7.5-8' })],
  );

const CrossIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props },
    [
      h('path', { d: 'M6 6l8 8' }),
      h('path', { d: 'M14 6l-8 8' }),
    ],
  );

const sensitivityCards = [
  {
    title: 'Sound',
    copy: 'Everyday noise like traffic or chatter can feel overwhelming',
    icon: EarIcon,
  },
  {
    title: 'Light',
    copy: 'Bright or flickering lights may cause discomfort or pain',
    icon: EyeIcon,
  },
  {
    title: 'Crowds',
    copy: 'Unpredictable movement in busy spaces creates anxiety',
    icon: CrowdIcon,
  },
];

const physicalAccessItems = [
  'Mapped and widely available',
  'Mandated by law',
  'Visible and standardized',
];

const sensoryAccessItems = [
  'Unmapped and hard to find',
  'No legal requirements',
  'Invisible and unpredictable',
];

const helpTips = [
  'Learn about sensory needs before planning outings together',
  'Choose quieter times and familiar spaces to build confidence',
  'Be patient - small steps toward participation matter',
];
</script>
