import { Client } from '@stomp/stompjs'
import type { User } from '@/stores/user'

/**
 * WebSocket消息类型
 */
export interface WebSocketMessage {
  id: string
  type: string
  content: any
  timestamp: number
  sender?: {
    id: number
    username: string
    nickname: string
    avatar: string
  }
}

/**
 * 游戏聊天消息
 */
export interface GameChatMessage {
  id: string
  serverId: string
  channel: 'global' | 'world' | 'party' | 'whisper' | 'staff'
  world?: string
  sender: {
    id: number
    username: string
    nickname: string
    avatar: string
    isPlayer: boolean
  }
  content: string
  timestamp: number
  replyTo?: string
}

/**
 * 服务器状态
 */
export interface ServerStatus {
  id: string
  name: string
  status: 'online' | 'offline' | 'maintenance'
  players: {
    online: number
    max: number
  }
  worlds: string[]
}

/**
 * WebSocket配置
 */
interface WebSocketConfig {
  url: string
  reconnectDelay: number
  heartbeatIncoming: number
  heartbeatOutgoing: number
}

/**
 * WebSocket服务类
 */
class WebSocketService {
  private client: Client | null = null
  private config: WebSocketConfig
  private isConnecting: boolean = false
  private reconnectAttempts: number = 0
  private maxReconnectAttempts: number = 10
  private subscriptions: Map<string, any> = new Map()

  constructor() {
    this.config = {
      url: import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws',
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000
    }
  }

  /**
   * 初始化WebSocket连接
   */
  public initialize(token?: string): void {
    if (this.client || this.isConnecting) {
      return
    }

    this.isConnecting = true
    this.reconnectAttempts = 0

    const connectHeaders: any = {}
    if (token) {
      connectHeaders['Authorization'] = `Bearer ${token}`
    }

    this.client = new Client({
      brokerURL: this.config.url,
      connectHeaders,
      reconnectDelay: this.config.reconnectDelay,
      heartbeatIncoming: this.config.heartbeatIncoming,
      heartbeatOutgoing: this.config.heartbeatOutgoing,
      debug: (str) => {
        console.debug('[STOMP]', str)
      },
      onConnect: () => {
        console.log('WebSocket connected successfully')
        this.isConnecting = false
        this.reconnectAttempts = 0
        this.onConnected()
      },
      onStompError: (frame) => {
        console.error('WebSocket STOMP error:', frame)
        this.isConnecting = false
        this.onError(frame)
      },
      onWebSocketError: (event) => {
        console.error('WebSocket connection error:', event)
        this.isConnecting = false
        this.onError(event)
      },
      onDisconnect: () => {
        console.log('WebSocket disconnected')
        this.onDisconnected()
      }
    })

    this.client.activate()
  }

  /**
   * 断开WebSocket连接
   */
  public disconnect(): void {
    if (this.client) {
      this.subscriptions.forEach((subscription, id) => {
        subscription.unsubscribe()
      })
      this.subscriptions.clear()

      this.client.deactivate()
      this.client = null
    }
    this.isConnecting = false
  }

  /**
   * 订阅游戏聊天消息
   */
  public subscribeToGameChat(
    serverId: string,
    channel: string,
    callback: (message: GameChatMessage) => void,
    world?: string
  ): string {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected')
      return ''
    }

    const topic = this.buildChatTopic(serverId, channel, world)
    const subscription = this.client.subscribe(topic, (message) => {
      try {
        const parsedMessage = JSON.parse(message.body) as GameChatMessage
        callback(parsedMessage)
      } catch (error) {
        console.error('Failed to parse chat message:', error)
      }
    })

    const subscriptionId = `chat-${serverId}-${channel}-${world || 'all'}`
    this.subscriptions.set(subscriptionId, subscription)
    return subscriptionId
  }

  /**
   * 订阅服务器状态
   */
  public subscribeToServerStatus(
    serverId: string,
    callback: (status: ServerStatus) => void
  ): string {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected')
      return ''
    }

    const topic = `/topic/server/${serverId}/status`
    const subscription = this.client.subscribe(topic, (message) => {
      try {
        const status = JSON.parse(message.body) as ServerStatus
        callback(status)
      } catch (error) {
        console.error('Failed to parse server status:', error)
      }
    })

    const subscriptionId = `status-${serverId}`
    this.subscriptions.set(subscriptionId, subscription)
    return subscriptionId
  }

  /**
   * 取消订阅
   */
  public unsubscribe(subscriptionId: string): void {
    const subscription = this.subscriptions.get(subscriptionId)
    if (subscription) {
      subscription.unsubscribe()
      this.subscriptions.delete(subscriptionId)
    }
  }

  /**
   * 发送聊天消息
   */
  public sendChatMessage(
    serverId: string,
    channel: string,
    world: string,
    content: string,
    replyTo?: string
  ): boolean {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected')
      return false
    }

    const message = {
      serverId,
      channel,
      world,
      content,
      replyTo,
      timestamp: Date.now()
    }

    this.client.publish({
      destination: `/app/chat/${serverId}/send`,
      body: JSON.stringify(message)
    })

    return true
  }

  /**
   * 加入聊天频道
   */
  public joinChatChannel(
    serverId: string,
    channel: string,
    world?: string
  ): boolean {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected')
      return false
    }

    const joinRequest = {
      serverId,
      channel,
      world,
      timestamp: Date.now()
    }

    this.client.publish({
      destination: `/app/chat/${serverId}/join`,
      body: JSON.stringify(joinRequest)
    })

    return true
  }

  /**
   * 离开聊天频道
   */
  public leaveChatChannel(
    serverId: string,
    channel: string,
    world?: string
  ): boolean {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected')
      return false
    }

    const leaveRequest = {
      serverId,
      channel,
      world,
      timestamp: Date.now()
    }

    this.client.publish({
      destination: `/app/chat/${serverId}/leave`,
      body: JSON.stringify(leaveRequest)
    })

    return true
  }

  /**
   * 获取连接状态
   */
  public isConnected(): boolean {
    return this.client?.connected || false
  }

  /**
   * 获取订阅数量
   */
  public getSubscriptionCount(): number {
    return this.subscriptions.size
  }

  /**
   * 构建聊天主题
   */
  private buildChatTopic(serverId: string, channel: string, world?: string): string {
    let topic = `/topic/chat/${serverId}/${channel}`
    if (world && world !== 'all') {
      topic += `/${world}`
    }
    return topic
  }

  /**
   * 连接成功回调
   */
  private onConnected(): void {
    console.log('WebSocket connected, subscribing to topics...')
    // 可以在这里添加全局订阅
  }

  /**
   * 断开连接回调
   */
  private onDisconnected(): void {
    console.log('WebSocket disconnected')
    // 可以在这里处理断开连接后的逻辑
  }

  /**
   * 错误处理
   */
  private onError(error: any): void {
    console.error('WebSocket error:', error)
    
    // 尝试重新连接
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
      
      setTimeout(() => {
        if (this.client && !this.client.connected) {
          this.disconnect()
          this.initialize()
        }
      }, this.config.reconnectDelay)
    } else {
      console.error('Max reconnection attempts reached')
    }
  }
}

// 导出单例实例
export const webSocketService = new WebSocketService()

// 默认导出
export default webSocketService
