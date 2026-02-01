<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/user'
import type { UpdateProfileRequest, Gender } from '@/types/user'
import WkButton from '@/components/common/WkButton.vue'
import WkInput from '@/components/common/WkInput.vue'
import WkCard from '@/components/common/WkCard.vue'
import WkLoading from '@/components/common/WkLoading.vue'
import WkAlert from '@/components/common/WkAlert.vue'
import UserAvatar from '@/components/user/UserAvatar.vue'

const { t } = useI18n()
const authStore = useAuthStore()

const activeTab = ref('profile')
const loading = ref(false)
const saving = ref(false)
const message = ref('')
const messageType = ref<'success' | 'error' | 'warning' | 'info'>('info')

/** 表单数据 */
const formData = reactive<UpdateProfileRequest>({
  nickname: '',
  avatar: '',
  bio: '',
  location: '',
  website: '',
  gender: 'UNSPECIFIED',
  email: '',
  phone: ''
})

/** 性别选项 */
const genderOptions: { value: Gender; label: string }[] = [
  { value: 'UNSPECIFIED', label: t('user.unspecified') },
  { value: 'MALE', label: t('user.male') },
  { value: 'FEMALE', label: t('user.female') },
  { value: 'OTHER', label: t('user.other') }
]

/** 标签页配置 */
const tabs = computed(() => [
  { key: 'profile', label: '个人资料', icon: 'i-tabler-user' },
  { key: 'account', label: '账户设置', icon: 'i-tabler-settings' },
  { key: 'privacy', label: '隐私设置', icon: 'i-tabler-shield-lock' },
  { key: 'notifications', label: '通知设置', icon: 'i-tabler-bell' },
  { key: 'minecraft', label: 'Minecraft', icon: 'i-tabler-brand-minecraft' },
  { key: 'preferences', label: '偏好设置', icon: 'i-tabler-palette' }
])

/** 加载用户数据 */
const loadUserData = () => {
  if (!authStore.user) return
  
  formData.nickname = authStore.user.nickname
  formData.avatar = authStore.user.avatar || ''
  formData.email = authStore.user.email
  formData.phone = authStore.user.phone || ''
  // TODO: 从 API 加载完整资料（bio, location, website, gender）
}

/** 保存资料 */
const saveProfile = async () => {
  saving.value = true
  message.value = ''
  
  try {
    await userApi.updateMyProfile(formData)
    messageType.value = 'success'
    message.value = '保存成功'
    
    // 更新 auth store
    if (authStore.user) {
      authStore.user.nickname = formData.nickname
      authStore.user.avatar = formData.avatar
      authStore.user.email = formData.email
      authStore.user.phone = formData.phone
    }
  } catch (error: any) {
    messageType.value = 'error'
    message.value = error.response?.data?.message || '保存失败，请重试'
  } finally {
    saving.value = false
  }
}

/** 头像上传 */
const handleAvatarUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  
  const reader = new FileReader()
  reader.onload = (e) => {
    formData.avatar = e.target?.result as string
  }
  reader.readAsDataURL(file)
}

onMounted(() => {
  loadUserData()
})
</script>

