import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { seedLocations } from '@/utils/adapters';

vi.mock('@/utils/live-data', () => ({
  fetchLiveLocationRefresh: vi.fn(),
}));

vi.mock('@/utils/profile-enrichment', () => ({
  fetchAutomatedProfileRefresh: vi.fn(),
}));

import { useLocationsStore } from '@/stores/locations';
import { fetchLiveLocationRefresh } from '@/utils/live-data';
import { fetchAutomatedProfileRefresh } from '@/utils/profile-enrichment';

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
});
