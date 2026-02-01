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
  tokenId: null as number | null,
  tokenName: '',
  description: '',
  tags: '',
  status: 1
})

const formErrors = reactive({
  tokenName: ''
})

// 复制状态
const copyingTokenId = ref<number | null>(null)
const copyingSecretId = ref<number | null>(null)

onMounted(() => {
  loadTokens()
})

async function loadTokens() {
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await serverTokenApi.getTokenList()
    tokens.value = response.data || []
    console.log('Loaded tokens:', tokens.value)
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
  formData.tokenId = token.tokenId
  formData.tokenName = token.tokenName
  formData.description = token.description || ''
  formData.tags = token.tags || ''
  formData.status = token.status
  dialogVisible.value = true
}

function resetForm() {
  formData.tokenId = null
  formData.tokenName = ''
  formData.description = ''
  formData.tags = ''
  formData.status = 1
  Object.keys(formErrors).forEach(key => {
    formErrors[key as keyof typeof formErrors] = ''
  })
}

function validateForm(): boolean {
  let isValid = true
  
  if (!formData.tokenName.trim()) {
    formErrors.tokenName = t('serverToken.tokenNameRequired')
    isValid = false
  } else if (formData.tokenName.trim().length < 3) {
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
    if (isEditing.value && formData.tokenId) {
      await serverTokenApi.updateToken({
        tokenId: formData.tokenId,
        tokenName: formData.tokenName,
        description: formData.description,
        tags: formData.tags,
        status: formData.status
      })
      showSuccessMessage(t('serverToken.updateSuccess'))
    } else {
      await serverTokenApi.createToken({
        tokenName: formData.tokenName,
        description: formData.description,
        tags: formData.tags,
        status: formData.status
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
      t('serverToken.deleteConfirm') + ` "${token.tokenName}"?`,
      t('common.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    loading.value = true
    await serverTokenApi.deleteToken(token.tokenId)
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
    await serverTokenApi.toggleTokenStatus(token.tokenId, newStatus as 0 | 1)
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
    const response = await serverTokenApi.regenerateSecret(token.tokenId)
    const newToken = response.data
    
    const index = tokens.value.findIndex(t => t.tokenId === token.tokenId)
    if (index !== -1) {
      tokens.value[index] = newToken
    }
    
    showSuccessMessage(t('serverToken.regenerateSuccess'))
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Regenerate secret error:', error)
      showErrorMessage(t('serverToken.regenerateFailed') + ': ' + (error.message || ''))
    }
  } finally {
    loading.value = false
  }
}

async function copyToClipboard(text: string, tokenId: number, type: 'token' | 'secret') {
  try {
    await navigator.clipboard.writeText(text)
    
    if (type === 'token') {
      copyingTokenId.value = tokenId
      setTimeout(() => {
        copyingTokenId.value = null
      }, 2000)
    } else {
      copyingSecretId.value = tokenId
      setTimeout(() => {
        copyingSecretId.value = null
      }, 2000)
    }
    
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

function maskSecret(secret: string): string {
  if (secret.length <= 8) return '*'.repeat(secret.length)
  return secret.substring(0, 4) + '*'.repeat(secret.length - 8) + secret.substring(secret.length - 4)
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
      <el-table
        :data="tokens"
        style="width: 100%"
        :empty-text="t('common.noData')"
        row-key="tokenId"
        class="!border-none"
      >
        <el-table-column prop="tokenName" :label="t('serverToken.tokenName')" min-width="150">
          <template #default="{ row }">
            <div class="font-medium text-[var(--text-default)]">{{ row.tokenName }}</div>
            <div v-if="row.description" class="text-xs text-[var(--text-secondary)] mt-1">
              {{ row.description }}
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="t('serverToken.tokenKey')" min-width="180">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <code class="bg-[var(--bg-surface)] px-2 py-1 rounded text-xs font-mono text-[var(--text-default)]">
                {{ row.tokenKey.substring(0, 8) }}...{{ row.tokenKey.substring(row.tokenKey.length - 4) }}
              </code>
              <WkButton
                variant="ghost"
                size="sm"
                :icon="copyingTokenId === row.tokenId ? 'i-tabler-check' : 'i-tabler-copy'"
                @click="copyToClipboard(row.tokenKey, row.tokenId, 'token')"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="t('serverToken.tags')" min-width="150">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-1">
              <el-tag
                v-for="tag in row.tags.split(',')"
                :key="tag"
                v-show="tag"
                size="small"
              >
                {{ tag.trim() }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="t('serverToken.status')" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'info'"
              size="small"
              effect="plain"
            >
              {{ row.status === 1 ? t('common.enabled') : t('common.disabled') }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column :label="t('serverToken.createTime')" width="180">
          <template #default="{ row }">
            <span class="text-sm text-[var(--text-secondary)]">
              {{ formatDate(row.createTime) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column :label="t('common.actions')" align="center" width="220" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-1">
              <WkButton
                variant="ghost"
                size="sm"
                icon="i-tabler-key"
                :title="t('serverToken.regenerateSecret')"
                @click="handleRegenerateSecret(row)"
              />
              <WkButton
                variant="ghost"
                size="sm"
                :icon="row.status === 1 ? 'i-tabler-ban' : 'i-tabler-check'"
                @click="handleToggleStatus(row)"
              />
              <WkButton
                variant="ghost"
                size="sm"
                icon="i-tabler-edit"
                @click="handleEdit(row)"
              />
              <WkButton
                variant="danger"
                size="sm"
                icon="i-tabler-trash"
                @click="handleDelete(row)"
              />
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Create/Edit Dialog -->
    <WkDialog v-model="dialogVisible" :title="isEditing ? t('serverToken.editToken') : t('serverToken.createToken')" size="md">
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-[var(--text-secondary)] mb-1">
            {{ t('serverToken.tokenName') }} <span class="text-red-500">*</span>
          </label>
          <WkInput
            v-model="formData.tokenName"
            :placeholder="t('serverToken.tokenNamePlaceholder')"
            :error="formErrors.tokenName"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-[var(--text-secondary)] mb-1">
            {{ t('serverToken.description') }}
          </label>
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            :placeholder="t('serverToken.descriptionPlaceholder')"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-[var(--text-secondary)] mb-1">
            {{ t('serverToken.tags') }}
          </label>
          <WkInput
            v-model="formData.tags"
            :placeholder="t('serverToken.tagsPlaceholder')"
          />
          <p class="text-xs text-[var(--text-secondary)] mt-1">
            {{ t('serverToken.tagsHint') }}
          </p>
        </div>

        <div>
          <label class="block text-sm font-medium text-[var(--text-secondary)] mb-1">
            {{ t('serverToken.status') }}
          </label>
          <select
            v-model.number="formData.status"
            class="w-full px-3 py-2 border border-[var(--border-default)] rounded-lg bg-[var(--bg-surface)] text-[var(--text-default)] focus:outline-none focus:ring-2 focus:ring-[var(--brand-default)]"
          >
            <option :value="1">{{ t('common.enabled') }}</option>
            <option :value="0">{{ t('common.disabled') }}</option>
          </select>
        </div>
      </form>

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
