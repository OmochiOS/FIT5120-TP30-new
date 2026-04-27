import { describe, expect, it } from 'vitest';

import { filterCoveragePlaces } from '@/utils/search-coverage';

describe('search coverage helpers', () => {
  it('returns broader official place matches while excluding existing featured names', () => {
    const places = [
      {
        name: 'Flagstaff Gardens',
        theme: 'Parks',
        subTheme: 'Garden',
        coordinates: null,
      },
      {
        name: 'Parliament Railway Station',
        theme: 'Transport',
        subTheme: 'Railway Station',
        coordinates: null,
      },
      {
        name: 'Queen Victoria Market',
        theme: 'Markets',
        subTheme: 'Retail',
        coordinates: null,
      },
    ];

    const results = filterCoveragePlaces(
      places,
      'parl',
      new Set(['flagstaff gardens', 'state library victoria']),
      5,
    );

    expect(results).toEqual([
      {
        name: 'Parliament Railway Station',
        theme: 'Transport',
        subTheme: 'Railway Station',
        coordinates: null,
      },
    ]);
  });

  it('does not expand search coverage for one-letter queries', () => {
    const results = filterCoveragePlaces(
      [
        {
          name: 'Queen Victoria Market',
          theme: 'Markets',
          subTheme: 'Retail',
          coordinates: null,
        },
      ],
      'q',
      new Set(),
      5,
    );

    expect(results).toEqual([]);
  });
});
