<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'
import { ProgressProvider } from '@bprogress/vue'
import AppLayout from '@/components/layout/AppLayout.vue'

// 挂载自动切换主题
useTheme()
</script>

<template>
  <ProgressProvider color="var(--brand-default)">
    <AppLayout />
  </ProgressProvider>
</template>


<style>
/* 防止移动端下拉出现白色底框 */
html {
  background-color: var(--bg-default);
  overscroll-behavior: none;
  -webkit-overflow-scrolling: touch;
  height: 100%;
  overflow: auto;
  /* scrollbar-gutter: stable; */
}

body {
  background-color: var(--bg-default);
  overscroll-behavior: none;
  min-height: 100vh;
  height: 100%;
}

/* 确保在主题切换时背景色同步更新 */
html[data-theme="light"] {
  background-color: #ffffff;
}

html[data-theme="dark"] {
  background-color: #0f0f0f;
}
</style>

