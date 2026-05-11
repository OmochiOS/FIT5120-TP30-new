import { beforeEach, describe, expect, it } from 'vitest';

import type { UserPreference } from '@/types/domain';
import { loadStoredPreferences, persistPreferences } from '@/utils/preferences';

const defaults: UserPreference = {
  noise: false,
  lighting: false,
  crowds: false,
  unpredictability: false,
  activity: 'any',
  predictionTime: 'any',
  darkModeEnabled: false,
};

describe('local storage preferences', () => {
  beforeEach(() => {
    window.localStorage.clear();
  });

  it('persists and restores preferences from localStorage', () => {
    const nextValue: UserPreference = {
      noise: true,
      lighting: true,
      crowds: true,
      unpredictability: false,
      activity: 'outdoor',
      predictionTime: '11am-1pm',
      darkModeEnabled: true,
    };

    persistPreferences(nextValue);

    expect(loadStoredPreferences(defaults)).toEqual(nextValue);
  });

  it('falls back to defaults when storage is empty', () => {
    expect(loadStoredPreferences(defaults)).toEqual(defaults);
  });
});
