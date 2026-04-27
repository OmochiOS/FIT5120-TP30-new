import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';

import LocationList from '@/components/map/LocationList.vue';
import type { Location } from '@/types/domain';
import { seedLocations } from '@/utils/adapters';

function makeLocations(count: number): Location[] {
  return Array.from({ length: count }, (_, index) => ({
    ...seedLocations[index % seedLocations.length],
    locationId: `location-${index + 1}`,
    name: `Location ${index + 1}`,
  }));
}

describe('LocationList', () => {
  it('shows up to ten map locations and links to the full locations page', () => {
    const wrapper = mount(LocationList, {
      props: {
        locations: makeLocations(12),
      },
      global: {
        stubs: {
          RouterLink: {
            props: ['to'],
            template: '<a :href="to"><slot /></a>',
          },
        },
      },
    });

    expect(wrapper.text()).toContain('More+');
    expect(wrapper.find('a[href="/locations"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('Location 10');
    expect(wrapper.text()).not.toContain('Location 11');
  });
});
