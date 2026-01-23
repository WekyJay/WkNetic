<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'


// 挂载自动切换主题
useTheme()
</script>

<template>
  <router-view />
</template>

<style scoped>
</style>
