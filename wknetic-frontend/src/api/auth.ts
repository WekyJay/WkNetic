import api from './axios'

/**
 * 登录参数
 */
export interface LoginParams {
  username: string
  password: string
  captchaToken?: string
  rememberMe?: boolean
}

/**
 * 注册参数
 */
export interface RegisterParams {
  username: string
  password: string
  email?: string
  nickname?: string
  captchaToken?: string
  emailCode?: string
}

/**
 * 重置密码参数
 */
export interface ResetPasswordParams {
  username: string
  email?: string
  newPassword: string
  captchaToken?: string
  emailCode?: string
}

/**
 * 登录响应
 */
export interface LoginResult {
  token: string
  expiresAt: number // Token 过期时间戳（毫秒）
}

/**
 * 用户信息
 */
export interface UserInfo {
  userId: number
  username: string
  nickname: string
  email: string
  phone?: string
  avatar?: string
  status: number
  role: 'ADMIN' | 'MODERATOR' | 'USER' | 'VIP' | 'BANNED'
  minecraftUuid?: string
  minecraftUsername?: string
}

/**
 * 验证码响应
 */
export interface CaptchaResult {
  sessionId: string
  image: string // base64 图片
}

/**
 * 认证相关 API
 */
export const authApi = {
  /**
   * 用户登录
   */
  login(params: LoginParams) {
    return api.post<LoginResult>('/api/v1/auth/login', params)
  },

  /**
   * 用户注册
   */
  register(params: RegisterParams) {
    return api.post<string>('/api/v1/auth/register', params)
  },

  /**
   * 重置密码
   */
  resetPassword(params: ResetPasswordParams) {
    return api.post<string>('/api/v1/auth/reset-password', params)
  },

  /**
   * 登出（可选，如果后端有登出接口）
   */
  logout() {
    return api.post('/api/v1/auth/logout')
  },

  /**
   * 获取当前用户信息
   */
  getUserInfo() {
    return api.get<UserInfo>('/api/v1/user/info')
  },

  /**
   * 获取验证码
   */
  getCaptcha() {
    return api.get<CaptchaResult>('/api/v1/captcha/generate')
  }
}

/**
 * 站点配置 API
 */
export const siteApi = {
  /**
   * 获取站点配置（公开接口）
   */
  getSiteConfig() {
    return api.get<Record<string, string>>('/api/v1/open/site-config')
  }
}
