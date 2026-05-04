import { mount, type VueWrapper } from '@vue/test-utils';
import { nextTick } from 'vue';
import { afterEach, describe, expect, it } from 'vitest';

import HomeView from '@/views/HomeView.vue';

const wrappers: VueWrapper[] = [];

function mountHomeView() {
  const wrapper = mount(HomeView, {
    global: {
      stubs: {
        RouterLink: {
          props: ['to'],
          template: '<a :href="to"><slot /></a>',
        },
      },
    },
  });

  wrappers.push(wrapper);
  return wrapper;
}

afterEach(() => {
  while (wrappers.length > 0) {
    wrappers.pop()?.unmount();
  }
});

describe('HomeView citation tooltips', () => {
  it('renders citation buttons for both why-this-matters cards', () => {
    const wrapper = mountHomeView();

    expect(wrapper.find('[data-testid="citation-button-participation"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="citation-button-sensitivity"]').exists()).toBe(true);
  });

  it('opens the left citation tooltip with the Orsmond reference', async () => {
    const wrapper = mountHomeView();

    await wrapper.get('[data-testid="citation-button-participation"]').trigger('click');
    await nextTick();

    expect(wrapper.get('[data-testid="citation-popup-participation"]').text()).toContain(
      '[1] Orsmond et al. (2013)',
    );
    expect(
      wrapper.get('[data-testid="citation-link-participation-1"]').attributes('href'),
    ).toBe('https://link.springer.com/article/10.1007/s10803-013-1833-8');
  });

  it('opens the right citation tooltip with both references and keeps only one popup open', async () => {
    const wrapper = mountHomeView();

    await wrapper.get('[data-testid="citation-button-participation"]').trigger('click');
    await nextTick();
    expect(wrapper.find('[data-testid="citation-popup-participation"]').exists()).toBe(true);

    await wrapper.get('[data-testid="citation-button-sensitivity"]').trigger('click');
    await nextTick();

    expect(wrapper.find('[data-testid="citation-popup-participation"]').exists()).toBe(false);
    const sensitivityPopup = wrapper.get('[data-testid="citation-popup-sensitivity"]');
    expect(sensitivityPopup.classes()).toContain('home-stat-card__tooltip--stacked');
    expect(sensitivityPopup.text()).toContain(
      '[1] Crane et al. (2009)',
    );
    expect(sensitivityPopup.text()).toContain(
      '[2] Kirby et al. (2022)',
    );
    expect(wrapper.get('[data-testid="citation-link-sensitivity-1"]').attributes('href')).toBe(
      'https://journals.sagepub.com/doi/10.1177/1362361309103794',
    );
    expect(wrapper.get('[data-testid="citation-link-sensitivity-2"]').attributes('href')).toBe(
      'https://onlinelibrary.wiley.com/doi/10.1002/aur.2670',
    );
  });

  it('closes citation tooltips on outside click and Escape', async () => {
    const wrapper = mountHomeView();

    await wrapper.get('[data-testid="citation-button-sensitivity"]').trigger('click');
    await nextTick();
    expect(wrapper.find('[data-testid="citation-popup-sensitivity"]').exists()).toBe(true);

    document.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }));
    await nextTick();
    expect(wrapper.find('[data-testid="citation-popup-sensitivity"]').exists()).toBe(false);

    await wrapper.get('[data-testid="citation-button-sensitivity"]').trigger('click');
    await nextTick();
    expect(wrapper.find('[data-testid="citation-popup-sensitivity"]').exists()).toBe(true);

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    await nextTick();
    expect(wrapper.find('[data-testid="citation-popup-sensitivity"]').exists()).toBe(false);
  });

  it('allows citation tooltips to be dragged to a new position', async () => {
    const wrapper = mountHomeView();

    await wrapper.get('[data-testid="citation-button-participation"]').trigger('click');
    await nextTick();

    const popup = wrapper.get('[data-testid="citation-popup-participation"]');
    await popup.trigger('mousedown', { clientX: 100, clientY: 120 });
    document.dispatchEvent(new MouseEvent('mousemove', { clientX: 132, clientY: 146, bubbles: true }));
    await nextTick();

    expect(popup.attributes('style')).toContain('translate(32px, 26px)');

    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
    await nextTick();
  });
});
