import request from './axios'
import type { IPage } from '@/types/common'


export interface CreatePostDTO {
  title: string
  content: string
  excerpt?: string
  topicId?: number
  tags?: string[]
  publish?: boolean
}

export interface UpdatePostDTO {
  title?: string
  content?: string
  excerpt?: string
  topicId?: number
  tags?: string[]
  changeSummary?: string
}

export interface PostVO {
  postId: number
  title: string
  excerpt: string
  userId: number
  topicId: number
  status: number
  viewCount: number
  likeCount: number
  commentCount: number
  createTime: string
  updateTime: string
  isLiked: boolean
  isBookmarked: boolean
  author?: {
    userId: number
    username: string
    nickname: string
    avatar: string
  }
  topic?: {
    topicId: number
    topicName: string
  }
  tags?: Array<{
    tagId: number
    tagName: string
  }>
}

export interface PostDetailVO extends PostVO {
  content: string
  auditorId?: number
  auditTime?: string
  auditReason?: string
}

/**
 * 创建帖子
 */
export const createPost = (data: CreatePostDTO) => {
  return request.post<number>('/api/v1/post', data)
}

/**
 * 更新帖子
 */
export const updatePost = (postId: number, data: UpdatePostDTO) => {
  return request.put<void>(`/api/v1/post/${postId}`, data)
}

/**
 * 删除帖子
 */
export const deletePost = (postId: number) => {
  return request.delete<void>(`/api/v1/post/${postId}`)
}

/**
 * 获取帖子详情
 */
export const getPostDetail = (postId: number) => {
  return request.get<PostDetailVO>(`/api/v1/post/${postId}`)
}

/**
 * 获取最新草稿
 */
export const getLatestDraft = () => {
  return request.get<PostDetailVO>('/api/v1/post/draft/latest')
}

/**
 * 获取帖子列表
 */
export const listPosts = (params: {
  page?: number
  size?: number
  topicId?: number
  status?: number
}) => {
  return request.get<IPage<PostVO>>('/api/v1/post/list', { params })
}

/**
 * 点赞/取消点赞帖子
 */
export const togglePostLike = (postId: number) => {
  return request.post<boolean>(`/api/v1/post/${postId}/like`)
}
