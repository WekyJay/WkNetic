import request from './axios'
import type { IPage } from '@/types/common'

export interface NotificationVO {
  id: number
  userId: number
  type: number
  typeName: string
  title: string
  content: string
  relatedId?: number
  isRead: number
  createTime: string
  readTime?: string
}

/**
 * 获取用户通知列表
 */
export const getUserNotifications = (params: { page?: number; size?: number }) => {
  return request.get<IPage<NotificationVO>>('/api/v1/notification/list', { params })
}

/**
 * 获取未读通知数量
 */
export const getUnreadCount = () => {
  return request.get<number>('/api/v1/notification/unread/count')
}

/**
 * 标记通知为已读
 */
export const markAsRead = (notificationId: number) => {
  return request.post<void>(`/api/v1/notification/${notificationId}/read`)
}

/**
 * 标记所有通知为已读
 */
export const markAllAsRead = () => {
  return request.post<void>('/api/v1/notification/read/all')
}
