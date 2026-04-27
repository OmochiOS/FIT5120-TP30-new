import { describe, expect, it } from 'vitest';

import { defaultPreferences, seedLocations } from '@/utils/adapters';

describe('seed data adapters', () => {
  it('merges every location with profile, guide, snapshot data, and activity tags', () => {
    expect(seedLocations).toHaveLength(8);

    for (const location of seedLocations) {
      expect(location.sensoryProfile.locationId).toBe(location.locationId);
      expect(location.preparationGuide.locationId).toBe(location.locationId);
      expect(location.crowdSnapshot.label.length).toBeGreaterThan(0);
      expect(location.mapPoint.x).toBeGreaterThan(0);
      expect(location.mapPoint.y).toBeGreaterThan(0);
      expect(location.activityTags.length).toBeGreaterThan(0);
    }
  });

  it('exposes the agreed default preference model', () => {
    expect(defaultPreferences).toEqual({
      noise: false,
      lighting: false,
      crowds: false,
      unpredictability: false,
      activity: 'any',
      darkModeEnabled: false,
    });
  });
});
