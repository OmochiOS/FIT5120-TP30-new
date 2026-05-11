<template>
  <section class="home-page page-intro w-full pb-10 pt-4 md:pb-16 md:pt-6">
    <div class="mx-auto flex w-full max-w-[1128px] flex-col gap-9 md:gap-12">
      <section class="grid min-h-[440px] gap-4 lg:grid-cols-[1fr_1.05fr] md:gap-5">
        <div class="home-hero-panel flex flex-col justify-center rounded-[32px] border border-[#e4e0d8] bg-white px-7 py-9 shadow-[0_18px_42px_rgba(76,88,80,0.08)] md:rounded-[36px] md:px-12 md:py-12 lg:px-14 dark:border-white/10 dark:bg-[#11222b]">
          <div class="max-w-[410px] space-y-6 md:space-y-7">
            <h1 class="home-hero-wordmark font-home-sans text-[2.8rem] font-bold leading-[0.9] tracking-[-0.082em] text-[#2f3135] sm:text-[3.6rem] md:text-[5.55rem] dark:text-[#e8f0ec]">
              AmitySpace
            </h1>

            <p class="font-home-sans max-w-[382px] text-[1.16rem] font-semibold leading-[1.56] tracking-[-0.03em] text-[#383d42] md:text-[1.34rem] dark:text-[#c8d8d0]">
              We help you find public spaces that feel calmer, safer, and easier to enjoy.
            </p>

            <RouterLink
              to="/map?guide=1"
              class="font-home-sans inline-flex min-h-[50px] w-max items-center rounded-full bg-[#accfa8] px-8 py-3 text-[0.98rem] font-semibold tracking-[-0.02em] text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.22),0_10px_18px_rgba(132,169,128,0.24)] transition duration-200 hover:bg-[#94bc8e] dark:bg-[#5a8c74] dark:text-[#f0faf5] dark:hover:bg-[#4e7d68]"
            >
              Get Started
            </RouterLink>
          </div>
        </div>

        <div class="overflow-hidden rounded-[32px] bg-[#d7e4dd] md:rounded-[36px] dark:bg-white/5">
          <img
            src="/landing-hero-reference.jpg"
            alt="Two people sitting and talking in a calm cafe setting."
            class="h-full min-h-[220px] sm:min-h-[280px] w-full object-cover object-center"
            loading="eager"
          />
        </div>
      </section>

      <section
        class="rounded-[32px] border border-[#e8e4db] bg-[#faf8f5] px-4 py-7 shadow-sm md:px-8 md:py-10 dark:border-white/5 dark:bg-[#15262a]"
      >
        <div class="mx-auto max-w-[780px] text-center">
          <h2 class="font-home-serif text-[1.85rem] leading-[1.08] tracking-[-0.04em] text-[#1f4f56] sm:text-[2.28rem] md:text-[3.35rem] dark:text-[#b8d8d0]">
            Three barriers we often cannot see
          </h2>
          <p
            class="font-home-sans mx-auto mt-5 max-w-[760px] text-[1.16rem] leading-[1.6] tracking-[-0.03em] text-[#717271] md:text-[1.34rem] dark:text-[#8aaba2]"
          >
            Some spaces look accessible, but may still feel overwhelming because of sound, light, or crowd movement.
          </p>
        </div>

        <div class="mt-9 grid grid-cols-1 gap-4 sm:grid-cols-3 sm:gap-3 md:mt-11 md:gap-8">
          <button
            v-for="card in barrierCards"
            :key="card.id"
            type="button"
            class="home-flip-card group"
            :class="{ 'is-flipped': flippedCards[card.id] }"
            :style="card.palette"
            :aria-pressed="flippedCards[card.id]"
            :aria-label="`${card.title} card`"
            @click="toggleCard(card.id)"
          >
            <div class="home-flip-card__inner">
              <div class="home-flip-card__face home-flip-card__face--front">
                <div class="home-flip-card__corner" aria-hidden="true">
                  <RefreshIcon class="h-4 w-4" />
                </div>
                <component
                  :is="card.icon"
                  class="home-flip-card__icon"
                  :style="card.frontIconStyle"
                  aria-hidden="true"
                />
                <span class="home-flip-card__title">
                  {{ card.title }}
                </span>
              </div>

              <div class="home-flip-card__face home-flip-card__face--back">
                <div class="home-flip-card__corner" aria-hidden="true">
                  <RefreshIcon class="h-4 w-4" />
                </div>
                <p class="home-flip-card__back-text">{{ card.backText }}</p>
                <component
                  :is="card.backIcon"
                  class="home-flip-card__back-icon"
                  :style="card.backIconStyle"
                  aria-hidden="true"
                />
              </div>
            </div>
          </button>
        </div>

        <div
          class="font-home-sans mt-8 flex items-center justify-center gap-2 text-[0.98rem] tracking-[-0.02em] text-[#767676] md:text-[1.08rem] dark:text-[#8aaba2]"
        >
          <InfoIcon class="h-4 w-4 shrink-0 text-[#7a7d7a] dark:text-[#8aaba2]" aria-hidden="true" />
          <span>Tap a card to flip and learn more.</span>
        </div>
      </section>

      <section
        class="rounded-[32px] bg-[#eef6f1] px-4 py-7 md:px-8 md:py-11 dark:bg-[#1a2e28]"
      >
        <div class="mx-auto max-w-[760px] text-center">
          <h2 class="font-home-serif text-[1.85rem] leading-[1.08] tracking-[-0.04em] text-[#1f4f56] sm:text-[2.18rem] md:text-[3.15rem] dark:text-[#a8d4c8]">
            Why this matters
          </h2>
          <p
            class="font-home-sans mx-auto mt-5 max-w-[720px] text-[1.12rem] leading-[1.58] tracking-[-0.03em] text-[#6d7571] md:text-[1.3rem] dark:text-[#8ab0a8]"
          >
            Sensory barriers can make everyday outings harder to plan, harder to enjoy, and easier to avoid.
          </p>
        </div>

        <div class="mt-9 grid gap-5 md:mt-10 md:grid-cols-2 md:gap-8">
          <article
            v-for="card in statCards"
            :key="card.id"
            class="home-stat-card rounded-[30px] border-2 px-6 py-6 md:px-8 md:py-7"
            :style="card.palette"
          >
            <div class="flex items-start justify-between gap-4">
              <p class="home-stat-card__value font-home-serif text-[4rem] leading-none tracking-[-0.055em] md:text-[5.2rem]">
                {{ card.stat }}
              </p>
              <img
                :src="card.imageSrc"
                :alt="card.imageAlt"
                class="home-stat-card__image"
                :style="card.imageStyle"
              />
            </div>
            <p class="home-stat-card__copy font-home-sans mt-4 text-[1.18rem] leading-[1.42] tracking-[-0.03em] md:text-[1.38rem]">
              {{ card.copy }}
            </p>

            <div class="home-stat-card__citation-shell" data-citation-shell="true">
              <div
                v-if="openCitationCard === card.id"
                class="home-stat-card__tooltip"
                :class="{
                  'home-stat-card__tooltip--stacked': card.citations.length > 1,
                  'home-stat-card__tooltip--dragging': draggingCitationCard === card.id,
                }"
                :style="getCitationTooltipStyle(card.id)"
                :data-testid="`citation-popup-${card.id}`"
                :aria-label="`References for the ${card.stat} statistic`"
                role="dialog"
                aria-modal="false"
                @mousedown="startCitationDrag(card.id, $event)"
              >
                <a
                  v-for="(citation, index) in card.citations"
                  :key="citation.href"
                  class="home-stat-card__tooltip-link"
                  :href="citation.href"
                  target="_blank"
                  rel="noopener noreferrer"
                  :data-testid="`citation-link-${card.id}-${index + 1}`"
                >
                  {{ citation.label }}
                </a>
              </div>

              <button
                type="button"
                class="home-stat-card__citation-button"
                :aria-expanded="openCitationCard === card.id"
                :aria-label="card.citationButtonLabel"
                :data-testid="`citation-button-${card.id}`"
                @click.stop="toggleCitation(card.id)"
              >
                <span class="home-stat-card__citation-mark" aria-hidden="true">?</span>
              </button>
            </div>
          </article>
        </div>

        <div class="mt-8 flex items-center justify-center gap-4 text-center md:mt-9">
          <div class="flex shrink-0 items-end gap-1" aria-hidden="true">
            <img src="/heartleft.png" alt="" class="h-[52px] w-auto" />
            <img src="/heartright.png" alt="" class="h-[38px] w-auto" />
          </div>
          <p
            class="font-home-sans max-w-[640px] text-[1.16rem] font-medium leading-[1.58] tracking-[-0.03em] text-[#2c635d] md:text-[1.4rem] dark:text-[#7dbdb0]"
          >
            AmitySpace helps make these hidden barriers easier to understand before people leave home.
          </p>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { FunctionalComponent, h, onBeforeUnmount, onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';

type BarrierCardId = 'sound' | 'light' | 'crowds';
type StatCardId = 'participation' | 'sensitivity';

type BarrierCard = {
  id: BarrierCardId;
  title: string;
  icon: FunctionalComponent;
  backIcon: FunctionalComponent;
  backText: string;
  frontIconStyle: Record<string, string>;
  backIconStyle: Record<string, string>;
  palette: Record<string, string>;
};

type CitationLink = {
  label: string;
  href: string;
};

type StatCard = {
  id: StatCardId;
  stat: string;
  copy: string;
  imageSrc: string;
  imageAlt: string;
  imageStyle: Record<string, string>;
  citationButtonLabel: string;
  citations: CitationLink[];
  palette: Record<string, string>;
};

type TooltipOffset = {
  x: number;
  y: number;
};

type ActiveCitationDrag = {
  cardId: StatCardId;
  startX: number;
  startY: number;
  baseX: number;
  baseY: number;
};

const SoundIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 88 88', fill: 'none', ...props },
    [
      h('path', {
        d: 'M22 49.5h13.3L50 63V24L35.3 36.8H22V49.5Z',
        stroke: 'currentColor',
        'stroke-width': '4.2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
      h('path', {
        d: 'M58 33.5c5.3 5.1 5.3 15.9 0 21',
        stroke: 'currentColor',
        'stroke-width': '4.2',
        'stroke-linecap': 'round',
      }),
      h('path', {
        d: 'M69 23.5c9.4 8.9 9.4 32.1 0 41',
        stroke: 'currentColor',
        'stroke-width': '4.2',
        'stroke-linecap': 'round',
      }),
    ],
  );

const LightIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 88 88', fill: 'none', ...props },
    [
      h('path', {
        d: 'M44 15c-13 0-22.8 9-22.8 21.8 0 8.5 4.3 13.6 9.9 18.5 2.8 2.4 4.1 4.8 4.4 8h16.9c.3-3.2 1.6-5.6 4.4-8 5.6-4.9 9.9-10 9.9-18.5C66.8 24 57 15 44 15Z',
        stroke: 'currentColor',
        'stroke-width': '4.2',
        'stroke-linejoin': 'round',
      }),
      h('path', {
        d: 'M35 66.5h18',
        stroke: 'currentColor',
        'stroke-width': '4.2',
        'stroke-linecap': 'round',
      }),
      h('path', {
        d: 'M37.5 74h13',
        stroke: 'currentColor',
        'stroke-width': '4.2',
        'stroke-linecap': 'round',
      }),
    ],
  );

const CrowdsIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 88 88', fill: 'none', ...props },
    [
      h('circle', {
        cx: '44',
        cy: '28',
        r: '12.5',
        stroke: 'currentColor',
        'stroke-width': '4.2',
      }),
      h('path', {
        d: 'M22 66c3.4-13 12.3-19 22-19s18.6 6 22 19',
        stroke: 'currentColor',
        'stroke-width': '4.2',
        'stroke-linecap': 'round',
      }),
    ],
  );

const RefreshIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 20 20', fill: 'none', ...props },
    [
      h('path', {
        d: 'M4 7.2A5.9 5.9 0 0 1 14.2 5.5',
        stroke: 'currentColor',
        'stroke-width': '1.75',
        'stroke-linecap': 'round',
      }),
      h('path', {
        d: 'M14.1 3.3v4h-4',
        stroke: 'currentColor',
        'stroke-width': '1.75',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
      h('path', {
        d: 'M16 12.8A5.9 5.9 0 0 1 5.8 14.5',
        stroke: 'currentColor',
        'stroke-width': '1.75',
        'stroke-linecap': 'round',
      }),
      h('path', {
        d: 'M5.9 16.7v-4h4',
        stroke: 'currentColor',
        'stroke-width': '1.75',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
    ],
  );

const InfoIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 20 20', fill: 'none', ...props },
    [
      h('circle', {
        cx: '10',
        cy: '10',
        r: '8.2',
        stroke: 'currentColor',
        'stroke-width': '1.8',
      }),
      h('path', {
        d: 'M10 8.2v4.6',
        stroke: 'currentColor',
        'stroke-width': '1.8',
        'stroke-linecap': 'round',
      }),
      h('circle', {
        cx: '10',
        cy: '5.8',
        r: '0.9',
        fill: 'currentColor',
      }),
    ],
  );

const SeatedFigureIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 88 88', fill: 'none', ...props },
    [
      h('circle', { cx: '44', cy: '22', r: '9', stroke: 'currentColor', 'stroke-width': '2.4' }),
      h('path', {
        d: 'M24 68c0-14 8-24 20-24s20 10 20 24',
        stroke: 'currentColor',
        'stroke-width': '2.4',
        'stroke-linecap': 'round',
      }),
      h('path', {
        d: 'M32 52c-4 4-6 9-6 16',
        stroke: 'currentColor',
        'stroke-width': '2.4',
        'stroke-linecap': 'round',
      }),
      h('path', {
        d: 'M32 68h20',
        stroke: 'currentColor',
        'stroke-width': '2.4',
        'stroke-linecap': 'round',
      }),
    ],
  );

const MindProfileIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 88 88', fill: 'none', ...props },
    [
      h('path', {
        d: 'M38 22c-10 2-17 11-17 22 0 8 4 15 10 19v8h22v-8c6-4 10-11 10-19 0-13-10-23-22-23-1 0-2 0-3 1Z',
        stroke: 'currentColor',
        'stroke-width': '2.4',
        'stroke-linejoin': 'round',
      }),
      h('path', { d: 'M30 34 c2-1 3 1 5 0 s3-2 5 0', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round' }),
      h('path', { d: 'M28 42 c2-1 4 1 6 0 s4-2 6 0 s3 1 5 0', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round' }),
      h('path', { d: 'M30 50 c2-1 3 1 5 0 s3-2 5 0', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round' }),
    ],
  );



const barrierCards: BarrierCard[] = [
  {
    id: 'sound',
    title: 'Sound',
    icon: SoundIcon,
    backIcon: SoundIcon,
    backText: 'Everyday noise like traffic or chatter can feel overwhelming.',
    frontIconStyle: {
      width: '140px',
      height: '116px',
    },
    backIconStyle: {
      width: '52px',
      height: '52px',
    },
    palette: {
      '--home-card-surface': '#eef4ef',
      '--home-card-accent': '#669181',
      '--home-card-border': 'rgba(106, 145, 129, 0.26)',
      '--home-card-shadow': 'rgba(92, 122, 108, 0.18)',
    },
  },
  {
    id: 'light',
    title: 'Light',
    icon: LightIcon,
    backIcon: LightIcon,
    backText: 'Bright or flickering lights may cause discomfort or pain.',
    frontIconStyle: {
      width: '92px',
      height: '136px',
    },
    backIconStyle: {
      width: '52px',
      height: '52px',
    },
    palette: {
      '--home-card-surface': '#fcf5e5',
      '--home-card-accent': '#cb9a28',
      '--home-card-border': 'rgba(208, 168, 73, 0.24)',
      '--home-card-shadow': 'rgba(196, 166, 95, 0.16)',
    },
  },
  {
    id: 'crowds',
    title: 'Crowds',
    icon: CrowdsIcon,
    backIcon: CrowdsIcon,
    backText: 'Unpredictable movement in busy spaces creates anxiety.',
    frontIconStyle: {
      width: '108px',
      height: '102px',
    },
    backIconStyle: {
      width: '52px',
      height: '52px',
    },
    palette: {
      '--home-card-surface': '#f4eff9',
      '--home-card-accent': '#7d69b0',
      '--home-card-border': 'rgba(143, 123, 189, 0.24)',
      '--home-card-shadow': 'rgba(146, 124, 189, 0.16)',
    },
  },
];

const flippedCards = ref<Record<BarrierCardId, boolean>>({
  sound: false,
  light: false,
  crowds: false,
});

const statCards: StatCard[] = [
  {
    id: 'participation',
    stat: '25%',
    copy: 'of autistic young adults are completely socially isolated.',
    imageSrc: '/sitting_icon.png',
    imageAlt: 'Person sitting alone',
    imageStyle: {
      width: '100px',
      height: '100px',
      objectFit: 'contain',
      flexShrink: '0',
    },
    citationButtonLabel: 'Show citation for the 25 percent statistic',
    citations: [
      {
        label: '[1] Orsmond et al. (2013)',
        href: 'https://link.springer.com/article/10.1007/s10803-013-1833-8',
      },
    ],
    palette: {
      '--stat-card-surface': 'rgba(203, 217, 202, 0.55)',
      '--stat-card-border': '#A9C2A8',
      '--stat-card-value': '#26524B',
      '--stat-card-copy': '#25514A',
      '--stat-card-question-surface': '#eef5ef',
      '--stat-card-question-border': '#7AA08C',
      '--stat-card-question-text': '#26524B',
      '--stat-card-tooltip-surface': '#EEEFEA',
      '--stat-card-tooltip-border': '#7AA08C',
      '--stat-card-tooltip-text': '#26524B',
      '--stat-card-tooltip-width': '235px',
    },
  },
  {
    id: 'sensitivity',
    stat: '94%',
    copy: 'of autistic individuals have extreme sensitivity in at least one sense.',
    imageSrc: '/head_icon.png',
    imageAlt: 'Head with sensory waves',
    imageStyle: {
      width: '100px',
      height: '100px',
      objectFit: 'contain',
      flexShrink: '0',
    },
    citationButtonLabel: 'Show citations for the 94 percent statistic',
    citations: [
      {
        label: '[1] Crane et al. (2009)',
        href: 'https://journals.sagepub.com/doi/10.1177/1362361309103794',
      },
      {
        label: '[2] Kirby et al. (2022)',
        href: 'https://onlinelibrary.wiley.com/doi/10.1002/aur.2670',
      },
    ],
    palette: {
      '--stat-card-surface': '#EEECEF',
      '--stat-card-border': '#D5CBDC',
      '--stat-card-value': '#4E3F72',
      '--stat-card-copy': '#534375',
      '--stat-card-question-surface': '#f4eef9',
      '--stat-card-question-border': '#9B86BE',
      '--stat-card-question-text': '#4E3F72',
      '--stat-card-tooltip-surface': '#F3EEF9',
      '--stat-card-tooltip-border': '#9B86BE',
      '--stat-card-tooltip-text': '#4E3F72',
      '--stat-card-tooltip-width': '292px',
    },
  },
];

const openCitationCard = ref<StatCardId | null>(null);
const draggingCitationCard = ref<StatCardId | null>(null);
const citationTooltipOffsets = ref<Record<StatCardId, TooltipOffset>>({
  participation: { x: 0, y: 0 },
  sensitivity: { x: 0, y: 0 },
});
let activeCitationDrag: ActiveCitationDrag | null = null;

void SeatedFigureIcon;
void MindProfileIcon;

function toggleCard(id: BarrierCardId) {
  flippedCards.value[id] = !flippedCards.value[id];
}

function toggleCitation(id: StatCardId) {
  openCitationCard.value = openCitationCard.value === id ? null : id;
}

function getCitationTooltipStyle(id: StatCardId) {
  const offset = citationTooltipOffsets.value[id];
  return {
    transform: `translate(${offset.x}px, ${offset.y}px)`,
  };
}

function startCitationDrag(id: StatCardId, event: MouseEvent) {
  const target = event.target;
  if (target instanceof Element && target.closest('a')) {
    return;
  }

  const offset = citationTooltipOffsets.value[id];
  activeCitationDrag = {
    cardId: id,
    startX: event.clientX,
    startY: event.clientY,
    baseX: offset.x,
    baseY: offset.y,
  };
  draggingCitationCard.value = id;
  event.preventDefault();
}

function handleCitationDragMove(event: MouseEvent) {
  if (!activeCitationDrag) {
    return;
  }

  citationTooltipOffsets.value[activeCitationDrag.cardId] = {
    x: activeCitationDrag.baseX + (event.clientX - activeCitationDrag.startX),
    y: activeCitationDrag.baseY + (event.clientY - activeCitationDrag.startY),
  };
}

function stopCitationDrag() {
  activeCitationDrag = null;
  draggingCitationCard.value = null;
}

function handleCitationMouseDown(event: MouseEvent) {
  if (!openCitationCard.value) {
    return;
  }

  const target = event.target;
  if (target instanceof Element && target.closest('[data-citation-shell="true"]')) {
    return;
  }

  openCitationCard.value = null;
}

function handleCitationEscape(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    openCitationCard.value = null;
    stopCitationDrag();
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handleCitationMouseDown);
  document.addEventListener('mousemove', handleCitationDragMove);
  document.addEventListener('mouseup', stopCitationDrag);
  document.addEventListener('keydown', handleCitationEscape);
});

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleCitationMouseDown);
  document.removeEventListener('mousemove', handleCitationDragMove);
  document.removeEventListener('mouseup', stopCitationDrag);
  document.removeEventListener('keydown', handleCitationEscape);
});
</script>

<style scoped>
.font-home-serif {
  font-family: "Iowan Old Style", "Palatino Linotype", "Book Antiqua", serif;
}

.font-home-sans {
  font-family: Aptos, "Avenir Next", "Trebuchet MS", "Segoe UI", sans-serif;
}

.home-hero-wordmark {
  text-wrap: balance;
}

.home-flip-card {
  position: relative;
  perspective: 1000px;
  height: auto;
  max-width: 320px;
  justify-self: center;
  width: 100%;
  flex-shrink: 0;
  border-radius: 28px;
  overflow: visible;
  cursor: pointer;
  background: transparent;
  border: none;
  padding: 0;
  transition: none;
}

@media (min-width: 768px) {
  .home-flip-card {
    perspective: 1200px;
    width: min(100%, 267px);
    max-width: none;
    justify-self: center;
  }
}

.home-flip-card__inner {
  position: relative;
  width: 100%;
  aspect-ratio: 267 / 286;
}

.home-flip-card__face {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: var(--home-card-accent);
  border-radius: 38px;
  border: 1px solid rgba(17, 67, 72, 0.05);
  background: var(--home-card-surface);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.16);
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
  overflow: hidden;
  padding: 1.2rem 1.2rem 1.15rem;
  transition:
    transform 560ms cubic-bezier(0.22, 0.61, 0.36, 1),
    opacity 220ms ease,
    filter 180ms ease;
}

