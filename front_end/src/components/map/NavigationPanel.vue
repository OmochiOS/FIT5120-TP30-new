<template>
  <Transition name="nav-panel">
    <div
      v-if="visible"
      class="pointer-events-auto fixed left-3 right-3 top-4 z-[9992] sm:left-1/2 sm:right-auto sm:w-[600px] sm:-translate-x-1/2"
    >
      <div
        class="rounded-[20px] border border-white/55 bg-white/96 p-3.5 shadow-[0_12px_40px_rgba(0,0,0,0.18)] backdrop-blur-xl dark:border-[#6f8792] dark:bg-[#0f2029] dark:shadow-[0_16px_48px_rgba(0,0,0,0.48)]"
      >
        <!-- Header -->
        <div class="mb-3 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <div
              class="flex h-7 w-7 items-center justify-center rounded-full bg-[#5f8b84]/10 dark:bg-[#4a7fa8]/20"
            >
              <svg
                viewBox="0 0 16 16"
                fill="none"
                class="h-4 w-4 text-[#5f8b84] dark:text-[#7ab8d8]"
                aria-hidden="true"
              >
                <path
                  d="M8 2.5L13 6.5L8 14L3 6.5L8 2.5Z"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linejoin="round"
                />
                <circle cx="8" cy="7" r="1.5" fill="currentColor" />
              </svg>
            </div>
            <p
              class="text-[11px] font-semibold uppercase tracking-[0.16em] text-[#5f8b84] dark:text-[#7ab8d8]"
            >
              Walking Navigation
            </p>
          </div>
          <button
            type="button"
            class="inline-flex h-7 w-7 items-center justify-center rounded-full border border-sense-teal/12 bg-white/80 text-sense-ink/50 transition hover:border-sense-teal/24 hover:text-sense-ink dark:border-[#6f8792] dark:bg-[#1b323e] dark:text-slate-400 dark:hover:text-white"
            aria-label="Close navigation"
            @click="handleClose"
          >
            <svg viewBox="0 0 16 16" fill="none" class="h-3.5 w-3.5" aria-hidden="true">
              <path
                d="M4 4l8 8M12 4 4 12"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
              />
            </svg>
          </button>
        </div>

        <!-- Input area: stacks vertically on mobile, single row on sm+ -->
        <div class="flex flex-col gap-2 sm:flex-row sm:items-stretch">
          <!-- Row 1 (mobile) / Left element (desktop): Start point input -->
          <div class="relative min-w-0 flex-1">
            <div
              class="flex items-center gap-2 rounded-[13px] border border-sense-teal/15 bg-white px-3 py-2.5 transition focus-within:border-[#22c55e]/40 focus-within:shadow-[0_0_0_2px_rgba(34,197,94,0.10)] dark:border-[#4a6672] dark:bg-[#142a35] dark:focus-within:border-[#22c55e]/40"
            >
              <!-- From label + green dot -->
              <div class="flex shrink-0 items-center gap-1.5" aria-hidden="true">
                <span class="text-[10px] font-bold uppercase tracking-[0.12em] text-[#22c55e] dark:text-[#4ade80]">From</span>
                <span class="h-2.5 w-2.5 rounded-full bg-[#22c55e] shadow-[0_0_5px_rgba(34,197,94,0.6)]"></span>
              </div>
              <input
                ref="startInputRef"
                v-model="startQuery"
                type="text"
                autocomplete="off"
                placeholder="Start point address..."
                class="min-w-0 flex-1 bg-transparent text-[13px] text-sense-ink outline-none placeholder:text-sense-ink/35 dark:text-slate-100 dark:placeholder:text-slate-500"
                aria-label="Start point"
                @input="onStartInput"
                @focus="showSuggestions = true"
                @keydown.escape="showSuggestions = false"
                @keydown.enter="selectFirstSuggestion"
              />
              <button
                v-if="startQuery"
                type="button"
                class="shrink-0 text-sense-ink/30 transition hover:text-sense-ink/60 dark:text-slate-500 dark:hover:text-slate-300"
                aria-label="Clear start"
                @click="clearStart"
              >
                <svg viewBox="0 0 16 16" fill="none" class="h-3 w-3" aria-hidden="true">
                  <path
                    d="M4 4l8 8M12 4 4 12"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                  />
                </svg>
              </button>
              <button
                v-else
                type="button"
                class="shrink-0 text-[#5f8b84] transition hover:text-[#4f7771] dark:text-[#7ab8d8] dark:hover:text-[#5c98b5]"
                aria-label="Locate me"
                title="Use my location"
                @click="autoLocate"
              >
                <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8" class="h-4 w-4" aria-hidden="true">
                  <circle cx="10" cy="10" r="4" />
                  <path d="M10 2v4M10 14v4M2 10h4M14 10h4" stroke-linecap="round" />
                </svg>
              </button>
            </div>

            <!-- Geocoding suggestions dropdown: full-width of the input container -->
            <Transition name="suggestions">
              <div
                v-if="showSuggestions && suggestions.length"
                class="absolute left-0 right-0 top-full z-10 mt-1.5 overflow-hidden rounded-[13px] border border-sense-teal/10 bg-white shadow-[0_8px_24px_rgba(0,0,0,0.12)] dark:border-[#4a6672] dark:bg-[#0f2029]"
              >
                <button
                  v-for="suggestion in suggestions"
                  :key="suggestion.id"
                  type="button"
                  class="flex w-full items-start gap-2.5 px-3 py-2.5 text-left transition hover:bg-sense-teal/5 dark:hover:bg-[#142a35]"
                  @mousedown.prevent="selectSuggestion(suggestion)"
                >
                  <svg
                    viewBox="0 0 16 16"
                    fill="none"
                    class="mt-0.5 h-3.5 w-3.5 shrink-0 text-sense-ink/30 dark:text-slate-500"
                    aria-hidden="true"
                  >
                    <path
                      d="M8 1.5C5.51 1.5 3.5 3.51 3.5 6c0 3.75 4.5 8.5 4.5 8.5S12.5 9.75 12.5 6c0-2.49-2.01-4.5-4.5-4.5Z"
                      stroke="currentColor"
                      stroke-width="1.4"
                    />
                    <circle cx="8" cy="6" r="1.5" fill="currentColor" />
                  </svg>
                  <div class="min-w-0">
                    <p class="truncate text-[12px] font-semibold text-sense-ink dark:text-slate-100">
                      {{ suggestion.text }}
                    </p>
                    <p class="truncate text-[11px] text-sense-ink/45 dark:text-slate-400">
                      {{ suggestion.place_name }}
                    </p>
                  </div>
                </button>
              </div>
            </Transition>
          </div>

          <!-- Row 2 (mobile) / Right elements (desktop): arrow + destination + button -->
          <div class="flex min-w-0 items-center gap-1.5 sm:gap-2">
            <!-- Arrow -->
            <div class="hidden shrink-0 items-center sm:flex">
              <svg
                viewBox="0 0 20 20"
                fill="none"
                class="h-4 w-4 text-sense-ink/25 dark:text-slate-500"
                aria-hidden="true"
              >
                <path
                  d="M4 10h12M12 6l4 4-4 4"
                  stroke="currentColor"
                  stroke-width="1.6"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </div>

            <!-- Destination (readonly) -->
            <div
              class="flex min-w-0 flex-1 items-center gap-1.5 rounded-[13px] border border-[#ef4444]/20 bg-[#ef4444]/[0.06] px-2 py-2.5 sm:flex-none sm:min-w-0 sm:gap-2 sm:px-3 dark:border-[#ef4444]/25 dark:bg-[#2a1515]"
            >
              <!-- To label + red pin -->
              <div class="flex shrink-0 items-center gap-1.5" aria-hidden="true">
                <span class="text-[10px] font-bold uppercase tracking-[0.12em] text-[#ef4444] dark:text-[#f87171]">To</span>
                <svg viewBox="0 0 14 18" fill="none" class="h-3.5 w-3 shrink-0 text-[#ef4444] dark:text-[#f87171]" aria-hidden="true">
                  <path d="M7 0C4.24 0 2 2.24 2 5c0 3.75 5 11 5 11s5-7.25 5-11c0-2.76-2.24-5-5-5Z" fill="currentColor" fill-opacity="0.2" stroke="currentColor" stroke-width="1.3"/>
                  <circle cx="7" cy="5" r="2" fill="currentColor"/>
                </svg>
              </div>
              <span
                class="min-w-0 truncate text-[13px] font-semibold text-sense-ink dark:text-slate-100 sm:max-w-[130px]"
                :title="destinationName"
              >
                {{ destinationName }}
              </span>
            </div>

            <!-- Show Route Button -->
            <button
              type="button"
              :disabled="!selectedStart || loading"
              class="shrink-0 rounded-[13px] bg-[#5f8b84] px-3 py-2.5 text-[12px] sm:px-4 sm:text-[13px] font-semibold text-white shadow-sm transition hover:bg-[#4f7771] disabled:cursor-not-allowed disabled:opacity-50 dark:bg-[#4a7fa8] dark:text-white dark:hover:bg-[#3d6a91]"
              @click="computeRoute"
            >
              <span v-if="loading" class="flex items-center gap-1.5">
                <svg
                  class="h-3.5 w-3.5 animate-spin"
                  viewBox="0 0 24 24"
                  fill="none"
                  aria-hidden="true"
                >
                  <circle
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    stroke-width="3"
                    stroke-opacity="0.25"
                  />
                  <path
                    d="M12 2a10 10 0 0 1 10 10"
                    stroke="currentColor"
                    stroke-width="3"
                    stroke-linecap="round"
                  />
                </svg>
                <span>Loading</span>
              </span>
              <span v-else>Show Route</span>
            </button>
          </div>
        </div>


        <!-- Route Result Bar -->
        <Transition name="result-slide">
          <div
            v-if="routeResult"
            class="mt-2.5 flex items-center justify-between rounded-[12px] border border-[#4a7fa8]/15 bg-[#4a7fa8]/10 px-3 py-2 dark:border-[#4a7fa8]/20 dark:bg-[#142a35]"
          >
            <div class="flex flex-wrap items-center gap-x-3 gap-y-1">
              <div class="flex items-center gap-1.5">
                <svg
                  viewBox="0 0 16 16"
                  fill="none"
                  class="h-3.5 w-3.5 text-[#4a7fa8] dark:text-[#7ab8d8]"
                  aria-hidden="true"
                >
                  <circle cx="6" cy="13" r="1.5" fill="currentColor" />
                  <circle cx="6" cy="3" r="1.5" fill="currentColor" />
                  <path
                    d="M6 4.5v7M6 4.5C6 4.5 10 5.5 10 8s-4 3.5-4 3.5"
                    stroke="currentColor"
                    stroke-width="1.4"
                    stroke-linecap="round"
                  />
                </svg>
                <span class="text-[13px] font-semibold text-sense-ink dark:text-slate-100">
                  {{ formatDuration(routeResult.duration) }}
                </span>
              </div>
              <span class="text-[12px] text-sense-ink/50 dark:text-slate-400">
                {{ formatDistance(routeResult.distance) }}
              </span>
              <span
                v-if="enRouteCount > 0"
                class="inline-flex items-center gap-1 rounded-full bg-[#4a7fa8]/12 px-2 py-0.5 text-[11px] font-semibold text-[#4a7fa8] dark:bg-[#4a7fa8]/20 dark:text-[#7ab8d8]"
              >
                <svg viewBox="0 0 12 12" fill="currentColor" class="h-2.5 w-2.5" aria-hidden="true">
                  <circle cx="6" cy="6" r="5" />
                </svg>
                {{ enRouteCount }} location{{ enRouteCount !== 1 ? 's' : '' }} along the way
              </span>
            </div>
            <button
              type="button"
              class="ml-2 shrink-0 text-[12px] font-medium text-sense-ink/45 transition hover:text-sense-ink/80 dark:text-slate-500 dark:hover:text-slate-300"
              @click="clearRoute"
            >
              Clear
            </button>
          </div>
        </Transition>

        <!-- Error -->
        <Transition name="result-slide">
          <p
            v-if="errorMsg"
            class="mt-2 text-[12px] font-medium text-rose-600 dark:text-rose-400"
          >
            {{ errorMsg }}
          </p>
        </Transition>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue';

