<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppTheme } from '@/composables/useTheme'
import { userApi } from '@/api/user'
import type { UpdateProfileRequest, Gender } from '@/types/user'
import WkButton from '@/components/common/WkButton.vue'
import WkInput from '@/components/common/WkInput.vue'
import WkCard from '@/components/common/WkCard.vue'
import WkLoading from '@/components/common/WkLoading.vue'
import WkAlert from '@/components/common/WkAlert.vue'
import UserAvatar from '@/components/user/UserAvatar.vue'
import MinecraftSettings from '@/components/user/MinecraftSettings.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const theme = useAppTheme()

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

const isValidTab = (key: string) => tabs.value.some((tab) => tab.key === key)

const syncTabFromRoute = () => {
  const tabParam = typeof route.params.tab === 'string' ? route.params.tab : ''
  activeTab.value = isValidTab(tabParam) ? tabParam : 'profile'
}

const goToTab = (key: string) => {
  if (!isValidTab(key)) return
  activeTab.value = key
  if (key === 'profile') {
    router.replace({ name: 'user-settings' })
    return
  }
  router.replace({ name: 'user-settings', params: { tab: key } })
}

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
  syncTabFromRoute()
})

watch(
  () => route.params.tab,
  () => {
    syncTabFromRoute()
  }
)
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
                    ? 'bg-brand/10 font-medium'
                    : 'text-text-secondary hover:bg-bg-hover hover:text-text'
                ]"
                @click="goToTab(tab.key)"
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
            
            <!-- Minecraft设置 -->
            <div v-else-if="activeTab === 'minecraft'" class="space-y-6">
              <MinecraftSettings />
            </div>
            
            <!-- 偏好设置 -->
            <div v-else-if="activeTab === 'preferences'" class="space-y-8">
              <div>
                <h2 class="text-xl font-semibold text-text mb-4">{{ t('settings.appearance.title') }}</h2>
                <p class="text-sm text-text-secondary mb-6">
                  {{ t('settings.appearance.subtitle') }}
                </p>
              </div>

              <!-- 如果用户不能切换主题，显示提示 -->
              <div v-if="!theme.userCanChangeTheme.value" class="bg-bg-raised rounded-lg p-6 border border-border">
                <div class="flex items-start gap-4">
                  <i class="i-tabler-lock text-3xl text-text-secondary flex-shrink-0 mt-1" />
                  <div>
                    <h4 class="text-base font-semibold text-text mb-2">
                      {{ t('settings.appearance.themeChangeLocked') }}
                    </h4>
                    <p class="text-sm text-text-secondary mb-3">
                      {{ t('settings.appearance.themeChangeLockedDesc') }}
                    </p>
                    <p class="text-sm text-text-muted">
                      {{ t('settings.appearance.currentTheme') }}: 
                      <span class="font-medium text-text">{{ theme.currentTheme.value.displayName }}</span>
                    </p>
                  </div>
                </div>
              </div>

              <!-- 颜色主题选择 -->
              <div v-else>
                <h3 class="text-lg font-medium text-text mb-4">
                  {{ t('settings.appearance.colorTheme') }}
                </h3>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <button
                    v-for="themeOption in theme.availableThemes.value"
                    :key="themeOption.id"
                    :class="[
                      'relative p-6 rounded-xl border-2 transition-all text-left',
                      'hover:shadow-lg',
                      theme.colorTheme.value === themeOption.id
                        ? 'border-brand shadow-lg'
                        : 'border-border hover:border-brand/50'
                    ]"
                    @click="theme.setColorTheme(themeOption.id)"
                  >
                    <!-- 选中标识 -->
                    <div
                      v-if="theme.colorTheme.value === themeOption.id"
                      class="absolute top-4 right-4 w-6 h-6 bg-brand text-white rounded-full flex items-center justify-center"
                    >
                      <i class="i-tabler-check text-sm" />
                    </div>

                    <!-- 主题名称 -->
                    <div class="mb-4">
                      <h4 class="text-base font-semibold text-text mb-1">
                        {{ themeOption.displayName }}
                      </h4>
                      <p class="text-sm text-text-secondary">
                        {{ t(`settings.appearance.themes.${themeOption.id}.desc`) }}
                      </p>
                    </div>

                    <!-- 颜色预览 -->
                    <div class="flex gap-2">
                      <div
                        class="w-12 h-12 rounded-lg border border-border"
                        :style="{ backgroundColor: themeOption.preview?.primary }"
                        :title="t('settings.appearance.primaryColor')"
                      />
                      <div
                        class="w-12 h-12 rounded-lg border border-border"
                        :style="{ backgroundColor: themeOption.preview?.background }"
                        :title="t('settings.appearance.backgroundColor')"
                      />
                      <div
                        class="w-12 h-12 rounded-lg border border-border flex items-center justify-center"
                        :style="{
                          backgroundColor: themeOption.preview?.background,
                          color: themeOption.preview?.text
                        }"
                        :title="t('settings.appearance.textColor')"
                      >
                        <span class="text-xl font-bold">Aa</span>
                      </div>
                    </div>
                  </button>
                </div>
              </div>

              <div class="pt-6 border-t border-border">
                <h3 class="text-lg font-medium text-text mb-4">
                  {{ t('settings.appearance.darkMode') }}
                </h3>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <button
                    :class="[
                      'p-4 rounded-lg border-2 transition-all text-center',
                      'hover:shadow-md',
                      theme.darkMode.value === 'light'
                        ? 'border-brand bg-brand/5'
                        : 'border-border hover:border-brand/50'
                    ]"
                    @click="theme.setDarkMode('light')"
                  >
                    <i class="i-tabler-sun text-3xl mb-2" :class="theme.darkMode.value === 'light' ? 'text-text' : 'text-text-secondary'" />
                    <div class="font-medium text-text">{{ t('settings.appearance.lightMode') }}</div>
                  </button>

                  <button
                    :class="[
                      'p-4 rounded-lg border-2 transition-all text-center',
                      'hover:shadow-md',
                      theme.darkMode.value === 'dark'
                        ? 'border-brand bg-brand/5'
                        : 'border-border hover:border-brand/50'
                    ]"
                    @click="theme.setDarkMode('dark')"
                  >
                    <i class="i-tabler-moon text-3xl mb-2" :class="theme.darkMode.value === 'dark' ? 'text-text' : 'text-text-secondary'" />
                    <div class="font-medium text-text">{{ t('settings.appearance.darkModeLabel') }}</div>
                  </button>

                  <button
                    :class="[
                      'p-4 rounded-lg border-2 transition-all text-center',
                      'hover:shadow-md',
                      theme.darkMode.value === 'auto'
                        ? 'border-brand bg-brand/5'
                        : 'border-border hover:border-brand/50'
                    ]"
                    @click="theme.setDarkMode('auto')"
                  >
                    <i class="i-tabler-device-laptop text-3xl mb-2" :class="theme.darkMode.value === 'auto' ? 'text-text' : 'text-text-secondary'" />
                    <div class="font-medium text-text">{{ t('settings.appearance.autoMode') }}</div>
                  </button>
                </div>
                <p class="text-sm text-text-muted mt-3">
                  {{ t('settings.appearance.autoModeDesc') }}
                </p>
              </div>

              <!-- 自定义主题提示 -->
              <div class="pt-6 border-t border-border">
                <div class="bg-bg-raised rounded-lg p-6">
                  <div class="flex items-start gap-4">
                    <i class="i-tabler-palette text-3xl text-brand flex-shrink-0 mt-1" />
                    <div class="flex-1">
                      <h4 class="text-base font-semibold text-text mb-2">
                        {{ t('settings.appearance.customTheme.title') }}
                      </h4>
                      <p class="text-sm text-text-secondary mb-4">
                        {{ t('settings.appearance.customTheme.desc') }}
                      </p>
                      <div class="text-xs text-text-muted">
                        <i class="i-tabler-info-circle inline mr-1" />
                        {{ t('settings.appearance.customTheme.adminOnly') }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
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
