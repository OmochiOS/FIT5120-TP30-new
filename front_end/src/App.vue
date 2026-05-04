<template>
  <div v-if="!authenticated" class="fixed inset-0 z-50 flex items-center justify-center bg-white dark:bg-gray-900">
    <div class="w-full max-w-sm rounded-2xl border border-gray-200 p-8 shadow-sm dark:border-white/10">
      <h1 class="mb-1 text-lg font-semibold text-gray-900 dark:text-white">Access Required</h1>
      <p class="mb-5 text-sm text-gray-500 dark:text-gray-400">Your IP: {{ userIp }}</p>
      <input
        v-model="password"
        type="password"
        placeholder="Enter password"
        class="mb-3 w-full rounded-lg border border-gray-200 px-4 py-2 text-sm outline-none focus:border-blue-500 dark:border-white/10 dark:bg-white/5 dark:text-white"
        @keydown.enter="submit"
      />
      <p v-if="error" class="mb-3 text-xs text-red-500">{{ error }}</p>
      <button
        class="w-full rounded-lg bg-blue-600 py-2 text-sm font-semibold text-white hover:bg-blue-700"
        @click="submit"
      >
        Enter
      </button>
    </div>
  </div>
  <AppShell v-else>
    <RouterView />
  </AppShell>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterView } from 'vue-router';

import AppShell from '@/components/layout/AppShell.vue';

const CORRECT_PASSWORD = '12345';
const AUTH_TTL_MS = 60 * 60 * 1000;

const authenticated = ref(false);
const password = ref('');
const error = ref('');
const userIp = ref('...');

function authKey(ip: string) {
  return `app_auth_${ip}`;
}

function isAuthValid(ip: string): boolean {
  const raw = localStorage.getItem(authKey(ip));
  if (!raw) return false;
  const expiry = Number(raw);
  return Date.now() < expiry;
}

function setAuth(ip: string) {
  localStorage.setItem(authKey(ip), String(Date.now() + AUTH_TTL_MS));
}

onMounted(async () => {
  try {
    const res = await fetch('https://api.ipify.org?format=json');
    const data = await res.json();
    userIp.value = data.ip;
  } catch {
    userIp.value = 'unknown';
  }
  authenticated.value = isAuthValid(userIp.value);
});

function submit() {
  if (password.value === CORRECT_PASSWORD) {
    setAuth(userIp.value);
    authenticated.value = true;
  } else {
    error.value = 'Incorrect password.';
  }
}
</script>
