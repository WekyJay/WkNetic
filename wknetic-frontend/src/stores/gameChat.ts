import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { GameChatMessage, ServerStatus } from '@/api/websocket'
import { webSocketService } from '@/api/websocket'
import { useUserStore } from './user'

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
  displayName: string
  type: 'normal' | 'resource' | 'nether' | 'end' | 'apocalypse' | 'custom'
}

/**
 * 服务器信息
 */
export interface ServerInfo {
  id: string
  name: string
  status: 'online' | 'offline' | 'maintenance'
  players: {
    online: number
    max: number
  }
  worlds: WorldInfo[]
}

/**
 * 聊天状态
 */
export interface ChatState {
  // 当前选择的服务器
  currentServer: ServerInfo | null
  // 当前选择的频道
  currentChannel: ChatChannel
  // 当前选择的世界（可选）
  currentWorld: WorldInfo | null
  // 消息列表
  messages: GameChatMessage[]
  // 是否已连接WebSocket
  isConnected: boolean
  // 是否正在加载
  isLoading: boolean
  // 错误信息
  error: string | null
  // 订阅ID
  subscriptionId: string | null
  // 是否可以发言（需要绑定游戏账号）
  canSpeak: boolean
  // 是否正在发送消息
  isSending: boolean
}

/**
 * 游戏聊天Store
 */
