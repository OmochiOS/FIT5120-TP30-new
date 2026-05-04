import { fileURLToPath, URL } from 'node:url';
import fs from 'node:fs';
import path from 'node:path';

import vue from '@vitejs/plugin-vue';
import { defineConfig } from 'vite';

export default defineConfig({
  plugins: [
    vue(),
    {
      name: 'copy-index-to-404',
      closeBundle() {
        const __dirname = fileURLToPath(new URL('.', import.meta.url));
        const dist = path.resolve(__dirname, 'dist');
        const index = path.join(dist, 'index.html');
        const errorDir = path.join(dist, 'error');
        if (fs.existsSync(index)) {
          if (!fs.existsSync(errorDir)) {
            fs.mkdirSync(errorDir);
          }
          fs.copyFileSync(index, path.join(errorDir, '404.html'));
        }
      }
    }
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    fs: {
      allow: [fileURLToPath(new URL('..', import.meta.url))],
    },
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    include: ['src/**/*.test.ts'],
  },
});
