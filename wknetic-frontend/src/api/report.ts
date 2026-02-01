import request from './axios'
import type { IPage } from '@/types/common'

export interface CreateReportDTO {
  targetType: string
  targetId: number
  reason: string
  description?: string
}

export interface ForumReport {
  id: number
  reporterId: number
  targetType: string
  targetId: number
  reason: string
  description: string
  status: number
  handlerId?: number
  handleTime?: string
  handleNote?: string
  createTime: string
  updateTime: string
}

/**
 * 创建举报
 */
export const createReport = (data: CreateReportDTO) => {
  return request.post<number>('/api/v1/report', data)
}

/**
 * 获取举报列表（审核员）
 */
export const getReports = (params: {
  page?: number
  size?: number
  status?: number
}) => {
  return request.get<IPage<ForumReport>>('/api/v1/report/list', { params })
}

/**
 * 处理举报（审核员）
 */
export const handleReport = (reportId: number, action: string, handleNote?: string) => {
  const params: any = { action }
  if (handleNote) {
    params.handleNote = handleNote
  }
  return request.post<void>(`/api/v1/report/${reportId}/handle`, null, { params })
}

/**
 * 标记举报为处理中（审核员）
 */
export const markAsProcessing = (reportId: number) => {
  return request.post<void>(`/api/v1/report/${reportId}/processing`)
}

/**
 * 获取待处理举报数量（审核员）
 */
export const getPendingReportCount = () => {
  return request.get<number>('/api/v1/report/pending/count')
}
