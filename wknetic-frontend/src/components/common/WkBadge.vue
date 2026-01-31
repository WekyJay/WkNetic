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

  // 尺寸（仅文本徽章）
  if (!props.dot && !props.count) {
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

  // 圆点徽章样式
  if (props.dot) {
    classes.push('w-2 h-2 p-0')
  }

  // 数字徽章样式
  if (props.count !== undefined) {
    classes.push('min-w-[20px] h-5 px-1.5 text-xs')
  }

  // 变体颜色
  switch (props.variant) {
    case 'default':
      classes.push('bg-[var(--bg-surface)] text-[var(--text-secondary)] border border-[var(--border-default)]')
      break
    case 'primary':
      classes.push('bg-[var(--brand-default)] text-white')
      break
    case 'success':
      classes.push('bg-green-500 text-white')
      break
    case 'warning':
      classes.push('bg-yellow-500 text-white')
      break
    case 'danger':
      classes.push('bg-red-500 text-white')
      break
    case 'info':
      classes.push('bg-blue-500 text-white')
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

// 是否有包裹内容
const slots = useSlots()
const hasWrapper = computed(() => !!slots.default)
</script>

<template>
  <!-- 独立徽章（无包裹内容） -->
  <span v-if="!hasWrapper && show" :class="badgeClasses">
    <span v-if="!dot && count === undefined">
      <slot />
    </span>
    <span v-else-if="count !== undefined">
      {{ displayCount }}
    </span>
  </span>

  <!-- 带包裹内容的徽章 -->
  <div v-else-if="hasWrapper" class="wk-badge-wrapper relative inline-flex">
    <slot />
    
    <span 
      v-if="show"
      :class="badgeClasses"
      class="absolute -top-1 -right-1 transform translate-x-1/2 -translate-y-1/2"
    >
      <span v-if="count !== undefined">{{ displayCount }}</span>
    </span>
  </div>
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
