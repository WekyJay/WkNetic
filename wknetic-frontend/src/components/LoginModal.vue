<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCaptcha } from '@/composables/useCaptcha'

const props = defineProps<{
  isOpen: boolean
}>()

const emit = defineEmits<{
  close: []
  success: []
}>()

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const rememberMe = ref(false)
const errorMessage = ref('')

// 验证码
const { captchaImage, captchaCode, isLoading: captchaLoading, getCaptchaToken, resetCaptcha, initCaptcha } = useCaptcha('simple')

// 监听弹窗打开/关闭，处理body滚动
watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    document.body.style.overflow = 'hidden'
    initCaptcha() // 打开弹窗时初始化验证码
  } else {
    document.body.style.overflow = ''
    resetForm()
  }
})

function resetForm() {
  username.value = ''
  password.value = ''
  captchaCode.value = ''
  rememberMe.value = false
  errorMessage.value = ''
}

function handleClose() {
  emit('close')
}

function handleBackdropClick(e: MouseEvent) {
  if (e.target === e.currentTarget) {
    handleClose()
  }
}

async function handleSubmit() {
  if (!username.value || !password.value) {
    errorMessage.value = '请填写用户名和密码'
    return
  }

  if (!captchaCode.value) {
    errorMessage.value = '请输入验证码'
    return
  }

  errorMessage.value = ''

  try {
    const captchaToken = getCaptchaToken()
    const success = await authStore.login(
      username.value,
      password.value,
      captchaToken,
      rememberMe.value
    )

    if (success) {
      emit('success')
      handleClose()
    } else {
      errorMessage.value = '登录失败，请检查用户名、密码和验证码'
      resetCaptcha()
      captchaCode.value = ''
    }
  } catch (error: any) {
    errorMessage.value = error.message || '登录失败，请重试'
    resetCaptcha()
    captchaCode.value = ''
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    handleClose()
  }
}

function goToRegister() {
  handleClose()
  router.push('/register')
}

