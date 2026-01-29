<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import * as pluginApi from '@/api/plugin'
import type { PluginStatusVO } from '@/api/plugin'
import { installPlugin, uninstallPlugin, togglePluginStatus } from '@/utils/plugin-install'

const { t } = useI18n()

// 已安装的插件列表
const installedPlugins = ref<PluginStatusVO[]>([])
const loading = ref(false)
const error = ref('')

// 可用插件列表（从 public/plugins 扫描）
const availablePlugins = ref([
  {
    id: 'wk-checkin',
    name: '每日签到',
    version: '1.0.0',
    description: '提供每日签到功能，用户可以通过签到获得积分奖励',
    author: 'WkNetic Team',
    permissions: ['http:api', 'storage:local', 'ui:notification'],
    installed: false
  },
  {
    id: 'wk-pure-js',
    name: '纯JS插件示例',
    version: '1.0.0',
    description: '演示如何创建纯JavaScript插件',
    author: 'WkNetic Team',
    permissions: ['ui:modal', 'storage:session'],
    installed: false
  }
])

// 加载已安装的插件
async function loadInstalledPlugins() {
  loading.value = true
  error.value = ''
  try {
    const response = await pluginApi.getInstalledPlugins()
    installedPlugins.value = response.data || []
    
    // 更新可用插件的安装状态
    availablePlugins.value.forEach(plugin => {
      plugin.installed = installedPlugins.value.some(p => p.pluginId === plugin.id)
    })
  } catch (err: any) {
    error.value = err.message || '加载插件列表失败'
    console.error('加载插件失败:', err)
  } finally {
    loading.value = false
  }
}

// 安装插件
async function handleInstall(plugin: any) {
  try {
    await installPlugin({
      pluginId: plugin.id,
      pluginName: plugin.name,
      pluginVersion: plugin.version,
      permissions: plugin.permissions
    })
    await loadInstalledPlugins()
  } catch (err: any) {
    console.error('安装插件失败:', err)
    alert(err.message || '安装失败')
  }
}

// 卸载插件
async function handleUninstall(pluginId: string) {
  if (!confirm('确定要卸载此插件吗？')) return
  
  try {
    await uninstallPlugin(pluginId)
    await loadInstalledPlugins()
  } catch (err: any) {
    console.error('卸载插件失败:', err)
    alert(err.message || '卸载失败')
  }
}

// 切换插件状态
async function handleToggleStatus(plugin: PluginStatusVO) {
  try {
    await togglePluginStatus(plugin.pluginId, !plugin.enabled)
    await loadInstalledPlugins()
  } catch (err: any) {
    console.error('切换插件状态失败:', err)
    alert(err.message || '操作失败')
  }
}

// 获取权限风险等级颜色
function getPermissionRiskColor(permission: string): string {
  const highRisk = ['http:external', 'user:modify', 'file:download']
  const mediumRisk = ['http:api', 'storage:local', 'router:guard', 'websocket:connect']
  
  if (highRisk.some(p => permission.includes(p))) return 'text-red-500'
  if (mediumRisk.some(p => permission.includes(p))) return 'text-orange-500'
  return 'text-green-500'
}

onMounted(() => {
  loadInstalledPlugins()
})
</script>

