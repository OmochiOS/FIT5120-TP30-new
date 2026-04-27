import type { GoodToKnowItem, Location } from '@/types/domain';

const GOOD_TO_KNOW: Record<string, GoodToKnowItem[]> = {
  'flagstaff-gardens': [
    { kind: 'quiet-area', label: 'Quiet area', value: 'Outer lawn edges and bench pockets feel calmer than the central paths.' },
    { kind: 'transport', label: 'Transport', value: 'Flagstaff Station and tram stops allow a quick exit without crossing the whole park.' },
    { kind: 'toilets', label: 'Toilets', value: 'Public toilet access is available in the broader park precinct.' },
  ],
  'state-library-victoria': [
    { kind: 'quiet-area', label: 'Quiet area', value: 'Reading rooms and quieter study floors are usually the easiest recovery option.' },
    { kind: 'transport', label: 'Transport', value: 'Swanston Street trams and Melbourne Central Station are within a short walk.' },
    { kind: 'toilets', label: 'Toilets', value: 'Accessible toilets are available inside the building.' },
  ],
  'fitzroy-gardens': [
    { kind: 'quiet-area', label: 'Quiet area', value: 'Peripheral shaded paths are calmer than the main tourist routes.' },
    { kind: 'transport', label: 'Transport', value: 'Tram access is nearby, though the gardens still reward a short walking approach.' },
    { kind: 'toilets', label: 'Toilets', value: 'Public toilet facilities are available within the gardens precinct.' },
  ],
  'federation-square': [
    { kind: 'quiet-area', label: 'Quiet area', value: 'Use the outer river edge or station side if the central plaza becomes too active.' },
    { kind: 'transport', label: 'Transport', value: 'Immediate access to Flinders Street Station and multiple tram lines.' },
    { kind: 'toilets', label: 'Toilets', value: 'Public toilets are available within the venue complex.' },
  ],
  'melbourne-central': [
    { kind: 'quiet-area', label: 'Quiet area', value: 'No consistently calm zone is guaranteed; keep your exit route planned in advance.' },
    { kind: 'transport', label: 'Transport', value: 'Train, tram, and street exits are all close, which helps for fast departures.' },
    { kind: 'toilets', label: 'Toilets', value: 'Toilet access is available inside the centre but may require navigating busy circulation areas.' },
  ],
  'carlton-gardens': [
    { kind: 'quiet-area', label: 'Quiet area', value: 'Southern and outer garden paths feel calmer than the museum-facing side.' },
    { kind: 'transport', label: 'Transport', value: 'A short walk from tram stops in Carlton and the museum edge.' },
    { kind: 'toilets', label: 'Toilets', value: 'Public toilet access is available in the surrounding gardens and museum precinct.' },
  ],
  'docklands-library': [
    { kind: 'quiet-area', label: 'Quiet area', value: 'Upper floors and study areas are typically calmer than the foyer edge.' },
    { kind: 'transport', label: 'Transport', value: 'Docklands tram access is nearby, and the waterfront promenade offers an easy recovery route.' },
    { kind: 'toilets', label: 'Toilets', value: 'Toilets are available inside the library building.' },
  ],
  'treasury-gardens': [
    { kind: 'quiet-area', label: 'Quiet area', value: 'Garden seating toward the east edge offers the softest reset point.' },
    { kind: 'transport', label: 'Transport', value: 'Close to Parliament Station and several city tram corridors.' },
    { kind: 'toilets', label: 'Toilets', value: 'Public toilet access is available in the wider gardens precinct.' },
  ],
};

function fallbackGoodToKnow(location: Location): GoodToKnowItem[] {
  return [
    {
      kind: 'quiet-area',
      label: 'Quiet area',
      value: location.sensoryProfile.escapeRouteNotes,
    },
    {
      kind: 'transport',
      label: 'Transport',
      value: location.preparationGuide.arrivalTip,
    },
    {
      kind: 'toilets',
      label: 'Toilets',
      value: 'Check venue signage or staff guidance for toilets and amenities before settling in.',
    },
  ];
}

export function getGoodToKnow(location: Location): GoodToKnowItem[] {
  if (location.goodToKnowItems?.length) {
    return location.goodToKnowItems;
  }

  return GOOD_TO_KNOW[location.locationId] ?? fallbackGoodToKnow(location);
}
