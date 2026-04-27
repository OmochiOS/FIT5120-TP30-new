import type { FilterState, LightingType, Location } from '@/types/domain';

const calmLighting: LightingType[] = ['natural', 'controlled-indoor'];

function matchesSearch(location: Location, searchText: string): boolean {
  const query = searchText.trim().toLowerCase();

  if (!query) {
    return true;
  }

  return [location.name, location.category, location.addressOrArea, location.displayType]
    .join(' ')
    .toLowerCase()
    .includes(query);
}

function matchesCategory(location: Location, category: FilterState['category']): boolean {
  if (category === 'All') {
    return true;
  }

  return location.category === category;
}

function matchesQuickFilters(location: Location, state: FilterState['preferences']): boolean {
  if (state.noise && location.sensoryProfile.noiseLevel !== 'low') {
    return false;
  }

  if (state.lighting && !calmLighting.includes(location.sensoryProfile.lightingType)) {
    return false;
  }

  if (state.crowds && location.crowdSnapshot.crowdLevel !== 'low') {
    return false;
  }

  if (state.unpredictability && location.sensoryProfile.crowdPredictability !== 'stable') {
    return false;
  }

  if (state.activity !== 'any' && !location.activityTags.includes(state.activity)) {
    return false;
  }

  return true;
}

export function filterAndSortLocations(locations: Location[], state: FilterState): Location[] {
  return locations
    .filter((location) => matchesSearch(location, state.searchText))
    .filter((location) => matchesCategory(location, state.category))
    .filter((location) => matchesQuickFilters(location, state.preferences))
    .sort((left, right) => left.priority - right.priority);
}

export function searchAndSortLocations(locations: Location[], searchText: string): Location[] {
  return locations
    .filter((location) => matchesSearch(location, searchText))
    .sort((left, right) => left.priority - right.priority);
}
