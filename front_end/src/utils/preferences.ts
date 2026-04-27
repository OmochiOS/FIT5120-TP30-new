import type { UserPreference } from '@/types/domain';

const STORAGE_KEY = 'amityspace.preferences';

export function loadStoredPreferences(defaultPreferences: UserPreference): UserPreference {
  if (typeof window === 'undefined') {
    return defaultPreferences;
  }

  try {
    const rawValue = window.localStorage.getItem(STORAGE_KEY);

    if (!rawValue) {
      return defaultPreferences;
    }

    const parsedValue = JSON.parse(rawValue) as Partial<UserPreference>;
    return {
      ...defaultPreferences,
      ...parsedValue,
    };
  } catch {
    return defaultPreferences;
  }
}

export function persistPreferences(preferences: UserPreference): void {
  if (typeof window === 'undefined') {
    return;
  }

  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences));
}

export function applyThemePreference(enabled: boolean): void {
  if (typeof document === 'undefined') {
    return;
  }

  document.documentElement.classList.toggle('dark', enabled);
}
