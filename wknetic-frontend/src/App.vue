<script setup lang="ts">
import { onMounted } from 'vue'
import { useTheme } from '@/composables/useTheme'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 挂载自动切换主题
useTheme()

// 应用初始化时恢复登录状态
onMounted(() => {
  authStore.checkAuth()
})
</script>

<template>
  <router-view />
</template>

<style scoped>
</style>
