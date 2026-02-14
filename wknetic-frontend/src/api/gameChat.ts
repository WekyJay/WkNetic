import axios from './axios'
import type { Result } from '@/types/common'

/**
 * 聊天消息接口
 */
export interface ChatMessage {
  id: string
  serverName: string
  channel: string
  world: string
  player: {
    uuid: string
    username: string
    avatar: string
  }
  content: string
  source: 'game' | 'web'
  timestamp: string
}

/**
 * 服务器信息
 */
export interface ServerInfo {
  id: string
  name: string
  players: number
  sessionId?: string  // 可选，如果不存在则使用id作为sessionId
}

/**
 * 发送消息参数
 */
export interface SendMessageParams {
  serverName?: string
  sessionId?: string
  channel: string
  world?: string
  content: string
}

/**
 * 服务器信息
 */
export interface ServerInfo {
  id: string
  name: string
  players: number
}

/**
 * 频道信息
 */
export interface ChannelInfo {
  id: string
  name: string
}

/**
 * 世界信息
 */
export interface WorldInfo {
  id: string
  name: string
}

/**
 * 在线玩家信息
 */
export interface OnlinePlayer {
  id: string
  uuid: string
  username: string
  avatar: string
}

/**
 * 游戏聊天API
 */
export const gameChatApi = {
  /**
   * 获取聊天历史
   */
  getChatHistory(params: ChatHistoryParams): Promise<Result<ChatMessage[]>> {
    return axios.get('/api/game/chat/history', { params })
  },

  /**
   * 发送聊天消息
   */
  sendMessage(data: SendMessageParams): Promise<Result<void>> {
    return axios.post('/api/game/chat/send', data)
  },

  /**
   * 获取服务器列表
   */
  getServers(): Promise<Result<ServerInfo[]>> {
    return axios.get('/api/game/chat/servers')
  },

  /**
   * 获取频道列表
   */
  getChannels(): Promise<Result<ChannelInfo[]>> {
    return axios.get('/api/game/chat/channels')
  },

  /**
   * 获取世界列表
   */
  getWorlds(): Promise<Result<WorldInfo[]>> {
    return axios.get('/api/game/chat/worlds')
  }
}
