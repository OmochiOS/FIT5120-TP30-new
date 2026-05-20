import { createRouter, createWebHistory } from 'vue-router';

import AboutView from '@/views/AboutView.vue';
import HomeView from '@/views/HomeView.vue';
import LocationDetailView from '@/views/LocationDetailView.vue';
import LocationsView from '@/views/LocationsView.vue';
import MapView from '@/views/MapView.vue';
import NotFoundView from '@/views/NotFoundView.vue';

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/map',
      name: 'map',
      component: MapView,
    },
    {
      path: '/locations',
      name: 'locations',
      component: LocationsView,
    },

    {
      path: '/about',
      name: 'about',
      component: AboutView,
    },
    {
      path: '/location/:locationId',
      name: 'location-detail',
      component: LocationDetailView,
      props: true,
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFoundView,
    },
  ],
  scrollBehavior() {
    return {
      top: 0,
    };
  },
});