import type { Location } from '@/types/domain';
import { useLocationsStore } from '@/stores/locations';
import { searchAndSortLocations } from '@/utils/filters';
import { fetchNearbyQuietPlaces } from '@/utils/java-backend';

// ── Props / Emits ──────────────────────────────────────────────────────────────

const props = defineProps<{
  visible: boolean;
  destinationLocation: Location | null;
}>();

const emit = defineEmits<{
  'route-computed': [
    payload: {
      /** GeoJSON coordinates converted to Leaflet [lat, lng][] */
      leafletCoords: [number, number][];
      duration: number;
      distance: number;
      enRouteIds: string[];
      quietPlaceIds: string[];
      startLocationId: string | null;
      allLocations: Location[];
    },
  ];
  'route-cleared': [];
  close: [];
}>();

// ── Constants ──────────────────────────────────────────────────────────────────

const MAPBOX_TOKEN = import.meta.env.VITE_MAPBOX_ACCESS_TOKEN as string;
// Max distance (meters) from route path to consider a location "along the way"
const EN_ROUTE_THRESHOLD_M = 80;

// ── State ─────────────────────────────────────────────────────────────────────

const startInputRef = ref<HTMLInputElement | null>(null);
const startQuery = ref('');
const suggestions = ref<GeocodingFeature[]>([]);
const showSuggestions = ref(false);
const selectedStart = ref<[number, number] | null>(null); // [lng, lat]
const selectedStartId = ref<string | null>(null); // Location ID if it's a known location
const loading = ref(false);
const errorMsg = ref('');
const routeResult = ref<{ duration: number; distance: number } | null>(null);
const enRouteCount = ref(0);

