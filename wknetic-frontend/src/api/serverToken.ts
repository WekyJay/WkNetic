import api from './axios'

/**
 * 服务器Token接口
 */

export interface ServerToken {
  tokenId: number
  tokenName: string
  tokenKey: string
  tokenSecret: string
  description?: string
  tags: string
  status: 0 | 1  // 0: 禁用, 1: 启用
  lastUsedTime?: string
  createTime: string
  updateTime: string
}

export interface CreateServerTokenRequest {
  tokenName: string
  description?: string
  tags: string
  status: number
}

export interface UpdateServerTokenRequest {
  tokenId: number
  tokenName: string
  description?: string
  tags: string
  status: number
}

export const serverTokenApi = {
  /**
   * 获取所有服务器Token列表
   */
  getTokenList() {
    return api.get<ServerToken[]>('/api/v1/admin/server-tokens')
  },

  /**
   * 创建新的服务器Token
   */
  createToken(data: CreateServerTokenRequest) {
    return api.post<ServerToken>('/api/v1/admin/server-tokens', data)
  },

  /**
   * 更新服务器Token
   */
  updateToken(token: UpdateServerTokenRequest) {
    return api.put<ServerToken>('/api/v1/admin/server-tokens', token)
  },

  /**
   * 删除服务器Token
   */
  deleteToken(tokenId: number) {
    return api.delete(`/api/v1/admin/server-tokens/${tokenId}`)
  },

  /**
   * 切换Token状态（启用/禁用）
   */
  toggleTokenStatus(tokenId: number, status: 0 | 1) {
    return api.patch(`/api/v1/admin/server-tokens/${tokenId}/status`, { status })
  },

  /**
   * 重新生成Token Secret
   */
  regenerateSecret(tokenId: number) {
    return api.post<ServerToken>(`/api/v1/admin/server-tokens/${tokenId}/regenerate`)
  }
}
