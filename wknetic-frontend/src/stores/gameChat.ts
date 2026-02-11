import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { gameChatApi, type ChatMessage, type ServerInfo, type OnlinePlayer } from '@/api/gameChat'
import { useAuthStore } from './auth'
import { ElMessage } from 'element-plus'

/**
 * 聊天频道类型
 */
export type ChatChannel = 'global' | 'world' | 'party' | 'whisper' | 'staff'

/**
 * 世界信息
 */
export interface WorldInfo {
  id: string
  name: string
}

/**
 * 游戏聊天Store
 */
export const useGameChatStore = defineStore('gameChat', () => {
  // State
  const currentServerName = ref<string>('')
  const currentChannel = ref<ChatChannel>('global')
  const currentWorld = ref<string>('all')
  const messages = ref<ChatMessage[]>([])
  const onlinePlayers = ref<OnlinePlayer[]>([])
  const isLoading = ref(false)
  const isSending = ref(false)
  const error = ref<string | null>(null)
  
  // Redis订阅连接
  let eventSource: EventSource | null = null
  
  // 轮询间隔
  let pollingInterval: number | null = null
  
  // 用户store
  const authStore = useAuthStore()

  // Getters
  const filteredMessages = computed(() => {
    return messages.value.filter(msg => {
      // 按服务器过滤
      if (currentServerName.value && msg.serverName !== currentServerName.value) {
        return false
      }
      // 按频道过滤
      if (msg.channel !== currentChannel.value) {
        return false
      }
      // 按世界过滤（如果选择了特定世界）
      if (currentWorld.value && currentWorld.value !== 'all' && msg.world !== currentWorld.value) {
        return false
      }
      return true
    })
  })

  const hasPermissionToView = computed(() => {
    return authStore.isAuthenticated
  })

  const hasPermissionToSpeak = computed(() => {
    return authStore.isAuthenticated && authStore.user?.minecraftUsername != null
  })

  // Actions
  /**
   * 初始化聊天 - 加载历史消息
   */
  const loadChatHistory = async (serverName: string, channel: ChatChannel = 'global', world: string = 'all') => {
    if (!authStore.isAuthenticated) {
      error.value = '请先登录'
      return false
    }

    isLoading.value = true
    error.value = null

    try {
      currentServerName.value = serverName
      currentChannel.value = channel
      currentWorld.value = world

      // 从Redis获取历史消息
      const response = await gameChatApi.getChatHistory({
        serverName,
        channel,
        world: world !== 'all' ? world : undefined,
        limit: 100
      })
      if (response.data) {
        messages.value = response.data
      } else {
        throw new Error(response.msg || '加载聊天历史失败')
      }

      // 订阅Redis消息
      subscribeToRedis()

      return true
    } catch (err) {
      error.value = `加载聊天历史失败: ${err instanceof Error ? err.message : String(err)}`
      ElMessage.error(error.value)
      return false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 订阅Redis消息（使用SSE或轮询）
   */
  const subscribeToRedis = () => {
    // 关闭现有连接
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }

    // 这里使用SSE订阅Redis发布的消息
    // 注意：需要后端提供SSE端点
    // 暂时使用轮询方式
    startPolling()
  }

  /**
   * 开始轮询新消息
   */
  const startPolling = () => {
    if (pollingInterval) {
      clearInterval(pollingInterval)
    }

    // 每3秒轮询一次
    pollingInterval = window.setInterval(async () => {
      if (!currentServerName.value) return

      try {
        const response = await gameChatApi.getChatHistory({
          serverName: currentServerName.value,
          channel: currentChannel.value,
          world: currentWorld.value !== 'all' ? currentWorld.value : undefined,
          limit: 10 // 只获取最新的10条
        })

        if (response.code === 200 && response.data) {
          // 合并新消息，避免重复
          response.data.forEach(newMsg => {
            if (!messages.value.some(m => m.id === newMsg.id)) {
              messages.value.push(newMsg)
            }
          })

          // 限制消息数量
          if (messages.value.length > 500) {
            messages.value = messages.value.slice(-500)
          }
        }
      } catch (err) {
        console.error('轮询消息失败:', err)
      }
    }, 3000)
  }

  /**
   * 停止轮询
   */
  const stopPolling = () => {
    if (pollingInterval) {
      clearInterval(pollingInterval)
      pollingInterval = null
    }
  }

  /**
   * 发送消息
   */
  const sendMessage = async (content: string) => {
    if (!authStore.isAuthenticated) {
      error.value = '请先登录'
      ElMessage.error(error.value)
      return false
    }

    if (!hasPermissionToSpeak.value) {
      error.value = '您需要绑定游戏账号才能发言'
      ElMessage.error(error.value)
      return false
    }

    if (!currentServerName.value) {
      error.value = '请先选择服务器'
      ElMessage.error(error.value)
      return false
    }

    if (!content.trim()) {
      error.value = '消息内容不能为空'
      return false
    }

    isSending.value = true
    error.value = null

    try {
      const response = await gameChatApi.sendMessage({
        serverName: currentServerName.value,
        channel: currentChannel.value,
        world: currentWorld.value !== 'all' ? currentWorld.value : undefined,
        content: content.trim()
      })

      if (response.code === 200) {
        ElMessage.success('消息发送成功')
        // 立即刷新消息列表
        await loadChatHistory(currentServerName.value, currentChannel.value, currentWorld.value)
        return true
      } else {
        throw new Error(response.msg || '发送消息失败')
      }
    } catch (err) {
      error.value = `发送消息失败: ${err instanceof Error ? err.message : String(err)}`
      ElMessage.error(error.value)
      return false
    } finally {
      isSending.value = false
    }
  }

  /**
   * 切换服务器
   */
  const switchServer = async (serverName: string) => {
    stopPolling()
    return loadChatHistory(serverName, currentChannel.value, currentWorld.value)
  }

  /**
   * 切换频道
   */
  const switchChannel = async (channel: ChatChannel) => {
    if (!currentServerName.value) {
      error.value = '请先选择服务器'
      return false
    }

    stopPolling()
    return loadChatHistory(currentServerName.value, channel, currentWorld.value)
  }

  /**
   * 切换世界
   */
  const switchWorld = async (world: string) => {
    if (!currentServerName.value) {
      error.value = '请先选择服务器'
      return false
    }

    stopPolling()
    return loadChatHistory(currentServerName.value, currentChannel.value, world)
  }

  /**
   * 清空消息
   */
  const clearMessages = () => {
    messages.value = []
  }

  /**
   * 断开连接
   */
  const disconnect = () => {
    stopPolling()
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }

    currentServerName.value = ''
    currentChannel.value = 'global'
    currentWorld.value = 'all'
    messages.value = []
  }

  return {
    // State
    currentServerName,
    currentChannel,
    currentWorld,
    messages,
    onlinePlayers,
    isLoading,
    isSending,
    error,

    // Getters
    filteredMessages,
    hasPermissionToView,
    hasPermissionToSpeak,

    // Actions
    loadChatHistory,
    sendMessage,
    switchServer,
    switchChannel,
    switchWorld,
    clearMessages,
    disconnect,
    startPolling,
    stopPolling,
    subscribeToRedis
  }
})
