<script setup lang="ts">
import { computed } from 'vue'

/**
 * WkAlert - 提示/警告信息展示组件
 * 
 * @example
 * <WkAlert type="success" title="Success!" message="Operation completed" />
 * <WkAlert type="error" :closable="true" @close="handleClose">
 *   <p>Custom content here</p>
 * </WkAlert>
 */

export type AlertType = 'info' | 'success' | 'warning' | 'error'

interface Props {
  /** 警告类型 */
  type?: AlertType
  /** 标题 */
  title?: string
  /** 描述文本 */
  message?: string
  /** 是否可关闭 */
  closable?: boolean
  /** 是否显示图标 */
  showIcon?: boolean
  /** 是否居中 */
  center?: boolean
  /** 自定义图标 */
  icon?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'info',
  closable: false,
  showIcon: true,
  center: false
})

const emit = defineEmits<{
  close: []
}>()

const visible = defineModel<boolean>('visible', { default: true })

const handleClose = () => {
  visible.value = false
  emit('close')
}

// 获取图标
const iconClass = computed(() => {
  if (props.icon) return props.icon
  
  switch (props.type) {
    case 'success':
      return 'i-tabler-circle-check'
    case 'warning':
      return 'i-tabler-alert-triangle'
    case 'error':
      return 'i-tabler-circle-x'
    default: // info
      return 'i-tabler-info-circle'
  }
})

// 计算样式类
const alertClasses = computed(() => {
  const classes: string[] = [
    'wk-alert',
    'relative flex gap-3 p-4 rounded-lg border transition-all duration-200'
  ]

  // 类型样式
  switch (props.type) {
    case 'success':
      classes.push(
        'bg-green-50 dark:bg-green-950/20',
        'border-green-300 dark:border-green-800',
        'text-green-800 dark:text-green-200'
      )
      break
    case 'warning':
      classes.push(
        'bg-yellow-50 dark:bg-yellow-950/20',
        'border-yellow-300 dark:border-yellow-800',
        'text-yellow-800 dark:text-yellow-200'
      )
      break
    case 'error':
      classes.push(
        'bg-red-50 dark:bg-red-950/20',
        'border-red-300 dark:border-red-800',
        'text-red-800 dark:text-red-200'
      )
      break
    default: // info
      classes.push(
        'bg-blue-50 dark:bg-blue-950/20',
        'border-blue-300 dark:border-blue-800',
        'text-blue-800 dark:text-blue-200'
      )
  }

  if (props.center) {
    classes.push('justify-center text-center')
  }

  return classes
})

const iconColorClass = computed(() => {
  switch (props.type) {
    case 'success':
      return 'text-green-600 dark:text-green-400'
    case 'warning':
      return 'text-yellow-600 dark:text-yellow-400'
    case 'error':
      return 'text-red-600 dark:text-red-400'
    default: // info
      return 'text-blue-600 dark:text-blue-400'
  }
})
</script>

<template>
  <transition
    enter-active-class="transition-all duration-300"
    enter-from-class="opacity-0 -translate-y-2"
    enter-to-class="opacity-100 translate-y-0"
    leave-active-class="transition-all duration-200"
    leave-from-class="opacity-100 translate-y-0"
    leave-to-class="opacity-0 -translate-y-2"
  >
    <div v-if="visible" :class="alertClasses" role="alert">
      <!-- 图标 -->
      <div v-if="showIcon" class="flex-shrink-0">
        <span :class="[iconClass, iconColorClass, 'text-xl']" />
      </div>

      <!-- 内容 -->
      <div class="flex-1 min-w-0">
        <h4 v-if="title" class="font-semibold mb-1">
          {{ title }}
        </h4>
        <div v-if="message || $slots.default" class="text-sm">
          <p v-if="message">{{ message }}</p>
          <slot />
        </div>
      </div>

      <!-- 关闭按钮 -->
      <button
        v-if="closable"
        type="button"
        class="flex-shrink-0 opacity-50 hover:opacity-100 transition-opacity"
        @click="handleClose"
      >
        <span class="i-tabler-x text-lg" />
      </button>
    </div>
  </transition>
</template>

<style scoped>
.wk-alert {
  animation: wk-alert-appear 0.3s ease-out;
}

@keyframes wk-alert-appear {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
