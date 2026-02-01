<template>
  <div class="p-6 space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-[var(--text-default)]">{{ t('menu.settings') }}</h1>
        <p class="mt-1 text-sm text-[var(--text-secondary)]">
          {{ t('settings.subtitle') }}
        </p>
      </div>
      <div class="flex gap-2">
        <WkButton
          variant="ghost"
          icon="i-tabler-refresh"
          @click="handleRefresh"
          :loading="loading"
        >
          {{ t('common.refresh') }}
        </WkButton>
        <WkButton
          variant="primary"
          icon="i-tabler-device-floppy"
          @click="handleSaveAll"
          :loading="saving"
        >
          {{ t('settings.saveAll') }}
        </WkButton>
      </div>
    </div>

    <!-- Success/Error Messages -->
    <WkAlert 
      v-if="successMessage"
      type="success" 
      :message="successMessage"
      :closable="true"
      @close="successMessage = ''"
    />
    
    <WkAlert 
      v-if="errorMessage"
      type="error" 
      :message="errorMessage"
      :closable="true"
      @close="errorMessage = ''"
    />

    <!-- Loading -->
    <WkLoading :loading="loading" fullscreen :text="t('common.loading')" />

    <!-- Tabs for Config Groups -->
    <div class="bg-[var(--bg-raised)] rounded-lg border border-[var(--border-default)] overflow-hidden">
      <el-tabs v-model="activeTab" class="config-tabs">
        <el-tab-pane
          v-for="group in configGroups"
          :key="group.groupKey"
          :label="group.groupLabel"
          :name="group.groupKey"
        >
          <div class="p-6 space-y-6">
            <!-- Config Items -->
            <div
              v-for="config in group.configs"
              :key="config.configId"
              class="config-item"
            >
              <div class="flex items-start justify-between gap-4">
                <div class="flex-1 space-y-2">
                  <div class="flex items-center gap-2">
                    <label class="text-sm font-medium text-[var(--text-default)]">
                      {{ config.configLabel }}
                    </label>
                    <el-tag v-if="config.isSystem === 1" type="warning" size="small">
                      {{ t('settings.systemConfig') }}
                    </el-tag>
                  </div>
                  <p v-if="config.configDesc" class="text-xs text-[var(--text-secondary)]">
                    {{ config.configDesc }}
                  </p>
                  
                  <!-- Dynamic Input Component -->
                  <div class="config-input">
                    <!-- String Type -->
                    <WkInput
                      v-if="config.configType === 'string'"
                      v-model="configValues[config.configKey]"
                      :placeholder="config.configLabel"
                      @change="handleConfigChange(config)"
                    />
                    
                    <!-- Number Type -->
                    <WkInput
                      v-else-if="config.configType === 'number'"
                      v-model="configValues[config.configKey]"
                      type="number"
                      :placeholder="config.configLabel"
                      @change="handleConfigChange(config)"
                    />
                    
                    <!-- Boolean Type -->
                    <el-switch
                      v-else-if="config.configType === 'boolean'"
                      v-model="configValues[config.configKey]"
                      :active-value="'1'"
                      :inactive-value="'0'"
                      @change="handleConfigChange(config)"
                    />
                    
                    <!-- Image Type -->
                    <div v-else-if="config.configType === 'image'" class="space-y-2">
                      <WkInput
                        v-model="configValues[config.configKey]"
                        :placeholder="t('settings.imageUrlPlaceholder')"
                        @change="handleConfigChange(config)"
                      >
                        <template #suffix>
                          <i class="i-tabler-photo text-[var(--text-secondary)]"></i>
                        </template>
                      </WkInput>
                      <div v-if="configValues[config.configKey]" class="flex items-center gap-2">
                        <img
                          :src="configValues[config.configKey]"
                          :alt="config.configLabel"
                          class="h-16 w-16 object-cover rounded border border-[var(--border-default)]"
                          @error="handleImageError"
                        />
                        <span class="text-xs text-[var(--text-secondary)]">{{ t('settings.preview') }}</span>
                      </div>
                    </div>
                    
                    <!-- Textarea Type -->
                    <el-input
                      v-else-if="config.configType === 'textarea'"
                      v-model="configValues[config.configKey]"
                      type="textarea"
                      :rows="4"
                      :placeholder="config.configLabel"
                      @change="handleConfigChange(config)"
                    />
                    
                    <!-- JSON Type -->
                    <div v-else-if="config.configType === 'json'" class="space-y-2">
                      <el-input
                        v-model="configValues[config.configKey]"
                        type="textarea"
                        :rows="6"
                        :placeholder="t('settings.jsonPlaceholder')"
                        @change="handleConfigChange(config)"
                      />
                      <div class="flex items-center gap-2">
                        <WkButton
                          variant="ghost"
                          size="sm"
                          icon="i-tabler-code"
                          @click="formatJSON(config.configKey)"
                        >
                          {{ t('settings.formatJson') }}
                        </WkButton>
                        <span
                          v-if="jsonValidation[config.configKey] === false"
                          class="text-xs text-red-500"
                        >
                          {{ t('settings.invalidJson') }}
                        </span>
                        <span
                          v-else-if="jsonValidation[config.configKey] === true"
                          class="text-xs text-green-500"
                        >
                          {{ t('settings.validJson') }}
                        </span>
                      </div>
                    </div>
                    
                    <!-- Default: String -->
                    <WkInput
                      v-else
                      v-model="configValues[config.configKey]"
                      :placeholder="config.configLabel"
                      @change="handleConfigChange(config)"
                    />
                  </div>
                  
                  <div class="text-xs text-[var(--text-muted)]">
                    {{ t('settings.configKey') }}: <code class="px-1 py-0.5 bg-[var(--bg-default)] rounded">{{ config.configKey }}</code>
                  </div>
                </div>
                
                <!-- Individual Save Button -->
                <WkButton
                  variant="ghost"
                  size="sm"
                  icon="i-tabler-check"
                  @click="handleSaveConfig(config)"
                  :loading="savingConfigs[config.configKey]"
                  v-if="changedConfigs.has(config.configKey)"
                >
                  {{ t('common.save') }}
                </WkButton>
              </div>
            </div>
            
            <!-- Empty State -->
            <div v-if="group.configs.length === 0" class="text-center py-12 text-[var(--text-secondary)]">
              <i class="i-tabler-settings-off text-4xl mb-2"></i>
              <p>{{ t('settings.noConfigs') }}</p>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { WkButton, WkInput, WkAlert, WkLoading } from '@/components/common'
