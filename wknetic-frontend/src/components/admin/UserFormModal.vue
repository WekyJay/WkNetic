<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { userApi, type User, type UserRole, USER_ROLE_MAP } from '@/api/user'
import { validateUuid, formatUuid, getMinecraftAvatarUrl } from '@/utils/minecraft'

interface Props {
  visible: boolean
  user?: User | null
  mode: 'create' | 'edit'
  asDialog?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  user: null,
  mode: 'create',
  asDialog: false
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const loading = ref(false)
const mcValidating = ref(false)
const mcValidated = ref(false)

const formData = reactive({
  userId: null as number | null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
  status: 1,
  role: 'USER' as UserRole,
  minecraftUuid: '',
  minecraftUsername: ''
})

const errors = reactive({
  username: '',
  password: '',
  email: '',
  minecraftUuid: ''
})

const roleOptions: { value: UserRole; label: string }[] = [
  { value: 'ADMIN', label: USER_ROLE_MAP.ADMIN },
  { value: 'MODERATOR', label: USER_ROLE_MAP.MODERATOR },
  { value: 'USER', label: USER_ROLE_MAP.USER },
  { value: 'VIP', label: USER_ROLE_MAP.VIP },
  { value: 'BANNED', label: USER_ROLE_MAP.BANNED }
]

const title = computed(() => props.mode === 'create' ? '创建用户' : '编辑用户')

const minecraftAvatar = computed(() => {
  if (formData.minecraftUuid && mcValidated.value) {
    return getMinecraftAvatarUrl(formData.minecraftUuid, 64)
  }
  return ''
})

// 监听props变化，重置表单
watch(() => props.visible, (newVal) => {
  if (newVal) {
    resetForm()
    if (props.user && props.mode === 'edit') {
      Object.assign(formData, {
        userId: props.user.userId,
        username: props.user.username,
        password: '',
        nickname: props.user.nickname,
        email: props.user.email || '',
        phone: props.user.phone || '',
        avatar: props.user.avatar || '',
        status: props.user.status,
        role: props.user.role,
        minecraftUuid: props.user.minecraftUuid || '',
        minecraftUsername: props.user.minecraftUsername || ''
      })
      mcValidated.value = !!props.user.minecraftUuid
    }
  }
})

function resetForm() {
  Object.assign(formData, {
    userId: null,
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    avatar: '',
    status: 1,
    role: 'USER',
    minecraftUuid: '',
    minecraftUsername: ''
  })
  Object.keys(errors).forEach(key => {
    errors[key as keyof typeof errors] = ''
  })
  mcValidated.value = false
}

function validateForm(): boolean {
  let isValid = true
  
  // 验证用户名
  if (!formData.username) {
    errors.username = '用户名不能为空'
    isValid = false
  } else if (!/^[a-zA-Z0-9_]{3,20}$/.test(formData.username)) {
    errors.username = '用户名必须为3-20位字母、数字或下划线'
    isValid = false
  } else {
    errors.username = ''
  }
  
  // 验证密码（创建时必填）
  if (props.mode === 'create' && !formData.password) {
    errors.password = '密码不能为空'
    isValid = false
  } else if (formData.password && formData.password.length < 6) {
    errors.password = '密码至少6位'
    isValid = false
  } else {
    errors.password = ''
  }
  
  // 验证邮箱
  if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
    errors.email = '邮箱格式不正确'
    isValid = false
  } else {
    errors.email = ''
  }
  
  // 验证MC UUID
  if (formData.minecraftUuid && !validateUuid(formData.minecraftUuid)) {
    errors.minecraftUuid = 'UUID格式不正确'
    isValid = false
  } else {
    errors.minecraftUuid = ''
  }
  
  return isValid
}

async function validateMinecraftUuid() {
  if (!formData.minecraftUuid) {
    alert('请输入Minecraft UUID')
    return
  }
  
  if (!validateUuid(formData.minecraftUuid)) {
    errors.minecraftUuid = 'UUID格式不正确'
    return
  }
  
  mcValidating.value = true
  errors.minecraftUuid = ''
  
  try {
    const formatted = formatUuid(formData.minecraftUuid)
    const response = await userApi.validateMinecraftUuid(formatted)
    const result = response.data
    
    if (result.valid) {
      formData.minecraftUuid = result.id
      formData.minecraftUsername = result.name
      mcValidated.value = true
      alert(`验证成功！用户名：${result.name}`)
    } else {
      errors.minecraftUuid = result.error || 'UUID验证失败'
      mcValidated.value = false
    }
  } catch (error: any) {
    errors.minecraftUuid = error.message || '验证失败'
    mcValidated.value = false
  } finally {
    mcValidating.value = false
  }
}

