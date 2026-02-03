<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { WkButton, WkAlert, WkLoading } from '@/components/common'
import { serverTokenApi, type ServerToken } from '@/api/serverToken'

const { t } = useI18n()

const dialogVisible = ref(false)
const isEditing = ref(false)
const loading = ref(false)
const formLoading = ref(false)
const tokens = ref<ServerToken[]>([])
const successMessage = ref('')
const errorMessage = ref('')

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
    console.error('Load error:', error)
    errorMessage.value = 'Load failed: ' + (error.message || '')
  } finally {
    loading.value = false
  }
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

function handleCreate() {
  isEditing.value = false
  resetForm()
  dialogVisible.value = true
  console.log('handleCreate - dialogVisible:', dialogVisible.value)
}

function handleSubmit() {
  console.log('handleSubmit called')
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

<template>
  <div class="p-6 space-y-6">
    <div class="flex items-center justify-between">
      <h1>ServerTokenPage - Simplified with Components</h1>
      <WkButton 
        variant="primary"
        icon="i-tabler-plus"
        @click="handleCreate"
      >
        Create Token
      </WkButton>
    </div>

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

    <WkLoading :loading="loading" fullscreen :text="'Loading...'" />
    
    <p>dialogVisible = {{ dialogVisible }}</p>
    <p>tokens.length = {{ tokens.length }}</p>

    <!-- Tokens List (instead of table) -->
    <div class="bg-[var(--bg-raised)] rounded-lg border border-[var(--border-default)] overflow-hidden p-4">
      <div v-if="tokens.length === 0" class="text-center text-[var(--text-secondary)]">
        No tokens found
      </div>
      <div v-for="token in tokens" :key="token.id" class="border-b last:border-b-0 py-2">
        <div class="font-medium text-[var(--text-default)]">{{ token.name }}</div>
      </div>
    </div>
    
    <el-dialog v-model="dialogVisible" title="Create Token">
      <div style="background: yellow; padding: 20px;">
        Create Token Dialog. dialogVisible = {{ dialogVisible }}
      </div>
      <template #footer>
        <WkButton variant="ghost" @click="dialogVisible = false">Cancel</WkButton>
        <WkButton variant="primary" :loading="formLoading" @click="handleSubmit">Create</WkButton>
      </template>
    </el-dialog>
  </div>
</template>
