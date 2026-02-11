<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/user'
import { formatUuid, getMinecraftAvatarUrl } from '@/utils/minecraft'
import type { MinecraftDeviceFlowStartResponse } from '@/api/user'
import WkButton from '@/components/common/WkButton.vue'
import WkCard from '@/components/common/WkCard.vue'
import WkAlert from '@/components/common/WkAlert.vue'

// 设备流状态接口
interface DeviceFlowStateDTO {
  deviceCode: string
  userCode: string
  verificationUri: string
  verificationUriComplete: string
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'EXPIRED' | 'ERROR'
  statusDescription?: string
  expiresAt: string
  timeRemaining: number
  interval: number
  terminal: boolean
  microsoftAccessToken?: string | null
  minecraftUuid?: string | null
  minecraftUsername?: string | null
  errorMessage?: string | null
}

const { t } = useI18n()
const authStore = useAuthStore()

// 状态管理
const message = ref('')
const messageType = ref<'success' | 'error' | 'warning' | 'info'>('info')

// MinecraftAuth配置
const minecraftAuthConfig = ref<MinecraftAuthConfigResponse | null>(null)
const minecraftAuthLoading = ref(false)

// 设备流状态
const deviceFlowActive = ref(false)
const deviceFlowData = ref<MinecraftDeviceFlowStartResponse | null>(null)
const deviceFlowPolling = ref(false)
const deviceFlowPollInterval = ref<NodeJS.Timeout | null>(null)
const deviceFlowExpiresAt = ref<number | null>(null)
const deviceFlowTimeRemaining = ref<number | null>(null)

// 当前绑定的Minecraft账号
const currentMinecraft = computed(() => {
  if (!authStore.user) return null
  return {
    uuid: authStore.user.minecraftUuid,
    username: authStore.user.minecraftUsername
  }
})

// 检查MinecraftAuth是否已配置
const isMinecraftAuthConfigured = computed(() => {
  return minecraftAuthConfig.value?.enabled && 
         minecraftAuthConfig.value?.client_id && 
         minecraftAuthConfig.value?.polling_interval && 
         minecraftAuthConfig.value?.polling_timeout
})

