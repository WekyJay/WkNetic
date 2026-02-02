import request from './axios'
import type { IPage } from '@/types/common'

/**
 * 审核统计VO
 */
export interface AuditStatistics {
  pendingCount: number
  todayApprovedCount: number
  todayRejectedCount: number
  weekApprovedCount: number
  weekRejectedCount: number
  approvalRate: number
  averageAuditTime: number
  totalAuditCount: number
}

/**
 * 审核帖子VO
 */
export interface AuditPostVO {
  id: number
  title: string
  content?: string
  excerpt?: string
  status: number
  viewCount: number
  likeCount: number
  commentCount: number
  createTime: string
  author?: {
    id: number
    username: string
    nickname: string
    avatar: string
  }
  topic?: {
    id: number
    name: string
  }
  tags?: Array<{
    id: number
    name: string
  }>
  auditorName?: string
  auditTime?: string
  auditRemark?: string
}

/**
 * 获取待审核帖子列表（审核员）
 */
export const getPendingPosts = (params: { page?: number; size?: number }) => {
  return request.get<IPage<AuditPostVO>>('/api/v1/audit/pending', { params })
}

/**
 * 审核通过帖子（审核员）
 */
export const approvePost = (postId: number) => {
  return request.post<void>(`/api/v1/audit/approve/${postId}`)
}

/**
 * 审核拒绝帖子（审核员）
 */
export const rejectPost = (postId: number, reason: string) => {
  return request.post<void>(`/api/v1/audit/reject/${postId}`, null, {
    params: { reason }
  })
}

/**
 * 获取审核历史（审核员）
 */
export const getAuditHistory = (params: {
  page?: number
  size?: number
  status?: number
}) => {
  return request.get<IPage<AuditPostVO>>('/api/v1/audit/history', { params })
}

/**
 * 获取待审核帖子数量（审核员）
 */
export const getPendingPostCount = () => {
  return request.get<number>('/api/v1/audit/pending/count')
}

/**
 * 获取审核统计信息（审核员）
 */
export const getAuditStatistics = () => {
  return request.get<AuditStatistics>('/api/v1/audit/statistics')
}
