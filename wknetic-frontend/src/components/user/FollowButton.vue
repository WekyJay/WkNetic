<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/user'
import { useI18n } from 'vue-i18n'
import WkButton from '@/components/common/WkButton.vue'

interface Props {
  /** 目标用户 ID */
  userId: number
  /** 是否正在关注 */
  isFollowing?: boolean
  /** 按钮尺寸 */
  size?: 'sm' | 'md' | 'lg'
  /** 是否显示为文本按钮 */
  text?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isFollowing: false,
  size: 'md',
  text: false
})

const emit = defineEmits<{
  followChange: [isFollowing: boolean]
}>()

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const localIsFollowing = ref(props.isFollowing)

/** 是否是当前登录用户自己 */
const isOwnProfile = computed(() => {
  return authStore.user?.userId === props.userId
})

/** 按钮文本 */
const buttonText = computed(() => {
  if (isOwnProfile.value) {
    return t('user.editProfile')
  }
  return localIsFollowing.value ? t('user.following') : t('user.follow')
})

/** 按钮图标 */
const buttonIcon = computed(() => {
  if (isOwnProfile.value) {
    return 'i-tabler-edit'
  }
  return localIsFollowing.value ? 'i-tabler-user-check' : 'i-tabler-user-plus'
})

/** 按钮变体 */
const buttonVariant = computed(() => {
  if (isOwnProfile.value) {
    return 'secondary'
  }
  return localIsFollowing.value ? 'ghost' : 'primary'
})

/** 处理按钮点击 */
const handleClick = async () => {
  // 如果是自己的资料，跳转到编辑页面
  if (isOwnProfile.value) {
    router.push('/settings')
    return
  }

  // 检查是否登录
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  // 关注/取消关注
  loading.value = true
  try {
    if (localIsFollowing.value) {
      await userApi.unfollowUser(props.userId)
      localIsFollowing.value = false
    } else {
      await userApi.followUser(props.userId)
      localIsFollowing.value = true
    }
    emit('followChange', localIsFollowing.value)
  } catch (error) {
    console.error('关注操作失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <WkButton
    :variant="buttonVariant"
    :size="size"
    :loading="loading"
    :text="text"
    @click="handleClick"
  >
    <i :class="buttonIcon" />
    <span>{{ buttonText }}</span>
  </WkButton>
</template>
