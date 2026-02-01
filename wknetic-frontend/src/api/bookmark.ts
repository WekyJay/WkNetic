import request from './axios'
import type { IPage } from '@/types/common'
import type { PostVO } from './post'

export interface BookmarkCategoryDTO {
  name: string
  description?: string
}

export interface BookmarkCategory {
  id: number
  userId: number
  name: string
  description: string
  isDefault: number
  createTime: string
  updateTime: string
}

/**
 * 收藏/取消收藏帖子
 */
export const toggleBookmark = (postId: number, categoryId?: number) => {
  const params = categoryId ? { categoryId } : undefined
  return request.post<boolean>(`/api/v1/bookmark/post/${postId}`, null, { params })
}

/**
 * 移动收藏到指定收藏夹
 */
export const moveBookmark = (postId: number, categoryId: number) => {
  return request.put<void>(`/api/v1/bookmark/post/${postId}/move`, null, {
    params: { categoryId }
  })
}

/**
 * 获取用户的收藏列表
 */
export const getUserBookmarks = (params: {
  page?: number
  size?: number
  categoryId?: number
}) => {
  return request.get<IPage<PostVO>>('/api/v1/bookmark/list', { params })
}

/**
 * 创建收藏夹
 */
export const createCategory = (data: BookmarkCategoryDTO) => {
  return request.post<number>('/api/v1/bookmark/category', data)
}

/**
 * 更新收藏夹
 */
export const updateCategory = (categoryId: number, data: BookmarkCategoryDTO) => {
  return request.put<void>(`/api/v1/bookmark/category/${categoryId}`, data)
}

/**
 * 删除收藏夹
 */
export const deleteCategory = (categoryId: number) => {
  return request.delete<void>(`/api/v1/bookmark/category/${categoryId}`)
}

/**
 * 获取用户的收藏夹列表
 */
export const getUserCategories = () => {
  return request.get<BookmarkCategory[]>('/api/v1/bookmark/category/list')
}