const locationsStore = useLocationsStore();

// ── Types ─────────────────────────────────────────────────────────────────────

interface GeocodingFeature {
  id: string;
  text: string;
  place_name: string;
  center: [number, number]; // [lng, lat]
}

interface DirectionsRoute {
  geometry: {
    coordinates: [number, number][]; // [lng, lat][]
  };
  duration: number;
  distance: number;
}

// ── Computed helpers ──────────────────────────────────────────────────────────

const destinationName = ref('');
watch(
  () => props.destinationLocation,
  (loc) => {
    destinationName.value = loc?.name ?? '';
  },
  { immediate: true },
);

// ── Geocoding ─────────────────────────────────────────────────────────────────

function onStartInput() {
  selectedStart.value = null;
  selectedStartId.value = null;
  errorMsg.value = '';

  if (!startQuery.value.trim()) {
    suggestions.value = [];
    return;
  }

  // Restrict search to internal locations only, bypassing Mapbox Geocoding
  const results = searchAndSortLocations(locationsStore.locations, startQuery.value);
  
  suggestions.value = results.slice(0, 6).map((loc) => ({
    id: loc.locationId,
    text: loc.name,
    place_name: loc.category || 'Location',
    center: [loc.coordinates.lon, loc.coordinates.lat],
  }));
}

