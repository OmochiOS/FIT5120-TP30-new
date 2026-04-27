<template>
  <section class="figma-panel-soft relative min-h-[440px] overflow-hidden p-3 md:min-h-[560px] md:p-4">
    <div class="amity-leaflet-shell">
      <div
        ref="mapElement"
        class="amity-leaflet-map"
        aria-label="AmitySpace live locations map"
      ></div>
    </div>

    <LocationSummaryPopup
      :location="selectedLocation"
      @close="closeLocationSummary"
      @view-profile="viewSelectedLocationDetail"
    />

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
  tileLayer,
  type LatLngBounds,
  type DivIcon,
  type FeatureGroup,
  type Map as LeafletMap,
} from 'leaflet';
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import MapLegend from '@/components/map/MapLegend.vue';
import LocationSummaryPopup from '@/components/map/LocationSummaryPopup.vue';
import type { Location } from '@/types/domain';

const props = defineProps<{
  locations: Location[];
  allLocations?: Location[];
}>();

const router = useRouter();
const mapElement = ref<HTMLElement | null>(null);
const selectedLocationId = ref<string | null>(null);

let leafletMap: LeafletMap | null = null;
let markerLayer: FeatureGroup | null = null;
let cbdBounds: LatLngBounds | null = null;

const defaultCenter: [number, number] = [-37.8136, 144.9631];
const fixedPadding: [number, number] = [34, 34];

const selectedLocation = computed(() => {
  if (!selectedLocationId.value) {
    return null;
  }

  return props.locations.find((location) => location.locationId === selectedLocationId.value) ?? null;
});

function markerTone(score: Location['sensoryScore']): string {
  if (score <= 2) {
    return 'amity-map-marker--calm';
  }

  if (score === 3) {
    return 'amity-map-marker--moderate';
  }

  return 'amity-map-marker--busy';
}

function buildMarkerIcon(location: Location): DivIcon {
  return divIcon({
    className: 'amity-map-marker-wrapper',
    iconSize: [34, 34],
    iconAnchor: [17, 17],
    html: `
      <span class="amity-map-marker ${markerTone(location.sensoryScore)}">
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

function renderMarkers() {
  if (!leafletMap || !markerLayer) {
    return;
  }

  markerLayer.clearLayers();

  const renderableLocations = getRenderableLocations(props.locations);

  for (const location of renderableLocations) {
    const locationMarker = marker([location.coordinates.lat, location.coordinates.lon], {
      icon: buildMarkerIcon(location),
      keyboard: true,
      riseOnHover: true,
      title: `${location.name}, sensory score ${location.sensoryScore}`,
    });

    locationMarker.on('click', () => {
      selectedLocationId.value = location.locationId;
    });

    markerLayer.addLayer(locationMarker);
  }
}

function closeLocationSummary() {
  selectedLocationId.value = null;
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

  tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 17,
    attribution: '&copy; OpenStreetMap contributors',
  }).addTo(leafletMap);

  markerLayer = featureGroup().addTo(leafletMap);
  updateCbdBounds();
  applyFixedBounds();
  renderMarkers();

  window.setTimeout(() => {
    leafletMap?.invalidateSize();
    applyFixedBounds();
  }, 0);
});

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

onBeforeUnmount(() => {
  leafletMap?.remove();
  leafletMap = null;
  markerLayer = null;
});
</script>