// 设备流剩余时间格式化
const deviceFlowTimeFormatted = computed(() => {
  if (!deviceFlowTimeRemaining.value) return '--:--'
  console.info("Device Flow Time Remaining:", deviceFlowTimeRemaining.value);
  const minutes = Math.floor(deviceFlowTimeRemaining.value / 60)
  const seconds = deviceFlowTimeRemaining.value % 60

  console.info("Formatted Time - Minutes:", minutes, "Seconds:", seconds);
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

// 解绑Minecraft账号
const unbindMinecraft = async () => {
  if (!confirm('确定要解绑Minecraft账号吗？')) return
  
  try {
    // 调用新的解绑API（不需要userId参数）
    await userApi.unbindMinecraftAccount()
    
    messageType.value = 'success'
    message.value = 'Minecraft账号已解绑'
    
    // 更新本地用户信息
    if (authStore.user) {
      authStore.user.minecraftUuid = undefined
      authStore.user.minecraftUsername = undefined
    }
  } catch (error: any) {
    messageType.value = 'error'
    message.value = error.response?.data?.message || '解绑失败，请重试'
  }
}

// 加载MinecraftAuth配置
const loadMinecraftAuthConfig = async () => {
  minecraftAuthLoading.value = true
  try {
    const result = await userApi.getMinecraftAuthConfig()
    minecraftAuthConfig.value = result.data
  } catch (error: any) {
    console.error('Failed to load MinecraftAuth config:', error)
    messageType.value = 'error'
    message.value = '无法加载MinecraftAuth配置'
  } finally {
    minecraftAuthLoading.value = false
  }
}

// 加载当前用户的Minecraft绑定信息
const loadCurrentUserMinecraftInfo = async () => {
  if (!authStore.user?.userId) return
  
  try {
    // 获取最新的用户信息，包括Minecraft绑定
    const result = await userApi.getUserProfile(authStore.user.userId)
    if (result.data && authStore.user) {
      // 更新本地用户信息中的Minecraft相关字段
      authStore.user.minecraftUuid = result.data.minecraftUuid
      authStore.user.minecraftUsername = result.data.minecraftUsername
    }
  } catch (error: any) {
    console.error('Failed to load user Minecraft info:', error)
  }
}

// 启动Minecraft设备流认证（使用MinecraftAuth库）
const startMinecraftDeviceFlow = async () => {
  if (!isMinecraftAuthConfigured.value) {
    messageType.value = 'error'
    message.value = 'MinecraftAuth功能未配置，请联系管理员'
    return
  }
  
  deviceFlowActive.value = true
  
  try {
    // 启动设备流
    const result = await userApi.startMinecraftDeviceFlow()
    deviceFlowData.value = result.data
    
    // 设置过期时间
    // 注意：expires_in 应该是设备码过期的时间戳（毫秒），或者是从现在开始的秒数
    // 根据微软设备流规范，expires_in 通常是从现在开始的秒数，比如 900 秒（15分钟）
    // 但后端可能返回的是过期时间戳，我们需要根据实际情况处理
    deviceFlowExpiresAt.value = deviceFlowData.value.expires_in;
    
    // 计算剩余时间：先尝试判断 expires_in 的单位
    const expiresIn = deviceFlowData.value.expires_in;
    let remainingSeconds;
    
    if (expiresIn > 1000000000000) {
      // 如果 expires_in 大于 1000000000000，很可能是毫秒时间戳
      // 计算剩余毫秒数并转换为秒
      const remainingMs = expiresIn - Date.now();
      remainingSeconds = Math.max(0, Math.floor(remainingMs / 1000));
      console.info("expires_in interpreted as timestamp (ms), remaining seconds:", remainingSeconds);
    } else if (expiresIn >= 0 && expiresIn <= 3600) {
      // 如果 expires_in 在 0-3600 之间，很可能是秒数
      remainingSeconds = expiresIn;
      console.info("expires_in interpreted as seconds, remaining seconds:", remainingSeconds);
    } else {
      // 无法确定单位，使用默认计算方式
      remainingSeconds = Math.max(0, Math.floor(expiresIn / 1000));
      console.warn("Cannot determine expires_in unit, using default calculation:", remainingSeconds);
    }
    
    deviceFlowTimeRemaining.value = remainingSeconds;
    console.info("最终倒计时时间（秒）: ", deviceFlowTimeRemaining.value);
    
    // 开始倒计时
    const countdownInterval = setInterval(() => {
      if (deviceFlowTimeRemaining.value && deviceFlowTimeRemaining.value > 0) {
        deviceFlowTimeRemaining.value--
      } else {
        clearInterval(countdownInterval)
        if (deviceFlowActive.value) {
          messageType.value = 'warning'
          message.value = '设备码已过期，请重新开始'
          stopDeviceFlow()
        }
      }
    }, 1000)
    
    // 开始轮询
    startPollingMinecraftDeviceFlow()
    
    messageType.value = 'info'
    message.value = '设备流已启动，请在另一台设备上完成授权'
    
  } catch (error: any) {
    deviceFlowActive.value = false
    messageType.value = 'error'
    message.value = error.response?.data?.message || error.message || '启动设备流失败'
  }
}

// 开始轮询Minecraft设备流状态
const startPollingMinecraftDeviceFlow = () => {
  if (!deviceFlowData.value) return
  
  deviceFlowPolling.value = true
  
  const poll = async () => {
    try {
      if (!deviceFlowData.value?.device_code) {
        stopDeviceFlow()
        return
      }
      
      // 轮询设备流状态
      const response = await userApi.pollDeviceFlowState(deviceFlowData.value.device_code)
      const result = response.data

      
      if (!result) {
        throw new Error(result?.message || '轮询失败')
      }
      
      const state = result as DeviceFlowStateDTO
      
      // 更新设备流剩余时间
      if (state.timeRemaining >= 0 && state.timeRemaining <= 3600) {
        // 合理范围：0-3600秒（0-1小时），直接使用
        deviceFlowTimeRemaining.value = state.timeRemaining
      } else if (state.timeRemaining > 1000 && state.timeRemaining <= 3600000) {
        // 如果值在1000-3600000之间，可能是毫秒，转换为秒
        deviceFlowTimeRemaining.value = Math.floor(state.timeRemaining / 1000)
      } else {
        // 异常值，不更新，继续使用前端倒计时
        console.warn('后端返回的timeRemaining值异常:', state.timeRemaining, '保持前端倒计时')
      }
      
      // 检查状态
      switch (state.status) {
        case 'PENDING':
          // 继续轮询，状态未变
          break
          
        case 'PROCESSING':
          // 处理中，继续轮询
          messageType.value = 'info'
          message.value = '授权正在进行中，请稍候...'
          break
          
        case 'COMPLETED':
          // 认证完成
          messageType.value = 'success'
          message.value = 'Minecraft账户认证成功！'
          
          // 如果认证成功且包含Minecraft信息，更新用户信息
          if (state.minecraftUuid && state.minecraftUsername && authStore.user) {
            authStore.user.minecraftUuid = state.minecraftUuid
            authStore.user.minecraftUsername = state.minecraftUsername
            
            // 自动绑定到当前用户账户
            try {
              await userApi.bindMinecraftAccount(state.minecraftUuid, state.minecraftUsername)
              message.value = 'Minecraft账户已成功绑定！'
              
              // 重新加载用户信息确保数据同步
              await loadCurrentUserMinecraftInfo()
            } catch (bindError: any) {
              console.error('自动绑定失败:', bindError)
              message.value = '认证成功但自动绑定失败，请手动绑定'
            }
          }
          
          stopDeviceFlow()
          break
          
        case 'EXPIRED':
          // 已过期
          messageType.value = 'warning'
          message.value = '设备码已过期，请重新开始认证'
          stopDeviceFlow()
          break
          
        case 'ERROR':
          // 发生错误
          messageType.value = 'error'
          message.value = state.statusDescription || state.errorMessage || '认证过程中发生错误'
          stopDeviceFlow()
          break
          
        default:
          // 未知状态
          messageType.value = 'warning'
          message.value = `未知状态: ${state.status}`
          stopDeviceFlow()
      }
      
      // 如果是终端状态，停止轮询
      if (state.terminal) {
        stopDeviceFlow()
      }
      
    } catch (error: any) {
      console.error('Device flow polling error:', error)
      
      // 如果是网络错误，继续轮询；如果是其他错误，停止轮询
      if (error.name === 'TypeError' && error.message.includes('fetch')) {
        // 网络错误，继续轮询
      } else {
        messageType.value = 'error'
        message.value = `轮询失败: ${error.message || '未知错误'}`
        stopDeviceFlow()
      }
    }
  }
  
  // 立即执行第一次轮询
  poll()
  
  // 设置轮询间隔（使用设备流返回的interval，或默认5秒）
  const interval = (deviceFlowData.value.interval || 5) * 1000
  deviceFlowPollInterval.value = setInterval(poll, interval)
}

// 停止设备流
const stopDeviceFlow = () => {
  deviceFlowActive.value = false
  deviceFlowPolling.value = false
  
  if (deviceFlowPollInterval.value) {
    clearInterval(deviceFlowPollInterval.value)
    deviceFlowPollInterval.value = null
  }
  
  deviceFlowData.value = null
  deviceFlowExpiresAt.value = null
  deviceFlowTimeRemaining.value = null
}


// 复制到剪贴板
const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    messageType.value = 'success'
    message.value = '已复制到剪贴板'
  } catch (error) {
    // 降级方案
    const textArea = document.createElement('textarea')
    textArea.value = text
    document.body.appendChild(textArea)
    textArea.select()
    try {
      document.execCommand('copy')
      messageType.value = 'success'
      message.value = '已复制到剪贴板'
    } catch (err) {
      messageType.value = 'error'
      message.value = '复制失败'
    }
    document.body.removeChild(textArea)
  }
}

