import api from './axios'
import type { ExtendedUserProfile, UpdateProfileRequest, FollowStatus, UserStats } from '@/types/user'

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
 * MinecraftAuth设备流启动响应
 */
export interface MinecraftDeviceFlowStartResponse {
  device_code: string
  user_code: string
  verification_uri: string
  verification_uri_complete: string
  expires_in: number
  interval: number
  message?: string
}

/**
 * MinecraftAuth设备流轮询响应
 */
export interface MinecraftDeviceFlowPollResponse {
  status: 'pending' | 'authorized' | 'expired' | 'error'
  access_token?: string
  error?: string
  error_description?: string
}

/**
 * Microsoft账户绑定结果
 */
export interface MinecraftBindResult {
  success: boolean
  message: string
  user_id?: number
  warning?: string
  minecraft_uuid?: string
  minecraft_username?: string
}


/**
 * MinecraftAuth配置响应
 */
export interface MinecraftAuthConfig {
  code: number
  timestamp: number
  message: string
  data?: {
    client_id: string
    scope: string
    polling_timeout: number
    enabled: boolean
    device_token_endpoint: string
    device_code_endpoint: string
    polling_interval: number
  }
  success?: boolean
}

/**
 * MinecraftAuth状态检查响应
 */
export interface MinecraftAuthStatusResponse {
  enabled: boolean
  message: string
  timestamp: number
}

/**
 * MinecraftAuth连接测试响应
 */
export interface MinecraftAuthTestConnectionResponse {
  status: 'success' | 'error'
  message: string
  timestamp: number
}

/**
 * 设备流轮询响应
 */
export interface DeviceFlowPollResponse {
  status: 'pending' | 'authorized' | 'expired' | 'error'
  accessToken?: string
  error?: string
  errorDescription?: string
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
    return api.get<MinecraftProfile>(`/api/v1/user/minecraft/validate/${uuid}`)
  },

  /**
   * 绑定Minecraft账号（当前登录用户）
   */
  bindMinecraftAccount(minecraftUuid: string, minecraftUsername: string) {
    return api.post<string>('/api/v1/user/minecraft/bind', {
      minecraftUuid,
      minecraftUsername
    })
  },

  /**
   * 解绑Minecraft账号（当前登录用户）
   */
  unbindMinecraftAccount() {
    return api.delete<string>('/api/v1/user/minecraft/unbind')
  },

  /**
   * 获取Minecraft绑定信息（当前登录用户）
   */
  getMinecraftBindingInfo() {
    return api.get<MinecraftBindingInfo>('/api/v1/user/minecraft/binding-info')
  },

  /**
   * 获取用户公开资料（任何人可访问）
   */
  getUserProfile(userId: number) {
    return api.get<ExtendedUserProfile>(`/api/v1/user/profile/${userId}`)
  },

  /**
   * 更新当前用户的个人资料
   */
  updateMyProfile(data: UpdateProfileRequest) {
    return api.put<string>('/api/v1/user/profile', data)
  },

  /**
   * 关注用户
   */
  followUser(userId: number) {
    return api.post<FollowStatus>(`/api/v1/user/${userId}/follow`)
  },

  /**
   * 取消关注用户
   */
  unfollowUser(userId: number) {
    return api.delete<FollowStatus>(`/api/v1/user/${userId}/follow`)
  },

  /**
   * 获取用户统计信息
   */
  getUserStats(userId: number) {
    return api.get<UserStats>(`/api/v1/user/profile/${userId}/stats`)
  },

  /**
   * 获取用户的关注列表
   */
  getUserFollowing(userId: number, page = 1, size = 20) {
    return api.get<PageResult<ExtendedUserProfile>>(`/api/v1/user/${userId}/following`, {
      params: { page, size }
    })
  },

  /**
   * 获取用户的粉丝列表
   */
  getUserFollowers(userId: number, page = 1, size = 20) {
    return api.get<PageResult<ExtendedUserProfile>>(`/api/v1/user/${userId}/followers`, {
      params: { page, size }
    })
  },

  /**
   * 获取Microsoft OAuth授权URL
   */
  getMicrosoftAuthorizationUrl() {
    return api.get<string>('/api/v1/oauth/microsoft/authorize')
  },

  /**
   * 获取Microsoft OAuth配置状态
   */
  getMicrosoftConfig() {
    return api.get<MinecraftAuthConfig>('/api/v1/oauth/microsoft/config')
  },

  /**
   * 启动Minecraft设备流认证（使用MinecraftAuth库）
   */
  startMinecraftDeviceFlow() {
    return api.post<MinecraftDeviceFlowStartResponse>('/api/v1/minecraft-auth/device-flow/start')
  },

  /**
   * 验证Minecraft UUID（使用MinecraftAuth库）
   */
  validateMinecraftUuidWithAuth(uuid: string) {
    return api.get<MinecraftProfile>('/api/v1/minecraft-auth/validate-uuid/' + encodeURIComponent(uuid))
  },

  /**
   * 使用Microsoft令牌绑定Minecraft账户（使用MinecraftAuth库）
   */
  bindMinecraftWithMicrosoftToken(accessToken: string, userId?: number) {
    return api.post<MinecraftBindResult>('/api/v1/minecraft-auth/bind-with-microsoft', {
      access_token: accessToken,
      user_id: userId?.toString() || ''
    })
  },

  /**
   * 轮询设备流状态
   */
  pollDeviceFlowState(deviceCode: string) {
    return api.get<any>(`/api/v1/minecraft-auth/device-flow/poll/${deviceCode}`)
  },

  /**
   * 启动带用户ID的设备流认证
   */
  startDeviceFlowWithUser(userId: number) {
    return api.post<MinecraftDeviceFlowStartResponse>(`/api/v1/minecraft-auth/device-flow/start-with-user/${userId}`)
  },

  /**
   * 获取Microsoft OAuth配置信息（使用MinecraftAuth库）
   */
  getMinecraftAuthConfig() {
    return api.get<MinecraftAuthConfig>('/api/v1/minecraft-auth/config')
  },

  /**
   * 测试MinecraftAuth库连接
   */
  testMinecraftAuthConnection() {
    return api.get<MinecraftAuthTestConnectionResponse>('/api/v1/minecraft-auth/test-connection')
  },

  /**
   * 检查Microsoft OAuth功能状态
   */
  checkMinecraftAuthStatus() {
    return api.get<MinecraftAuthStatusResponse>('/api/v1/minecraft-auth/status')
  },

  /**
   * 启动Microsoft OAuth设备流（旧版兼容）
   */
  startDeviceFlow() {
    return api.post<MinecraftDeviceFlowStartResponse>('/api/v1/minecraft-auth/device-flow/start')
  },

  /**
   * 轮询设备流状态（旧版兼容）
   */
  pollDeviceFlow(deviceCode: string) {
    return api.post<MinecraftDeviceFlowPollResponse>('/api/v1/minecraft-auth/device-flow/poll', {
      deviceCode
    })
  },

  /**
   * 使用设备流访问令牌绑定Microsoft账户（旧版兼容）
   */
  bindMicrosoftWithDeviceFlow(accessToken: string) {
    return api.post<MinecraftBindResult>('/api/v1/minecraft-auth/bind-with-microsoft', {
      access_token: accessToken
    })
  }
}
