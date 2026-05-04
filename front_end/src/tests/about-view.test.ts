import { mount, type VueWrapper } from '@vue/test-utils';
import { nextTick } from 'vue';
import { afterEach, describe, expect, it } from 'vitest';

import AboutView from '@/views/AboutView.vue';

const wrappers: VueWrapper[] = [];

function mountAboutView() {
  const wrapper = mount(AboutView, {
    attachTo: document.body,
  });

  wrappers.push(wrapper);
  return wrapper;
}

afterEach(() => {
  while (wrappers.length > 0) {
    wrappers.pop()?.unmount();
  }
});

describe('AboutView redesign', () => {
  it('renders the redesigned sections and key content blocks', () => {
    const wrapper = mountAboutView();

    expect(wrapper.text()).toContain('How we calculate Sensory Score');
    expect(wrapper.text()).toContain('Autism & Sensory Experience');
    expect(wrapper.text()).toContain('The hidden accessibility gap');
    expect(wrapper.text()).toContain('How you can help');

    expect(wrapper.text()).toContain('Noise');
    expect(wrapper.text()).toContain('Unpredictability');
    expect(wrapper.text()).toContain('Lighting');
    expect(wrapper.text()).toContain('Quiet Modifier');
    expect(wrapper.text()).toContain('×0.35');
    expect(wrapper.text()).toContain('×0.30');
    expect(wrapper.text()).toContain('×0.20');
    expect(wrapper.text()).toContain('×0.15');

    expect(wrapper.text()).toContain('1 in 40');
    expect(wrapper.text()).toContain('Physical Accessibility');
    expect(wrapper.text()).toContain('Sensory Accessibility');
    expect(wrapper.get('[data-testid="about-vs-badge"]').text()).toContain('vs.');
    expect(wrapper.text()).toContain('Be aware');
    expect(wrapper.text()).toContain('Plan ahead');
    expect(wrapper.text()).toContain('Support inclusion');
  });

  it('opens factor and statistic tooltips while keeping only one open at a time', async () => {
    const wrapper = mountAboutView();

    await wrapper.get('[data-testid="about-tooltip-button-noise"]').trigger('click');
    await nextTick();

    expect(wrapper.get('[data-testid="about-tooltip-popup-noise"]').text()).toContain(
      'Noise details coming soon.',
    );

    await wrapper.get('[data-testid="about-tooltip-button-statistic"]').trigger('click');
    await nextTick();

    expect(wrapper.find('[data-testid="about-tooltip-popup-noise"]').exists()).toBe(false);
    expect(wrapper.get('[data-testid="about-tooltip-popup-statistic"]').text()).toContain(
      'Statistic details coming soon.',
    );
  });

  it('closes tooltips on outside click and Escape', async () => {
    const wrapper = mountAboutView();

    await wrapper.get('[data-testid="about-tooltip-button-lighting"]').trigger('click');
    await nextTick();
    expect(wrapper.find('[data-testid="about-tooltip-popup-lighting"]').exists()).toBe(true);

    document.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }));
    await nextTick();
    expect(wrapper.find('[data-testid="about-tooltip-popup-lighting"]').exists()).toBe(false);

    await wrapper.get('[data-testid="about-tooltip-button-quiet-modifier"]').trigger('click');
    await nextTick();
    expect(wrapper.find('[data-testid="about-tooltip-popup-quiet-modifier"]').exists()).toBe(true);

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    await nextTick();
    expect(wrapper.find('[data-testid="about-tooltip-popup-quiet-modifier"]').exists()).toBe(false);
  });
});
