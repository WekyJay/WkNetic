<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProgress } from '@bprogress/vue'
import { useAuthStore } from '@/stores/auth'
import { initializePlugins, fetchInstalledPlugins } from '@/utils/plugin-manager'

const router = useRouter()
const authStore = useAuthStore()
const { start, stop } = useProgress()

// 初始化路由进度条监听
onMounted(() => {
  router.beforeEach(() => {
    start()
  })

  router.afterEach(() => {
    stop()
  })
})

// 初始化插件系统
onMounted(async () => {
  console.log('AppLayout mounted. 开始初始化插件系统...')

  try {
    // 从数据库获取已启用的插件列表
    const pluginIds = await fetchInstalledPlugins()
    console.log(`📡 从数据库获取到 ${pluginIds.length} 个已启用的插件:`, pluginIds)

    // 初始化插件系统（自动扫描、验证、加载）
    // 注意：权限已在安装时确认，这里无需再次检查
    await initializePlugins(pluginIds)

    console.log('✅ 插件系统初始化完成')
  } catch (error) {
    console.error('❌ 插件系统初始化失败:', error)
  }
})

// 应用初始化时恢复登录状态
onMounted(() => {
  authStore.checkAuth()
})
</script>

<template>
  <router-view />
</template>
