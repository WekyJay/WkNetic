<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { WkButton, WkInput, WkAlert, WkDialog, WkLoading } from '@/components/common'
import { serverTokenApi, type ServerToken } from '@/api/serverToken'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()

const loading = ref(false)
const tokens = ref<ServerToken[]>([])
const successMessage = ref('')
const errorMessage = ref('')

// 表单对话框
const dialogVisible = ref(false)
const isEditing = ref(false)
const formLoading = ref(false)

const formData = reactive({
  id: null as number | null,
  name: '',
  remark: ''
})

const formErrors = reactive({
  tokenName: ''
})

// 复制状态
const copyingTokenId = ref<number | null>(null)

onMounted(() => {
  loadTokens()
})

async function loadTokens() {
  loading.value = false
  errorMessage.value = ''
  try {
    const response = await serverTokenApi.getTokenList()
    tokens.value = response.data?.records || []
  } catch (error: any) {
    console.error('Load tokens error:', error)
    errorMessage.value = t('serverToken.loadFailed') + ': ' + (error.message || '')
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  isEditing.value = false
  resetForm()
  dialogVisible.value = true
}

function handleEdit(token: ServerToken) {
  isEditing.value = true
  formData.id = token.id
  formData.name = token.name
  formData.remark = token.remark || ''
  dialogVisible.value = true
}

function resetForm() {
  formData.id = null
  formData.name = ''
  formData.remark = ''
  Object.keys(formErrors).forEach(key => {
    formErrors[key as keyof typeof formErrors] = ''
  })
}

function validateForm(): boolean {
  let isValid = true
  
  if (!formData.name.trim()) {
    formErrors.tokenName = t('serverToken.tokenNameRequired')
    isValid = false
  } else if (formData.name.trim().length < 3) {
    formErrors.tokenName = t('serverToken.tokenNameMin')
    isValid = false
  } else {
    formErrors.tokenName = ''
  }
  
  return isValid
}

async function handleSubmit() {
  if (!validateForm()) return
  
  formLoading.value = true
  try {
    if (isEditing.value && formData.id) {
      await serverTokenApi.updateToken(formData.id, {
        name: formData.name,
        remark: formData.remark
      })
      showSuccessMessage(t('serverToken.updateSuccess'))
    } else {
      await serverTokenApi.createToken({
        name: formData.name,
        remark: formData.remark
      })
      showSuccessMessage(t('serverToken.createSuccess'))
    }
    
    dialogVisible.value = false
    loadTokens()
  } catch (error: any) {
    console.error('Save token error:', error)
    showErrorMessage(t('serverToken.saveFailed') + ': ' + (error.message || ''))
  } finally {
    formLoading.value = false
  }
}

async function handleDelete(token: ServerToken) {
  try {
    await ElMessageBox.confirm(
      t('serverToken.deleteConfirm') + ` "${token.name}"?`,
      t('common.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    loading.value = true
    await serverTokenApi.deleteToken(token.id)
    showSuccessMessage(t('serverToken.deleteSuccess'))
    loadTokens()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Delete token error:', error)
      showErrorMessage(t('serverToken.deleteFailed') + ': ' + (error.message || ''))
    }
  } finally {
    loading.value = false
  }
}

async function handleToggleStatus(token: ServerToken) {
  try {
    const newStatus = token.status === 1 ? 0 : 1
    await serverTokenApi.toggleTokenStatus(token.id, newStatus as 0 | 1)
    token.status = newStatus
    showSuccessMessage(t('serverToken.statusUpdateSuccess'))
  } catch (error: any) {
    console.error('Toggle status error:', error)
    showErrorMessage(t('serverToken.statusUpdateFailed') + ': ' + (error.message || ''))
  }
}

async function handleRegenerateSecret(token: ServerToken) {
  try {
    await ElMessageBox.confirm(
      t('serverToken.regenerateConfirm'),
      t('common.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    loading.value = true
    await serverTokenApi.regenerateToken(token.id)
    showSuccessMessage(t('serverToken.regenerateSuccess'))
    loadTokens()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Regenerate token error:', error)
      showErrorMessage(t('serverToken.regenerateFailed') + ': ' + (error.message || ''))
    }
  } finally {
    loading.value = false
  }
}

async function copyToClipboard(text: string, id: number) {
  try {
    await navigator.clipboard.writeText(text)
    
    copyingTokenId.value = id
    setTimeout(() => {
      copyingTokenId.value = null
    }, 2000)
    
    ElMessage.success(t('common.copiedToClipboard'))
  } catch (error) {
    ElMessage.error(t('common.copyFailed'))
  }
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

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<template>
  <div class="p-6 space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-[var(--text-default)]">{{ t('menu.serverToken') }}</h1>
        <p class="mt-1 text-sm text-[var(--text-secondary)]">
          {{ t('serverToken.subtitle') }}
        </p>
      </div>
      <WkButton
        variant="primary"
        icon="i-tabler-plus"
        @click="handleCreate"
      >
        {{ t('serverToken.createNew') }}
      </WkButton>
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

    <!-- Tokens Table -->
    <div class="bg-[var(--bg-raised)] rounded-lg border border-[var(--border-default)] overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-[var(--bg-surface)] border-b border-[var(--border-default)]">
            <tr>
              <th class="px-6 py-3 text-left text-sm font-medium text-[var(--text-secondary)]">{{ t('serverToken.tokenName') }}</th>
              <th class="px-6 py-3 text-left text-sm font-medium text-[var(--text-secondary)]">{{ t('serverToken.tokenKey') }}</th>
              <th class="px-6 py-3 text-left text-sm font-medium text-[var(--text-secondary)]">{{ t('serverToken.remark') }}</th>
              <th class="px-6 py-3 text-left text-sm font-medium text-[var(--text-secondary)]">{{ t('serverToken.status') }}</th>
              <th class="px-6 py-3 text-left text-sm font-medium text-[var(--text-secondary)]">{{ t('serverToken.createTime') }}</th>
              <th class="px-6 py-3 text-center text-sm font-medium text-[var(--text-secondary)]">{{ t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="tokens.length === 0" class="border-b border-[var(--border-default)]">
              <td colspan="6" class="px-6 py-8 text-center text-[var(--text-secondary)]">
                {{ t('common.noData') }}
              </td>
            </tr>
            <tr v-for="token in tokens" :key="token.id" class="border-b border-[var(--border-default)] hover:bg-[var(--bg-surface)]">
              <td class="px-6 py-3">
                <div class="font-medium text-[var(--text-default)]">{{ token.name }}</div>
                <div v-if="token.remark" class="text-xs text-[var(--text-secondary)] mt-1">
                  {{ token.remark }}
                </div>
              </td>
              <td class="px-6 py-3">
                <div class="flex items-center gap-2">
                  <code class="bg-[var(--bg-surface)] px-2 py-1 rounded text-xs font-mono text-[var(--text-default)]">
                    {{ token.tokenValue ? token.tokenValue.substring(0, 8) + '...' + token.tokenValue.substring(token.tokenValue.length - 4) : 'N/A' }}
                  </code>
                  <WkButton
                    v-if="token.tokenValue"
                    variant="ghost"
                    size="sm"
                    :icon="copyingTokenId === token.id ? 'i-tabler-check' : 'i-tabler-copy'"
                    @click="copyToClipboard(token.tokenValue, token.id)"
                  />
                </div>
              </td>
              <td class="px-6 py-3">
                <div class="text-sm text-[var(--text-default)]">
                  {{ token.remark || '-' }}
                </div>
              </td>
              <td class="px-6 py-3">
                <el-tag
                  :type="token.status === 1 ? 'success' : 'info'"
                  size="small"
                  effect="plain"
                >
                  {{ token.status === 1 ? t('common.enabled') : t('common.disabled') }}
                </el-tag>
              </td>
              <td class="px-6 py-3 text-sm text-[var(--text-secondary)]">
                {{ formatDate(token.createTime) }}
              </td>
              <td class="px-6 py-3">
                <div class="flex items-center justify-center gap-1">
                  <WkButton
                    variant="ghost"
                    size="sm"
                    icon="i-tabler-key"
                    :title="t('serverToken.regenerateSecret')"
                    @click="handleRegenerateSecret(token)"
                  />
                  <WkButton
                    variant="ghost"
                    size="sm"
                    :icon="token.status === 1 ? 'i-tabler-ban' : 'i-tabler-check'"
                    @click="handleToggleStatus(token)"
                  />
                  <WkButton
                    variant="ghost"
                    size="sm"
                    icon="i-tabler-edit"
                    @click="handleEdit(token)"
                  />
                  <WkButton
                    variant="danger"
                    size="sm"
                    icon="i-tabler-trash"
                    @click="handleDelete(token)"
                  />
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Create/Edit Dialog -->
    <WkDialog v-model="dialogVisible" :title="isEditing ? t('serverToken.editToken') : t('serverToken.createToken')" size="md">
        <div>
          <label class="block text-sm font-medium text-[var(--text-secondary)] mb-1">
            {{ t('serverToken.tokenName') }} <span class="text-red-500">*</span>
          </label>
          <WkInput
            v-model="formData.name"
            :placeholder="t('serverToken.tokenNamePlaceholder')"
            :error="formErrors.tokenName"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-[var(--text-secondary)] mb-1">
            {{ t('serverToken.remark') }}
          </label>
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            :placeholder="t('serverToken.remarkPlaceholder')"
          />
        </div>

      <template #footer>
        <WkButton variant="ghost" @click="dialogVisible = false">{{ t('common.cancel') }}</WkButton>
        <WkButton variant="primary" :loading="formLoading" @click="handleSubmit">
          {{ isEditing ? t('common.update') : t('common.create') }}
        </WkButton>
      </template>
    </WkDialog>
  </div>
</template>

<style scoped>
/* Token display styles */
code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.875em;
}
</style>
