<template>
  <section class="figma-panel-soft relative min-h-[440px] p-3 md:min-h-[560px] md:p-4">
    <div class="amity-leaflet-shell h-[416px] md:h-[528px]">
      <div
        ref="mapElement"
        class="amity-leaflet-map"
        aria-label="AmitySpace live locations map"
      ></div>
    </div>

    <!-- Teleport to body: escapes Leaflet container width/overflow constraints
         so position:fixed works against the viewport on all screen sizes -->
    <Teleport to="body">
      <LocationSummaryPopup
        :location="selectedLocation"
        @close="closeLocationSummary"
        @view-profile="viewSelectedLocationDetail"
        @navigate="openNavPanel"
      />
    </Teleport>

    <!-- Navigation Panel (also teleported to body so it sits above everything) -->
    <Teleport to="body">
      <NavigationPanel
        ref="navPanelRef"
        :visible="showNavPanel"
        :destination-location="selectedLocation"
        @route-computed="onRouteComputed"
        @route-cleared="onRouteCleared"
        @close="closeNavPanel"
      />
    </Teleport>

    <div class="pointer-events-none absolute inset-x-3 bottom-3 z-[450] flex justify-end md:inset-x-4 md:bottom-4">
      <div class="pointer-events-auto">
        <MapLegend />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import {
  control,
  divIcon,
  featureGroup,
  latLngBounds,
  map,
  marker,
  polyline,
  tileLayer,
  type LatLngBounds,
  type DivIcon,
  type FeatureGroup,
  type Map as LeafletMap,
  type Polyline,
  type TileLayer,
} from 'leaflet';
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import MapLegend from '@/components/map/MapLegend.vue';
import LocationSummaryPopup from '@/components/map/LocationSummaryPopup.vue';
import NavigationPanel from '@/components/map/NavigationPanel.vue';
import type { Location } from '@/types/domain';

const props = defineProps<{
  locations: Location[];
  allLocations?: Location[];
  externalSelectedId?: string | null;
  /** Pass true when the map is inside the Quick View grid so it can
   *  invalidate its size after the layout shifts. */
  compact?: boolean;
}>();

const emit = defineEmits<{
  'clear-external': [];
  'location-clicked': [id: string | null];
}>();

const router = useRouter();
const mapElement = ref<HTMLElement | null>(null);
const selectedLocationId = ref<string | null>(null);
const showNavPanel = ref(false);
const enRouteIds = ref<string[]>([]);
const navPanelRef = ref<InstanceType<typeof NavigationPanel> | null>(null);

let leafletMap: LeafletMap | null = null;
let markerLayer: FeatureGroup | null = null;
let routePolyline: Polyline | null = null;
let cbdBounds: LatLngBounds | null = null;
let baseLayer: TileLayer | null = null;
let themeObserver: MutationObserver | null = null;
let isShowingRoute = false; // skip CBD re-lock while a nav route is displayed

const defaultCenter: [number, number] = [-37.8136, 144.9631];
const fixedPadding: [number, number] = [34, 34];
const MAPBOX_ACCESS_TOKEN = import.meta.env.VITE_MAPBOX_ACCESS_TOKEN;
const MAPBOX_ATTRIBUTION =
  '&copy; <a href="https://www.mapbox.com/about/maps/" target="_blank" rel="noopener noreferrer">Mapbox</a> ' +
  '&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noopener noreferrer">OpenStreetMap</a>';
const MAPBOX_TILE_OPTIONS = {
  attribution: MAPBOX_ATTRIBUTION,
  maxZoom: 18,
  tileSize: 512,
  zoomOffset: -1,
};

const selectedLocation = computed(() => {
  if (!selectedLocationId.value) {
    return null;
  }

  return props.locations.find((location) => location.locationId === selectedLocationId.value) ?? null;
});

function currentMapTheme(): 'light' | 'dark' {
  if (typeof document === 'undefined') {
    return 'light';
  }

  return document.documentElement.classList.contains('dark') ? 'dark' : 'light';
}

function buildMapboxTileUrl(theme: 'light' | 'dark'): string {
  const styleId = theme === 'dark' ? 'navigation-night-v1' : 'navigation-day-v1';
  return `https://api.mapbox.com/styles/v1/mapbox/${styleId}/tiles/512/{z}/{x}/{y}@2x?access_token=${MAPBOX_ACCESS_TOKEN}`;
}

