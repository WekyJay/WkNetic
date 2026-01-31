<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { pluginApi, type PluginStatus } from '@/api/plugin'
import { 
  WkButton, 
  WkCard, 
  WkBadge, 
  WkAlert, 
  WkDialog,
  WkLoading
} from '@/components/common'

// 已安装的插件列表
const installedPlugins = ref<PluginStatus[]>([])
const loading = ref(false)
const error = ref('')
const successMessage = ref('')

// 确认对话框状态
const confirmDialog = reactive({
  visible: false,
  title: '',
  message: '',
  onConfirm: () => {}
})

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
      plugin.installed = installedPlugins.value.some((p: PluginStatus) => p.pluginId === plugin.id)
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
    loading.value = true
    await pluginApi.installPlugin({
      pluginId: plugin.id,
      pluginName: plugin.name,
      pluginVersion: plugin.version,
      grantedPermissions: plugin.permissions
    })
    await loadInstalledPlugins()
    successMessage.value = '插件安装成功'
    setTimeout(() => successMessage.value = '', 3000)
  } catch (err: any) {
    console.error('安装插件失败:', err)
    error.value = err.message || '安装失败'
  } finally {
    loading.value = false
  }
}

// 卸载插件
function handleUninstall(pluginId: string, pluginName: string) {
  confirmDialog.visible = true
  confirmDialog.title = '卸载插件'
  confirmDialog.message = `确定要卸载插件"${pluginName}"吗？此操作无法撤销。`
  confirmDialog.onConfirm = async () => {
    try {
      await pluginApi.uninstallPlugin(pluginId)
      await loadInstalledPlugins()
      successMessage.value = '插件卸载成功'
      setTimeout(() => successMessage.value = '', 3000)
    } catch (err: any) {
      console.error('卸载插件失败:', err)
      error.value = err.message || '卸载失败'
    } finally {
      confirmDialog.visible = false
    }
  }
}

// 切换插件状态
async function handleToggleStatus(plugin: PluginStatus) {
  try {
    await pluginApi.updatePluginStatus({
      pluginId: plugin.pluginId,
      enabled: !plugin.enabled
    })
    await loadInstalledPlugins()
    successMessage.value = `插件已${!plugin.enabled ? '启用' : '禁用'}`
    setTimeout(() => successMessage.value = '', 3000)
  } catch (err: any) {
    console.error('切换插件状态失败:', err)
    error.value = err.message || '操作失败'
  }
}

onMounted(() => {
  loadInstalledPlugins()
})
</script>