export const useGameChatStore = defineStore('gameChat', () => {
  // State
  const currentServer = ref<ServerInfo | null>(null)
  const currentChannel = ref<ChatChannel>('global')
  const currentWorld = ref<WorldInfo | null>(null)
  const messages = ref<GameChatMessage[]>([])
  const isConnected = ref(false)
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const subscriptionId = ref<string | null>(null)
  const canSpeak = ref(false)
  const isSending = ref(false)

  // 用户store
  const userStore = useUserStore()

  // Getters
  const filteredMessages = computed(() => {
    return messages.value.filter(msg => {
      // 按服务器过滤
      if (currentServer.value && msg.serverId !== currentServer.value.id) {
        return false
      }
      // 按频道过滤
      if (msg.channel !== currentChannel.value) {
        return false
      }
      // 按世界过滤
      if (currentWorld.value && msg.world !== currentWorld.value.id) {
        return false
      }
      return true
    })
  })

  const unreadCount = computed(() => {
    // 简单实现：返回最新消息数量，实际可根据时间戳判断
    return messages.value.length > 100 ? messages.value.length - 100 : 0
  })

  const hasPermissionToSpeak = computed(() => {
    return userStore.isLoggedIn && canSpeak.value
  })

  const hasPermissionToView = computed(() => {
    return userStore.isLoggedIn
  })

  // Actions
  /**
   * 初始化聊天
   */
  const initializeChat = async (serverId: string, channel: ChatChannel = 'global', worldId?: string) => {
    if (!userStore.isLoggedIn) {
      error.value = '请先登录'
      return false
    }

    isLoading.value = true
    error.value = null

    try {
      // 连接WebSocket
      webSocketService.initialize(userStore.token)

      // 设置当前服务器、频道、世界
      // 这里需要从API获取服务器信息，暂时用模拟数据
      currentServer.value = {
        id: serverId,
        name: `服务器 ${serverId}`,
        status: 'online',
        players: {
          online: 0,
          max: 100
        },
        worlds: [
          { id: 'world', name: 'world', displayName: '主世界', type: 'normal' },
          { id: 'nether', name: 'nether', displayName: '地狱', type: 'nether' },
          { id: 'end', name: 'end', displayName: '末地', type: 'end' },
          { id: 'apocalypse', name: 'apocalypse', displayName: '末日世界', type: 'apocalypse' },
          { id: 'resource', name: 'resource', displayName: '资源世界', type: 'resource' }
        ]
      }

      currentChannel.value = channel

      if (worldId) {
        const world = currentServer.value.worlds.find(w => w.id === worldId)
        currentWorld.value = world || null
      } else {
        currentWorld.value = null
      }

      // 清空消息
      messages.value = []

      // 检查用户是否可以发言（需要绑定游戏账号）
      // 这里需要调用API检查，暂时假设为true
      canSpeak.value = true

      // 订阅聊天消息
      subscribeToChat(serverId, channel, worldId)

      return true
    } catch (err) {
      error.value = `初始化聊天失败: ${err instanceof Error ? err.message : String(err)}`
      return false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 订阅聊天消息
   */
  const subscribeToChat = (serverId: string, channel: ChatChannel, worldId?: string) => {
    // 取消现有订阅
    if (subscriptionId.value) {
      webSocketService.unsubscribe(subscriptionId.value)
      subscriptionId.value = null
    }

    // 订阅新频道
    const subId = webSocketService.subscribeToGameChat(
      serverId,
      channel,
      (message) => {
        // 添加消息到列表
        addMessage(message)
      },
      worldId
    )

    if (subId) {
      subscriptionId.value = subId
      isConnected.value = true
    }
  }

  /**
   * 添加消息
   */
  const addMessage = (message: GameChatMessage) => {
    // 避免重复消息
    if (messages.value.some(m => m.id === message.id)) {
      return
    }

    messages.value.push(message)

    // 限制消息数量，最多保留500条
    if (messages.value.length > 500) {
      messages.value = messages.value.slice(-500)
    }
  }

  /**
   * 发送消息
   */
  const sendMessage = async (content: string, replyTo?: string) => {
    if (!userStore.isLoggedIn) {
      error.value = '请先登录'
      return false
    }

    if (!canSpeak.value) {
      error.value = '您需要绑定游戏账号才能发言'
      return false
    }

    if (!currentServer.value) {
      error.value = '请先选择服务器'
      return false
    }

    if (!content.trim()) {
      error.value = '消息内容不能为空'
      return false
    }

    isSending.value = true
    error.value = null

    try {
      const success = webSocketService.sendChatMessage(
        currentServer.value.id,
        currentChannel.value,
        currentWorld.value?.id || 'all',
        content,
        replyTo
      )

      if (!success) {
        throw new Error('发送消息失败')
      }

      return true
    } catch (err) {
      error.value = `发送消息失败: ${err instanceof Error ? err.message : String(err)}`
      return false
    } finally {
      isSending.value = false
    }
  }

  /**
   * 切换服务器
   */
  const switchServer = async (serverId: string) => {
    if (subscriptionId.value) {
      webSocketService.unsubscribe(subscriptionId.value)
      subscriptionId.value = null
    }

    return initializeChat(serverId, currentChannel.value, currentWorld.value?.id)
  }

  /**
   * 切换频道
   */
  const switchChannel = (channel: ChatChannel) => {
    if (!currentServer.value) {
      error.value = '请先选择服务器'
      return false
    }

    currentChannel.value = channel

    subscribeToChat(currentServer.value.id, channel, currentWorld.value?.id)

    return true
  }

  /**
   * 切换世界
   */
  const switchWorld = (worldId: string | null) => {
    if (!currentServer.value) {
      error.value = '请先选择服务器'
      return false
    }

    if (worldId) {
      const world = currentServer.value.worlds.find(w => w.id === worldId)
      currentWorld.value = world || null
    } else {
      currentWorld.value = null
    }

    subscribeToChat(currentServer.value.id, currentChannel.value, currentWorld.value?.id)

    return true
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
    if (subscriptionId.value) {
      webSocketService.unsubscribe(subscriptionId.value)
      subscriptionId.value = null
    }

    webSocketService.disconnect()

    isConnected.value = false
    currentServer.value = null
    currentChannel.value = 'global'
    currentWorld.value = null
    messages.value = []
    canSpeak.value = false
  }

  /**
   * 重新连接
   */
  const reconnect = () => {
    if (!currentServer.value) {
      return false
    }

    disconnect()
    return initializeChat(currentServer.value.id, currentChannel.value, currentWorld.value?.id)
  }

  /**
   * 检查连接状态
   */
  const checkConnection = () => {
    isConnected.value = webSocketService.isConnected()
    return isConnected.value
  }

  // 初始化时检查用户登录状态
  const init = () => {
    if (!userStore.isLoggedIn) {
      disconnect()
    }
  }

  return {
    // State
    currentServer,
    currentChannel,
    currentWorld,
    messages,
    isConnected,
    isLoading,
    error,
    subscriptionId,
    canSpeak,
    isSending,

    // Getters
    filteredMessages,
    unreadCount,
    hasPermissionToSpeak,
    hasPermissionToView,

    // Actions
    initializeChat,
    subscribeToChat,
    addMessage,
    sendMessage,
    switchServer,
    switchChannel,
    switchWorld,
    clearMessages,
    disconnect,
    reconnect,
    checkConnection,
    init
  }
})
