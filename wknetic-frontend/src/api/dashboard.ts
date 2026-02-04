import api from './axios'

/**
 * 仪表板统计数据接口
 */
export interface DashboardStatistics {
  totalUserCount: number
  totalUserChangeRate: number
  onlineUserCount: number
  onlineUserChangeRate: number
  totalPostCount: number
  totalPostChangeRate: number
  pendingAuditCount: number
  pendingAuditChangeRate: number
}

/**
 * 发帖趋势数据接口
 */
export interface PostTrendItem {
  date: string
  postCount: number
  commentCount?: number
}

/**
 * 最近活动日志接口
 */
export interface RecentActivity {
  logId: number
  title: string
  businessType: number
  businessTypeLabel: string
  operName: string
  operTime: string
  status: number
  statusLabel: string
  operIp: string
  errorMsg: string | null
}

/**
 * 快捷入口接口
 */
export interface QuickAction {
  actionId?: number
  actionKey: string
  actionName: string
  actionUrl: string
  icon?: string
  sortOrder?: number
  status?: number
}

/**
 * 可用快捷入口选项接口
 */
export interface AvailableQuickActionOption {
  actionKey: string
  actionName: string
  actionUrl: string
  icon: string
}

/**
 * 获取仪表板统计数据
 */
export const getDashboardStatistics = () => {
  return api.get<DashboardStatistics>('api/v1/admin/dashboard/statistics')
}

/**
 * 获取帖子发布趋势
 * @param days 天数（7或30），默认7
 */
export const getPostTrend = (days: number = 7) => {
  return api.get<PostTrendItem[]>('api/v1/admin/dashboard/post-trend', {
    params: { days }
  })
}

/**
 * 获取最近活动日志
 * @param limit 返回条数，默认10，最多50
 */
export const getRecentActivities = (limit: number = 10) => {
  return api.get<RecentActivity[]>('api/v1/admin/dashboard/recent-activity', {
    params: { limit }
  })
}

/**
 * 获取用户快捷入口
 */
export const getUserQuickActions = () => {
  return api.get<QuickAction[]>('api/v1/admin/dashboard/quick-actions')
}

/**
 * 保存或更新用户快捷入口
 * @param actions 快捷入口列表
 */
export const saveUserQuickActions = (actions: QuickAction[]) => {
  return api.post<void>('api/v1/admin/dashboard/quick-actions', actions)
}

/**
 * 获取可用的快捷入口选项
 */
export const getAvailableQuickActions = () => {
  return api.get<AvailableQuickActionOption[]>('api/v1/admin/dashboard/quick-actions/available')
}
