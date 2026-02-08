import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface User {
  id: number
  username: string
  nickname: string
  email: string
  avatar: string
  role: string
  roles: string[]
  createdAt: string
  minecraftAccount?: {
    id: string
    username: string
    uuid: string
    linkedAt: string
  }
}

export const useUserStore = defineStore(
  'user',
  () => {
    // State
    const user = ref<User | null>(null)
    const token = ref<string>('')
    const refreshToken = ref<string>('')

    // Getters
    const isLoggedIn = computed(() => !!token.value && !!user.value)
    const userId = computed(() => user.value?.id || 0)
    const username = computed(() => user.value?.username || '')
    const nickname = computed(() => user.value?.nickname || '')
    const avatar = computed(() => user.value?.avatar || '')
    const userRole = computed(() => user.value?.role || 'USER')
    const roles = computed(() => user.value?.roles || [])

    // 权限检查
    const hasRole = (role: string) => {
      return roles.value.includes(role)
    }

    const isAdmin = computed(() => hasRole('ADMIN'))
    const isModerator = computed(() => hasRole('MODERATOR') || hasRole('ADMIN'))

    // Actions
    const setUser = (userData: User) => {
      user.value = userData
    }

    const setToken = (tokenValue: string, refreshTokenValue?: string) => {
      token.value = tokenValue
      if (refreshTokenValue) {
        refreshToken.value = refreshTokenValue
      }
    }

    const login = (userData: User, tokenValue: string, refreshTokenValue?: string) => {
      setUser(userData)
      setToken(tokenValue, refreshTokenValue)
    }

    const logout = () => {
      user.value = null
      token.value = ''
      refreshToken.value = ''
    }

    const updateUser = (userData: Partial<User>) => {
      if (user.value) {
        user.value = { ...user.value, ...userData }
      }
    }

    const updateAvatar = (avatarUrl: string) => {
      if (user.value) {
        user.value.avatar = avatarUrl
      }
    }

    return {
      // State
      user,
      token,
      refreshToken,
      
      // Getters
      isLoggedIn,
      userId,
      username,
      nickname,
      avatar,
      userRole,
      roles,
      isAdmin,
      isModerator,
      
      // Actions
      setUser,
      setToken,
      login,
      logout,
      updateUser,
      updateAvatar,
      hasRole
    }
  },
  { // 作用：数据持久化到 localStorage
    persist: {
      key: 'wknetic-user',
      storage: localStorage,
      pick: ['user', 'token', 'refreshToken']
    }
  }
)
