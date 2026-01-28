<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import LegalModal from '@/components/common/LegalModal.vue'
import { termsOfService, privacyPolicy } from '@/data/legal'

const router = useRouter()
const { t } = useI18n()

const formData = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const acceptTerms = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')
const errors = ref<Record<string, string>>({})

// 弹窗状态
const showTermsModal = ref(false)
const showPrivacyModal = ref(false)

const validateForm = () => {
  errors.value = {}
  
  if (!formData.value.username) {
    errors.value.username = t('register.errors.usernameRequired')
  } else if (formData.value.username.length < 3) {
    errors.value.username = t('register.errors.usernameMinLength')
  }
  
  if (!formData.value.email) {
    errors.value.email = t('register.errors.emailRequired')
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.value.email)) {
    errors.value.email = t('register.errors.emailInvalid')
  }
  
  if (!formData.value.password) {
    errors.value.password = t('register.errors.passwordRequired')
  } else if (formData.value.password.length < 8) {
    errors.value.password = t('register.errors.passwordMinLength')
  }
  
  if (formData.value.password !== formData.value.confirmPassword) {
    errors.value.confirmPassword = t('register.errors.passwordMismatch')
  }
  
  if (!acceptTerms.value) {
    errors.value.terms = t('register.errors.termsRequired')
  }
  
  return Object.keys(errors.value).length === 0
}

const handleSubmit = async () => {
  errorMessage.value = ''
  
  if (!validateForm()) {
    return
  }
  
  isLoading.value = true
  
  try {
    // 模拟 API 调用
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 注册成功后跳转到首页
    router.push('/')
  } catch (error) {
    errorMessage.value = t('register.errors.registrationFailed')
  } finally {
    isLoading.value = false
  }
}

const goToLogin = () => {
  router.push('/')
}

const openTermsModal = (e: Event) => {
  e.preventDefault()
  showTermsModal.value = true
}

