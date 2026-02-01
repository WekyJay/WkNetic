import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, type LoginParams, type LoginResult, type UserInfo } from '@/api/auth'
import { storageManager } from '@/utils/storage'

export type AdminUser = UserInfo

export const useAuthStore = defineStore('auth', () => {
  const user = ref<AdminUser | null>(null)
  const token = ref<string | null>(null)
  const isLoading = ref(false)

  const isAuthenticated = computed(() => {
    // 检查 token 是否存在且未过期
    return !!token.value && !!user.value && !storageManager.isTokenExpired()
  })
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isModerator = computed(() => user.value?.role === 'MODERATOR' || user.value?.role === 'ADMIN')
  const userRole = computed(() => user.value?.role || 'USER')

  /**
   * 用户登录
   */
  async function login(
    username: string, 
    password: string, 
    captchaToken?: string, 
    rememberMe: boolean = false
  ): Promise<boolean> {
    isLoading.value = true
    
    try {
      const params: LoginParams = {
        username,
        password,
        captchaToken,
        rememberMe
      }
      
      // 调用后端登录接口
      const loginResponse = await authApi.login(params)
      const result = loginResponse.data as LoginResult
      
      // 存储 token 和过期时间（统一使用 localStorage，由后端控制过期时间）
      token.value = result.token
      storageManager.setToken(result.token)
      storageManager.setTokenExpiration(result.expiresAt)
      
      // 获取用户信息
      const userInfoResponse = await authApi.getUserInfo()
      const userInfo = userInfoResponse.data as UserInfo
      user.value = userInfo
      storageManager.setUser(userInfo)
      
      isLoading.value = false
      return true
      
    } catch (error: any) {
      console.error('登录失败:', error.message)
      isLoading.value = false
      return false
    }
  }

  /**
   * 用户注册
   */
  async function register(params: {
    username: string
    password: string
    email?: string
    nickname?: string
    captchaToken?: string
  }): Promise<{ success: boolean; message: string }> {
    isLoading.value = true
    
    try {
      await authApi.register(params)
      isLoading.value = false
      return { success: true, message: '注册成功' }
    } catch (error: any) {
      isLoading.value = false
      return { success: false, message: error.message || '注册失败' }
    }
  }

  /**
   * 重置密码
   */
  async function resetPassword(params: {
    username: string
    email?: string
    newPassword: string
    captchaToken?: string
  }): Promise<{ success: boolean; message: string }> {
    isLoading.value = true
    
    try {
      await authApi.resetPassword(params)
      isLoading.value = false
      return { success: true, message: '密码重置成功' }
    } catch (error: any) {
      isLoading.value = false
      return { success: false, message: error.message || '密码重置失败' }
    }
  }

  /**
   * 登出
   */
  function logout() {
    user.value = null
    token.value = null
    storageManager.clearAll()
  }

  /**
   * 检查并恢复登录状态
   */
  function checkAuth(): boolean {
    // 检查 token 是否过期
    if (storageManager.isTokenExpired()) {
      logout()
      return false
    }

    const storedToken = storageManager.getToken()
    const storedUser = storageManager.getUser()
    
    if (storedToken && storedUser) {
      try {
        token.value = storedToken
        user.value = storedUser
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
    isModerator,
    userRole,
    login,
    register,
    resetPassword,
    logout,
    checkAuth
  }
})
