/**
 * 用户相关类型定义
 */

import type { UserInfo } from './auth'

/**
 * 性别枚举
 */
export type Gender = 'MALE' | 'FEMALE' | 'OTHER' | 'UNSPECIFIED'

/**
 * 用户状态
 */
export type UserStatus = 0 | 1  // 0: 禁用, 1: 启用

/**
 * 用户角色
 */
export type UserRole = 'ADMIN' | 'MODERATOR' | 'USER' | 'VIP' | 'BANNED'

/**
 * 扩展的用户资料
 * 包含公开展示的详细信息
 */
export interface ExtendedUserProfile extends UserInfo {
  /** 个性签名/简介 */
  bio?: string
  /** 所在地 */
  location?: string
  /** 个人网站 */
  website?: string
  /** 性别 */
  gender?: Gender
  /** 加入时间 */
  joinDate: string
  /** 粉丝数量 */
  followerCount: number
  /** 关注数量 */
  followingCount: number
  /** 发帖数量 */
  postCount: number
  /** 当前用户是否关注了此用户 */
  isFollowing?: boolean
}

/**
 * 用户资料更新请求
 */
export interface UpdateProfileRequest {
  nickname?: string
  avatar?: string
  bio?: string
  location?: string
  website?: string
  gender?: Gender
  email?: string
  phone?: string
}

/**
 * 关注状态
 */
export interface FollowStatus {
  userId: number
  isFollowing: boolean
  followerCount: number
}

/**
 * 用户统计信息
 */
export interface UserStats {
  postCount: number
  followerCount: number
  followingCount: number
  likeCount: number
}

/**
 * 性别显示配置
 */
export const GENDER_CONFIG = {
  MALE: {
    label: '男',
    icon: 'i-tabler-gender-male',
    color: 'text-blue-500',
    bgColor: 'bg-blue-500/10'
  },
  FEMALE: {
    label: '女',
    icon: 'i-tabler-gender-female',
    color: 'text-pink-500',
    bgColor: 'bg-pink-500/10'
  },
  OTHER: {
    label: '其他',
    icon: 'i-tabler-gender-genderqueer',
    color: 'text-purple-500',
    bgColor: 'bg-purple-500/10'
  },
  UNSPECIFIED: {
    label: '未设置',
    icon: 'i-tabler-help-circle',
    color: 'text-gray-500',
    bgColor: 'bg-gray-500/10'
  }
} as const
