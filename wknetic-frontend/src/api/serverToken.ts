import api from './axios'

/**
 * 服务器Token接口
 */

export interface ServerToken {
  id: number
  name: string
  tokenValue: string
  remark?: string
  status: 0 | 1  // 0: 禁用, 1: 启用
  lastLoginIp?: string
  lastLoginTime?: string
  createTime: string
  updateTime: string
}

export interface CreateServerTokenRequest {
  name: string
  remark?: string
}

export interface UpdateServerTokenRequest {
  name: string
  remark?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const serverTokenApi = {
  /**
   * 获取所有服务器Token列表（分页）
   */
  getTokenList(page = 1, size = 10, name?: string, status?: number) {
    return api.get<PageResult<ServerToken>>('/api/v1/admin/server-token/list', {
      params: { page, size, name, status }
    })
  },

  /**
   * 创建新的服务器Token
   */
  createToken(data: CreateServerTokenRequest) {
    return api.post<ServerToken>('/api/v1/admin/server-token/create', data)
  },

  /**
   * 更新服务器Token
   */
  updateToken(id: number, data: UpdateServerTokenRequest) {
    return api.put<void>(`/api/v1/admin/server-token/update/${id}`, data)
  },

  /**
   * 删除服务器Token
   */
  deleteToken(id: number) {
    return api.delete(`/api/v1/admin/server-token/delete/${id}`)
  },

  /**
   * 切换Token状态（启用/禁用）
   */
  toggleTokenStatus(id: number, status: 0 | 1) {
    return api.put<void>(`/api/v1/admin/server-token/status/${id}`, null, {
      params: { status }
    })
  },

  /**
   * 重新生成Token值
   */
  regenerateToken(id: number) {
    return api.post<string>(`/api/v1/admin/server-token/regenerate/${id}`)
  }
}