async function handleSubmit() {
  if (!validateForm()) {
    return
  }
  
  loading.value = true
  
  try {
    const data: any = {
      username: formData.username,
      nickname: formData.nickname || formData.username,
      email: formData.email || undefined,
      phone: formData.phone || undefined,
      avatar: formData.avatar || undefined,
      status: formData.status,
      role: formData.role,
      minecraftUuid: formData.minecraftUuid || undefined,
      minecraftUsername: formData.minecraftUsername || undefined
    }
    
    if (formData.password) {
      data.password = formData.password
    }
    
    if (props.mode === 'create') {
      await userApi.createUser(data)
      alert('创建成功')
    } else {
      await userApi.updateUser(formData.userId!, data)
      alert('更新成功')
    }
    
    emit('success')
    handleClose()
  } catch (error: any) {
    alert(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

function handleClose() {
  emit('update:visible', false)
}

// expose methods for parent dialog to control the form
defineExpose({ submit: handleSubmit, close: handleClose })
</script>

<template>

    <div class=" rounded-lgmax-w-2xl mx-4">
      <!-- Header (only when not used inside WkDialog) -->
      <div v-if="!props.asDialog" class="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700">
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white">{{ title }}</h2>
        <button
          @click="handleClose"
          class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
        >
          <i class="i-tabler-x w-6 h-6" />
        </button>
      </div>

      <!-- Body -->
      <form @submit.prevent="handleSubmit" class="p-6 space-y-6">
        <!-- 基本信息 -->
        <div class="space-y-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white">基本信息</h3>
          
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                用户名 <span class="text-red-500">*</span>
              </label>
              <input
                v-model="formData.username"
                :disabled="mode === 'edit'"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white disabled:opacity-50"
                placeholder="3-20位字母数字下划线"
              />
              <p v-if="errors.username" class="mt-1 text-sm text-red-500">{{ errors.username }}</p>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                密码 <span v-if="mode === 'create'" class="text-red-500">*</span>
                <span v-else class="text-gray-500">(留空不修改)</span>
              </label>
              <input
                v-model="formData.password"
                type="password"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                placeholder="至少6位"
              />
              <p v-if="errors.password" class="mt-1 text-sm text-red-500">{{ errors.password }}</p>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">昵称</label>
              <input
                v-model="formData.nickname"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                placeholder="默认同用户名"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">邮箱</label>
              <input
                v-model="formData.email"
                type="email"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                placeholder="user@example.com"
              />
              <p v-if="errors.email" class="mt-1 text-sm text-red-500">{{ errors.email }}</p>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">手机</label>
              <input
                v-model="formData.phone"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                placeholder="手机号"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">头像URL</label>
              <input
                v-model="formData.avatar"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                placeholder="https://..."
              />
            </div>
          </div>
        </div>

        <!-- 权限设置 -->
        <div class="space-y-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white">权限设置</h3>
          
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">角色</label>
              <select
                v-model="formData.role"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option v-for="option in roleOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">状态</label>
              <select
                v-model="formData.status"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Minecraft账号绑定 -->
        <div class="space-y-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white">Minecraft账号</h3>
          
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">UUID</label>
              <div class="flex gap-2">
                <input
                  v-model="formData.minecraftUuid"
                  type="text"
                  class="flex-1 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                  placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
                  @input="mcValidated = false"
                />
                <button
                  type="button"
                  @click="validateMinecraftUuid"
                  :disabled="mcValidating || !formData.minecraftUuid"
                  class="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {{ mcValidating ? '验证中...' : '验证' }}
                </button>
              </div>
              <p v-if="errors.minecraftUuid" class="mt-1 text-sm text-red-500">{{ errors.minecraftUuid }}</p>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">游戏用户名</label>
              <input
                v-model="formData.minecraftUsername"
                type="text"
                :disabled="true"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-gray-50 dark:bg-gray-700 text-gray-900 dark:text-white"
                placeholder="验证后自动填充"
              />
            </div>

            <!-- Minecraft头像预览 -->
            <div v-if="minecraftAvatar" class="flex items-center gap-3 p-3 bg-gray-50 dark:bg-gray-700 rounded-md">
              <img :src="minecraftAvatar" alt="MC Avatar" class="w-16 h-16 rounded" />
              <div>
                <p class="text-sm font-medium text-gray-900 dark:text-white">{{ formData.minecraftUsername }}</p>
                <p class="text-xs text-gray-500 dark:text-gray-400">验证成功</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer (only when not used inside WkDialog) -->
        <div v-if="!props.asDialog" class="flex justify-end gap-3 pt-4 border-t border-gray-200 dark:border-gray-700">
          <button
            type="button"
            @click="handleClose"
            class="px-4 py-2 text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 rounded-md hover:bg-gray-200 dark:hover:bg-gray-600"
          >
            取消
          </button>
          <button
            type="submit"
            :disabled="loading"
            class="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ loading ? '保存中...' : '保存' }}
          </button>
        </div>
      </form>
    </div>
</template>
