import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it } from 'vitest';

import PreferencesDrawer from '@/components/preferences/PreferencesDrawer.vue';

function getNoiseButton(wrapper: ReturnType<typeof mount>) {
  const noiseButton = wrapper.find('[data-testid="preference-filter-noise"]');

  if (!noiseButton.exists()) {
    throw new Error('Noise button was not rendered.');
  }

  return noiseButton;
}

describe('PreferencesDrawer', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('shows the muted noise icon when the Noise filter is selected', async () => {
    const wrapper = mount(PreferencesDrawer, {
      props: {
        modelValue: true,
      },
      global: {
        stubs: {
          Teleport: true,
        },
      },
    });

    const noiseButton = getNoiseButton(wrapper);

    expect(noiseButton.attributes('aria-pressed')).toBe('false');
    expect(noiseButton.html()).toContain('a3 3 0 0 1 0 6');

    await noiseButton.trigger('click');

    expect(noiseButton.attributes('aria-pressed')).toBe('true');
    expect(noiseButton.html()).toContain('x1="13" y1="7" x2="19" y2="13"');
    expect(noiseButton.html()).not.toContain('a3 3 0 0 1 0 6');
  });
});
