import axios from 'axios'
import type { AxiosResponse } from 'axios'
import { useAuthStore } from '@/stores/auth'
import { storageManager } from '@/utils/storage'

/**
 * API 响应格式
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

/**
 * 创建 axios 实例
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器：自动添加 Token 并检查过期
 */
api.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    
    // 检查 token 是否过期
    if (authStore.token && storageManager.isTokenExpired()) {
      console.warn('Token 已过期，清除登录状态')
      authStore.logout()
      
      // 如果不是在登录相关页面，跳转到首页（会触发登录弹窗）
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/'
      }
      
      return Promise.reject(new Error('Token 已过期，请重新登录'))
    }
    
    // 如果有 token，自动添加到请求头
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器：统一处理错误
 */
api.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    console.log('API 响应:', response.data)
    const { code, message, data } = response.data
    
    // 成功响应
    if (code === 20000 || code === 200) {
      // 修改 response.data 为解包后的数据，这样 axios 会自动传递给调用者
      response.data = data
      return response
    }
    
    // 业务错误
    console.error('API 业务错误:', { code, message })
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    // HTTP 错误处理
    if (error.response) {
      const status = error.response.status
      const authStore = useAuthStore()
      
      switch (status) {
        case 401:
          // Token 过期或无效
          console.warn('身份验证失败：Token 过期或无效')
          
          // 清除登录状态
          authStore.logout()
          
          // 如果当前不在登录页，则跳转到首页
          if (!window.location.pathname.includes('/login')) {
            window.location.href = '/'
          }
          break
          
        case 403:
          console.error('无权限访问')
          // 如果是访问管理页面被拒绝，跳转到404
          if (window.location.pathname.startsWith('/admin')) {
            window.location.href = '/404'
          }
          break
          
        case 404:
          console.error('请求的资源不存在')
          break
          
        case 500:
          console.error('服务器错误')
          break
          
        default:
          console.error(`请求错误 (${status}):`, error.response.data?.message || error.message)
      }
    } else if (error.request) {
      console.error('网络错误：无法连接到服务器')
    } else {
      console.error('请求配置错误:', error.message)
    }
    
    return Promise.reject(error)
  }
)

export default api