function syncBaseLayer() {
  if (!leafletMap) {
    return;
  }

  baseLayer?.remove?.();
  baseLayer = tileLayer(buildMapboxTileUrl(currentMapTheme()), MAPBOX_TILE_OPTIONS).addTo(leafletMap);
}

function markerTone(score: Location['sensoryScore']): string {
  if (score <= 2) {
    return 'amity-map-marker--calm';
  }

  if (score === 3) {
    return 'amity-map-marker--moderate';
  }

  return 'amity-map-marker--busy';
}

function buildMarkerIcon(location: Location, isSelected = false, isEnRoute = false): DivIcon {
  const classes = [
    'amity-map-marker',
    markerTone(location.sensoryScore),
    isSelected ? 'amity-map-marker--selected' : '',
    isEnRoute && !isSelected ? 'amity-map-marker--en-route' : '',
  ]
    .filter(Boolean)
    .join(' ');

  return divIcon({
    className: 'amity-map-marker-wrapper',
    iconSize: [34, 34],
    iconAnchor: [17, 17],
    html: `
      <span class="${classes}">
        <span class="amity-map-marker__glow"></span>
        <span class="amity-map-marker__label">${location.sensoryScore}</span>
      </span>
    `,
  });
}

function getRenderableLocations(locations: Location[]) {
  return locations.filter(
    (location) =>
      Number.isFinite(location.coordinates.lat) && Number.isFinite(location.coordinates.lon),
  );
}

function updateCbdBounds() {
  const renderableLocations = getRenderableLocations(
    props.allLocations?.length ? props.allLocations : props.locations,
  );

  if (!renderableLocations.length) {
    cbdBounds = null;
    return;
  }

  const nextBounds = latLngBounds(
    renderableLocations.map((location) => [location.coordinates.lat, location.coordinates.lon] as const),
  );

  cbdBounds =
    'pad' in nextBounds && typeof nextBounds.pad === 'function' ? nextBounds.pad(0.18) : nextBounds;
}

function applyFixedBounds() {
  if (!leafletMap) {
    return;
  }

  if (!cbdBounds?.isValid()) {
    leafletMap.setView(defaultCenter, 13);
    return;
  }

  leafletMap.fitBounds(cbdBounds, {
    padding: fixedPadding,
    maxZoom: 14,
    animate: false,
  });

  if (typeof leafletMap.getZoom === 'function') {
    leafletMap.setMinZoom(leafletMap.getZoom());
  }

  if (typeof leafletMap.setMaxBounds === 'function') {
    leafletMap.setMaxBounds(cbdBounds);
  }
}

function focusLocationOnMap(location: Location) {
  if (!leafletMap) {
    return;
  }

  const nextZoom = Math.max(leafletMap.getZoom(), 15);
  leafletMap.flyTo([location.coordinates.lat, location.coordinates.lon], nextZoom, {
    animate: true,
    duration: 0.45,
  });
}

function renderMarkers() {
  if (!leafletMap || !markerLayer) {
    return;
  }

  markerLayer.clearLayers();

  const renderableLocations = getRenderableLocations(props.locations);

  for (const location of renderableLocations) {
    const isSelected = location.locationId === selectedLocationId.value;
    const isEnRoute = enRouteIds.value.includes(location.locationId);
    const locationMarker = marker([location.coordinates.lat, location.coordinates.lon], {
      icon: buildMarkerIcon(location, isSelected, isEnRoute),
      keyboard: true,
      riseOnHover: true,
      title: `${location.name}, sensory score ${location.sensoryScore}`,
    });

    locationMarker.on('click', () => {
      selectedLocationId.value = location.locationId;
      emit('location-clicked', location.locationId);
    });

    markerLayer.addLayer(locationMarker);
  }
}

// ── Navigation helpers ────────────────────────────────────────────────────────

function openNavPanel() {
  showNavPanel.value = true;
}

function closeNavPanel() {
  showNavPanel.value = false;
  clearRoutePolyline();
}

