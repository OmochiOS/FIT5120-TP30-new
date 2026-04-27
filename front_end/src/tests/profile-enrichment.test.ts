import { describe, expect, it, vi } from 'vitest';

import { seedLocations } from '@/utils/adapters';
import { fetchAutomatedProfileRefresh } from '@/utils/profile-enrichment';

describe('official profile enrichment', () => {
  it('applies nearby official ambient noise readings to outdoor locations only', async () => {
    const fetchImpl = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue({
        results: [
          {
            latitude: -37.8111,
            longitude: 144.9547,
            noise_db: 52,
            measured_at: '2026-04-01T15:00:00+11:00',
          },
          {
            latitude: -37.8178,
            longitude: 144.9690,
            noise_db: 72,
            measured_at: '2026-04-01T15:05:00+11:00',
          },
        ],
      }),
    });

    const result = await fetchAutomatedProfileRefresh(seedLocations, fetchImpl);
    const flagstaff = result.find((location) => location.locationId === 'flagstaff-gardens');
    const federationSquare = result.find((location) => location.locationId === 'federation-square');
    const stateLibrary = result.find((location) => location.locationId === 'state-library-victoria');

    expect(flagstaff?.sensoryProfile.noiseLevel).toBe('low');
    expect(flagstaff?.sensoryProfile.signalSources?.noiseLevel?.source).toBe('official');
    expect(federationSquare?.sensoryProfile.noiseLevel).toBe('high');
    expect(stateLibrary?.sensoryProfile.signalSources?.noiseLevel).toBeUndefined();
    expect(stateLibrary?.sensoryProfile.noiseLevel).toBe('low');
  });
});
