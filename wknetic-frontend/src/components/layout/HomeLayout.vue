<script setup lang="ts">
import TheHeader from '@/components/layout/TheHeader.vue'
import TheFooter from '@/components/layout/TheFooter.vue'
import { useTheme } from "@/composables/useTheme";

// 挂载自动切换主题
useTheme()
</script>

<template>
  <div class="w-auto h-screen bg-bg">
    <div class="min-h-screen flex flex-col bg-bg">
      <TheHeader />
      <main class="flex-1">
        <router-view /> <!-- 改回 router-view -->
      </main>
      <TheFooter />
    </div>
  </div>
</template>

<style scoped>
</style>