function selectSuggestion(feature: GeocodingFeature) {
  startQuery.value = feature.text;
  selectedStart.value = feature.center;
  selectedStartId.value = feature.id;
  suggestions.value = [];
  showSuggestions.value = false;
  errorMsg.value = '';
}

function selectFirstSuggestion() {
  if (suggestions.value[0]) selectSuggestion(suggestions.value[0]);
}

function clearStart() {
  startQuery.value = '';
  selectedStart.value = null;
  selectedStartId.value = null;
  suggestions.value = [];
  errorMsg.value = '';
  void nextTick(() => startInputRef.value?.focus());
}

function autoLocate() {
  if (!navigator.geolocation) {
    errorMsg.value = 'Geolocation is not supported by your browser.';
    return;
  }

  loading.value = true;
  errorMsg.value = '';

  navigator.geolocation.getCurrentPosition(
    (position) => {
      const { latitude, longitude } = position.coords;
      const cbdLat = -37.8136;
      const cbdLng = 144.9631;

      // Max service radius 30km
      if (haversineM(latitude, longitude, cbdLat, cbdLng) > 30000) {
        errorMsg.value = 'Routing is only available within Melbourne CBD';
        loading.value = false;
        return;
      }

      // Snap to the nearest location if within 1000m threshold, otherwise reject.
      const locations = locationsStore.locations;
      if (!locations.length) {
        loading.value = false;
        return;
      }

      let closestLoc = locations[0];
      let minD = Infinity;

      for (const loc of locations) {
        const d = haversineM(latitude, longitude, loc.coordinates.lat, loc.coordinates.lon);
        if (d < minD) {
          minD = d;
          closestLoc = loc;
        }
      }

      // Snap threshold is 1000m
      if (minD > 1000) {
        errorMsg.value = 'Routing is only available within Melbourne CBD';
        loading.value = false;
        return;
      }

      selectedStart.value = [closestLoc.coordinates.lon, closestLoc.coordinates.lat];
      startQuery.value = closestLoc.name;
      selectedStartId.value = closestLoc.locationId;
      
      void computeRoute();
    },
    (err) => {
      loading.value = false;
      if (err.code === err.PERMISSION_DENIED) {
        errorMsg.value = 'Location access was denied. Please input manually.';
      } else {
        errorMsg.value = 'Failed to get location. Please input manually.';
      }
    },
    { timeout: 10000, maximumAge: 60000 }
  );
}

// ── Directions ────────────────────────────────────────────────────────────────