.home-flip-card:hover .home-flip-card__face,
.home-flip-card:focus-visible .home-flip-card__face {
  filter: brightness(0.99);
}

.home-flip-card__face--front {
  opacity: 1;
  transform: rotateY(0deg);
  justify-content: flex-start;
}

.home-flip-card__face--back {
  opacity: 0;
  transform: rotateY(-180deg);
  justify-content: flex-start;
}

.home-flip-card.is-flipped .home-flip-card__face--front {
  opacity: 0;
  transform: rotateY(180deg);
}

.home-flip-card.is-flipped .home-flip-card__face--back {
  opacity: 1;
  transform: rotateY(0deg);
}

.home-flip-card__corner {
  position: absolute;
  right: 1.05rem;
  top: 1rem;
  color: var(--home-card-accent);
  opacity: 0.92;
}

.home-flip-card__icon {
  flex-shrink: 0;
  margin-top: 1.9rem;
}

.home-flip-card__title {
  margin-top: auto;
  width: 164px;
  padding-bottom: 1rem;
  font-family: Lato, Aptos, "Avenir Next", "Trebuchet MS", "Segoe UI", sans-serif;
  font-size: 2.8rem;
  font-weight: 700;
  letter-spacing: -0.04em;
  line-height: 1.2;
  text-align: center;
}

