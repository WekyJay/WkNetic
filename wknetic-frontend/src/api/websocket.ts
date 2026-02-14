import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

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
    serverName: string
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
  private pendingSubscriptions: Map<string, {
    serverName: string
    channel: string
    world?: string
    callback: (message: GameChatMessage) => void
  }> = new Map()

  constructor() {
    // 根据useServerMonitor.ts的配置，后端使用SockJS端点/ws-connect
    const baseUrl = window.WkConfig?.apiBaseUrl || import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    const wsUrl = baseUrl + '/ws-connect'

    this.config = {
      url: wsUrl,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
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
      webSocketFactory: () => {
        return new SockJS(this.config.url) as any
      },
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
    serverName: string,
    channel: string,
    callback: (message: GameChatMessage) => void,
    world?: string
  ): string {
    if (!this.client) {
      console.error('WebSocket client not initialized')
      return ''
    }

    const subscriptionId = `chat-${serverName}-${channel}-${world || 'all'}`

    // 如果已经订阅，直接返回
    if (this.subscriptions.has(subscriptionId)) {
      return subscriptionId
    }

    const doSubscribe = () => {
      const topic = this.buildChatTopic(serverName, channel, world)
      const subscription = this.client!.subscribe(topic, (message) => {
        try {
          const rawMessage = JSON.parse(message.body)
          
          let chatMessage: GameChatMessage
          
          if (rawMessage.player && typeof rawMessage.player === 'object') {
            chatMessage = {
              id: rawMessage.id || `msg-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
              serverName: rawMessage.serverName || '',
              channel: rawMessage.channel || 'global',
              world: rawMessage.world,
              sender: {
                id: 0,
                username: rawMessage.player.username || 'Unknown',
                nickname: rawMessage.player.username || 'Unknown',
                avatar: rawMessage.player.avatar || '',
                isPlayer: true
              },
              content: rawMessage.content || '',
              timestamp: typeof rawMessage.timestamp === 'string' 
                ? new Date(rawMessage.timestamp).getTime() 
                : rawMessage.timestamp || Date.now(),
              replyTo: undefined
            }
          } else if (rawMessage.sender && typeof rawMessage.sender === 'object') {
            chatMessage = rawMessage as GameChatMessage
          } else {
            console.warn('Unknown chat message format:', rawMessage)
            chatMessage = {
              id: rawMessage.id || `msg-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
              serverName: rawMessage.serverName || '',
              channel: 'global',
              sender: {
                id: 0,
                username: 'System',
                nickname: 'System',
                avatar: '',
                isPlayer: false
              },
              content: rawMessage.content || JSON.stringify(rawMessage),
              timestamp: Date.now()
            }
          }
          
          callback(chatMessage)
        } catch (error) {
          console.error('Failed to parse chat message:', error, message.body)
        }
      })
      this.subscriptions.set(subscriptionId, subscription)
      this.pendingSubscriptions.delete(subscriptionId)
      return subscriptionId
    }

    if (this.client.connected) {
      return doSubscribe()
    }

    // 未连接时，先记录，等 onConnected 时补订阅
    this.pendingSubscriptions.set(subscriptionId, { serverName, channel, world, callback })
    console.debug(`Queued pending chat subscription ${subscriptionId} until WebSocket connects`)
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
    if (this.pendingSubscriptions.has(subscriptionId)) {
      this.pendingSubscriptions.delete(subscriptionId)
    }
  }

  /**
   * 发送聊天消息
   */
  public sendChatMessage(
    serverName: string,
    channel: string,
    world: string,
    content: string,
    replyTo?: string,
    sessionId?: string
  ): boolean {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected')
      return false
    }

    const message = {
      serverName,
      channel,
      world,
      content,
      replyTo,
      sessionId,
      timestamp: Date.now()
    }

    this.client.publish({
      destination: `/app/chat/${serverName}/send`,
      body: JSON.stringify(message)
    })

    console.info('Sent chat message via WebSocket:', message)

    return true
  }

  /**
   * 发送聊天消息（使用sessionId）
   */
  public sendChatMessageWithSessionId(
    sessionId: string,
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
      sessionId,
      channel,
      world,
      content,
      replyTo,
      timestamp: Date.now()
    }

    this.client.publish({
      destination: `/app/chat/${sessionId}/send`,
      body: JSON.stringify(message)
    })

    return true
  }

  /**
   * 加入聊天频道
   */
  public joinChatChannel(
    serverName: string,
    channel: string,
    world?: string
  ): boolean {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected')
      return false
    }

    const joinRequest = {
      serverName,
      channel,
      world,
      timestamp: Date.now()
    }

    this.client.publish({
      destination: `/app/chat/${serverName}/join`,
      body: JSON.stringify(joinRequest)
    })

    return true
  }

  /**
   * 离开聊天频道
   */
  public leaveChatChannel(
    serverName: string,
    channel: string,
    world?: string
  ): boolean {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected')
      return false
    }

    const leaveRequest = {
      serverName,
      channel,
      world,
      timestamp: Date.now()
    }

    this.client.publish({
      destination: `/app/chat/${serverName}/leave`,
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
  private buildChatTopic(serverName: string, channel: string, world?: string): string {
    let topic = `/topic/chat/${serverName}/${channel}`
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
    // 补上连接前排队的订阅
    if (this.pendingSubscriptions.size > 0) {
      this.pendingSubscriptions.forEach((pending, subscriptionId) => {
        this.subscribeToGameChat(
          pending.serverName,
          pending.channel,
          pending.callback,
          pending.world
        )
        console.debug(`Flushed pending subscription ${subscriptionId}`)
      })
    }
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
