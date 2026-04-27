import { describe, expect, it } from 'vitest';

import { seedLocations, defaultPreferences } from '@/utils/adapters';
import { filterAndSortLocations } from '@/utils/filters';

describe('filter and ranking rules', () => {
  it('applies category and quick filters before sorting into figma list order', () => {
    const results = filterAndSortLocations(seedLocations, {
      searchText: 'library',
      category: 'Library',
      preferences: {
        ...defaultPreferences,
        crowds: true,
      },
    });

    expect(results.map((location) => location.locationId)).toEqual(['state-library-victoria']);
  });

  it('filters by activity tags and keeps map list ordering', () => {
    const results = filterAndSortLocations(seedLocations, {
      searchText: '',
      category: 'All',
      preferences: {
        ...defaultPreferences,
        activity: 'cultural',
        lighting: true,
      },
    });

    expect(results.map((location) => location.locationId)).toEqual([
      'state-library-victoria',
      'carlton-gardens',
    ]);
  });
});
