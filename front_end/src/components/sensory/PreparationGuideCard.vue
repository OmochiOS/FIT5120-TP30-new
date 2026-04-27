<template>
  <section class="space-y-3.5">
    <p class="text-[18px] font-semibold text-sense-ink dark:text-white">
      Good to Know
    </p>

    <div class="detail-surface detail-guide overflow-hidden rounded-[12px] border border-sense-teal/10 bg-white shadow-[0_12px_24px_-20px_rgba(16,74,85,0.35)] dark:border-white/8 dark:bg-[#16252f]/92">
      <article
        v-for="item in items"
        :key="item.kind"
        class="detail-guide-row flex items-center gap-3 border-b border-sense-teal/8 px-4 py-4 last:border-b-0 dark:border-white/8"
      >
        <div class="detail-chip flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-sense-mint/30 text-[10px] font-bold uppercase text-sense-teal dark:bg-white/10 dark:text-sense-mint">
          <component :is="iconFor(item.kind)" class="h-4 w-4" />
        </div>
        <p class="detail-copy text-[14px] leading-6 text-sense-ink/76 dark:text-slate-100">
          {{ item.value }}
        </p>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { FunctionalComponent } from 'vue';
import { h } from 'vue';

import type { GoodToKnowItem } from '@/types/domain';

defineProps<{
  items: GoodToKnowItem[];
}>();

const QuietAreaIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...props },
    [
      h('path', { d: 'M10 17s5-4.4 5-8.2A5 5 0 0 0 5 8.8C5 12.6 10 17 10 17Z', 'stroke-linejoin': 'round' }),
      h('circle', { cx: '10', cy: '8.5', r: '1.8' }),
    ],
  );

const TransportIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...props },
    [
      h('rect', { x: '4', y: '5.5', width: '12', height: '8', rx: '2', 'stroke-linejoin': 'round' }),
      h('path', { d: 'M7 13.5v2', 'stroke-linecap': 'round' }),
      h('path', { d: 'M13 13.5v2', 'stroke-linecap': 'round' }),
      h('path', { d: 'M4 9.5h12', 'stroke-linecap': 'round' }),
      h('circle', { cx: '7', cy: '15.5', r: '0.8', fill: 'currentColor', stroke: 'none' }),
      h('circle', { cx: '13', cy: '15.5', r: '0.8', fill: 'currentColor', stroke: 'none' }),
    ],
  );

const ToiletsIcon: FunctionalComponent = (props) =>
  h(
    'svg',
    { viewBox: '0 0 20 20', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', ...props },
    [
      h('circle', { cx: '10', cy: '4.3', r: '1.5' }),
      h('path', { d: 'M10 6.2v4.2', 'stroke-linecap': 'round' }),
      h('path', { d: 'M7.2 8.2h5.6', 'stroke-linecap': 'round' }),
      h('path', { d: 'm8.1 15.8 1-5.4', 'stroke-linecap': 'round' }),
      h('path', { d: 'm11.9 15.8-1-5.4', 'stroke-linecap': 'round' }),
      h('path', { d: 'M6.2 11.2a3.2 3.2 0 1 0 6.4 0', 'stroke-linecap': 'round' }),
    ],
  );

function iconFor(kind: GoodToKnowItem['kind']): FunctionalComponent {
  if (kind === 'quiet-area') {
    return QuietAreaIcon;
  }

  if (kind === 'transport') {
    return TransportIcon;
  }

  return ToiletsIcon;
}
</script>
