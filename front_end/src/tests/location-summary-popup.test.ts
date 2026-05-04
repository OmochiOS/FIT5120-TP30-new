import { mount } from '@vue/test-utils';
import { nextTick } from 'vue';
import { describe, expect, it } from 'vitest';

import LocationSummaryPopup from '@/components/map/LocationSummaryPopup.vue';
import { seedLocations } from '@/utils/adapters';

const libraryLocation =
  seedLocations.find((location) => location.displayType === 'Library') ?? seedLocations[0];

describe('LocationSummaryPopup', () => {
  it('renders the redesigned summary layout and removes the old field list', () => {
    const wrapper = mount(LocationSummaryPopup, {
      props: {
        location: libraryLocation,
      },
    });

    expect(wrapper.text()).toContain('Location summary');
    expect(wrapper.text()).toContain(libraryLocation.name);
    expect(wrapper.get('[data-testid="summary-display-type"]').text()).toContain(
      libraryLocation.displayType,
    );
    expect(wrapper.get('[data-testid="summary-crowd-bar"]').text()).toContain('Crowd level');
    expect(wrapper.get('[data-testid="summary-crowd-bar"]').text()).toContain('Calm');
    expect(wrapper.get('[data-testid="summary-gauge"]').attributes('aria-label')).toBe(
      `Sensory score ${libraryLocation.sensoryScore} out of 4`,
    );
    expect(wrapper.get('[data-testid="summary-description"]').text()).toContain(
      'Description coming soon.',
    );

    expect(wrapper.text()).not.toContain('Location ID');
    expect(wrapper.text()).not.toContain('Coordinates');
    expect(wrapper.text()).not.toContain('Quick backend snapshot');
    expect(wrapper.text()).not.toContain(`${libraryLocation.sensoryScore}/4`);
  });

  it('keeps the close and view profile actions wired', async () => {
    const wrapper = mount(LocationSummaryPopup, {
      props: {
        location: libraryLocation,
      },
    });

    await wrapper.get('[data-testid="summary-close"]').trigger('click');
    await wrapper.get('[data-testid="summary-view-profile"]').trigger('click');

    expect(wrapper.emitted('close')).toHaveLength(1);
    expect(wrapper.emitted('view-profile')).toHaveLength(1);
  });

  it('allows the summary card to be dragged without changing its content', async () => {
    const wrapper = mount(LocationSummaryPopup, {
      attachTo: document.body,
      props: {
        location: libraryLocation,
      },
    });

    await wrapper.get('[data-testid="summary-panel"]').trigger('mousedown', {
      button: 0,
      clientX: 40,
      clientY: 56,
    });

    window.dispatchEvent(
      new MouseEvent('mousemove', {
        bubbles: true,
        clientX: 78,
        clientY: 104,
      }),
    );

    await nextTick();

    expect(wrapper.get('[data-testid="summary-shell"]').attributes('style')).toContain(
      'translate(38px, 48px)',
    );
    expect(wrapper.text()).toContain(libraryLocation.name);

    window.dispatchEvent(
      new MouseEvent('mouseup', {
        bubbles: true,
      }),
    );

    wrapper.unmount();
  });
});
