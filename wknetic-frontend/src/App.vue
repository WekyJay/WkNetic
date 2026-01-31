<script setup lang="ts">
import { onMounted } from 'vue'
import { useTheme } from '@/composables/useTheme'
import { useAuthStore } from '@/stores/auth'
import { initializePlugins, fetchInstalledPlugins } from '@/utils/plugin-manager'

const authStore = useAuthStore()

// 挂载自动切换主题
useTheme()

// 初始化插件系统
onMounted(async () => {
  console.log('App.vue mounted. 开始初始化插件系统...');
  
  try {
    // 从数据库获取已启用的插件列表
    const pluginIds = await fetchInstalledPlugins();
    console.log(`📡 从数据库获取到 ${pluginIds.length} 个已启用的插件:`, pluginIds);
    
    // 初始化插件系统（自动扫描、验证、加载）
    // 注意：权限已在安装时确认，这里无需再次检查
    await initializePlugins(pluginIds);
    
    console.log('✅ 插件系统初始化完成');
  } catch (error) {
    console.error('❌ 插件系统初始化失败:', error);
  }
});

// 应用初始化时恢复登录状态
onMounted(() => {
  authStore.checkAuth()
})
</script>

<template>
  <router-view />
</template>

<style>
/* 防止移动端下拉出现白色底框 */
html {
  background-color: var(--bg-default);
  overscroll-behavior: none;
  -webkit-overflow-scrolling: touch;
  height: 100%;
  overflow: auto;
  scrollbar-gutter: stable;
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

