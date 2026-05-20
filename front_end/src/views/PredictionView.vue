<template>
  <!-- Prediction page has been removed. Redirect to /map. -->
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
const router = useRouter();
router.replace('/map');
</script>
