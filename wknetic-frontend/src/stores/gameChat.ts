import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { gameChatApi, type ChatMessage, type ServerInfo, type OnlinePlayer } from '@/api/gameChat'
import { useAuthStore } from './auth'
import { ElMessage } from 'element-plus'
import { useGameChatWebSocket, type ChatChannel } from '@/composables/useGameChatWebSocket'

/**
 * 世界信息
 */
export interface WorldInfo {
  id: string
  name: string
}

/**
 * 游戏聊天Store（使用WebSocket）
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
  const webSocketConnected = ref(false)
  
  // WebSocket Composable
  const {
    isConnected: wsConnected,
    connectionError: wsError,
    subscribe: wsSubscribe,
    unsubscribeByConfig: wsUnsubscribe,
    sendMessage: wsSendMessage,
    joinChannel: wsJoinChannel,
    leaveChannel: wsLeaveChannel,
    connect: wsConnect,
    disconnect: wsDisconnect,
    convertToChatMessage
  } = useGameChatWebSocket()
  
  // 当前订阅ID
  let currentSubscriptionId = ref<string>('')
  
  // 用户store
  const authStore = useAuthStore()

  // 监听WebSocket连接状态
  watch(wsConnected, (newValue) => {
    webSocketConnected.value = newValue
    console.debug('WebSocket连接状态变化:', newValue)
  })

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

  const isWebSocketConnected = computed(() => webSocketConnected.value)

  // 消息处理函数
  const handleNewMessage = (message: any) => {
    // 将WebSocket消息转换为ChatMessage格式
    const chatMessage = convertToChatMessage(message)
    
    // 检查消息是否重复
    if (!messages.value.some(m => m.id === chatMessage.id)) {
      messages.value.push(chatMessage)
      
      // 限制消息数量
      if (messages.value.length > 500) {
        messages.value = messages.value.slice(-500)
      }
    }
  }

  // Actions
  /**
   * 初始化聊天 - 加载历史消息并建立WebSocket连接
   */
  const loadChatHistory = async (serverName: string, channel: ChatChannel = 'global', world: string = 'all') => {
    if (!authStore.isAuthenticated) {
      error.value = '请先登录'
      return false
    }

    isLoading.value = true
    error.value = null

    try {
      // 先取消之前的订阅
      if (currentSubscriptionId.value) {
        wsUnsubscribe(currentServerName.value, currentChannel.value, currentWorld.value)
        currentSubscriptionId.value = ''
      }

      currentServerName.value = serverName
      currentChannel.value = channel
      currentWorld.value = world

      // 从API获取历史消息
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

      // 建立WebSocket连接并订阅
      if (serverName) {
        // 连接到WebSocket
        wsConnect()
        
        // 订阅新消息
        const subscriptionId = wsSubscribe(
          serverName,
          channel,
          handleNewMessage,
          world !== 'all' ? world : undefined
        )
        
        currentSubscriptionId.value = subscriptionId
        
        // 加入频道
        wsJoinChannel(serverName, channel, world !== 'all' ? world : undefined)
      }

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
   * 发送消息（通过WebSocket）
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
      // 使用WebSocket发送消息
      const success = wsSendMessage(
        currentServerName.value,
        currentChannel.value,
        content.trim(),
        currentWorld.value !== 'all' ? currentWorld.value : undefined
      )

      if (success) {
        ElMessage.success('消息发送成功')
        return true
      } else {
        throw new Error('WebSocket发送消息失败')
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
    // 先离开当前频道
    if (currentServerName.value && currentChannel.value) {
      wsLeaveChannel(currentServerName.value, currentChannel.value, currentWorld.value !== 'all' ? currentWorld.value : undefined)
    }
    
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

    // 离开当前频道
    wsLeaveChannel(currentServerName.value, currentChannel.value, currentWorld.value !== 'all' ? currentWorld.value : undefined)
    
    // 加入新频道
    const success = wsJoinChannel(currentServerName.value, channel, currentWorld.value !== 'all' ? currentWorld.value : undefined)
    
    if (success) {
      // 重新加载历史消息
      return loadChatHistory(currentServerName.value, channel, currentWorld.value)
    }
    
    return false
  }

  /**
   * 切换世界
   */
  const switchWorld = async (world: string) => {
    if (!currentServerName.value) {
      error.value = '请先选择服务器'
      return false
    }

    // 重新加载历史消息
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
    // 离开当前频道
    if (currentServerName.value && currentChannel.value) {
      wsLeaveChannel(currentServerName.value, currentChannel.value, currentWorld.value !== 'all' ? currentWorld.value : undefined)
    }
    
    // 取消订阅
    if (currentSubscriptionId.value) {
      wsUnsubscribe(currentServerName.value, currentChannel.value, currentWorld.value !== 'all' ? currentWorld.value : undefined)
      currentSubscriptionId.value = ''
    }
    
    // 断开WebSocket连接
    wsDisconnect()

    // 重置状态
    currentServerName.value = ''
    currentChannel.value = 'global'
    currentWorld.value = 'all'
    messages.value = []
    webSocketConnected.value = false
  }

  /**
   * 手动重连WebSocket
   */
  const reconnectWebSocket = () => {
    wsConnect()
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
    webSocketConnected,

    // Getters
    filteredMessages,
    hasPermissionToView,
    hasPermissionToSpeak,
    isWebSocketConnected,

    // Actions
    loadChatHistory,
    sendMessage,
    switchServer,
    switchChannel,
    switchWorld,
    clearMessages,
    disconnect,
    reconnectWebSocket
  }
})
