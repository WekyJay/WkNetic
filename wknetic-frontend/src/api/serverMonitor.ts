import request from './axios'

export interface ServerToken {
  id: number
  name: string
  tokenValue: string
  status: number
  remark?: string
  lastLoginIp?: string
  lastLoginTime?: string
  createBy?: string
  createTime: string
  updateTime: string
}

export interface CreateTokenRequest {
  name: string
  remark?: string
}

export interface ServerInfo {
  token: string
  serverName: string
  motd: string
  onlinePlayers: number
  maxPlayers: number
  tps: number
  ramUsage: number
  maxRam: number
  playerList: PlayerInfo[]
  pluginList: PluginInfo[]
}

export interface PlayerInfo {
  uuid: string
  name: string
  ping: number
  world: string
  gameMode: string
}

export interface PluginInfo {
  name: string
  version: string
  enabled: boolean
  author?: string
  description?: string
}

export interface SendCommandRequest {
  token: string
  commandType: 'KICK' | 'BAN' | 'COMMAND' | 'MESSAGE'
  targetPlayer?: string
  command?: string
  reason?: string
}

/**
 * 获取Token列表
 */
export function getTokenList(params: {
  page: number
  size: number
  name?: string
  status?: number
}) {
  return request<{ records: ServerToken[]; total: number }>({
    url: '/api/v1/admin/server-token/list',
    method: 'GET',
    params
  })
}

/**
 * 创建Token
 */
export function createToken(data: CreateTokenRequest) {
  return request<ServerToken>({
    url: '/api/v1/admin/server-token/create',
    method: 'POST',
    data
  })
}

/**
 * 更新Token
 */
export function updateToken(id: number, data: { name: string; remark?: string }) {
  return request({
    url: `/api/v1/admin/server-token/update/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除Token
 */
export function deleteToken(id: number) {
  return request({
    url: `/api/v1/admin/server-token/delete/${id}`,
    method: 'DELETE'
  })
}

/**
 * 更新Token状态
 */
export function updateTokenStatus(id: number, status: number) {
  return request({
    url: `/api/v1/admin/server-token/status/${id}`,
    method: 'PUT',
    params: { status }
  })
}

/**
 * 重新生成Token
 */
export function regenerateToken(id: number) {
  return request<string>({
    url: `/api/v1/admin/server-token/regenerate/${id}`,
    method: 'POST'
  })
}

/**
 * 发送管理命令到服务器
 */
export function sendCommand(data: SendCommandRequest) {
  return request({
    url: '/api/v1/admin/server-monitor/command',
    method: 'POST',
    data
  })
}