import { configApi, type SysConfig, CONFIG_GROUP_LABELS } from '@/api/config'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const configs = ref<SysConfig[]>([])
const activeTab = ref('site')
const successMessage = ref('')
const errorMessage = ref('')

// 配置值的响应式对象
const configValues = reactive<Record<string, string>>({})

// 记录已修改的配置
const changedConfigs = reactive(new Set<string>())

// 记录正在保存的配置
const savingConfigs = reactive<Record<string, boolean>>({})

// JSON 验证状态
const jsonValidation = reactive<Record<string, boolean | null>>({})

// 分组后的配置
const configGroups = computed(() => {
  const groups = new Map<string, SysConfig[]>()
  
  configs.value.forEach(config => {
    if (!groups.has(config.configGroup)) {
      groups.set(config.configGroup, [])
    }
    groups.get(config.configGroup)!.push(config)
  })
  
  // 对每个分组内的配置按 sortOrder 排序
  groups.forEach(configs => {
    configs.sort((a, b) => a.sortOrder - b.sortOrder)
  })
  
  // 转换为数组格式
  return Array.from(groups.entries()).map(([groupKey, configs]) => ({
    groupKey,
    groupLabel: CONFIG_GROUP_LABELS[groupKey] || groupKey,
    configs
  }))
})

onMounted(() => {
  loadConfigs()
})

async function loadConfigs() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await configApi.getAllConfigs()
    configs.value = result || []
    
    // 初始化配置值
    configs.value.forEach(config => {
      configValues[config.configKey] = config.configValue || ''
    })
    
    // 清空修改记录
    changedConfigs.clear()
    
    console.log('Loaded configs:', configs.value)
  } catch (error: any) {
    console.error('Load configs error:', error)
    errorMessage.value = t('settings.loadFailed') + ': ' + (error.message || '')
  } finally {
    loading.value = false
  }
}

function handleConfigChange(config: SysConfig) {
  changedConfigs.add(config.configKey)
  
  // 如果是 JSON 类型，验证格式
  if (config.configType === 'json') {
    validateJSON(config.configKey)
  }
}

