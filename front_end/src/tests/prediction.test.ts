import { describe, expect, it, vi } from 'vitest';

import { seedLocations } from '@/utils/adapters';
import type { Location } from '@/types/domain';
import { fetchLocationPrediction, findPredictionLocation, getLocationPrediction } from '@/utils/prediction';

describe('prediction summaries', () => {
  it('returns the designed planning copy for a supported location', () => {
    const location = seedLocations.find((entry) => entry.locationId === 'melbourne-central');

    expect(location).toBeDefined();

    const prediction = getLocationPrediction(location!);

    expect(prediction.rows).toHaveLength(8);
    expect(prediction.timeBar).toHaveLength(8);
    expect(prediction.quietestNote).toContain('Quietest');
    expect(prediction.rows[5].recommendation).toBe('Peak avoid');
  });

  it('hydrates prediction rows from official historical counts when records are usable', async () => {
    const location = seedLocations.find((entry) => entry.locationId === 'state-library-victoria');

    expect(location).toBeDefined();

    const responsePayload = {
      results: [
        { sensor_id: 66, hour_beginning: '2026-04-01T07:00:00+11:00', hourly_counts: 18, day: 'Tuesday' },
        { sensor_id: 66, hour_beginning: '2026-04-01T09:00:00+11:00', hourly_counts: 36, day: 'Tuesday' },
        { sensor_id: 66, hour_beginning: '2026-04-01T11:00:00+11:00', hourly_counts: 88, day: 'Tuesday' },
        { sensor_id: 66, hour_beginning: '2026-04-01T13:00:00+11:00', hourly_counts: 120, day: 'Tuesday' },
        { sensor_id: 66, hour_beginning: '2026-04-01T15:00:00+11:00', hourly_counts: 182, day: 'Tuesday' },
        { sensor_id: 66, hour_beginning: '2026-04-01T17:00:00+11:00', hourly_counts: 204, day: 'Tuesday' },
        { sensor_id: 66, hour_beginning: '2026-04-01T19:00:00+11:00', hourly_counts: 92, day: 'Tuesday' },
        { sensor_id: 66, hour_beginning: '2026-04-01T21:00:00+11:00', hourly_counts: 28, day: 'Tuesday' },
      ],
    };

    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => responsePayload,
    });

    const snapshot = await fetchLocationPrediction(location!, fetchMock);

    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(snapshot.source).toBe('official');
    expect(snapshot.status).toBe('fresh');
    expect(snapshot.prediction?.rows).toHaveLength(8);
    expect(snapshot.prediction?.rows[0].estimate).toBe(18);
    expect(snapshot.prediction?.rows[5].recommendation).toBe('Peak avoid');
  });

  it('shows estimated visit guidance when historical counts fail', async () => {
    const location = seedLocations.find((entry) => entry.locationId === 'melbourne-central');

    expect(location).toBeDefined();

    const snapshot = await fetchLocationPrediction(
      location!,
      vi.fn().mockRejectedValue(new Error('network down')),
    );

    expect(snapshot.source).toBe('fallback');
    expect(snapshot.status).toBe('error');
    expect(snapshot.prediction?.rows).toHaveLength(8);
    expect(snapshot.note).toContain('estimated visit pattern');
  });

  it('matches broader area searches against backend-style location names', () => {
    const backendLikeLocations: Location[] = [
      {
        ...seedLocations[0],
        locationId: '28',
        name: 'The Arts Centre',
        addressOrArea: 'Melbourne CBD sensor area',
        displayType: 'Community Centre',
      },
      {
        ...seedLocations[1],
        locationId: '31',
        name: 'Lygon St (West)',
        addressOrArea: 'Melbourne CBD sensor area',
        displayType: 'Park',
      },
    ];

    expect(findPredictionLocation(backendLikeLocations, 'Southbank')?.locationId).toBe('28');
    expect(findPredictionLocation(backendLikeLocations, 'Carlton')?.locationId).toBe('31');
  });
});
