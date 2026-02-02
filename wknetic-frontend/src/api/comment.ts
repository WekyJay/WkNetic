import request from './axios'

export interface CreateCommentDTO {
  postId: number
  content: string
  parentId?: number
  replyToUserId?: number
}

export interface CommentVO {
  id: number
  postId: number
  userId: number
  content: string
  parentId?: number
  replyToUserId?: number
  likeCount: number
  createTime: string
  liked: boolean
  author: {
    id: number
    username: string
    nickname: string
    avatar: string
  }
  replyToUser?: {
    id: number
    username: string
    nickname: string
  }
  replies: CommentVO[]
}

/**
 * 创建评论
 */
export const createComment = (data: CreateCommentDTO) => {
  return request.post<number>('/api/v1/comment', data)
}

/**
 * 删除评论
 */
export const deleteComment = (commentId: number) => {
  return request.delete<void>(`/api/v1/comment/${commentId}`)
}

/**
 * 获取帖子评论列表（树形结构）
 */
export const getPostComments = (postId: number) => {
  return request.get<CommentVO[]>(`/api/v1/comment/post/${postId}`)
}

/**
 * 获取帖子评论列表（带分页）
 */
export const listComments = (params: {
  postId: number
  page?: number
  size?: number
}) => {
  return request.get<{ records: CommentVO[], total: number }>('/api/v1/comment/list', { params })
}

/**
 * 点赞/取消点赞评论
 */
export const toggleCommentLike = (commentId: number) => {
  return request.post<boolean>(`/api/v1/comment/${commentId}/like`)
}
