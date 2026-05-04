import { mount } from '@vue/test-utils';
import { nextTick } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import MapCanvas from '@/components/map/MapCanvas.vue';
import { seedLocations } from '@/utils/adapters';

const leafletState = vi.hoisted(() => ({
  push: vi.fn(),
  fitBounds: vi.fn(),
  flyTo: vi.fn(),
  invalidateSize: vi.fn(),
  remove: vi.fn(),
  getZoom: vi.fn(() => 13),
  setMinZoom: vi.fn(),
  setMaxBounds: vi.fn(),
  clearLayers: vi.fn(),
  addLayer: vi.fn(),
  markerInstances: [] as Array<{
    coords: [number, number];
    handlers: Record<string, () => void>;
  }>,
}));

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: leafletState.push,
  }),
}));

vi.mock('leaflet', () => {
  const mapInstance = {
    setView: vi.fn(() => mapInstance),
    fitBounds: leafletState.fitBounds,
    flyTo: leafletState.flyTo,
    invalidateSize: leafletState.invalidateSize,
    remove: leafletState.remove,
    getZoom: leafletState.getZoom,
    setMinZoom: leafletState.setMinZoom,
    setMaxBounds: leafletState.setMaxBounds,
  };

  const featureGroupInstance = {
    addTo: vi.fn(() => featureGroupInstance),
    clearLayers: leafletState.clearLayers,
    addLayer: leafletState.addLayer,
  };

  return {
    map: vi.fn(() => mapInstance),
    tileLayer: vi.fn(() => ({
      addTo: vi.fn(),
    })),
    control: {
      zoom: vi.fn(() => ({
        addTo: vi.fn(),
      })),
      attribution: vi.fn(() => ({
        addTo: vi.fn(),
      })),
    },
    featureGroup: vi.fn(() => featureGroupInstance),
    latLngBounds: vi.fn((coordinates: Array<[number, number]>) => ({
      coordinates,
      isValid: () => coordinates.length > 0,
    })),
    divIcon: vi.fn((options) => options),
    marker: vi.fn((coords: [number, number]) => {
      const handlers: Record<string, () => void> = {};
      const markerInstance = {
        on: vi.fn((event: string, handler: () => void) => {
          handlers[event] = handler;
          return markerInstance;
        }),
      };

      leafletState.markerInstances.push({ coords, handlers });
      return markerInstance;
    }),
  };
});

import { latLngBounds, marker as leafletMarker } from 'leaflet';

describe('MapCanvas', () => {
  beforeEach(() => {
    leafletState.push.mockReset();
    leafletState.fitBounds.mockReset();
    leafletState.flyTo.mockReset();
    leafletState.invalidateSize.mockReset();
    leafletState.remove.mockReset();
    leafletState.getZoom.mockReset();
    leafletState.getZoom.mockReturnValue(13);
    leafletState.setMinZoom.mockReset();
    leafletState.setMaxBounds.mockReset();
    leafletState.clearLayers.mockReset();
    leafletState.addLayer.mockReset();
    leafletState.markerInstances.length = 0;
    vi.mocked(leafletMarker).mockClear();
    vi.mocked(latLngBounds).mockClear();
  });

  it('renders seed markers from real coordinates and opens a summary on marker click', async () => {
    const locations = seedLocations.slice(0, 3);

    const wrapper = mount(MapCanvas, {
      props: {
        locations,
        allLocations: locations,
      },
      global: {
        stubs: {
          MapLegend: true,
        },
      },
    });

    await nextTick();

    expect(vi.mocked(leafletMarker)).toHaveBeenCalledTimes(3);
    expect(vi.mocked(leafletMarker)).toHaveBeenNthCalledWith(
      1,
      [locations[0].coordinates.lat, locations[0].coordinates.lon],
      expect.objectContaining({
        title: `${locations[0].name}, sensory score ${locations[0].sensoryScore}`,
      }),
    );
    expect(vi.mocked(latLngBounds)).toHaveBeenCalledWith([
      [locations[0].coordinates.lat, locations[0].coordinates.lon],
      [locations[1].coordinates.lat, locations[1].coordinates.lon],
      [locations[2].coordinates.lat, locations[2].coordinates.lon],
    ]);

    leafletState.markerInstances[0].handlers.click();
    await nextTick();

    expect(leafletState.push).not.toHaveBeenCalled();
    expect(wrapper.text()).toContain(locations[0].name);
    expect(wrapper.text()).toContain('Location summary');
  });

  it('opens a summary first and only routes after confirming', async () => {
    const locations = seedLocations.slice(0, 2);

    const wrapper = mount(MapCanvas, {
      props: {
        locations,
        allLocations: locations,
      },
      global: {
        stubs: {
          MapLegend: true,
        },
      },
    });

    leafletState.markerInstances[1].handlers.click();
    await nextTick();

    expect(wrapper.text()).toContain(locations[1].name);
    expect(wrapper.text()).toContain('View full profile');
    expect(leafletState.push).not.toHaveBeenCalled();

    await wrapper.get('[data-testid="summary-view-profile"]').trigger('click');
    expect(leafletState.push).toHaveBeenCalledWith(`/location/${locations[1].locationId}`);
  });

  it('closes the summary when the close control is used', async () => {
    const locations = seedLocations.slice(0, 1);

    const wrapper = mount(MapCanvas, {
      props: {
        locations,
        allLocations: locations,
      },
      global: {
        stubs: {
          MapLegend: true,
        },
      },
    });

    leafletState.markerInstances[0].handlers.click();
    await nextTick();

    expect(wrapper.text()).toContain(locations[0].name);

    await wrapper.get('[data-testid="summary-close"]').trigger('click');
    await nextTick();

    expect(wrapper.text()).not.toContain('Location summary');
    expect(leafletState.push).not.toHaveBeenCalled();
  });

  it('re-renders markers when the filtered location set changes', async () => {
    const locations = seedLocations.slice(0, 3);

    const wrapper = mount(MapCanvas, {
      props: {
        locations,
        allLocations: locations,
      },
      global: {
        stubs: {
          MapLegend: true,
        },
      },
    });

    await wrapper.setProps({
      locations: locations.slice(0, 1),
    });

    expect(leafletState.clearLayers).toHaveBeenCalledTimes(2);
    expect(vi.mocked(leafletMarker)).toHaveBeenCalledTimes(4);
    expect(vi.mocked(latLngBounds)).toHaveBeenCalledTimes(1);
    expect(vi.mocked(latLngBounds)).toHaveBeenLastCalledWith([
      [locations[0].coordinates.lat, locations[0].coordinates.lon],
      [locations[1].coordinates.lat, locations[1].coordinates.lon],
      [locations[2].coordinates.lat, locations[2].coordinates.lon],
    ]);
    expect(leafletState.fitBounds).toHaveBeenLastCalledWith(
      expect.objectContaining({
        isValid: expect.any(Function),
      }),
      expect.objectContaining({
        maxZoom: 14,
      }),
    );
  });
});