.home-flip-card__back-text {
  margin-top: 2.4rem;
  width: 80%;
  font-family: Lato, Aptos, "Avenir Next", "Trebuchet MS", "Segoe UI", sans-serif;
  font-size: 1.125rem;
  font-weight: 500;
  line-height: 28px;
  letter-spacing: -0.01em;
  text-align: center;
  color: var(--home-card-accent);
}

.home-flip-card__back-icon {
  margin-top: auto;
  margin-bottom: 0.5rem;
  flex-shrink: 0;
}

.home-stat-card {
  position: relative;
  min-height: 100%;
  border-color: var(--stat-card-border);
  background: var(--stat-card-surface);
  padding-bottom: 3.5rem;
}

.home-stat-card__value {
  color: var(--stat-card-value);
}

.home-stat-card__copy {
  padding-right: 2rem;
  color: var(--stat-card-copy);
}

.home-stat-card__image {
  flex-shrink: 0;
}

.home-stat-card__citation-shell {
  position: absolute;
  right: 1rem;
  bottom: 0.95rem;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.75rem;
}

.home-stat-card__citation-button {
  display: inline-flex;
  height: 1.65rem;
  width: 1.65rem;
  align-items: center;
  justify-content: center;
  border-radius: 9999px;
  border: 2px solid var(--stat-card-question-border);
  background: var(--stat-card-question-surface);
  color: var(--stat-card-question-text);
  box-shadow: 0 3px 10px rgba(29, 37, 33, 0.12);
  transition:
    transform 140ms ease,
    box-shadow 140ms ease,
    opacity 140ms ease;
}

