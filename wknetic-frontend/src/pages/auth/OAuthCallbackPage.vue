<template>
  <div class="oauth-callback-page">
    <div class="loading-container" v-if="loading">
      <div class="loading-content">
        <div class="loading-spinner">
          <div class="spinner"></div>
        </div>
        <div class="loading-text">
          <h2>正在处理授权...</h2>
          <p>请稍候，我们正在完成Microsoft账户授权流程</p>
        </div>
      </div>
    </div>

    <div class="result-container" v-else>
      <div class="result-content" :class="{ 'success': success, 'error': !success }">
        <div class="result-icon">
          <i v-if="success" class="i-tabler-check text-4xl text-green-500"></i>
          <i v-else class="i-tabler-alert-circle text-4xl text-red-500"></i>
        </div>
        <div class="result-text">
          <h2>{{ message }}</h2>
          <p v-if="success">
            授权成功！正在返回主页面...
          </p>
          <p v-else>
            授权失败，请稍后重试或联系管理员。
          </p>
          <div v-if="errorDetails" class="error-details">
            <p class="text-xs">{{ errorDetails }}</p>
          </div>
        </div>
        <div class="result-actions">
          <button
            v-if="!success && countdown > 0"
            class="retry-button"
            @click="retryAuthorization"
          >
            重新授权 ({{ countdown }})
          </button>
          <button
            v-else-if="!success"
            class="retry-button"
            @click="retryAuthorization"
          >
            重新授权
          </button>
          <button
            class="close-button"
            @click="closeWindow"
          >
            关闭窗口
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi } from '@/api/user'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const success = ref(false)
const message = ref('处理中...')
const errorDetails = ref('')
const countdown = ref(5)

const retryAuthorization = () => {
  window.close()
}

const closeWindow = () => {
  window.close()
}

const processOAuthCallback = async () => {
  try {
    // 获取URL参数
    const code = route.query.code as string
    const state = route.query.state as string
    const error = route.query.error as string
    const errorDescription = route.query.error_description as string

    if (error) {
      // 处理授权错误
      loading.value = false
      success.value = false
      message.value = '授权失败'
      errorDetails.value = errorDescription || error
      
      // 通知父窗口授权失败
      if (window.opener) {
        window.opener.postMessage({
          type: 'microsoft_oauth_callback',
          error: error,
          errorDescription: errorDescription
        }, window.location.origin)
      }
      return
    }

    if (!code) {
      throw new Error('未收到授权码')
    }

    // 调用后端处理回调 - 使用POST请求，传递JSON数据
    const result = await userApi.microsoftOAuthCallback(code, state, undefined, undefined) 
    console.log('OAuth callback result:', result.data)
    loading.value = false
    success.value = true
    message.value = '授权成功！'

    // 通知父窗口授权成功
    if (window.opener) {
      window.opener.postMessage({
        type: 'microsoft_oauth_callback',
        success: true,
        code: code,
        state: state,
      }, window.location.origin)
    }

    // 自动关闭窗口倒计时
    const timer = setInterval(() => {
      if (countdown.value > 0) {
        countdown.value--
      } else {
        clearInterval(timer)
        window.close()
      }
    }, 3000)

  } catch (error: any) {
    console.error('OAuth callback error:', error)
    
    loading.value = false
    success.value = false
    message.value = '处理授权时出错'
    errorDetails.value = error.response?.data?.message || error.message || '未知错误'

    // 通知父窗口错误
    if (window.opener) {
      window.opener.postMessage({
        type: 'microsoft_oauth_callback',
        success: false,
        error: 'callback_error',
        errorDescription: errorDetails.value
      }, window.location.origin)
    }
  }
}

onMounted(() => {
  // 设置页面标题
  document.title = 'Microsoft账户授权 - WkNetic'
  
  // 处理OAuth回调
  processOAuthCallback()
})
</script>

<style scoped>
.oauth-callback-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.loading-container {
  width: 100%;
  max-width: 400px;
}

.loading-content {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.loading-spinner {
  margin-bottom: 24px;
}

.spinner {
  width: 60px;
  height: 60px;
  border: 4px solid rgba(102, 126, 234, 0.2);
  border-radius: 50%;
  border-top-color: #667eea;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.loading-text p {
  color: #666;
  font-size: 0.95rem;
}

.result-container {
  width: 100%;
  max-width: 400px;
}

.result-content {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.result-content.success {
  border-top: 4px solid #10b981;
}

.result-content.error {
  border-top: 4px solid #ef4444;
}

.result-icon {
  margin-bottom: 20px;
}

.result-text h2 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 12px;
}

.result-content.success .result-text h2 {
  color: #10b981;
}

.result-content.error .result-text h2 {
  color: #ef4444;
}

.result-text p {
  color: #666;
  font-size: 0.95rem;
  margin-bottom: 16px;
}

.error-details {
  background: #fef2f2;
  border: 1px solid #fee2e2;
  border-radius: 6px;
  padding: 12px;
  margin-top: 16px;
  text-align: left;
}

.error-details p {
  color: #dc2626;
  font-size: 0.85rem;
  margin: 0;
  word-break: break-all;
}

.result-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

.retry-button,
.close-button {
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: 500;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.retry-button {
  background: #3b82f6;
  color: white;
}

.retry-button:hover {
  background: #2563eb;
}

.close-button {
  background: #6b7280;
  color: white;
}

.close-button:hover {
  background: #4b5563;
}
</style>