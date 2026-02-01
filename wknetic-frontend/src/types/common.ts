/**
 * 通用类型定义
 */

/**
 * 分页响应
 */
export interface IPage<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * API响应
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

/**
 * 用户信息
 */
export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string
  email?: string
  createTime: string
}

/**
 * 帖子状态枚举
 */
export const PostStatus = {
  DRAFT: 0,
  UNDER_REVIEW: 1,
  PUBLISHED: 2,
  REJECTED: 3,
  DELETED: 4
} as const

export type PostStatus = (typeof PostStatus)[keyof typeof PostStatus]

/**
 * 通知类型枚举
 */
export const NotificationType = {
  SYSTEM: 0,
  POST_AUDIT_PASS: 1,
  POST_AUDIT_REJECT: 2,
  COMMENT: 3,
  REPLY: 4,
  POST_LIKE: 5,
  COMMENT_LIKE: 6,
  MENTION: 7,
  FOLLOW: 8
} as const

export type NotificationType = (typeof NotificationType)[keyof typeof NotificationType]

/**
 * 举报状态枚举
 */
export const ReportStatus = {
  PENDING: 0,
  PROCESSING: 1,
  ACCEPTED: 2,
  REJECTED: 3
} as const

export type ReportStatus = (typeof ReportStatus)[keyof typeof ReportStatus]

/**
 * 举报原因枚举
 */
export const ReportReason = {
  SPAM: 'SPAM',
  INAPPROPRIATE: 'INAPPROPRIATE',
  HARASSMENT: 'HARASSMENT',
  COPYRIGHT: 'COPYRIGHT',
  OTHER: 'OTHER'
} as const

export type ReportReason = (typeof ReportReason)[keyof typeof ReportReason]
