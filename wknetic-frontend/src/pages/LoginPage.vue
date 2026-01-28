<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const { t } = useI18n()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const error = ref('')
const rememberMe = ref(false)

// 如果已登录，重定向到 dashboard
const checkAuthAndRedirect = () => {
  if (authStore.checkAuth()) {
    router.push('/admin')
  }
}

checkAuthAndRedirect()

async function handleLogin() {
  error.value = ''
  
  if (!username.value || !password.value) {
    error.value = t('pages.error_required')
    return
  }
  
  const success = await authStore.login(username.value, password.value)
  
  if (success) {
    router.push('/admin')
  } else {
    error.value = t('pages.error_invalid')
  }
}
</script>

<template>
  <div class="min-h-screen bg-bg-darker flex items-center justify-center p-4">
    <!-- Background decoration -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none">
      <div class="absolute top-1/4 left-1/4 w-96 h-96 bg-brand/5 rounded-full blur-3xl"></div>
      <div class="absolute bottom-1/4 right-1/4 w-96 h-96 bg-brand/5 rounded-full blur-3xl"></div>
    </div>

    <div class="w-full max-w-md relative z-10">
      <!-- Logo -->
      <div class="text-center mb-8">
        <router-link to="/" class="inline-flex items-center gap-3 mb-4">
          <div class="w-14 h-14 bg-brand rounded-2xl flex-center">
            <span class="i-tabler-cube text-bg text-3xl"></span>
          </div>
        </router-link>
        <h1 class="text-2xl font-bold text-text">{{ t('pages.admin_login_title') }}</h1>
        <p class="text-text-muted mt-1">{{ t('pages.admin_login_subtitle') }}</p>
      </div>

      <!-- Login form -->
      <div class="bg-bg rounded-2xl border border-border p-8">
        <form @submit.prevent="handleLogin" class="space-y-5">
          <!-- Error message -->
          <div 
            v-if="error"
            class="p-3 rounded-lg bg-danger/10 border border-danger/20 text-danger text-sm flex items-center gap-2"
          >
            <span class="i-tabler-alert-circle text-lg flex-shrink-0"></span>
            {{ error }}
          </div>

          <!-- Username -->
          <div class="space-y-2">
            <label for="username" class="block text-sm font-medium text-text">
              {{ t('pages.username') }}
            </label>
            <div class="relative">
              <span class="i-tabler-user absolute left-3 top-1/2 -translate-y-1/2 text-text-muted"></span>
              <input 
                id="username"
                v-model="username"
                type="text"
                :placeholder="t('pages.username')"
                class="w-full input-base pl-10"
                autocomplete="username"
              />
            </div>
          </div>

          <!-- Password -->
          <div class="space-y-2">
            <label for="password" class="block text-sm font-medium text-text">
              {{ t('pages.password') }}
            </label>
            <div class="relative">
              <span class="i-tabler-lock absolute left-3 top-1/2 -translate-y-1/2 text-text-muted"></span>
              <input 
                id="password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                :placeholder="t('pages.password')"
                class="w-full input-base pl-10 pr-10"
                autocomplete="current-password"
              />
              <button
                type="button"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted hover:text-text transition-colors"
                @click="showPassword = !showPassword"
              >
                <span :class="showPassword ? 'i-tabler-eye' : 'i-tabler-eye-off'"></span>
              </button>
            </div>
          </div>

          <!-- Remember me -->
          <div class="flex items-center">
            <input
              id="remember"
              v-model="rememberMe"
              type="checkbox"
              class="w-4 h-4 text-brand bg-bg-surface border-border rounded focus:ring-brand focus:ring-2"
            />
            <label for="remember" class="ml-2 text-sm text-text-muted">
              {{ t('pages.remember_me') }}
            </label>
          </div>

          <!-- Submit button -->
          <button type="submit" class="w-full btn-primary py-3">
            {{ t('pages.sign_in') }}
          </button>
                :type="showPassword ? 'text' : 'password'"
                placeholder="Enter your password"
                class="w-full input-base pl-10 pr-10"
                autocomplete="current-password"
              />
              <button 
                type="button"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted hover:text-text transition-colors"
                @click="showPassword = !showPassword"
              >
                <span :class="showPassword ? 'i-tabler-eye-off' : 'i-tabler-eye'" class="text-lg"></span>
              </button>
            </div>
          </div>

          <!-- Remember me & Forgot password -->
          <div class="flex items-center justify-between">
            <label class="flex items-center gap-2 cursor-pointer">
              <input 
                v-model="rememberMe"
                type="checkbox"
                class="w-4 h-4 rounded border-border bg-bg-surface text-brand focus:ring-brand/50"
              />
              <span class="text-sm text-text-muted">Remember me</span>
            </label>
            <a href="#" class="text-sm text-brand hover:underline">
              Forgot password?
            </a>
          </div>

          <!-- Submit button -->
          <button 
            type="submit"
            :disabled="authStore.isLoading"
            class="w-full btn-primary py-3 text-base font-medium disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="authStore.isLoading" class="i-tabler-loader-2 animate-spin text-lg"></span>
            <span v-else>Sign in</span>
          </button>
        </form>

        <!-- Demo credentials -->
        <div class="mt-6 p-4 rounded-lg bg-bg-surface border border-border">
          <p class="text-sm text-text-muted mb-2">Demo credentials:</p>
          <div class="flex items-center gap-4 text-sm">
            <code class="px-2 py-1 bg-bg rounded text-brand">admin</code>
            <code class="px-2 py-1 bg-bg rounded text-brand">admin123</code>
          </div>
        </div>
      </div>

      <!-- Back to site -->
      <div class="mt-6 text-center">
        <router-link to="/" class="text-sm text-text-muted hover:text-text inline-flex items-center gap-1">
          <span class="i-tabler-arrow-left"></span>
          Back to WkNetic
        </router-link>
      </div>
    </div>
  </div>
</template>
