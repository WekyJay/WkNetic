<script setup lang="ts">
import { computed, useSlots } from 'vue'
/**
 * WkBadge - 徽章/标签组件
 * 
 * @example
 * <WkBadge variant="success">Active</WkBadge>
 * <WkBadge variant="danger" dot />
 * <WkBadge :count="5" max="99">
 *   <button>Messages</button>
 * </WkBadge>
 */

export type BadgeVariant = 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info'
export type BadgeSize = 'sm' | 'md' | 'lg'

interface Props {
  /** 徽章变体样式 */
  variant?: BadgeVariant
  /** 徽章大小 */
  size?: BadgeSize
  /** 是否显示为圆点 */
  dot?: boolean
  /** 数字徽章的值 */
  count?: number | string
  /** 最大显示数字（超过显示为 max+） */
  max?: number
  /** 是否显示徽章（用于条件显示） */
  show?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default',
  size: 'md',
  dot: false,
  show: true
})

const badgeClasses = computed(() => {
  const classes: string[] = [
    'wk-badge',
    'inline-flex items-center justify-center',
    'font-medium rounded-full transition-all duration-200'
  ]

  // 圆点徽章样式
  if (props.dot) {
    classes.push('w-2 h-2 p-0')
  }
  // 数字徽章样式
  else if (props.count !== undefined) {
    classes.push('min-w-5 h-5 px-1.5 text-xs')
  }
  // 文本徽章尺寸
  else {
    switch (props.size) {
      case 'sm':
        classes.push('text-xs px-2 py-0.5')
        break
      case 'lg':
        classes.push('text-base px-3 py-1')
        break
      default: // md
        classes.push('text-sm px-2.5 py-0.5')
    }
  }

  // 变体颜色
  switch (props.variant) {
    case 'default':
      classes.push('bg-bg-surface text-text-secondary border border-border')
      break
    case 'primary':
      classes.push('bg-brand text-white')
      break
    case 'success':
      classes.push('bg-green-500/15 text-green-600 dark:text-green-400')
      break
    case 'warning':
      classes.push('bg-yellow-500/15 text-yellow-600 dark:text-yellow-400')
      break
    case 'danger':
      classes.push('bg-red-500/15 text-red-600 dark:text-red-400')
      break
    case 'info':
      classes.push('bg-blue-500/15 text-blue-600 dark:text-blue-400')
      break
  }

  return classes
})

// 计算显示的数字
const displayCount = computed(() => {
  if (props.count === undefined) return ''
  
  const numCount = typeof props.count === 'number' ? props.count : parseInt(props.count, 10)
  
  if (isNaN(numCount)) return props.count
  
  if (props.max && numCount > props.max) {
    return `${props.max}+`
  }
  
  return numCount
})

// 是否是带数字角标的包裹徽章模式（有 slot 且有 count）
const slots = useSlots()
const isWrapperMode = computed(() => !!slots.default && props.count !== undefined)
</script>

<template>
  <!-- 带数字角标的包裹徽章 -->
  <div v-if="isWrapperMode" class="wk-badge-wrapper relative inline-flex">
    <slot />
    
    <span 
      v-if="show"
      :class="badgeClasses"
      class="absolute -top-1 -right-1 transform translate-x-1/2 -translate-y-1/2"
    >
      {{ displayCount }}
    </span>
  </div>

  <!-- 独立徽章 -->
  <span v-else-if="show" :class="badgeClasses">
    <!-- 圆点徽章 -->
    <template v-if="dot"></template>
    <!-- 数字徽章 -->
    <span v-else-if="count !== undefined">{{ displayCount }}</span>
    <!-- 文本徽章 -->
    <slot v-else />
  </span>
</template>

<style scoped>
.wk-badge {
  user-select: none;
  white-space: nowrap;
}

.wk-badge-wrapper {
  position: relative;
}
</style>