<template>
  <div class="user-settings-page min-h-screen bg-bg-default py-8">
    <div class="max-w-6xl mx-auto px-4">
      <!-- 页头 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-text mb-2">{{ t('settings.title') }}</h1>
        <p class="text-text-secondary">{{ t('settings.subtitle') }}</p>
      </div>
      
      <div class="flex gap-6">
        <!-- 侧边标签栏 -->
        <aside class="w-64 flex-shrink-0">
          <WkCard class="sticky">
            <nav class="space-y-1">
              <button
                v-for="tab in tabs"
                :key="tab.key"
                :class="[
                  'w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors text-left',
                  activeTab === tab.key
                    ? 'bg-brand/10 text-brand font-medium'
                    : 'text-text-secondary hover:bg-bg-hover hover:text-text'
                ]"
                @click="activeTab = tab.key"
              >
                <i :class="tab.icon" class="text-lg" />
                <span>{{ tab.label }}</span>
              </button>
            </nav>
          </WkCard>
        </aside>
        
        <!-- 主内容区 -->
        <main class="flex-1">
          <WkCard>
            <WkLoading v-if="loading" :loading="true" />
            
            <!-- 提示消息 -->
            <WkAlert
              v-if="message"
              :type="messageType"
              :message="message"
              class="mb-6"
              @close="message = ''"
            />
            
            <!-- 个人资料 -->
            <div v-if="activeTab === 'profile'" class="space-y-6">
              <div>
                <h2 class="text-xl font-semibold text-text mb-4">个人资料</h2>
                <p class="text-sm text-text-secondary mb-6">
                  这些信息将在您的个人主页公开显示
                </p>
              </div>
              
              <!-- 头像 -->
              <div class="flex items-center gap-4">
                <UserAvatar
                  :src="formData.avatar"
                  :nickname="formData.nickname"
                  size="xl"
                  bordered
                />
                <div>
                  <label
                    for="avatar-upload"
                    class="btn btn-secondary cursor-pointer inline-block"
                  >
                    <i class="i-tabler-upload" />
                    <span>上传头像</span>
                  </label>
                  <input
                    id="avatar-upload"
                    type="file"
                    accept="image/*"
                    class="hidden"
                    @change="handleAvatarUpload"
                  />
                  <p class="text-xs text-text-muted mt-2">
                    推荐尺寸：200x200px，支持 JPG、PNG
                  </p>
                </div>
              </div>
              
              <!-- 昵称 -->
              <div>
                <label class="block text-sm font-medium text-text mb-2">昵称</label>
                <WkInput
                  v-model="formData.nickname"
                  placeholder="请输入昵称"
                />
              </div>
              
              <!-- 个性签名 -->
              <div>
                <label class="block text-sm font-medium text-text mb-2">
                  {{ t('user.bio') }}
                </label>
                <textarea
                  v-model="formData.bio"
                  class="input-base min-h-24 resize-y"
                  placeholder="介绍一下自己..."
                  maxlength="200"
                />
                <p class="text-xs text-text-muted mt-1">
                  {{ formData.bio?.length || 0 }} / 200
                </p>
              </div>
              
              <!-- 性别 -->
              <div>
                <label class="block text-sm font-medium text-text mb-2">
                  {{ t('user.gender') }}
                </label>
                <div class="flex gap-2">
                  <button
                    v-for="option in genderOptions"
                    :key="option.value"
                    :class="[
                      'px-4 py-2 rounded-lg border transition-colors',
                      formData.gender === option.value
                        ? 'bg-brand text-white border-brand'
                        : 'bg-bg-raised text-text border-border hover:border-brand/50'
                    ]"
                    @click="formData.gender = option.value"
                  >
                    {{ option.label }}
                  </button>
                </div>
              </div>
              
              <!-- 所在地 -->
              <div>
                <label class="block text-sm font-medium text-text mb-2">
                  {{ t('user.location') }}
                </label>
                <WkInput
                  v-model="formData.location"
                  placeholder="例如：北京, 中国"
                >
                  <template #prefix>
                    <i class="i-tabler-map-pin text-text-secondary" />
                  </template>
                </WkInput>
              </div>
              
              <!-- 个人网站 -->
              <div>
                <label class="block text-sm font-medium text-text mb-2">
                  {{ t('user.website') }}
                </label>
                <WkInput
                  v-model="formData.website"
                  placeholder="https://example.com"
                  type="url"
                >
                  <template #prefix>
                    <i class="i-tabler-link text-text-secondary" />
                  </template>
                </WkInput>
              </div>
            </div>
            
            <!-- 账户设置 -->
            <div v-else-if="activeTab === 'account'" class="space-y-6">
              <div>
                <h2 class="text-xl font-semibold text-text mb-4">账户设置</h2>
                <p class="text-sm text-text-secondary mb-6">
                  管理您的账户信息和安全设置
                </p>
              </div>
              
              <!-- 邮箱 -->
              <div>
                <label class="block text-sm font-medium text-text mb-2">邮箱地址</label>
                <WkInput
                  v-model="formData.email"
                  type="email"
                  placeholder="your@email.com"
                >
                  <template #prefix>
                    <i class="i-tabler-mail text-text-secondary" />
                  </template>
                </WkInput>
              </div>
              
              <!-- 手机号 -->
              <div>
                <label class="block text-sm font-medium text-text mb-2">手机号</label>
                <WkInput
                  v-model="formData.phone"
                  type="tel"
                  placeholder="请输入手机号"
                >
                  <template #prefix>
                    <i class="i-tabler-phone text-text-secondary" />
                  </template>
                </WkInput>
              </div>
              
              <!-- 修改密码 -->
              <div class="pt-4 border-t border-border">
                <h3 class="text-lg font-medium text-text mb-3">修改密码</h3>
                <WkButton variant="secondary">
                  <i class="i-tabler-lock" />
                  <span>更改密码</span>
                </WkButton>
              </div>
            </div>
            
            <!-- 其他标签页（占位） -->
            <div v-else class="text-center py-12 text-text-secondary">
              <i class="i-tabler-settings text-5xl mb-4" />
              <p class="text-lg">{{ tabs.find(t => t.key === activeTab)?.label }}</p>
              <p class="text-sm mt-2">此功能正在开发中...</p>
            </div>
            
            <!-- 保存按钮 -->
            <div
              v-if="activeTab === 'profile' || activeTab === 'account'"
              class="flex justify-end gap-3 pt-6 border-t border-border mt-6"
            >
              <WkButton variant="ghost" @click="loadUserData">
                重置
              </WkButton>
              <WkButton
                variant="primary"
                :loading="saving"
                @click="saveProfile"
              >
                {{ t('settings.saveAll') }}
              </WkButton>
            </div>
          </WkCard>
        </main>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-settings-page {
  min-height: calc(100vh - var(--header-height, 64px));
}
</style>