async function computeRoute() {
  if (!selectedStart.value || !props.destinationLocation) return;

  const [startLng, startLat] = selectedStart.value;
  const destLat = props.destinationLocation.coordinates.lat;
  const destLng = props.destinationLocation.coordinates.lon;

  loading.value = true;
  errorMsg.value = '';

  try {
    const url =
      `https://api.mapbox.com/directions/v5/mapbox/walking/` +
      `${startLng},${startLat};${destLng},${destLat}` +
      `?geometries=geojson&access_token=${MAPBOX_TOKEN}`;

    const res = await fetch(url);
    const data = (await res.json()) as { routes: DirectionsRoute[]; message?: string };

    if (!data.routes?.length) {
      errorMsg.value = data.message ?? 'No walking route found. Try a different start point.';
      return;
    }

    const route = data.routes[0];
    // Convert Mapbox [lng, lat] → Leaflet [lat, lng]
    const leafletCoords: [number, number][] = route.geometry.coordinates.map(
      ([lng, lat]) => [lat, lng],
    );

    routeResult.value = { duration: route.duration, distance: route.distance };

    let quietPlaceIds: string[] = [];
    try {
      const quietPlaces = await fetchNearbyQuietPlaces(props.destinationLocation.locationId, 200);
      // Backend returns sorted by noise. We just take up to top 3.
      quietPlaceIds = quietPlaces.slice(0, 3).map((q) => String(q.id));
    } catch (err) {
      console.warn('Failed to fetch nearby quiet places', err);
    }

    // Emit with all needed data so MapCanvas can compute en-route IDs
    emit('route-computed', {
      leafletCoords,
      duration: route.duration,
      distance: route.distance,
      enRouteIds: [], // filled by MapCanvas after en-route check
      quietPlaceIds,
      startLocationId: selectedStartId.value,
      allLocations: [], // filled by MapCanvas
    });
  } catch {
    errorMsg.value = 'Network error. Please check your connection and try again.';
  } finally {
    loading.value = false;
  }
}

// Called by parent (MapCanvas) after it calculates en-route locations
function setEnRouteCount(count: number) {
  enRouteCount.value = count;
}

function clearRoute() {
  routeResult.value = null;
  enRouteCount.value = 0;
  emit('route-cleared');
}

function handleClose() {
  clearRoute();
  emit('close');
}

// Reset when panel closes
watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      if (!startQuery.value) {
        autoLocate();
      }
    } else {
      startQuery.value = '';
      selectedStart.value = null;
      selectedStartId.value = null;
      suggestions.value = [];
      showSuggestions.value = false;
      errorMsg.value = '';
      routeResult.value = null;
      enRouteCount.value = 0;
    }
  },
);

// ── Helpers ───────────────────────────────────────────────────────────────────

function formatDuration(seconds: number): string {
  const mins = Math.round(seconds / 60);
  if (mins < 60) return `${mins} min walk`;
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  return m > 0 ? `${h}h ${m} min walk` : `${h}h walk`;
}

function formatDistance(meters: number): string {
  if (meters < 1000) return `${Math.round(meters)} m`;
  return `${(meters / 1000).toFixed(1)} km`;
}

// ── En-route calculation (pure, used by parent) ────────────────────────────────

/** Haversine distance between two lat/lng points (metres) */
function haversineM(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const R = 6371000;
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLng = ((lng2 - lng1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) * Math.sin(dLng / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

/** Minimum distance (metres) from point to any vertex on the polyline */
function minDistToRoute(lat: number, lng: number, routeCoords: [number, number][]): number {
  let minDist = Infinity;
  for (const [rLat, rLng] of routeCoords) {
    const d = haversineM(lat, lng, rLat, rLng);
    if (d < minDist) minDist = d;
  }
  return minDist;
}

function computeEnRouteIds(
  routeCoords: [number, number][],
  locations: Location[],
  threshold = EN_ROUTE_THRESHOLD_M,
): string[] {
  return locations
    .filter(
      (loc) =>
        Number.isFinite(loc.coordinates.lat) &&
        Number.isFinite(loc.coordinates.lon) &&
        minDistToRoute(loc.coordinates.lat, loc.coordinates.lon, routeCoords) <= threshold,
    )
    .map((loc) => loc.locationId);
}

// Expose helpers for MapCanvas to call
defineExpose({ computeEnRouteIds, setEnRouteCount });
</script>

<style scoped>
/* Panel entrance */
.nav-panel-enter-active,
.nav-panel-leave-active {
  transition:
    opacity 200ms ease,
    transform 200ms ease;
}
.nav-panel-enter-from,
.nav-panel-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.97);
}

/* Suggestions dropdown */
.suggestions-enter-active,
.suggestions-leave-active {
  transition:
    opacity 140ms ease,
    transform 140ms ease;
  transform-origin: top center;
}
.suggestions-enter-from,
.suggestions-leave-to {
  opacity: 0;
  transform: scaleY(0.94) translateY(-4px);
}

/* Route result bar */
.result-slide-enter-active,
.result-slide-leave-active {
  transition:
    opacity 180ms ease,
    transform 180ms ease;
}
.result-slide-enter-from,
.result-slide-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
