/**
 * Storage 工具类
 * 统一使用 localStorage 存储，由后端管理 token 过期时间
 */

const TOKEN_KEY = 'wknetic_token'
const USER_KEY = 'wknetic_user'
const EXPIRES_KEY = 'wknetic_token_expires_at'

class StorageManager {
  /**
   * 存储 token
   */
  setToken(token: string) {
    try {
      localStorage.setItem(TOKEN_KEY, token)
    } catch (error) {
      console.error('存储 token 失败:', error)
    }
  }

  /**
   * 获取 token
   */
  getToken(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY)
    } catch (error) {
      console.error('读取 token 失败:', error)
      return null
    }
  }

  /**
   * 删除 token
   */
  removeToken() {
    try {
      localStorage.removeItem(TOKEN_KEY)
    } catch (error) {
      console.error('删除 token 失败:', error)
    }
  }

  /**
   * 存储用户信息
   */
  setUser(user: any) {
    try {
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    } catch (error) {
      console.error('存储用户信息失败:', error)
    }
  }

  /**
   * 获取用户信息
   */
  getUser(): any | null {
    try {
      const userStr = localStorage.getItem(USER_KEY)
      return userStr ? JSON.parse(userStr) : null
    } catch (error) {
      console.error('读取用户信息失败:', error)
      return null
    }
  }

  /**
   * 删除用户信息
   */
  removeUser() {
    try {
      localStorage.removeItem(USER_KEY)
    } catch (error) {
      console.error('删除用户信息失败:', error)
    }
  }

  /**
   * 存储 token 过期时间
   */
  setTokenExpiration(expiresAt: number) {
    try {
      localStorage.setItem(EXPIRES_KEY, String(expiresAt))
    } catch (error) {
      console.error('存储过期时间失败:', error)
    }
  }

  /**
   * 获取 token 过期时间
   */
  getTokenExpiration(): number | null {
    try {
      const expiresStr = localStorage.getItem(EXPIRES_KEY)
      return expiresStr ? parseInt(expiresStr, 10) : null
    } catch (error) {
      console.error('读取过期时间失败:', error)
      return null
    }
  }

  /**
   * 删除过期时间
   */
  removeTokenExpiration() {
    try {
      localStorage.removeItem(EXPIRES_KEY)
    } catch (error) {
      console.error('删除过期时间失败:', error)
    }
  }

  /**
   * 检查 token 是否过期
   */
  isTokenExpired(): boolean {
    const expiresAt = this.getTokenExpiration()
    if (!expiresAt) return true
    return Date.now() >= expiresAt
  }

  /**
   * 清除所有认证数据
   */
  clearAll() {
    this.removeToken()
    this.removeUser()
    this.removeTokenExpiration()
  }
}

export const storageManager = new StorageManager()
