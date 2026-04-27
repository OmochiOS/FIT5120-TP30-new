import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { seedLocations } from '@/utils/adapters';

vi.mock('@/utils/live-data', () => ({
  fetchLiveLocationRefresh: vi.fn(),
}));

vi.mock('@/utils/profile-enrichment', () => ({
  fetchAutomatedProfileRefresh: vi.fn(),
}));

vi.mock('@/utils/prediction', async () => {
  const actual = await vi.importActual<typeof import('@/utils/prediction')>('@/utils/prediction');

  return {
    ...actual,
    fetchLocationPrediction: vi.fn(),
  };
});

vi.mock('@/utils/search-coverage', async () => {
  const actual = await vi.importActual<typeof import('@/utils/search-coverage')>(
    '@/utils/search-coverage',
  );

  return {
    ...actual,
    fetchSearchCoveragePlaces: vi.fn(),
  };
});

import { useLocationsStore } from '@/stores/locations';
import { fetchLiveLocationRefresh } from '@/utils/live-data';
import { fetchLocationPrediction } from '@/utils/prediction';
import { fetchAutomatedProfileRefresh } from '@/utils/profile-enrichment';
import { fetchSearchCoveragePlaces } from '@/utils/search-coverage';

describe('locations store state handling', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.resetAllMocks();
  });

  it('supports manual live refresh and updates sync metadata', async () => {
    vi.mocked(fetchLiveLocationRefresh).mockResolvedValue({
      locations: seedLocations,
      dataStatus: 'fresh',
      syncedAt: '2026-04-01T15:25:00+11:00',
    });
    vi.mocked(fetchAutomatedProfileRefresh).mockResolvedValue(seedLocations);

    const store = useLocationsStore();
    store.locations = seedLocations;
    await store.refreshLiveData();

    expect(fetchLiveLocationRefresh).toHaveBeenCalledTimes(1);
    expect(fetchAutomatedProfileRefresh).toHaveBeenCalledTimes(1);
    expect(store.liveDataStatus).toBe('fresh');
    expect(store.liveDataSyncedAt).toBe('2026-04-01T15:25:00+11:00');
    expect(store.liveDataRefreshing).toBe(false);
  });

  it('explains when only broader official place matches are available', async () => {
    vi.mocked(fetchSearchCoveragePlaces).mockResolvedValue([
      {
        name: 'Parliament Railway Station',
        theme: 'Transport',
        subTheme: 'Railway Station',
        coordinates: { lat: -37.8116, lon: 144.9730 },
      },
    ]);

    const store = useLocationsStore();
    await store.hydrateSearchCoverage();
    store.setSearchText('Parliament');

    expect(store.filteredLocations).toEqual([]);
    expect(store.expandedSearchResults).toHaveLength(1);
    expect(store.searchCoverageMessage).toBe(
      'No featured map locations match this query. Showing broader official place matches below.',
    );
  });

  it('hydrates historical predictions for a location', async () => {
    vi.mocked(fetchLocationPrediction).mockResolvedValue({
      prediction: {
        rows: [
          { time: '7-9am', level: 'low', estimate: 10, recommendation: 'Recommended' },
          { time: '9-11am', level: 'low', estimate: 20, recommendation: 'Recommended' },
          { time: '11am-1pm', level: 'moderate', estimate: 40, recommendation: 'Manageable' },
          { time: '1-3pm', level: 'moderate', estimate: 50, recommendation: 'Manageable' },
          { time: '3-5pm', level: 'high', estimate: 90, recommendation: 'Peak avoid' },
          { time: '5-7pm', level: 'high', estimate: 100, recommendation: 'Peak avoid' },
          { time: '7-9pm', level: 'moderate', estimate: 60, recommendation: 'Manageable' },
          { time: '9-11pm', level: 'low', estimate: 15, recommendation: 'Recommended' },
        ],
        timeBar: [
          { time: '7-9am', level: 'low' },
          { time: '9-11am', level: 'low' },
          { time: '11am-1pm', level: 'moderate' },
          { time: '1-3pm', level: 'moderate' },
          { time: '3-5pm', level: 'high' },
          { time: '5-7pm', level: 'high' },
          { time: '7-9pm', level: 'moderate' },
          { time: '9-11pm', level: 'low' },
        ],
        quietestNote: 'Quietest: Tue 7-9am',
        activeDay: 'Tue',
      },
      status: 'fresh',
      source: 'official',
      syncedAt: '2026-04-09T09:00:00+11:00',
      note: 'Based on recent historical pedestrian counts from City of Melbourne open data.',
    });

    const store = useLocationsStore();
    store.locations = seedLocations;
    await store.hydratePrediction('state-library-victoria');

    expect(fetchLocationPrediction).toHaveBeenCalledTimes(1);
    expect(store.getPredictionByLocationId('state-library-victoria')?.rows).toHaveLength(8);
    expect(store.getPredictionMessage('state-library-victoria')).toContain('historical pedestrian counts');
  });
});
