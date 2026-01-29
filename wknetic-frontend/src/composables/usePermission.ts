import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

/**
 * 用户角色类型
 */
export type UserRole = 'ADMIN' | 'MODERATOR' | 'USER' | 'VIP' | 'BANNED'

/**
 * 权限等级映射
 */
const ROLE_LEVELS: Record<UserRole, number> = {
  ADMIN: 100,
  MODERATOR: 50,
  VIP: 20,
  USER: 10,
  BANNED: 0
}

/**
 * 权限检查 Hook
 */
export function usePermission() {
  const authStore = useAuthStore()

  /**
   * 检查用户是否有指定角色
   */
  const hasRole = (role: UserRole): boolean => {
    return authStore.user?.role === role
  }

  /**
   * 检查用户是否有至少某个角色的权限（权限等级比较）
   */
  const hasMinRole = (minRole: UserRole): boolean => {
    const userRole = authStore.user?.role as UserRole
    if (!userRole) return false
    
    const userLevel = ROLE_LEVELS[userRole] || 0
    const minLevel = ROLE_LEVELS[minRole] || 0
    
    return userLevel >= minLevel
  }

  /**
   * 检查用户是否是管理员
   */
  const isAdmin = computed(() => hasRole('ADMIN'))

  /**
   * 检查用户是否是管理员或审核员
   */
  const isModerator = computed(() => hasMinRole('MODERATOR'))

  /**
   * 检查用户是否是VIP
   */
  const isVIP = computed(() => hasMinRole('VIP'))

  /**
   * 检查用户是否被封禁
   */
  const isBanned = computed(() => hasRole('BANNED'))

  /**
   * 获取用户角色
   */
  const userRole = computed(() => authStore.user?.role || 'USER')

  /**
   * 获取用户角色显示名称
   */
  const userRoleLabel = computed(() => {
    const roleLabels: Record<UserRole, string> = {
      ADMIN: '管理员',
      MODERATOR: '审核员',
      VIP: 'VIP会员',
      USER: '普通用户',
      BANNED: '已封禁'
    }
    return roleLabels[userRole.value as UserRole] || '未知'
  })

  /**
   * 获取角色颜色类
   */
  const userRoleColor = computed(() => {
    const roleColors: Record<UserRole, string> = {
      ADMIN: 'text-red-500',
      MODERATOR: 'text-blue-500',
      VIP: 'text-yellow-500',
      USER: 'text-gray-500',
      BANNED: 'text-gray-400'
    }
    return roleColors[userRole.value as UserRole] || 'text-gray-500'
  })

  return {
    hasRole,
    hasMinRole,
    isAdmin,
    isModerator,
    isVIP,
    isBanned,
    userRole,
    userRoleLabel,
    userRoleColor
  }
}