<template>
  <div class="plugins-page">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-text mb-2">插件中心</h1>
      <p class="text-text-muted">管理和配置系统插件，扩展平台功能</p>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="mb-4 p-4 bg-red-500/10 border border-red-500/20 rounded-lg">
      <div class="flex items-center gap-2 text-red-500">
        <span class="i-tabler-alert-circle text-lg"></span>
        <span>{{ error }}</span>
      </div>
    </div>

    <!-- 已安装插件 -->
    <div class="mb-8">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-semibold text-text">已安装插件</h2>
        <button class="btn-ghost text-sm" @click="loadInstalledPlugins">
          <span class="i-tabler-refresh" :class="{ 'animate-spin': loading }"></span>
          刷新
        </button>
      </div>

      <div v-if="loading" class="text-center py-12 text-text-muted">
        <span class="i-tabler-loader-2 text-2xl animate-spin"></span>
        <p class="mt-2">加载中...</p>
      </div>

      <div v-else-if="installedPlugins.length === 0" class="text-center py-12 bg-bg border border-border rounded-lg">
        <span class="i-tabler-package-off text-4xl text-text-muted mb-2"></span>
        <p class="text-text-muted">暂无已安装的插件</p>
      </div>

      <div v-else class="grid gap-4">
        <div 
          v-for="plugin in installedPlugins" 
          :key="plugin.pluginId"
          class="bg-bg border border-border rounded-lg p-6 hover:border-brand/50 transition-colors"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-2">
                <div class="w-12 h-12 bg-brand/10 rounded-lg flex-center">
                  <span class="i-tabler-puzzle text-brand text-xl"></span>
                </div>
                <div>
                  <h3 class="text-lg font-semibold text-text">{{ plugin.pluginName }}</h3>
                  <p class="text-sm text-text-muted">v{{ plugin.pluginVersion }} · ID: {{ plugin.pluginId }}</p>
                </div>
                <div 
                  :class="[
                    'px-2 py-1 rounded text-xs font-medium',
                    plugin.enabled 
                      ? 'bg-green-500/10 text-green-500' 
                      : 'bg-gray-500/10 text-gray-500'
                  ]"
                >
                  {{ plugin.enabled ? '已启用' : '已禁用' }}
                </div>
              </div>

              <div class="mb-3">
                <p class="text-sm text-text-muted mb-2">权限：</p>
                <div class="flex flex-wrap gap-2">
                  <span 
                    v-for="permission in plugin.grantedPermissions" 
                    :key="permission"
                    :class="[
                      'px-2 py-1 rounded text-xs font-mono',
                      getPermissionRiskColor(permission)
                    ]"
                    class="bg-bg-darker"
                  >
                    {{ permission }}
                  </span>
                </div>
              </div>

              <div class="text-xs text-text-muted">
                安装时间: {{ new Date(plugin.installedAt).toLocaleString('zh-CN') }}
              </div>
            </div>

            <div class="flex gap-2 ml-4">
              <button 
                class="btn-ghost text-sm"
                @click="handleToggleStatus(plugin)"
                :title="plugin.enabled ? '禁用插件' : '启用插件'"
              >
                <span :class="plugin.enabled ? 'i-tabler-player-pause' : 'i-tabler-player-play'"></span>
                {{ plugin.enabled ? '禁用' : '启用' }}
              </button>
              <button 
                class="btn-ghost text-sm text-red-500 hover:bg-red-500/10"
                @click="handleUninstall(plugin.pluginId)"
              >
                <span class="i-tabler-trash"></span>
                卸载
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 可用插件市场 -->
    <div>
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-semibold text-text">可用插件</h2>
        <span class="text-sm text-text-muted">{{ availablePlugins.length }} 个插件</span>
      </div>

      <div class="grid gap-4">
        <div 
          v-for="plugin in availablePlugins" 
          :key="plugin.id"
          class="bg-bg border border-border rounded-lg p-6 hover:border-brand/50 transition-colors"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-3">
                <div class="w-12 h-12 bg-brand/10 rounded-lg flex-center">
                  <span class="i-tabler-puzzle text-brand text-xl"></span>
                </div>
                <div>
                  <h3 class="text-lg font-semibold text-text">{{ plugin.name }}</h3>
                  <p class="text-sm text-text-muted">v{{ plugin.version }} · by {{ plugin.author }}</p>
                </div>
              </div>

              <p class="text-text-muted mb-3">{{ plugin.description }}</p>

              <div class="mb-2">
                <p class="text-sm text-text-muted mb-2">需要权限：</p>
                <div class="flex flex-wrap gap-2">
                  <span 
                    v-for="permission in plugin.permissions" 
                    :key="permission"
                    :class="[
                      'px-2 py-1 rounded text-xs font-mono',
                      getPermissionRiskColor(permission)
                    ]"
                    class="bg-bg-darker"
                  >
                    {{ permission }}
                  </span>
                </div>
              </div>
            </div>

            <div class="ml-4">
              <button 
                v-if="!plugin.installed"
                class="btn-brand"
                @click="handleInstall(plugin)"
              >
                <span class="i-tabler-download"></span>
                安装
              </button>
              <div v-else class="px-4 py-2 bg-green-500/10 text-green-500 rounded text-sm font-medium">
                <span class="i-tabler-check"></span>
                已安装
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.plugins-page {
  @apply p-6;
}

.btn-brand {
  @apply px-4 py-2 bg-brand text-bg rounded-lg hover:bg-brand-hover transition-colors flex items-center gap-2 text-sm font-medium;
}

.btn-ghost {
  @apply px-3 py-1.5 text-text-muted hover:text-text hover:bg-bg-darker rounded-lg transition-colors flex items-center gap-1.5;
}
</style>
