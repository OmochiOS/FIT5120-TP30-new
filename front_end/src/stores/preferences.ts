import { defineStore } from 'pinia';

import type { MapQuickFilters, UserPreference } from '@/types/domain';
import { defaultPreferences } from '@/utils/adapters';
import { applyThemePreference, loadStoredPreferences, persistPreferences } from '@/utils/preferences';

export const usePreferencesStore = defineStore('preferences', {
  state: () => ({
    noise: defaultPreferences.noise,
    lighting: defaultPreferences.lighting,
    crowds: defaultPreferences.crowds,
    unpredictability: defaultPreferences.unpredictability,
    activity: defaultPreferences.activity,
    predictionTime: defaultPreferences.predictionTime,
    darkModeEnabled: defaultPreferences.darkModeEnabled,
    quickViewModeActive: false,
    hydrated: false,
  }),
  getters: {
      currentFilters: (state): MapQuickFilters => ({
        noise: state.noise,
        lighting: state.lighting,
        crowds: state.crowds,
        unpredictability: state.unpredictability,
        activity: state.activity,
        predictionTime: state.predictionTime,
      }),
      currentPreferences: (state): UserPreference => ({
        noise: state.noise,
        lighting: state.lighting,
        crowds: state.crowds,
        unpredictability: state.unpredictability,
        activity: state.activity,
        predictionTime: state.predictionTime,
        darkModeEnabled: state.darkModeEnabled,
      }),
  },
  actions: {
    hydrate() {
      const storedPreferences = loadStoredPreferences(defaultPreferences);

      this.noise = storedPreferences.noise;
      this.lighting = storedPreferences.lighting;
      this.crowds = storedPreferences.crowds;
      this.unpredictability = storedPreferences.unpredictability;
      this.activity = storedPreferences.activity;
      this.predictionTime = storedPreferences.predictionTime;
      this.darkModeEnabled = storedPreferences.darkModeEnabled;
      this.hydrated = true;
      applyThemePreference(this.darkModeEnabled);
    },
    replaceFilters(nextFilters: MapQuickFilters) {
      this.$patch({
        ...nextFilters,
      });

      persistPreferences(this.currentPreferences);
    },
    clearFilters() {
      this.replaceFilters({
        noise: false,
        lighting: false,
        crowds: false,
        unpredictability: false,
        activity: 'any',
        predictionTime: 'any',
      });
    },
    toggleTheme() {
      this.darkModeEnabled = !this.darkModeEnabled;
      persistPreferences(this.currentPreferences);
      applyThemePreference(this.darkModeEnabled);
    },
    toggleQuickViewMode() {
      this.quickViewModeActive = !this.quickViewModeActive;
    },
    setQuickViewMode(active: boolean) {
      this.quickViewModeActive = active;
    },
    reset() {
      this.$patch({
        ...defaultPreferences,
      });

      persistPreferences(this.currentPreferences);
      applyThemePreference(this.darkModeEnabled);
    },
  },
});