function goToFullLogin() {
  handleClose()
  router.push('/login')
}
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition-opacity duration-200"
      leave-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      leave-to-class="opacity-0"
    >
      <div
        v-if="isOpen"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm"
        @click="handleBackdropClick"
        @keydown="handleKeydown"
      >
        <Transition
          enter-active-class="transition-all duration-200"
          leave-active-class="transition-all duration-200"
          enter-from-class="opacity-0 scale-95"
          leave-to-class="opacity-0 scale-95"
        >
          <div
            v-if="isOpen"
            class="relative w-full max-w-md bg-bg border border-border rounded-2xl shadow-2xl overflow-hidden"
            @click.stop
          >
            <!-- 关闭按钮 -->
            <button
              class="absolute top-4 right-4 p-2 text-text-muted hover:text-text transition-colors rounded-lg hover:bg-bg-surface"
              @click="handleClose"
            >
              <span class="i-tabler-x text-xl"></span>
            </button>

            <!-- 头部 -->
            <div class="px-6 pt-6 pb-4 text-center">
              <div class="inline-flex items-center justify-center w-16 h-16 bg-brand/10 rounded-2xl mb-4">
                <span class="i-tabler-cube text-brand text-3xl"></span>
              </div>
              <h2 class="text-2xl font-bold text-text mb-2">Welcome Back</h2>
              <p class="text-text-muted text-sm">Sign in to your WkNetic account</p>
            </div>

            <!-- 表单 -->
            <form @submit.prevent="handleSubmit" class="px-6 pb-6 space-y-4">
              <!-- 错误提示 -->
              <div
                v-if="errorMessage"
                class="p-3 bg-danger/10 border border-danger/20 rounded-lg text-danger text-sm flex items-center gap-2"
              >
                <span class="i-tabler-alert-circle text-lg"></span>
                <span>{{ errorMessage }}</span>
              </div>

              <!-- Username -->
              <div class="space-y-2">
                <label for="username" class="block text-sm font-medium text-text">
                  用户名
                </label>
                <div class="relative">
                  <span class="i-tabler-user absolute left-3 top-1/2 -translate-y-1/2 text-text-muted"></span>
                  <input
                    id="username"
                    v-model="username"
                    type="text"
                    autocomplete="username"
                    placeholder="请输入用户名"
                    class="w-full input-base pl-10 pr-4"
                    :disabled="authStore.isLoading"
                  />
                </div>
              </div>

              <!-- Password -->
              <div class="space-y-2">
                <div class="flex items-center justify-between">
                  <label for="password" class="block text-sm font-medium text-text">
                    密码
                  </label>
                </div>
                <div class="relative">
                  <span class="i-tabler-lock absolute left-3 top-1/2 -translate-y-1/2 text-text-muted"></span>
                  <input
                    id="password"
                    v-model="password"
                    type="password"
                    autocomplete="current-password"
                    placeholder="请输入密码"
                    class="w-full input-base pl-10 pr-4"
                    :disabled="authStore.isLoading"
                  />
                </div>
              </div>

              <!-- Captcha -->
              <div class="space-y-2">
                <label for="captcha" class="block text-sm font-medium text-text">
                  验证码
                </label>
                <div class="flex gap-2">
                  <div class="relative flex-1">
                    <span class="i-tabler-shield-check absolute left-3 top-1/2 -translate-y-1/2 text-text-muted"></span>
                    <input
                      id="captcha"
                      v-model="captchaCode"
                      type="text"
                      placeholder="请输入验证码"
                      class="w-full input-base pl-10"
                      maxlength="4"
                      :disabled="authStore.isLoading"
                    />
                  </div>
                  <div
                    class="w-24 h-10 bg-bg-surface rounded-lg border border-border overflow-hidden cursor-pointer hover:opacity-80 transition-opacity flex-shrink-0"
                    @click="resetCaptcha"
                    :class="{ 'opacity-50': captchaLoading }"
                  >
                    <img v-if="captchaImage" :src="captchaImage" alt="验证码" class="w-full h-full object-cover" />
                    <div v-else class="w-full h-full flex items-center justify-center text-xs text-text-muted">
                      <span class="i-tabler-loader-2 animate-spin"></span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Remember me -->
              <div class="flex items-center justify-between">
                <div class="flex items-center">
                  <input
                    id="remember"
                    v-model="rememberMe"
                    type="checkbox"
                    class="w-4 h-4 text-brand bg-bg-surface border-border rounded focus:ring-brand focus:ring-2"
                    :disabled="authStore.isLoading"
                  />
                  <label for="remember" class="ml-2 text-sm text-text-muted">
                    记住我(30天)
                  </label>
                </div>
                <button
                  type="button"
                  @click="goToFullLogin"
                  class="text-xs text-brand hover:underline"
                  :disabled="authStore.isLoading"
                >
                  前往完整登录页
                </button>
              </div>

              <!-- Submit button -->
              <button
                type="submit"
                class="w-full btn-primary py-3"
                :disabled="authStore.isLoading"
              >
                <span v-if="authStore.isLoading" class="i-tabler-loader-2 animate-spin text-lg"></span>
                <span v-else>登录</span>
              </button>

              <!-- Divider -->
              <div class="relative my-6">
                <div class="absolute inset-0 flex items-center">
                  <div class="w-full border-t border-border"></div>
                </div>
                <div class="relative flex justify-center text-xs">
                  <span class="px-2 bg-bg text-text-muted">其他登录方式</span>
                </div>
              </div>

              <!-- Social login -->
              <div class="grid grid-cols-2 gap-3">
                <button
                  type="button"
                  class="btn-secondary py-2.5 text-sm"
                  :disabled="true"
                  title="暂未开放"
                >
                  <span class="i-tabler-brand-github text-lg"></span>
                  <span>GitHub</span>
                </button>
                <button
                  type="button"
                  class="btn-secondary py-2.5 text-sm"
                  :disabled="true"
                  title="暂未开放"
                >
                  <span class="i-tabler-brand-google text-lg"></span>
                  <span>Google</span>
                </button>
              </div>

              <!-- Sign up link -->
              <p class="text-center text-sm text-text-muted pt-4">
                还没有账号？
                <button 
                  type="button"
                  class="text-brand hover:underline font-medium"
                  @click="goToRegister"
                >
                  立即注册
                </button>
              </p>
            </form>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
input:focus {
  outline: none;
  box-shadow: 0 0 0 2px var(--color-brand-alpha);
}

input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
