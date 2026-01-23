import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface AdminUser {
  id: string
  username: string
  email: string
  avatar?: string
  role: 'admin' | 'moderator' | 'editor'
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<AdminUser | null>(null)
  const token = ref<string | null>(null)
  const isLoading = ref(false)

  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'admin')

  // 模拟登录
  async function login(username: string, password: string): Promise<boolean> {
    isLoading.value = true
    
    // 模拟 API 请求延迟
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟验证 (实际应用中应调用后端 API)
    if (username === 'admin' && password === 'admin123') {
      user.value = {
        id: '1',
        username: 'admin',
        email: 'admin@modrinth.com',
        avatar: undefined,
        role: 'admin'
      }
      token.value = 'mock-jwt-token-' + Date.now()
      
      // 持久化到 localStorage
      localStorage.setItem('admin_token', token.value)
      localStorage.setItem('admin_user', JSON.stringify(user.value))
      
      isLoading.value = false
      return true
    }
    
    isLoading.value = false
    return false
  }

  // 登出
  function logout() {
    user.value = null
    token.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
  }

  // 检查并恢复登录状态
  function checkAuth(): boolean {
    const storedToken = localStorage.getItem('admin_token')
    const storedUser = localStorage.getItem('admin_user')
    
    if (storedToken && storedUser) {
      try {
        token.value = storedToken
        user.value = JSON.parse(storedUser)
        return true
      } catch {
        logout()
        return false
      }
    }
    return false
  }

  return {
    user,
    token,
    isLoading,
    isAuthenticated,
    isAdmin,
    login,
    logout,
    checkAuth
  }
})