.home-stat-card__citation-button:hover,
.home-stat-card__citation-button:focus-visible {
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(29, 37, 33, 0.14);
}

.home-stat-card__citation-button:focus-visible {
  outline: 2px solid color-mix(in srgb, var(--stat-card-question-border) 70%, white);
  outline-offset: 2px;
}

.home-stat-card__citation-mark {
  font-family: Aptos, "Avenir Next", "Trebuchet MS", "Segoe UI", sans-serif;
  font-size: 0.95rem;
  font-weight: 700;
  line-height: 1;
}

.home-stat-card__tooltip {
  position: relative;
  display: flex;
  width: min(calc(100vw - 4rem), var(--stat-card-tooltip-width));
  align-items: center;
  justify-content: center;
  gap: 0.55rem;
  border: 3px solid var(--stat-card-tooltip-border);
  border-radius: 16px;
  background: var(--stat-card-tooltip-surface);
  padding: 0.95rem 1.15rem 0.85rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.16);
  cursor: grab;
}

.home-stat-card__tooltip::before {
  content: "";
  position: absolute;
  left: 50%;
  bottom: calc(100% - 1px);
  height: 0.95rem;
  width: 1.8rem;
  transform: translateX(-50%);
  background: var(--stat-card-tooltip-border);
  clip-path: polygon(50% 0, 100% 100%, 0 100%);
}

