import { createPinia } from 'pinia';
import { createApp } from 'vue';

import App from './App.vue';
import { router } from './router';
import { useLocationsStore } from './stores/locations';
import { usePreferencesStore } from './stores/preferences';
import { statusRules } from './utils/adapters';
import 'leaflet/dist/leaflet.css';
import './styles/index.css';

const app = createApp(App);
const pinia = createPinia();

function bootstrap() {
  app.use(pinia);
  app.use(router);
  app.mount('#app');

  const preferencesStore = usePreferencesStore(pinia);
  preferencesStore.hydrate();

  const locationsStore = useLocationsStore(pinia);

  void locationsStore.hydrateLocations().then(() =>
    Promise.all([
      locationsStore.refreshLiveData(),
      locationsStore.hydrateSearchCoverage(),
    ]),
  );

  if (typeof window !== 'undefined') {
    window.setInterval(() => {
      void locationsStore.refreshLiveData();
    }, statusRules.refreshIntervalMinutes * 60_000);
  }
}

bootstrap();
