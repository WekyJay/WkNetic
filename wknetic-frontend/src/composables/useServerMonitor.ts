import { ref, onMounted, onUnmounted } from 'vue'
import { Client, type StompSubscription } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import type { ServerInfo } from '@/api/serverMonitor'
import { useAuthStore } from '@/stores/auth'
import { storageManager } from '@/utils/storage'

interface ServerStatus extends ServerInfo {
  isOnline: boolean
  lastUpdate: number
}

const OFFLINE_THRESHOLD = 60000 // 60秒无数据则判定为离线

// 共享状态 - 在模块级别创建，所有组件共享同一个实例
const servers = ref<Map<string, ServerStatus>>(new Map())
const isConnected = ref(false)
const stompClient = ref<Client | null>(null)
const subscription = ref<StompSubscription | null>(null)
const connectionRefCount = ref(0) // 引用计数（使用 ref 以便响应式）
const lastMessageTime = ref(0) // 最后一次收到消息的时间
let checkInterval: number | undefined

export function useServerMonitor() {
  // 连接WebSocket
  const connect = () => {
    if (stompClient.value?.active) {
      return
    }

    // TODO: 根据实际环境配置WebSocket URL，确定DOCKER容器部署时的地址
    const baseUrl = window.WkConfig?.apiBaseUrl || import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    const wsUrl = baseUrl + '/ws-connect'

    const authStore = useAuthStore()
    const authToken = authStore.token || storageManager.getToken()

    stompClient.value = new Client({
      webSocketFactory: () => {
        return new SockJS(wsUrl) as any
      },
      connectHeaders: authToken ? { Authorization: `Bearer ${authToken}` } : {},
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        isConnected.value = true
        subscription.value = stompClient.value!.subscribe(
          '/topic/server/monitor',
          (message) => {
            try {
              const serverInfo: ServerInfo = JSON.parse(message.body)
              lastMessageTime.value = Date.now() // 更新最后消息时间
              updateServerStatus(serverInfo)
            } catch (error) {
              console.error('解析服务器状态失败:', error)
            }
          }
        )
      },
      onDisconnect: () => {
        isConnected.value = false
      },
      onStompError: (frame) => {
        console.error('STOMP错误:', frame)
      }
    })

    stompClient.value.activate()
  }

  // 断开连接
  const disconnect = () => {
    if (subscription.value) {
      subscription.value.unsubscribe()
      subscription.value = null
    }

    if (stompClient.value) {
      stompClient.value.deactivate()
      stompClient.value = null
    }

    if (checkInterval) {
      clearInterval(checkInterval)
      checkInterval = undefined
    }

    isConnected.value = false
  }

  // 更新服务器状态
  const updateServerStatus = (info: ServerInfo) => {
    const sessionId = info.sessionId
    const now = Date.now()

    const status: ServerStatus = {
      ...info,
      isOnline: true,
      lastUpdate: now
    }

    servers.value.set(sessionId, status)
  }

  // 定时检查离线服务器
  const checkOfflineServers = () => {
    const now = Date.now()
    servers.value.forEach((server, sessionId) => {
      if (now - server.lastUpdate > OFFLINE_THRESHOLD) {
        server.isOnline = false
      }
    })
  }

  // 获取在线服务器列表
  const getOnlineServers = () => {
    return Array.from(servers.value.values()).filter(s => s.isOnline)
  }

  // 获取指定SessionId的服务器信息
  const getServerBySessionId = (sessionId: string) => {
    return servers.value.get(sessionId)
  }

  // 使用引用计数管理连接生命周期
  onMounted(() => {
    connectionRefCount.value++
    console.debug('组件挂载，连接计数:', connectionRefCount.value)
    
    // 只在第一个组件挂载时建立连接
    if (connectionRefCount.value === 1) {
      connect()
      checkInterval = window.setInterval(checkOfflineServers, 10000)
    }
  })

  onUnmounted(() => {
    connectionRefCount.value--
    console.debug('组件卸载，连接计数:', connectionRefCount.value)
    
    // 只在最后一个组件卸载时断开连接
    if (connectionRefCount.value === 0) {
      disconnect()
    }
  })

  return {
    servers,
    isConnected,
    connectionRefCount,
    lastMessageTime,
    connect,
    disconnect,
    getOnlineServers,
    getServerBySessionId
  }
}