.home-stat-card__tooltip::after {
  content: "";
  position: absolute;
  left: 50%;
  bottom: calc(100% - 1px);
  height: 0.7rem;
  width: 1.25rem;
  transform: translateX(-50%) translateY(3px);
  background: var(--stat-card-tooltip-surface);
  clip-path: polygon(50% 0, 100% 100%, 0 100%);
}

.home-stat-card__tooltip--stacked {
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-start;
  gap: 0.45rem;
}

.home-stat-card__tooltip--dragging {
  cursor: grabbing;
}

.home-stat-card__tooltip-link {
  display: inline-block;
  color: var(--stat-card-tooltip-text);
  font-family: Aptos, "Avenir Next", "Trebuchet MS", "Segoe UI", sans-serif;
  font-size: 1.02rem;
  line-height: 1.3;
  text-decoration: underline;
  text-decoration-thickness: 1.5px;
  text-underline-offset: 0.2em;
  white-space: nowrap;
}

.home-stat-card__tooltip--stacked .home-stat-card__tooltip-link {
  width: 100%;
}

.home-stat-card__tooltip-link:hover,
.home-stat-card__tooltip-link:focus-visible {
  opacity: 0.8;
}

/* ─── Dark Mode ─────────────────────────────────── */

.dark .home-page .home-flip-card:nth-child(1) {
  --home-card-surface: #192b27;
  --home-card-accent: #86bfa9;
  --home-card-border: rgba(134, 191, 169, 0.2);
}

