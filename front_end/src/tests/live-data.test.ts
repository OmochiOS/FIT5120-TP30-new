import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { seedLocations } from '@/utils/adapters';
import { fetchLiveLocationRefresh } from '@/utils/live-data';

describe('live data integration', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('merges the latest live counts into supported locations and recalculates live scores', async () => {
    vi.setSystemTime(new Date('2026-04-01T15:40:00+11:00'));

    const mockPayload = [
      { location_id: 109, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 8 },
      { location_id: 132, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 12 },
      { location_id: 107, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 10 },
      { location_id: 66, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 44 },
      { location_id: 3, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 49 },
      { location_id: 40, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 2 },
      { location_id: 20, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 4 },
      { location_id: 75, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 18 },
      { location_id: 17, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 22 },
      { location_id: 91, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 30 },
      { location_id: 130, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 34 },
      { location_id: 94, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 26 },
      { location_id: 95, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 19 },
      { location_id: 72, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 88 },
      { location_id: 5, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 110 },
      { location_id: 161, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 95 },
      { location_id: 62, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 61 },
      { location_id: 181, sensing_datetime: '2026-04-01T15:25:00+11:00', total_of_directions: 58 },
    ];
    const fetchImpl = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(mockPayload),
    });

    const result = await fetchLiveLocationRefresh(seedLocations, 30, fetchImpl);
    const flagstaff = result.locations.find((location) => location.locationId === 'flagstaff-gardens');
    const federationSquare = result.locations.find((location) => location.locationId === 'federation-square');

    expect(result.dataStatus).toBe('fresh');
    expect(fetchImpl).toHaveBeenCalledTimes(1);
    expect(flagstaff?.crowdSnapshot.rawCount).toBe(10);
    expect(flagstaff?.crowdSnapshot.crowdLevel).toBe('low');
    expect(flagstaff?.sensoryScore).toBe(1);
    expect(federationSquare?.crowdSnapshot.rawCount).toBe(98);
    expect(federationSquare?.crowdSnapshot.crowdLevel).toBe('high');
    expect(federationSquare?.sensoryScore).toBe(4);
  });

  it('keeps the fallback locations when the live payload has no usable readings', async () => {
    vi.setSystemTime(new Date('2026-04-01T15:40:00+11:00'));

    const fetchImpl = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue([]),
    });

    const result = await fetchLiveLocationRefresh(seedLocations, 30, fetchImpl);

    expect(result.dataStatus).toBe('empty');
    expect(result.locations).toEqual(seedLocations);
    expect(result.syncedAt).toBeNull();
  });

  it('derives crowd predictability from official minute-count variability when enough readings exist', async () => {
    vi.setSystemTime(new Date('2026-04-01T15:40:00+11:00'));

    const mockPayload = [
      { location_id: 109, sensing_datetime: '2026-04-01T15:20:00+11:00', total_of_directions: 9 },
      { location_id: 109, sensing_datetime: '2026-04-01T15:21:00+11:00', total_of_directions: 10 },
      { location_id: 109, sensing_datetime: '2026-04-01T15:22:00+11:00', total_of_directions: 11 },
      { location_id: 132, sensing_datetime: '2026-04-01T15:20:00+11:00', total_of_directions: 10 },
      { location_id: 132, sensing_datetime: '2026-04-01T15:21:00+11:00', total_of_directions: 11 },
      { location_id: 132, sensing_datetime: '2026-04-01T15:22:00+11:00', total_of_directions: 10 },
      { location_id: 72, sensing_datetime: '2026-04-01T15:20:00+11:00', total_of_directions: 30 },
      { location_id: 72, sensing_datetime: '2026-04-01T15:21:00+11:00', total_of_directions: 110 },
      { location_id: 72, sensing_datetime: '2026-04-01T15:22:00+11:00', total_of_directions: 45 },
    ];
    const fetchImpl = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(mockPayload),
    });

    const result = await fetchLiveLocationRefresh(seedLocations, 30, fetchImpl);
    const flagstaff = result.locations.find((location) => location.locationId === 'flagstaff-gardens');
    const federationSquare = result.locations.find((location) => location.locationId === 'federation-square');

    expect(flagstaff?.sensoryProfile.crowdPredictability).toBe('stable');
    expect(flagstaff?.sensoryProfile.signalSources?.crowdPredictability?.source).toBe('derived');
    expect(federationSquare?.sensoryProfile.crowdPredictability).toBe('event-sensitive');
  });
});
