import request from './axios'
import type { IPage } from '@/types/common'

export interface TopicVO {
  topicId: number
  topicName: string
  topicDesc: string
  icon: string
  color?: string | null
  postCount: number
  createTime: string
}

/**
 * 创建板块（管理员）
 */
export const createTopic = (data: {
  name: string
  description?: string
  icon?: string
}) => {
  return request.post<number>('/api/v1/topic', null, { params: data })
}

/**
 * 更新板块（管理员）
 */
export const updateTopic = (topicId: number, data: {
  name?: string
  description?: string
  icon?: string
}) => {
  return request.put<void>(`/api/v1/topic/${topicId}`, null, { params: data })
}

/**
 * 删除板块（管理员）
 */
export const deleteTopic = (topicId: number) => {
  return request.delete<void>(`/api/v1/topic/${topicId}`)
}

/**
 * 获取板块详情
 */
export const getTopicDetail = (topicId: number) => {
  return request.get<TopicVO>(`/api/v1/topic/${topicId}`)
}

/**
 * 获取所有板块列表
 */
export const listAllTopics = () => {
  return request.get<TopicVO[]>('/api/v1/topic/all')
}

/**
 * 分页获取板块列表
 */
export const listTopics = (params: { page?: number; size?: number }) => {
  return request.get<IPage<TopicVO>>('/api/v1/topic/list', { params })
}