.dark .home-page .home-flip-card:nth-child(2) {
  --home-card-surface: #2f2514;
  --home-card-accent: #e1b446;
  --home-card-border: rgba(225, 180, 70, 0.2);
}

.dark .home-page .home-flip-card:nth-child(3) {
  --home-card-surface: #252033;
  --home-card-accent: #ab95e8;
  --home-card-border: rgba(171, 149, 232, 0.2);
}

.dark .home-page .home-flip-card__face {
  filter: none;
  background: var(--home-card-surface);
  border-color: var(--home-card-border);
  color: var(--home-card-accent);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.32);
}

.dark .home-page .home-flip-card:hover .home-flip-card__face,
.dark .home-page .home-flip-card:focus-visible .home-flip-card__face {
  filter: brightness(1.15);
}

.dark .home-page .home-flip-card__title {
  color: inherit;
}

/* Stat cards */
.dark .home-page article.home-stat-card:nth-of-type(1) {
  --stat-card-surface: rgba(80, 120, 96, 0.32);
  --stat-card-border: rgba(120, 170, 130, 0.35);
  --stat-card-value: #a8d4c0;
  --stat-card-copy: #a8d4c0;
  --stat-card-question-surface: #1a3025;
  --stat-card-question-border: #4d7a60;
  --stat-card-question-text: #a8d4c0;
  --stat-card-tooltip-surface: #15261d;
  --stat-card-tooltip-border: #4d7a60;
  --stat-card-tooltip-text: #a8d4c0;
}

