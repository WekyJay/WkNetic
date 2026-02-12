import { ref, onMounted, onUnmounted, computed } from 'vue'
import { webSocketService, type GameChatMessage } from '@/api/websocket'
import { useAuthStore } from '@/stores/auth'
import { storageManager } from '@/utils/storage'

// 聊天频道类型
export type ChatChannel = 'global' | 'world' | 'party' | 'whisper' | 'staff'

// 订阅配置
interface SubscriptionConfig {
  serverName: string
  channel: ChatChannel
  world?: string
}

// 订阅状态
interface SubscriptionState {
  config: SubscriptionConfig
  subscriptionId: string
  messageHandler: (message: GameChatMessage) => void
}

// 共享状态 - 所有组件共享同一个实例
const connectionRefCount = ref(0) // 引用计数
const activeSubscriptions = ref<Map<string, SubscriptionState>>(new Map())
const isConnected = ref(false)
const connectionError = ref<string | null>(null)
const lastMessageTime = ref(0)

// 转换为ChatMessage格式的辅助函数
const convertToChatMessage = (wsMessage: any) => {
  // 处理不同类型的消息格式
  // 1. 后端发送的ChatMessageVO格式（有player对象）
  // 2. 前端期望的GameChatMessage格式（有sender对象）
  
  // 统一处理时间戳
  let timestamp: number;
  if (wsMessage.timestamp) {
    if (typeof wsMessage.timestamp === 'string') {
      timestamp = new Date(wsMessage.timestamp).getTime();
    } else if (typeof wsMessage.timestamp === 'number') {
      timestamp = wsMessage.timestamp;
    } else if (wsMessage.timestamp instanceof Date) {
      timestamp = wsMessage.timestamp.getTime();
    } else {
      timestamp = Date.now();
    }
  } else {
    timestamp = Date.now();
  }
  
  // 处理player信息
  let playerInfo = {
    uuid: '',
    username: 'Unknown',
    avatar: ''
  };
  
  if (wsMessage.player && typeof wsMessage.player === 'object') {
    // 这是后端ChatMessageVO格式
    playerInfo = {
      uuid: wsMessage.player.uuid || '',
      username: wsMessage.player.username || 'Unknown',
      avatar: wsMessage.player.avatar || ''
    };
  } else if (wsMessage.sender && typeof wsMessage.sender === 'object') {
    // 这是前端期望的GameChatMessage格式
    playerInfo = {
      uuid: wsMessage.sender.id?.toString() || wsMessage.sender.uuid || '',
      username: wsMessage.sender.username || 'Unknown',
      avatar: wsMessage.sender.avatar || ''
    };
  }
  
  return {
    id: wsMessage.id || `msg-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
    serverName: wsMessage.serverName || '',
    channel: wsMessage.channel || 'global',
    world: wsMessage.world || '',
    player: playerInfo,
    content: wsMessage.content || '',
    source: wsMessage.source || 'game',
    timestamp: timestamp
  };
}

export function useGameChatWebSocket() {
  // 连接WebSocket
  const connect = () => {
    if (webSocketService.isConnected()) {
      isConnected.value = true
      return
    }

    const authStore = useAuthStore()
    const authToken = authStore.token || storageManager.getToken()

    webSocketService.initialize(authToken)
    
    // 监听连接状态变化（需要扩展websocket服务以支持状态监听）
    // 暂时使用定时器检查
    const checkConnection = () => {
      isConnected.value = webSocketService.isConnected()
    }
    
    const interval = window.setInterval(checkConnection, 1000)
    
    // 5秒后停止检查
    setTimeout(() => {
      clearInterval(interval)
      isConnected.value = webSocketService.isConnected()
    }, 5000)
  }

  // 断开WebSocket连接
  const disconnect = () => {
    if (webSocketService.isConnected()) {
      webSocketService.disconnect()
    }
    isConnected.value = false
  }

  // 订阅聊天消息
  const subscribe = (
    serverName: string,
    channel: ChatChannel,
    messageHandler: (message: GameChatMessage) => void,
    world?: string
  ): string => {
    // 确保连接
    if (!webSocketService.isConnected()) {
      connect()
    }

    const config: SubscriptionConfig = { serverName, channel, world }
    const configKey = `${serverName}-${channel}-${world || 'all'}`
    
    // 如果已经订阅相同的配置，则直接返回现有订阅ID
    const existingSubscription = activeSubscriptions.value.get(configKey)
    if (existingSubscription) {
      console.debug(`Already subscribed to ${configKey}, returning existing subscription`)
      return existingSubscription.subscriptionId
    }

    // 创建新的订阅
    const subscriptionId = webSocketService.subscribeToGameChat(
      serverName,
      channel,
      (message) => {
        lastMessageTime.value = Date.now()
        messageHandler(message)
      },
      world
    )

    if (subscriptionId) {
      const subscriptionState: SubscriptionState = {
        config,
        subscriptionId,
        messageHandler
      }
      activeSubscriptions.value.set(configKey, subscriptionState)
      console.debug(`Subscribed to ${configKey} with ID: ${subscriptionId}`)
    } else {
      console.error(`Failed to subscribe to ${configKey}`)
      connectionError.value = '订阅聊天频道失败'
    }

    return subscriptionId || ''
  }

  // 取消订阅
  const unsubscribe = (subscriptionId: string) => {
    if (!subscriptionId) return

    // 查找对应的配置键
    let configKeyToRemove = ''
    activeSubscriptions.value.forEach((state, configKey) => {
      if (state.subscriptionId === subscriptionId) {
        configKeyToRemove = configKey
      }
    })

    if (configKeyToRemove) {
      webSocketService.unsubscribe(subscriptionId)
      activeSubscriptions.value.delete(configKeyToRemove)
      console.debug(`Unsubscribed from ${configKeyToRemove}`)
    }
  }

  // 取消订阅特定配置
  const unsubscribeByConfig = (serverName: string, channel: ChatChannel, world?: string) => {
    const configKey = `${serverName}-${channel}-${world || 'all'}`
    const subscriptionState = activeSubscriptions.value.get(configKey)
    
    if (subscriptionState) {
      unsubscribe(subscriptionState.subscriptionId)
    }
  }

  // 发送聊天消息
  const sendMessage = (
    serverName: string,
    channel: ChatChannel,
    content: string,
    world?: string,
    replyTo?: string
  ): boolean => {
    if (!webSocketService.isConnected()) {
      console.error('WebSocket not connected')
      return false
    }

    const success = webSocketService.sendChatMessage(
      serverName,
      channel,
      world || 'global',
      content,
      replyTo
    )

    if (!success) {
      connectionError.value = '发送消息失败：WebSocket未连接'
    }

    return success
  }

  // 加入聊天频道
  const joinChannel = (serverName: string, channel: ChatChannel, world?: string): boolean => {
    if (!webSocketService.isConnected()) {
      console.error('WebSocket not connected')
      return false
    }

    const success = webSocketService.joinChatChannel(serverName, channel, world)

    if (!success) {
      connectionError.value = '加入频道失败：WebSocket未连接'
    }

    return success
  }

  // 离开聊天频道
  const leaveChannel = (serverName: string, channel: ChatChannel, world?: string): boolean => {
    if (!webSocketService.isConnected()) {
      console.error('WebSocket not connected')
      return false
    }

    const success = webSocketService.leaveChatChannel(serverName, channel, world)

    if (!success) {
      connectionError.value = '离开频道失败：WebSocket未连接'
    }

    return success
  }

  // 获取连接状态
  const getConnectionStatus = computed(() => ({
    isConnected: isConnected.value,
    connectionError: connectionError.value,
    lastMessageTime: lastMessageTime.value,
    subscriptionCount: activeSubscriptions.value.size,
    webSocketConnected: webSocketService.isConnected()
  }))

  // 清理所有订阅
  const cleanupSubscriptions = () => {
    activeSubscriptions.value.forEach((state) => {
      webSocketService.unsubscribe(state.subscriptionId)
    })
    activeSubscriptions.value.clear()
    console.debug('Cleaned up all subscriptions')
  }

  // 使用引用计数管理连接生命周期
  onMounted(() => {
    connectionRefCount.value++
    console.debug('组件挂载，连接计数:', connectionRefCount.value)
    
    // 只在第一个组件挂载时建立连接
    if (connectionRefCount.value === 1) {
      connect()
    }
  })

  onUnmounted(() => {
    connectionRefCount.value--
    console.debug('组件卸载，连接计数:', connectionRefCount.value)
    
    // 清理当前组件的订阅
    // 注意：这里我们不清理所有订阅，因为可能还有其他组件在使用
    
    // 只在最后一个组件卸载时断开连接
    if (connectionRefCount.value === 0) {
      cleanupSubscriptions()
      disconnect()
    }
  })

  return {
    // 状态
    isConnected,
    connectionError,
    lastMessageTime,
    activeSubscriptions: computed(() => activeSubscriptions.value),
    
    // 方法
    connect,
    disconnect,
    subscribe,
    unsubscribe,
    unsubscribeByConfig,
    sendMessage,
    joinChannel,
    leaveChannel,
    getConnectionStatus,
    cleanupSubscriptions,
    
    // 辅助函数
    convertToChatMessage
  }
}