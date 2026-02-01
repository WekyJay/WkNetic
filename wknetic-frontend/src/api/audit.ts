import request from './axios'
import type { IPage } from '@/types/common'
import type { ForumPost } from './post'

/**
 * 获取待审核帖子列表（审核员）
 */
export const getPendingPosts = (params: { page?: number; size?: number }) => {
  return request.get<IPage<ForumPost>>('/api/v1/audit/pending', { params })
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
  return request.get<IPage<ForumPost>>('/api/v1/audit/history', { params })
}

/**
 * 获取待审核帖子数量（审核员）
 */
export const getPendingPostCount = () => {
  return request.get<number>('/api/v1/audit/pending/count')
}
