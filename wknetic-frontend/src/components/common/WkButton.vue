<script setup lang="ts">
import { computed } from 'vue'
/**
 * WkButton - 统一的按钮组件
 * 
 * @example
 * <WkButton variant="primary" size="md" @click="handleClick">Click me</WkButton>
 * <WkButton variant="danger" :loading="isLoading" :disabled="true">Submit</WkButton>
 */

export type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'success' | 'warning' | 'ghost' | 'text'
export type ButtonSize = 'sm' | 'md' | 'lg'

interface Props {
  /** 按钮变体样式 */
  variant?: ButtonVariant
  /** 按钮大小 */
  size?: ButtonSize
  /** 是否禁用 */
  disabled?: boolean
  /** 是否加载中 */
  loading?: boolean
  /** 是否块级元素（100%宽度） */
  block?: boolean
  /** 原生按钮类型 */
  type?: 'button' | 'submit' | 'reset'
  /** 图标（可选，显示在文字前） */
  icon?: string
  /** 图标位置 */
  iconPosition?: 'left' | 'right'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  disabled: false,
  loading: false,
  block: false,
  type: 'button',
  iconPosition: 'left'
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const handleClick = (event: MouseEvent) => {
  if (props.disabled || props.loading) {
    event.preventDefault()
    return
  }
  emit('click', event)
}

// 计算按钮样式类
const buttonClasses = computed(() => {
  const classes: string[] = [
    'wk-button',
    'inline-flex items-center justify-center gap-2',
    'font-medium rounded-lg transition-all duration-200',
    'focus:outline-none',
    'disabled:opacity-50 disabled:cursor-not-allowed'
  ]

  // 尺寸
  switch (props.size) {
    case 'sm':
      classes.push('text-sm px-3 py-1.5')
      break
    case 'lg':
      classes.push('text-lg px-6 py-3')
      break
    default: // md
      classes.push('text-base px-4 py-2')
  }

  // 变体样式
  switch (props.variant) {
    case 'primary':
      classes.push(
        'bg-[var(--brand-default)] text-white',
        'hover:opacity-90'
      )
      break
    case 'secondary':
      classes.push(
        'bg-[var(--bg-surface)] text-[var(--text-default)] border border-[var(--border-default)]',
        'hover:bg-[var(--bg-hover)]'
      )
      break
    case 'danger':
      classes.push(
        'bg-red-600 text-white',
        'hover:bg-red-700'
      )
      break
    case 'success':
      classes.push(
        'bg-green-600 text-white',
        'hover:bg-green-700'
      )
      break
    case 'warning':
      classes.push(
        'bg-yellow-600 text-white',
        'hover:bg-yellow-700'
      )
      break
    case 'ghost':
      classes.push(
        'bg-transparent text-[var(--text-default)] border border-[var(--border-default)]',
        'hover:bg-[var(--bg-hover)]'
      )
      break
    case 'text':
      classes.push(
        'bg-transparent text-[var(--brand-default)]',
        'hover:bg-[var(--bg-hover)]'
      )
      break
  }

  // 块级
  if (props.block) {
    classes.push('w-full')
  }

  return classes
})

const iconClasses = computed(() => {
  const size = props.size === 'sm' ? 'text-base' : props.size === 'lg' ? 'text-2xl' : 'text-xl'
  return size
})
</script>

<template>
  <button
    :type="type"
    :class="buttonClasses"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <!-- Loading spinner -->
    <span v-if="loading" class="i-tabler-loader-2 animate-spin" :class="iconClasses" />
    
    <!-- Left icon -->
    <span 
      v-else-if="icon && iconPosition === 'left'" 
      :class="[icon, iconClasses]" 
    />
    
    <!-- Button text/content -->
    <span v-if="$slots.default">
      <slot />
    </span>
    
    <!-- Right icon -->
    <span 
      v-if="!loading && icon && iconPosition === 'right'" 
      :class="[icon, iconClasses]" 
    />
  </button>
</template>

<style scoped>
.wk-button {
  position: relative;
  user-select: none;
  white-space: nowrap;
}

.wk-button:active:not(:disabled) {
  transform: scale(0.98);
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
