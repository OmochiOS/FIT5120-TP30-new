import sourceRegisterJson from '../../../data/source-register.json';
import type { SearchCoveragePlace } from '@/types/domain';

interface RawSourceRegisterFile {
  sources: Array<{
    datasetId: string;
    accessPattern: string;
  }>;
}

interface RawLandmarksResponse {
  total_count: number;
  results: RawLandmarkRecord[];
}

interface RawLandmarkRecord {
  theme?: string;
  sub_theme?: string;
  feature_name?: string;
  co_ordinates?: {
    lat?: number;
    lon?: number;
  };
}

const sourceRegister = sourceRegisterJson as RawSourceRegisterFile;
const landmarksEndpointTemplate =
  sourceRegister.sources.find(
    (source) =>
      source.datasetId ===
      'landmarks-and-places-of-interest-including-schools-theatres-health-services-spor',
  )?.accessPattern ??
  'https://data.melbourne.vic.gov.au/api/explore/v2.1/catalog/datasets/landmarks-and-places-of-interest-including-schools-theatres-health-services-spor/records?limit=100&offset={offset}';

function normaliseQuery(value: string): string {
  return value.trim().toLowerCase();
}

function buildLandmarksUrl(offset: number, limit: number): string {
  return landmarksEndpointTemplate
    .replace('{offset}', String(offset))
    .replace('limit=100', `limit=${limit}`);
}

function toCoveragePlace(record: RawLandmarkRecord): SearchCoveragePlace | null {
  const name = record.feature_name?.trim();

  if (!name) {
    return null;
  }

  const lat = record.co_ordinates?.lat;
  const lon = record.co_ordinates?.lon;

  return {
    name,
    theme: record.theme?.trim() || 'Place of interest',
    subTheme: record.sub_theme?.trim() || 'Official landmark record',
    coordinates:
      typeof lat === 'number' && typeof lon === 'number'
        ? { lat, lon }
        : null,
  };
}

export async function fetchSearchCoveragePlaces(
  fetchImpl: typeof fetch = fetch,
): Promise<SearchCoveragePlace[]> {
  const limit = 100;
  const collected: SearchCoveragePlace[] = [];
  const seen = new Set<string>();
  let offset = 0;
  let totalCount = Number.POSITIVE_INFINITY;

  while (offset < totalCount) {
    const response = await fetchImpl(buildLandmarksUrl(offset, limit));

    if (!response.ok) {
      throw new Error(`Unable to fetch search coverage places: ${response.status}`);
    }

    const payload = (await response.json()) as RawLandmarksResponse;
    totalCount = payload.total_count;

    for (const record of payload.results ?? []) {
      const place = toCoveragePlace(record);

      if (!place) {
        continue;
      }

      const key = `${normaliseQuery(place.name)}|${normaliseQuery(place.theme)}`;

      if (seen.has(key)) {
        continue;
      }

      seen.add(key);
      collected.push(place);
    }

    offset += limit;
  }

  return collected.sort((left, right) => left.name.localeCompare(right.name));
}

export function filterCoveragePlaces(
  places: SearchCoveragePlace[],
  query: string,
  excludedSeedNames: Set<string>,
  limit = 5,
): SearchCoveragePlace[] {
  const normalisedQuery = normaliseQuery(query);

  if (normalisedQuery.length < 2) {
    return [];
  }

  return places
    .filter((place) => !excludedSeedNames.has(normaliseQuery(place.name)))
    .filter((place) =>
      [place.name, place.theme, place.subTheme].join(' ').toLowerCase().includes(normalisedQuery),
    )
    .slice(0, limit);
}