function clearRoutePolyline() {
  if (routePolyline && leafletMap) {
    leafletMap.removeLayer(routePolyline);
    routePolyline = null;
  }
  isShowingRoute = false;
  enRouteIds.value = [];
  renderMarkers();
}

function onRouteComputed(payload: {
  leafletCoords: [number, number][];
  duration: number;
  distance: number;
  enRouteIds: string[];
  allLocations: Location[];
}) {
  if (!leafletMap) return;

  // Clear any previous route
  clearRoutePolyline();

  // Draw route polyline
  routePolyline = polyline(payload.leafletCoords, {
    color: '#4a7fa8',
    weight: 5,
    opacity: 0.85,
    lineCap: 'round',
    lineJoin: 'round',
    dashArray: undefined,
  }).addTo(leafletMap);

  // Compute en-route locations using NavigationPanel's helper
  const ids = navPanelRef.value?.computeEnRouteIds(payload.leafletCoords, props.locations) ?? [];
  enRouteIds.value = ids;

  // Inform NavigationPanel of the count
  navPanelRef.value?.setEnRouteCount(ids.length);

  // Re-render markers with highlights
  renderMarkers();

  isShowingRoute = true;

  // Fit map to route
  if (routePolyline) {
    leafletMap.fitBounds(routePolyline.getBounds(), { padding: [40, 40], maxZoom: 16, animate: true });
  }
}

function onRouteCleared() {
  clearRoutePolyline();
}

function closeLocationSummary() {
  selectedLocationId.value = null;
  emit('location-clicked', null);
}

function viewSelectedLocationDetail() {
  if (!selectedLocation.value) {
    return;
  }

  void router.push(`/location/${selectedLocation.value.locationId}`);
}

onMounted(() => {
  if (!mapElement.value) {
    return;
  }

  leafletMap = map(mapElement.value, {
    zoomControl: false,
    attributionControl: false,
    scrollWheelZoom: true,
    touchZoom: true,
    doubleClickZoom: false,
    maxBoundsViscosity: 1,
  }).setView(defaultCenter, 13);

  control.zoom({ position: 'topright' }).addTo(leafletMap);
  control.attribution({ position: 'bottomleft', prefix: false }).addTo(leafletMap);

  syncBaseLayer();

  markerLayer = featureGroup().addTo(leafletMap);
  updateCbdBounds();
  applyFixedBounds();
  renderMarkers();

  window.setTimeout(() => {
    leafletMap?.invalidateSize();
    applyFixedBounds();
  }, 120);

  if (typeof MutationObserver !== 'undefined' && typeof document !== 'undefined') {
    themeObserver = new MutationObserver(() => {
      syncBaseLayer();
    });

    themeObserver.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['class'],
    });
  }
});

watch(
  () => props.externalSelectedId,
  (id) => {
    if (id) {
      selectedLocationId.value = id;
      emit('clear-external');
    }
  },
);

watch(
  selectedLocation,
  (location) => {
    renderMarkers();

    if (location) {
      focusLocationOnMap(location);
    }
  },
);

watch(
  () => props.locations,
  () => {
    if (
      selectedLocationId.value &&
      !props.locations.some((location) => location.locationId === selectedLocationId.value)
    ) {
      selectedLocationId.value = null;
    }

    renderMarkers();
  },
  { deep: true },
);

watch(
  () =>
    props.allLocations?.length
      ? props.allLocations
          .map((location) => `${location.locationId}:${location.coordinates.lat}:${location.coordinates.lon}`)
          .join('|')
      : null,
  (signature) => {
    if (!signature) {
      return;
    }

    updateCbdBounds();
    applyFixedBounds();
    renderMarkers();
  },
);

// When Quick View mode toggles, the grid layout shifts the map container width.
// Give CSS time to repaint (60ms) then resync Leaflet.
watch(
  () => props.compact,
  () => {
    window.setTimeout(() => {
      leafletMap?.invalidateSize();
      if (!isShowingRoute) applyFixedBounds();
    }, 60);
  },
);


onBeforeUnmount(() => {
  themeObserver?.disconnect();
  themeObserver = null;
  baseLayer = null;
  routePolyline = null;
  leafletMap?.remove();
  leafletMap = null;
  markerLayer = null;
});
</script>
