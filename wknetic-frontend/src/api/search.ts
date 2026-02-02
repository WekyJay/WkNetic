import request from './axios'

export interface SearchPostDTO {
  keyword?: string
  topicId?: number
  tags?: string[]
  status?: number
  startTime?: string
  endTime?: string
  sortBy?: string
  sortOrder?: string
  page?: number
  size?: number
}

export interface PostSearchVO {
  postId: number
  title: string
  excerpt: string
  userId: number
  username: string
  topicId: number
  topicName: string
  tags: string[]
  status: number
  isPinned: boolean
  isHot: boolean
  likeCount: number
  commentCount: number
  viewCount: number
  bookmarkCount: number
  createTime: string
  updateTime: string
  lastCommentTime?: string
  score?: number
  highlights?: Record<string, string[]>
}

export interface SearchResultPage {
  records: PostSearchVO[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 搜索帖子
 */
export function searchPosts(params: SearchPostDTO) {
  return request.post<SearchResultPage>('/post/search', params)
}

/**
 * 获取搜索建议
 */
export function getSearchSuggestions(prefix: string) {
  return request.get<string[]>('/post/search/suggest', { params: { prefix } })
}

/**
 * 获取热门搜索词
 */
export function getHotSearchKeywords() {
  return request.get<string[]>('/post/search/hot')
}
