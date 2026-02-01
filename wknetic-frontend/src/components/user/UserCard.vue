<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { userApi } from '@/api/user'
import type { ExtendedUserProfile } from '@/types/user'
import { GENDER_CONFIG } from '@/types/user'
import UserAvatar from './UserAvatar.vue'
import FollowButton from './FollowButton.vue'
import WkCard from '@/components/common/WkCard.vue'
import WkLoading from '@/components/common/WkLoading.vue'
import type { E } from 'vue-router/dist/router-CWoNjPRp.mjs'

interface Props {
  /** 用户 ID */
  userId: number
  /** 是否显示完整信息 */
  detailed?: boolean
  /** 是否可点击跳转 */
  clickable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  detailed: true,
  clickable: true
})

const { t } = useI18n()
const loading = ref(true)
const user = ref<ExtendedUserProfile | null>(null)

/** 加载用户信息 */
const loadUserProfile = async () => {
  loading.value = true
  try {
    const response = await userApi.getUserProfile(props.userId)
    user.value = response.data
    console.log('用户资料:', response.data)
  } catch (error) {
    console.error('加载用户资料失败:', error)
    user.value = null
  } finally {
    loading.value = false
  }
}

/** 处理关注状态变化 */
const handleFollowChange = (isFollowing: boolean) => {
  if (user.value) {
    user.value.isFollowing = isFollowing
    user.value.followerCount += isFollowing ? 1 : -1
  }
}

/** 处理卡片点击 */
const handleCardClick = () => {
  if (props.clickable && user.value) {
    window.location.href = `/user/${user.value.userId}`
  }
}

onMounted(() => {
  loadUserProfile()
})
</script>

<template>
  <WkCard
    :class="[
      'user-card',
      { 'cursor-pointer hover:shadow-lg': clickable }
    ]"
    @click="handleCardClick"
  >
    <WkLoading v-if="loading" :loading="true" />
    
    <div v-else-if="user" class="flex flex-col gap-4">
      <!-- 用户头像和基本信息 -->
      <div class="flex items-start gap-4">
        <UserAvatar
          :src="user.avatar"
          :nickname="user.nickname"
          size="lg"
          bordered
        />
        
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 mb-1">
            <h3 class="text-lg font-semibold text-text truncate">
              {{ user.nickname }}
            </h3>
            
            <!-- 性别标识 -->
            <span
              v-if="user.gender && user.gender !== 'UNSPECIFIED'"
              :class="[
                'inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs',
                GENDER_CONFIG[user.gender].color,
                GENDER_CONFIG[user.gender].bgColor
              ]"
            >
              <i :class="GENDER_CONFIG[user.gender].icon" class="text-sm" />
              <span>{{ t(`user.${user.gender.toLowerCase()}`) }}</span>
            </span>
          </div>
          
          <p class="text-sm text-text-secondary">@{{ user.username }}</p>
          
          <!-- 个性签名 -->
          <p v-if="detailed && user.bio" class="mt-2 text-sm text-text-secondary line-clamp-2">
            {{ user.bio }}
          </p>
        </div>
      </div>
      
      <!-- 详细信息 -->
      <div v-if="detailed" class="flex flex-wrap gap-4 text-sm text-text-secondary">
        <div v-if="user.location" class="flex items-center gap-1">
          <i class="i-tabler-map-pin text-base" />
          <span>{{ user.location }}</span>
        </div>
        
        <div v-if="user.website" class="flex items-center gap-1">
          <i class="i-tabler-link text-base" />
          <a
            :href="user.website"
            target="_blank"
            rel="noopener noreferrer"
            class="text-brand hover:underline"
            @click.stop
          >
            {{ user.website }}
          </a>
        </div>
        
        <div class="flex items-center gap-1">
          <i class="i-tabler-calendar text-base" />
          <span>{{ t('user.joinedAt') }} {{ new Date(user.joinDate).toLocaleDateString() }}</span>
        </div>
      </div>
      
      <!-- 统计数据 -->
      <div v-if="detailed" class="flex gap-6 text-sm">
        <div class="flex items-center gap-1">
          <span class="font-semibold text-text">{{ user.postCount }}</span>
          <span class="text-text-secondary">{{ t('user.posts') }}</span>
        </div>
        
        <div class="flex items-center gap-1">
          <span class="font-semibold text-text">{{ user.followerCount }}</span>
          <span class="text-text-secondary">{{ t('user.followers') }}</span>
        </div>
        
        <div class="flex items-center gap-1">
          <span class="font-semibold text-text">{{ user.followingCount }}</span>
          <span class="text-text-secondary">{{ t('user.following') }}</span>
        </div>
      </div>
      
      <!-- 关注按钮 -->
      <div class="pt-2 border-t border-border">
        <FollowButton
          :user-id="user.userId"
          :is-following="user.isFollowing"
          size="md"
          @follow-change="handleFollowChange"
          @click.stop
        />
      </div>
    </div>
    
    <!-- 加载失败 -->
    <div v-else class="text-center py-8 text-text-secondary">
      <i class="i-tabler-user-x text-4xl mb-2" />
      <p>{{ t('user.notFound') }}</p>
    </div>
  </WkCard>
</template>

<style scoped>
.user-card {
  transition: all 0.2s ease;
}

.user-card:hover {
  transform: translateY(-2px);
}
</style>
