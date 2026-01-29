import api from './axios'

/**
 * 用户角色类型
 */
export type UserRole = 'ADMIN' | 'MODERATOR' | 'USER' | 'VIP' | 'BANNED'

/**
 * 用户角色映射
 */
export const USER_ROLE_MAP: Record<UserRole, string> = {
  ADMIN: '管理员',
  MODERATOR: '版主',
  USER: '普通用户',
  VIP: 'VIP会员',
  BANNED: '封禁'
}

/**
 * 用户状态类型
 */
export type UserStatus = 0 | 1

/**
 * 用户状态映射
 */
export const USER_STATUS_MAP: Record<UserStatus, string> = {
  0: '禁用',
  1: '启用'
}

/**
 * 用户信息
 */
export interface User {
  userId: number
  username: string
  password?: string
  nickname: string
  email?: string
  phone?: string
  avatar?: string
  status: UserStatus
  role: UserRole
  minecraftUuid?: string
  minecraftUsername?: string
  createTime: string
  updateTime: string
}

/**
 * 用户查询参数
 */
export interface UserQueryParams {
  page?: number
  size?: number
  keyword?: string
  status?: UserStatus
  role?: UserRole
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * Minecraft账号信息
 */
export interface MinecraftProfile {
  id: string
  name: string
  valid: boolean
  error?: string
}

/**
 * 用户管理 API
 */
export const userApi = {
  /**
   * 获取用户列表（分页）
   */
  getUserList(params: UserQueryParams) {
    return api.get<PageResult<User>>('/api/v1/admin/users', { params })
  },

  /**
   * 获取单个用户信息
   */
  getUserById(id: number) {
    return api.get<User>(`/api/v1/admin/users/${id}`)
  },

  /**
   * 创建用户
   */
  createUser(user: Partial<User>) {
    return api.post<string>('/api/v1/admin/users', user)
  },

  /**
   * 更新用户信息
   */
  updateUser(id: number, user: Partial<User>) {
    return api.put<string>(`/api/v1/admin/users/${id}`, user)
  },

  /**
   * 删除用户
   */
  deleteUser(id: number) {
    return api.delete<string>(`/api/v1/admin/users/${id}`)
  },

  /**
   * 切换用户状态
   */
  toggleUserStatus(id: number, status: UserStatus) {
    return api.patch<string>(`/api/v1/admin/users/${id}/status`, null, {
      params: { status }
    })
  },

  /**
   * 验证Minecraft UUID
   */
  validateMinecraftUuid(uuid: string) {
    return api.get<MinecraftProfile>(`/api/v1/admin/users/validate-mc-uuid/${uuid}`)
  }
}
