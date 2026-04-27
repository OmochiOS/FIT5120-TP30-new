import type { CrowdLevel } from '@/types/domain';

const crowdLabels: Record<CrowdLevel, string> = {
  low: 'Calm',
  moderate: 'Manageable',
  high: 'Busy',
};

const crowdSummaries: Record<CrowdLevel, string> = {
  low: 'Low crowd intensity and a more predictable visit.',
  moderate: 'Steady movement, but still manageable with a plan.',
  high: 'Busy conditions that may require a quick exit strategy.',
};

export function getCrowdLabel(level: CrowdLevel): string {
  return crowdLabels[level];
}

export function getCrowdSummary(level: CrowdLevel): string {
  return crowdSummaries[level];
}

export function getCrowdToneClass(level: CrowdLevel): string {
  if (level === 'low') {
    return 'bg-emerald-100 text-emerald-800 dark:bg-emerald-500/20 dark:text-emerald-200';
  }

  if (level === 'moderate') {
    return 'bg-amber-100 text-amber-800 dark:bg-amber-500/20 dark:text-amber-100';
  }

  return 'bg-rose-100 text-rose-800 dark:bg-rose-500/20 dark:text-rose-100';
}
