import { ref } from 'vue'
import { authApi, type CaptchaResult } from '@/api/auth'

/**
 * 验证码类型
 */
export type CaptchaType = 'simple' | 'cloudflare' | 'none'

/**
 * 验证码 Hook
 */
export function useCaptcha(type: CaptchaType = 'simple') {
  const captchaImage = ref<string>('')
  const captchaSessionId = ref<string>('')
  const captchaCode = ref<string>('')
  const isLoading = ref(false)

  /**
   * 获取简易验证码
   */
  async function fetchSimpleCaptcha() {
    isLoading.value = true
    try {
      console.log('正在获取验证码...')
      const response = await authApi.getCaptcha()
      const result = response.data
      console.log('验证码获取成功:', result)
      
      // 直接使用返回的数据，axios 拦截器已经解包
      captchaImage.value = result.image
      captchaSessionId.value = result.sessionId
      captchaCode.value = '' // 清空输入
    } catch (error: any) {
      console.error('获取验证码失败:', error)
      console.error('错误详情:', {
        message: error.message,
        response: error.response,
        stack: error.stack
      })
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 生成验证码令牌
   * 
   * @returns captchaToken 用于提交给后端验证
   */
  function getCaptchaToken(): string | undefined {
    switch (type) {
      case 'simple':
        // 简易验证码：sessionId:code
        return captchaSessionId.value && captchaCode.value
          ? `${captchaSessionId.value}:${captchaCode.value}`
          : undefined
        
      case 'cloudflare':
        // Cloudflare Turnstile：由组件提供的 token
        // 需要在组件中使用 Cloudflare Turnstile 组件获取
        return undefined
        
      case 'none':
        // 无验证码
        return undefined
        
      default:
        return undefined
    }
  }

  /**
   * 重置验证码
   */
  function resetCaptcha() {
    if (type === 'simple') {
      fetchSimpleCaptcha()
    }
  }

  // 初始化时自动获取验证码
  if (type === 'simple') {
    fetchSimpleCaptcha()
  }

  return {
    captchaImage,
    captchaSessionId,
    captchaCode,
    isLoading,
    fetchSimpleCaptcha,
    getCaptchaToken,
    resetCaptcha
  }
}
