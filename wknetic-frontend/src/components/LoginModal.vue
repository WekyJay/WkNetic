<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const props = defineProps<{
  isOpen: boolean
}>()

const emit = defineEmits<{
  close: []
  login: [credentials: { email: string; password: string }]
}>()

const { t } = useI18n()
const router = useRouter()

const email = ref('')
const password = ref('')
const rememberMe = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')

// 监听弹窗打开/关闭，处理body滚动
watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
    resetForm()
  }
})

function resetForm() {
  email.value = ''
  password.value = ''
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
  if (!email.value || !password.value) {
    errorMessage.value = 'Please fill in all fields'
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    emit('login', { email: email.value, password: password.value })
    handleClose()
  } catch (error) {
    errorMessage.value = 'Login failed. Please check your credentials.'
  } finally {
    isLoading.value = false
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

              <!-- Email -->
              <div class="space-y-2">
                <label for="email" class="block text-sm font-medium text-text">
                  Email or Username
                </label>
                <div class="relative">
                  <span class="i-tabler-mail absolute left-3 top-1/2 -translate-y-1/2 text-text-muted"></span>
                  <input
                    id="email"
                    v-model="email"
                    type="text"
                    autocomplete="email"
                    placeholder="Enter your email or username"
                    class="w-full input-base pl-10 pr-4"
                    :disabled="isLoading"
                  />
                </div>
              </div>

              <!-- Password -->
              <div class="space-y-2">
                <div class="flex items-center justify-between">
                  <label for="password" class="block text-sm font-medium text-text">
                    Password
                  </label>
                  <a href="#" class="text-xs text-brand hover:underline">
                    Forgot password?
                  </a>
                </div>
                <div class="relative">
                  <span class="i-tabler-lock absolute left-3 top-1/2 -translate-y-1/2 text-text-muted"></span>
                  <input
                    id="password"
                    v-model="password"
                    type="password"
                    autocomplete="current-password"
                    placeholder="Enter your password"
                    class="w-full input-base pl-10 pr-4"
                    :disabled="isLoading"
                  />
                </div>
              </div>

              <!-- Remember me -->
              <div class="flex items-center">
                <input
                  id="remember"
                  v-model="rememberMe"
                  type="checkbox"
                  class="w-4 h-4 text-brand bg-bg-surface border-border rounded focus:ring-brand focus:ring-2"
                  :disabled="isLoading"
                />
                <label for="remember" class="ml-2 text-sm text-text-muted">
                  Remember me for 30 days
                </label>
              </div>

              <!-- Submit button -->
              <button
                type="submit"
                class="w-full btn-primary py-3"
                :disabled="isLoading"
              >
                <span v-if="isLoading" class="i-tabler-loader-2 animate-spin text-lg"></span>
                <span v-else>Sign in</span>
              </button>

              <!-- Divider -->
              <div class="relative my-6">
                <div class="absolute inset-0 flex items-center">
                  <div class="w-full border-t border-border"></div>
                </div>
                <div class="relative flex justify-center text-xs">
                  <span class="px-2 bg-bg text-text-muted">Or continue with</span>
                </div>
              </div>

              <!-- Social login -->
              <div class="grid grid-cols-2 gap-3">
                <button
                  type="button"
                  class="btn-secondary py-2.5 text-sm"
                  :disabled="isLoading"
                >
                  <span class="i-tabler-brand-github text-lg"></span>
                  <span>GitHub</span>
                </button>
                <button
                  type="button"
                  class="btn-secondary py-2.5 text-sm"
                  :disabled="isLoading"
                >
                  <span class="i-tabler-brand-google text-lg"></span>
                  <span>Google</span>
                </button>
              </div>

              <!-- Sign up link -->
              <p class="text-center text-sm text-text-muted pt-4">
                Don't have an account?
                <button 
                  type="button"
                  class="text-brand hover:underline font-medium"
                  @click="goToRegister"
                >
                  Sign up
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