<template>
  <div class="plugins-page p-6 space-y-6">
    <!-- 页面头部 -->
    <div>
      <h1 class="text-2xl font-bold text-[var(--text-default)] mb-2">插件中心</h1>
      <p class="text-[var(--text-secondary)]">管理和配置系统插件，扩展平台功能</p>
    </div>

    <!-- 成功提示 -->
    <WkAlert 
      v-if="successMessage"
      type="success" 
      :message="successMessage"
      :closable="true"
      @close="successMessage = ''"
    />

    <!-- 错误提示 -->
    <WkAlert 
      v-if="error"
      type="error" 
      title="错误"
      :message="error"
      :closable="true"
      @close="error = ''"
    />

    <!-- 全局加载 -->
    <WkLoading :loading="loading" fullscreen text="处理中..." />

    <!-- 已安装插件 -->
    <section>
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-semibold text-[var(--text-default)]">已安装插件</h2>
        <WkButton
          variant="ghost"
          size="sm"
          icon="i-tabler-refresh"
          @click="loadInstalledPlugins"
        >
          刷新
        </WkButton>
      </div>

      <WkCard v-if="installedPlugins.length === 0" :padding="'lg'">
        <div class="text-center py-8">
          <span class="i-tabler-package-off text-5xl text-[var(--text-muted)] mb-3 block"></span>
          <p class="text-[var(--text-secondary)]">暂无已安装的插件</p>
        </div>
      </WkCard>

      <div v-else class="space-y-4">
        <WkCard
          v-for="plugin in installedPlugins" 
          :key="plugin.pluginId"
          :hoverable="true"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-3">
                <div class="w-12 h-12 bg-[var(--brand-default)]/10 rounded-lg flex items-center justify-center">
                  <span class="i-tabler-puzzle text-[var(--brand-default)] text-xl"></span>
                </div>
                <div class="flex-1">
                  <div class="flex items-center gap-2">
                    <h3 class="text-lg font-semibold text-[var(--text-default)]">
                      {{ plugin.pluginName }}
                    </h3>
                    <WkBadge 
                      :variant="plugin.enabled ? 'success' : 'default'"
                      size="sm"
                    >
                      {{ plugin.enabled ? '已启用' : '已禁用' }}
                    </WkBadge>
                  </div>
                  <p class="text-sm text-[var(--text-secondary)]">
                    v{{ plugin.pluginVersion }} · ID: {{ plugin.pluginId }}
                  </p>
                </div>
              </div>

              <div class="mb-3">
                <p class="text-sm text-[var(--text-secondary)] mb-2">权限：</p>
                <div class="flex flex-wrap gap-2">
                  <WkBadge 
                    v-for="permission in plugin.grantedPermissions" 
                    :key="permission"
                    :variant="
                      permission.includes('http:external') || permission.includes('user:modify') ? 'danger' :
                      permission.includes('http:api') || permission.includes('storage') ? 'warning' :
                      'info'
                    "
                    size="sm"
                  >
                    {{ permission }}
                  </WkBadge>
                </div>
              </div>

              <div class="text-xs text-[var(--text-muted)]">
                安装时间: {{ new Date(plugin.installedAt).toLocaleString('zh-CN') }}
              </div>
            </div>

            <div class="flex gap-2 ml-4">
              <WkButton
                variant="ghost"
                size="sm"
                :icon="plugin.enabled ? 'i-tabler-player-pause' : 'i-tabler-player-play'"
                @click="handleToggleStatus(plugin)"
              >
                {{ plugin.enabled ? '禁用' : '启用' }}
              </WkButton>
              <WkButton
                variant="danger"
                size="sm"
                icon="i-tabler-trash"
                @click="handleUninstall(plugin.pluginId, plugin.pluginName)"
              >
                卸载
              </WkButton>
            </div>
          </div>
        </WkCard>
      </div>
    </section>

    <!-- 可用插件市场 -->
    <section>
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-semibold text-[var(--text-default)]">可用插件</h2>
        <WkBadge variant="info">{{ availablePlugins.length }} 个插件</WkBadge>
      </div>

      <div class="space-y-4">
        <WkCard
          v-for="plugin in availablePlugins" 
          :key="plugin.id"
          :hoverable="true"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-3">
                <div class="w-12 h-12 bg-[var(--brand-default)]/10 rounded-lg flex items-center justify-center">
                  <span class="i-tabler-puzzle text-[var(--brand-default)] text-xl"></span>
                </div>
                <div>
                  <h3 class="text-lg font-semibold text-[var(--text-default)]">{{ plugin.name }}</h3>
                  <p class="text-sm text-[var(--text-secondary)]">
                    v{{ plugin.version }} · by {{ plugin.author }}
                  </p>
                </div>
              </div>

              <p class="text-[var(--text-secondary)] mb-3">{{ plugin.description }}</p>

              <div>
                <p class="text-sm text-[var(--text-secondary)] mb-2">需要权限：</p>
                <div class="flex flex-wrap gap-2">
                  <WkBadge 
                    v-for="permission in plugin.permissions" 
                    :key="permission"
                    :variant="
                      permission.includes('http:external') || permission.includes('user:modify') ? 'danger' :
                      permission.includes('http:api') || permission.includes('storage') ? 'warning' :
                      'success'
                    "
                    size="sm"
                  >
                    {{ permission }}
                  </WkBadge>
                </div>
              </div>
            </div>

            <div class="ml-4">
              <WkButton
                v-if="!plugin.installed"
                variant="primary"
                icon="i-tabler-download"
                @click="handleInstall(plugin)"
              >
                安装
              </WkButton>
              <WkBadge v-else variant="success" size="md">
                <span class="i-tabler-check mr-1"></span>
                已安装
              </WkBadge>
            </div>
          </div>
        </WkCard>
      </div>
    </section>

    <!-- 确认对话框 -->
    <WkDialog
      v-model="confirmDialog.visible"
      :title="confirmDialog.title"
      size="sm"
    >
      <p class="text-[var(--text-secondary)]">{{ confirmDialog.message }}</p>

      <template #footer>
        <WkButton variant="ghost" @click="confirmDialog.visible = false">
          取消
        </WkButton>
        <WkButton variant="danger" @click="confirmDialog.onConfirm">
          确认
        </WkButton>
      </template>
    </WkDialog>
  </div>
</template>

<style scoped>
.plugins-page {
  min-height: 100vh;
}
</style>