onMounted(() => {
  // 加载授权配置
  loadMinecraftAuthConfig()
  // 获取玩家绑定的Minecraft账号信息
  loadCurrentUserMinecraftInfo()
})

onUnmounted(() => {
  // 停止设备流轮询
  if (deviceFlowPollInterval.value) {
    clearInterval(deviceFlowPollInterval.value)
  }
})
</script>

<template>
  <div class="minecraft-settings space-y-6">
    <!-- 提示消息 -->
    <WkAlert
      v-if="message"
      :type="messageType"
      :message="message"
      @close="message = ''"
    />
    
    <!-- 当前绑定状态 -->
    <WkCard>
      <div class="space-y-4">
        <h3 class="text-lg font-semibold text-text">当前绑定的Minecraft账号</h3>
        
        <div v-if="currentMinecraft?.uuid" class="space-y-4">
          <div class="flex items-center gap-4 p-4 bg-bg-raised rounded-lg">
            <!-- Minecraft头像 -->
            <div class="relative">
              <img
                :src="getMinecraftAvatarUrl(currentMinecraft.uuid, 64)"
                :alt="currentMinecraft.username || 'Minecraft玩家'"
                class="w-16 h-16 rounded-lg border-2 border-border"
                @error="(e: any) => e.target.src = '/default-avatar.png'"
              />
              <div class="absolute -bottom-1 -right-1 w-6 h-6 bg-green-500 rounded-full border-2 border-bg-raised flex items-center justify-center">
                <i class="i-tabler-check text-xs text-white" />
              </div>
            </div>
            
            <!-- 账号信息 -->
            <div class="flex-1">
              <div class="flex items-center gap-2">
                <h4 class="text-base font-medium text-text">
                  {{ currentMinecraft.username || '未知玩家' }}
                </h4>
                <span class="px-2 py-0.5 text-xs bg-brand/10 rounded">
                  正版认证
                </span>
              </div>
              <p class="text-sm text-text-secondary mt-1">
                UUID: {{ formatUuid(currentMinecraft.uuid) }}
              </p>
              <!-- <p class="text-xs text-text-muted mt-1">
                绑定时间: {{ authStore.user?.updateTime ? new Date(authStore.user.updateTime).toLocaleDateString() : '未知' }}
              </p> -->
            </div>
            
            <!-- 解绑按钮 -->
            <WkButton
              variant="ghost"
              size="sm"
              @click="unbindMinecraft"
            >
              <i class="i-tabler-unlink" />
              <span>解绑</span>
            </WkButton>
          </div>
          
          <div class="text-sm text-text-secondary">
            <p>✅ 此账号已成功绑定到您的WkNetic账户</p>
            <p class="mt-1">您可以在游戏中使用此账号登录WkNetic相关服务</p>
          </div>
        </div>
        
        <div v-else class="text-center py-8 text-text-secondary">
          <i class="i-tabler-brand-minecraft text-4xl mb-3" />
          <p class="text-base">尚未绑定Minecraft账号</p>
          <p class="text-sm mt-1">绑定后可在游戏中使用WkNetic功能</p>
        </div>
      </div>
    </WkCard>
    
    <!-- 绑定新账号 -->
    <WkCard v-if="!currentMinecraft?.uuid">
      <div class="space-y-6">
        <div>
          <h3 class="text-lg font-semibold text-text mb-2">绑定Minecraft账号</h3>
          <p class="text-sm text-text-secondary">
            目前仅支持通过Microsoft账户或游戏中绑定方式获取Minecraft账号信息
          </p>
        </div>
        
        <!-- 绑定方式说明 -->
        <div class="space-y-6">
          <div class="p-6 bg-bg-raised rounded-xl border border-border">
            <div class="flex items-start gap-4">
              <div class="flex-shrink-0 w-12 h-12 bg-blue-500 rounded-lg flex items-center justify-center">
                <i class="i-tabler-brand-windows text-2xl text-white" />
              </div>
              <div class="flex-1">
                <h4 class="text-lg font-semibold text-text mb-2">Microsoft账户绑定</h4>
                <p class="text-sm text-text-secondary mb-4">
                  通过Microsoft OAuth设备流授权，自动获取您的Minecraft账号信息。支持Bedrock版和Java版账号。
                </p>
                <div class="space-y-3">
                  <div class="flex items-center gap-2 text-sm text-text-secondary">
                    <i class="i-tabler-device-mobile text-base text-blue-500" />
                    <span>设备流模式：无需重定向，适合所有设备</span>
                  </div>
                  <div class="flex items-center gap-2 text-sm text-text-secondary">
                    <i class="i-tabler-shield-check text-base text-green-500" />
                    <span>安全可靠：使用Microsoft官方OAuth 2.0认证</span>
                  </div>
                  <div class="flex items-center gap-2 text-sm text-text-secondary">
                    <i class="i-tabler-brand-minecraft text-base text-green-700" />
                    <span>自动获取：自动获取您的Minecraft账号信息</span>
                  </div>
                  
                  <!-- 设备流状态显示 -->
                  <div v-if="deviceFlowActive" class="mt-4 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg border border-blue-200 dark:border-blue-800">
                    <div class="flex items-center justify-between mb-3">
                      <div class="flex items-center gap-2">
                        <i class="i-tabler-device-mobile text-lg text-blue-600 dark:text-blue-400" />
                        <span class="font-medium text-blue-700 dark:text-blue-300">设备流认证进行中</span>
                      </div>
                      <div class="flex items-center gap-2">
                        <span class="text-sm text-blue-600 dark:text-blue-400">剩余时间: {{ deviceFlowTimeFormatted }}</span>
                        <WkButton
                          variant="ghost"
                          size="sm"
                          @click="stopDeviceFlow"
                        >
                          <i class="i-tabler-x" />
                          <span>取消</span>
                        </WkButton>
                      </div>
                    </div>
                    
                    <div class="space-y-3">
                      <div class="bg-white dark:bg-gray-800 p-3 rounded border">
                        <div class="text-center mb-2">
                          <div class="text-2xl font-mono font-bold tracking-wider bg-gray-100 dark:bg-gray-700 py-2 px-4 rounded inline-block">
                            {{ deviceFlowData?.user_code }}
                          </div>
                        </div>
                        <p class="text-sm text-center text-gray-600 dark:text-gray-400 mb-3">
                          请在另一台设备上访问以下链接并输入此代码：
                        </p>
                        <div class="flex items-center justify-center gap-2">
                          <a 
                            :href="deviceFlowData?.verification_uri" 
                            target="_blank"
                            class="text-blue-600 dark:text-blue-400 hover:underline font-medium"
                          >
                            {{ deviceFlowData?.verification_uri }}
                          </a>
                          <WkButton
                            variant="ghost"
                            size="sm"
                            @click="copyToClipboard(deviceFlowData?.verification_uri || '')"
                          >
                            <i class="i-tabler-copy" />
                          </WkButton>
                        </div>
                      </div>
                      
                      <div class="flex items-center justify-center">
                        <div class="flex items-center gap-2">
                          <i v-if="deviceFlowPolling" class="i-tabler-info-circle text-base text-gray-600 dark:text-gray-400" />
                          <span class="text-sm text-gray-600 dark:text-gray-400">
                            {{ deviceFlowPolling ? '等待授权中...' : '准备轮询...' }}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 设备流操作按钮 -->
                  <div class="flex gap-3">
                    <WkButton
                      v-if="!deviceFlowActive"
                      @click="startMinecraftDeviceFlow"
                      :loading="minecraftAuthLoading"
                      :disabled="!isMinecraftAuthConfigured"
                    >
                      <i class="i-tabler-device-mobile" />
                      <span>启动Minecraft设备流认证</span>
                    </WkButton>
                    
                    <WkButton
                      v-if="deviceFlowActive"
                      variant="secondary"
                      @click="stopDeviceFlow"
                    >
                      <i class="i-tabler-x" />
                      <span>取消认证</span>
                    </WkButton>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 手动绑定（高级）- 已移除手动UUID绑定 -->
          <div class="p-6 bg-bg-raised rounded-xl border border-border">
            <div class="flex items-start gap-4">
              <div class="flex-shrink-0 w-12 h-12 bg-gray-500 rounded-lg flex items-center justify-center">
                <i class="i-tabler-key text-2xl text-white"></i>
              </div>
              <div class="flex-1">
                <h4 class="text-lg font-semibold text-text mb-2">服务器内绑定</h4>
                <p class="text-sm text-text-secondary mb-4">
                  在Minecraft服务器内使用<code>/wknetic bind</code>命令进行绑定。这种方式更简单且安全。
                </p>
                
                <div class="space-y-4">
                  <div class="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
                    <h5 class="font-medium text-text mb-2">绑定步骤：</h5>
                    <ol class="list-decimal pl-5 space-y-2 text-sm text-text-secondary">
                      <li>在Minecraft服务器中登录游戏</li>
                      <li>在游戏中输入命令：<code class="bg-gray-200 dark:bg-gray-700 px-1.5 py-0.5 rounded">/wknetic bind</code></li>
                      <li>按照游戏内提示完成绑定流程</li>
                      <li>绑定成功后，系统会自动同步您的Minecraft账号信息</li>
                    </ol>
                    <div class="mt-3 text-xs text-text-muted">
                      <p>注意：您需要在服务器中拥有WkNetic-Bridge插件才能使用此功能。</p>
                    </div>
                  </div>
                  
                  <div class="p-3 bg-green-50 dark:bg-green-900/20 rounded border border-green-200 dark:border-green-800">
                    <div class="flex items-center gap-2">
                      <i class="i-tabler-shield-check text-green-600 dark:text-green-400"></i>
                      <span class="text-sm text-green-700 dark:text-green-300">推荐使用此方式，更安全便捷！</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </WkCard>
  </div>
</template>

<style scoped>
.minecraft-settings {
  max-width: 800px;
  margin: 0 auto;
}
</style>