const openPrivacyModal = (e: Event) => {
  e.preventDefault()
  showPrivacyModal.value = true
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-bg via-bg to-bg-surface flex items-center justify-center px-4 py-12">
    <!-- 背景装饰 - 使用 fixed 定位 -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none">
      <div class="absolute top-20 left-10 w-72 h-72 bg-brand/5 rounded-full blur-3xl" />
      <div class="absolute bottom-20 right-10 w-96 h-96 bg-brand-dark/5 rounded-full blur-3xl" />
    </div>
    
    <div class="relative w-full max-w-md">
      <!-- 返回首页按钮 -->
      <button 
        class="flex items-center gap-2 text-text-secondary hover:text-brand transition-colors mb-6"
        @click="goToLogin"
      >
        <span class="i-tabler-arrow-left" />
        {{ t('register.backToHome') }}
      </button>
      
      <!-- 注册卡片 -->
      <div class="bg-bg-raised border border-border rounded-xl shadow-xl p-8">
        <!-- 头部 -->
        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-brand to-brand-dark rounded-2xl mb-4">
            <span class="i-tabler-user-plus text-3xl text-white" />
          </div>
          <h1 class="text-2xl font-bold text-text mb-2">{{ t('register.title') }}</h1>
          <p class="text-text-muted">{{ t('register.subtitle') }}</p>
        </div>
        
        <!-- 错误提示 -->
        <Transition
          enter-active-class="transition-all duration-200"
          enter-from-class="opacity-0 -translate-y-2"
          leave-active-class="transition-all duration-150"
          leave-to-class="opacity-0 -translate-y-2"
        >
          <div 
            v-if="errorMessage" 
            class="mb-4 p-3 bg-red-500/10 border border-red-500/20 rounded-lg flex items-center gap-2 text-red-400 text-sm"
          >
            <span class="i-tabler-alert-circle" />
            {{ errorMessage }}
          </div>
        </Transition>
        
        <!-- 注册表单 -->
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <!-- 用户名 -->
          <div>
            <label class="block text-sm font-medium text-text-secondary mb-2">
              {{ t('register.username') }}
            </label>
            <div class="relative">
              <span class="absolute left-3 top-1/2 -translate-y-1/2 i-tabler-user text-text-muted" />
              <input
                v-model="formData.username"
                type="text"
                :placeholder="t('register.usernamePlaceholder')"
                class="w-full pl-10 pr-4 py-2.5 bg-bg border rounded-lg transition-colors"
                :class="[
                  errors.username 
                    ? 'border-red-500/50 focus:border-red-500' 
                    : 'border-border focus:border-brand'
                ]"
              />
            </div>
            <p v-if="errors.username" class="mt-1 text-xs text-red-400">{{ errors.username }}</p>
          </div>
          
          <!-- 邮箱 -->
          <div>
            <label class="block text-sm font-medium text-text-secondary mb-2">
              {{ t('register.email') }}
            </label>
            <div class="relative">
              <span class="absolute left-3 top-1/2 -translate-y-1/2 i-tabler-mail text-text-muted" />
              <input
                v-model="formData.email"
                type="email"
                :placeholder="t('register.emailPlaceholder')"
                class="w-full pl-10 pr-4 py-2.5 bg-bg border rounded-lg transition-colors"
                :class="[
                  errors.email 
                    ? 'border-red-500/50 focus:border-red-500' 
                    : 'border-border focus:border-brand'
                ]"
              />
            </div>
            <p v-if="errors.email" class="mt-1 text-xs text-red-400">{{ errors.email }}</p>
          </div>
          
          <!-- 密码 -->
          <div>
            <label class="block text-sm font-medium text-text-secondary mb-2">
              {{ t('register.password') }}
            </label>
            <div class="relative">
              <span class="absolute left-3 top-1/2 -translate-y-1/2 i-tabler-lock text-text-muted" />
              <input
                v-model="formData.password"
                type="password"
                :placeholder="t('register.passwordPlaceholder')"
                class="w-full pl-10 pr-4 py-2.5 bg-bg border rounded-lg transition-colors"
                :class="[
                  errors.password 
                    ? 'border-red-500/50 focus:border-red-500' 
                    : 'border-border focus:border-brand'
                ]"
              />
            </div>
            <p v-if="errors.password" class="mt-1 text-xs text-red-400">{{ errors.password }}</p>
            <p v-else class="mt-1 text-xs text-text-muted">{{ t('register.passwordHint') }}</p>
          </div>
          
          <!-- 确认密码 -->
          <div>
            <label class="block text-sm font-medium text-text-secondary mb-2">
              {{ t('register.confirmPassword') }}
            </label>
            <div class="relative">
              <span class="absolute left-3 top-1/2 -translate-y-1/2 i-tabler-lock-check text-text-muted" />
              <input
                v-model="formData.confirmPassword"
                type="password"
                :placeholder="t('register.confirmPasswordPlaceholder')"
                class="w-full pl-10 pr-4 py-2.5 bg-bg border rounded-lg transition-colors"
                :class="[
                  errors.confirmPassword 
                    ? 'border-red-500/50 focus:border-red-500' 
                    : 'border-border focus:border-brand'
                ]"
              />
            </div>
            <p v-if="errors.confirmPassword" class="mt-1 text-xs text-red-400">{{ errors.confirmPassword }}</p>
          </div>
          
          <!-- 服务条款 -->
          <div>
            <label class="flex items-start gap-2 cursor-pointer group">
              <input
                v-model="acceptTerms"
                type="checkbox"
                class="mt-0.5 w-4 h-4 rounded border-border bg-bg text-brand focus:ring-brand focus:ring-offset-0"
              />
              <span class="text-sm text-text-secondary group-hover:text-text transition-colors">
                {{ t('register.termsPrefix') }}
                <a href="#" class="text-brand hover:underline" @click="openTermsModal">{{ t('register.termsOfService') }}</a> 
                {{ t('register.and') }}
                <a href="#" class="text-brand hover:underline" @click="openPrivacyModal">{{ t('register.privacyPolicy') }}</a>
              </span>
            </label>
            <p v-if="errors.terms" class="mt-1 text-xs text-red-400">{{ errors.terms }}</p>
          </div>
          
          <!-- 提交按钮 -->
          <button
            type="submit"
            class="w-full py-3 bg-gradient-to-r from-brand to-brand-dark text-white font-medium rounded-lg hover:shadow-lg hover:shadow-brand/20 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            :disabled="isLoading"
          >
            <span v-if="isLoading" class="i-tabler-loader-2 animate-spin" />
            <span v-else class="i-tabler-user-check" />
            {{ isLoading ? t('register.signingUp') : t('register.signUp') }}
          </button>
          
          <!-- 分割线 -->
          <div class="relative my-6">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-border" />
            </div>
            <div class="relative flex justify-center text-xs">
              <span class="px-2 bg-bg-raised text-text-muted">{{ t('register.orContinueWith') }}</span>
            </div>
          </div>
          
          <!-- 第三方登录 -->
          <div class="grid grid-cols-2 gap-3">
            <button
              type="button"
              class="py-2.5 px-4 bg-bg border border-border rounded-lg hover:bg-bg-hover transition-colors flex items-center justify-center gap-2 text-sm font-medium text-text"
            >
              <span class="i-tabler-brand-github text-lg" />
              GitHub
            </button>
            <button
              type="button"
              class="py-2.5 px-4 bg-bg border border-border rounded-lg hover:bg-bg-hover transition-colors flex items-center justify-center gap-2 text-sm font-medium text-text"
            >
              <span class="i-tabler-brand-google text-lg" />
              Google
            </button>
          </div>
          
          <!-- 登录链接 -->
          <p class="text-center text-sm text-text-muted pt-4">
            {{ t('register.alreadyHaveAccount') }}
            <button 
              type="button"
              class="text-brand hover:underline font-medium"
              @click="goToLogin"
            >
              {{ t('register.signIn') }}
            </button>
          </p>
        </form>
      </div>
    </div>

    <!-- 服务条款弹窗 -->
    <LegalModal
      :is-open="showTermsModal"
      :content="termsOfService"
      @close="showTermsModal = false"
    />

    <!-- 隐私政策弹窗 -->
    <LegalModal
      :is-open="showPrivacyModal"
      :content="privacyPolicy"
      @close="showPrivacyModal = false"
    />
  </div>
</template>