function validateJSON(configKey: string) {
  try {
    const value = configValues[configKey]
    if (value) {
      JSON.parse(value)
      jsonValidation[configKey] = true
    }
  } catch {
    jsonValidation[configKey] = false
  }
}

function formatJSON(configKey: string) {
  try {
    const value = configValues[configKey]
    if (value) {
      const parsed = JSON.parse(value)
      configValues[configKey] = JSON.stringify(parsed, null, 2)
      jsonValidation[configKey] = true
      ElMessage.success(t('settings.formatSuccess'))
    }
  } catch {
    ElMessage.error(t('settings.invalidJson'))
  }
}

async function handleSaveConfig(config: SysConfig) {
  // 如果是 JSON 类型，先验证
  if (config.configType === 'json' && jsonValidation[config.configKey] === false) {
    ElMessage.error(t('settings.invalidJson'))
    return
  }
  
  savingConfigs[config.configKey] = true
  try {
    const configValue = configValues[config.configKey] || ''
    const updatedConfig: SysConfig = {
      ...config,
      configValue
    }
    
    await configApi.updateConfig(updatedConfig)
    
    // 更新本地数据
    const index = configs.value.findIndex(c => c.configId === config.configId)
    if (index !== -1 && configs.value[index]) {
      configs.value[index].configValue = configValue
    }
    
    changedConfigs.delete(config.configKey)
    showSuccessMessage(t('settings.saveSuccess'))
  } catch (error: any) {
    console.error('Save config error:', error)
    showErrorMessage(t('settings.saveFailed') + ': ' + (error.message || ''))
  } finally {
    savingConfigs[config.configKey] = false
  }
}

async function handleSaveAll() {
  if (changedConfigs.size === 0) {
    ElMessage.info(t('settings.noChanges'))
    return
  }
  
  // 验证所有 JSON 配置
  for (const configKey of changedConfigs) {
    const config = configs.value.find(c => c.configKey === configKey)
    if (config?.configType === 'json' && jsonValidation[configKey] === false) {
      ElMessage.error(t('settings.invalidJson') + `: ${config.configLabel}`)
      return
    }
  }
  
  saving.value = true
  try {
    const configMap: Record<string, string> = {}
    changedConfigs.forEach(configKey => {
      configMap[configKey] = configValues[configKey] || ''
    })
    
    await configApi.batchUpdateConfigs(configMap)
    
    // 更新本地数据
    configs.value.forEach(config => {
      if (changedConfigs.has(config.configKey)) {
        config.configValue = configValues[config.configKey] || ''
      }
    })
    
    changedConfigs.clear()
    showSuccessMessage(t('settings.saveAllSuccess'))
  } catch (error: any) {
    console.error('Batch save error:', error)
    showErrorMessage(t('settings.saveFailed') + ': ' + (error.message || ''))
  } finally {
    saving.value = false
  }
}

function handleRefresh() {
  if (changedConfigs.size > 0) {
    ElMessage.warning(t('settings.unsavedChanges'))
    return
  }
  loadConfigs()
}

function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  img.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="100"%3E%3Crect fill="%23ddd" width="100" height="100"/%3E%3Ctext fill="%23999" x="50%25" y="50%25" text-anchor="middle" dy=".3em"%3EError%3C/text%3E%3C/svg%3E'
}

function showSuccessMessage(message: string) {
  successMessage.value = message
  setTimeout(() => {
    successMessage.value = ''
  }, 3000)
}

function showErrorMessage(message: string) {
  errorMessage.value = message
  setTimeout(() => {
    errorMessage.value = ''
  }, 5000)
}
</script>

<style scoped>
.config-item {
  padding: 1rem;
  border: 1px solid var(--border-default);
  border-radius: 0.5rem;
  background: var(--bg-default);
  transition: all 0.2s;
}

.config-item:hover {
  border-color: var(--border-light);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.config-input {
  max-width: 600px;
}

:deep(.config-tabs .el-tabs__header) {
  margin: 0;
  padding: 0 1.5rem;
  background: var(--bg-default);
  border-bottom: 1px solid var(--border-default);
}

:deep(.config-tabs .el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.config-tabs .el-tabs__content) {
  padding: 0;
}

code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.875em;
}
</style>
