<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <div class="container-main py-8">
    <h1 class="text-2xl font-bold text-text mb-6">Project: {{ route.params.id }}</h1>
    <p class="text-text-secondary">Project details page coming soon...</p>
  </div>
</template>