.dark .home-page article.home-stat-card:nth-of-type(2) {
  --stat-card-surface: rgba(90, 70, 130, 0.32);
  --stat-card-border: rgba(150, 130, 190, 0.35);
  --stat-card-value: #c0aee0;
  --stat-card-copy: #c0aee0;
  --stat-card-question-surface: #2b1f3d;
  --stat-card-question-border: #6a5391;
  --stat-card-question-text: #c0aee0;
  --stat-card-tooltip-surface: #211630;
  --stat-card-tooltip-border: #6a5391;
  --stat-card-tooltip-text: #c0aee0;
}

.dark .home-page .home-stat-card {
  background: var(--stat-card-surface) !important;
  border-color: var(--stat-card-border) !important;
}

.dark .home-page .home-stat-card__citation-button {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.28);
}

.dark .home-page .home-stat-card__tooltip {
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.28);
}

@media (min-width: 768px) {
  .home-flip-card {
    width: min(100%, 267px);
  }

  .home-flip-card__title {
    font-size: 2.5rem;
  }
}

@media (max-width: 767px) {
  /* Hero panel: tighten up padding on small phones */
  .home-hero-panel {
    padding: 1.75rem 1.25rem;
  }

  /* Flip cards: now 1-col full-width on mobile — restore normal sizes */
  .home-flip-card__title {
    font-size: 2.8rem;
    line-height: 1.2;
    width: auto;
    padding-bottom: 1rem;
  }

  .home-flip-card__icon {
    margin-top: 2rem;
  }

  .home-flip-card__back-text {
    margin-top: 2rem;
    font-size: 1.1rem;
    line-height: 1.55;
    width: 80%;
  }

  .home-flip-card__back-icon {
    margin-bottom: 0.5rem;
  }

  .home-page .home-hero-panel p:nth-of-type(2) {
    display: none;
  }

  .home-stat-card__copy {
    padding-right: 1.6rem;
  }

  .home-stat-card__citation-shell {
    right: 0.85rem;
    bottom: 0.85rem;
  }

  .home-stat-card__tooltip-link {
    white-space: normal;
  }
}

@media (max-width: 400px) {
  /* Very small phones (iPhone SE etc) */
  .home-flip-card__title {
    font-size: 1.25rem;
  }
}
</style>
