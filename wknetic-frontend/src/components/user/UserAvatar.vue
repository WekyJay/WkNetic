<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  /** 头像 URL */
  src?: string
  /** 用户昵称（用于生成 fallback） */
  nickname?: string
  /** 头像尺寸 */
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
  /** 是否显示边框 */
  bordered?: boolean
  /** 是否可点击 */
  clickable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  bordered: false,
  clickable: false
})

const emit = defineEmits<{
  click: []
}>()

/** 尺寸映射 */
const sizeClass = computed(() => {
  const sizes = {
    xs: 'w-6 h-6 text-xs',
    sm: 'w-8 h-8 text-sm',
    md: 'w-10 h-10 text-base',
    lg: 'w-12 h-12 text-lg',
    xl: 'w-16 h-16 text-xl'
  }
  return sizes[props.size]
})

/** 生成头像 fallback 文字（取昵称首字母） */
const fallbackText = computed(() => {
  if (!props.nickname) return '?'
  return props.nickname.charAt(0).toUpperCase()
})

/** 头像加载失败标识 */
const imageError = computed(() => !props.src)

const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}
</script>

<template>
  <div
    :class="[
      'user-avatar',
      'flex-center',
      'rounded-full',
      'overflow-hidden',
      'flex-shrink-0',
      'select-none',
      sizeClass,
      {
        'border-2 border-border': bordered,
        'cursor-pointer hover:opacity-80 transition-opacity': clickable,
        'bg-gradient-to-br from-brand-light to-brand-default text-white font-semibold': imageError
      }
    ]"
    @click="handleClick"
  >
    <img
      v-if="src && !imageError"
      :src="src"
      :alt="nickname || 'User avatar'"
      class="w-full h-full object-cover"
      @error="() => {}"
    />
    <span v-else>{{ fallbackText }}</span>
  </div>
</template>

<style scoped>
.user-avatar {
  transition: all 0.2s ease;
}

.user-avatar:active {
  transform: scale(0.95);
}
</style>
