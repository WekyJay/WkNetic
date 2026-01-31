<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

/**
 * WkNotification - 通知消息组件
 * 
 * @example
 * <WkNotification
 *   type="success"
 *   title="Success"
 *   message="Operation completed successfully!"
 *   :duration="3000"
 * />
 */

export type NotificationType = 'info' | 'success' | 'warning' | 'error'
export type NotificationPosition = 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left'

interface Props {
  /** 通知类型 */
  type?: NotificationType
  /** 标题 */
  title?: string
  /** 消息内容 */
  message?: string
  /** 显示时长（毫秒），0表示不自动关闭 */
  duration?: number
  /** 是否可关闭 */
  closable?: boolean
  /** 位置 */
  position?: NotificationPosition
  /** 是否显示图标 */
  showIcon?: boolean
  /** 偏移距离 */
  offset?: number
}

const props = withDefaults(defineProps<Props>(), {
  type: 'info',
  duration: 4500,
  closable: true,
  position: 'top-right',
  showIcon: true,
  offset: 16
})

const emit = defineEmits<{
  close: []
  click: []
}>()

const visible = ref(false)
let timer: ReturnType<typeof setTimeout> | null = null

// 获取图标
const iconClass = computed(() => {
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
const notificationClasses = computed(() => {
  const classes: string[] = [
    'wk-notification',
    'pointer-events-auto',
    'flex gap-3 p-4 rounded-lg shadow-lg border',
    'bg-[var(--bg-raised)]',
    'border-[var(--border-default)]',
    'min-w-[320px] max-w-[420px]',
    'cursor-pointer hover:shadow-xl transition-shadow'
  ]

  return classes
})

const iconColorClass = computed(() => {
  switch (props.type) {
    case 'success':
      return 'text-green-500'
    case 'warning':
      return 'text-yellow-500'
    case 'error':
      return 'text-red-500'
    default: // info
      return 'text-blue-500'
  }
})

const positionStyle = computed(() => {
  const style: Record<string, string> = {}
  
  if (props.position.includes('top')) {
    style.top = `${props.offset}px`
  } else {
    style.bottom = `${props.offset}px`
  }
  
  if (props.position.includes('right')) {
    style.right = `${props.offset}px`
  } else {
    style.left = `${props.offset}px`
  }
  
  return style
})

// 开始自动关闭计时
const startTimer = () => {
  if (props.duration > 0) {
    timer = setTimeout(() => {
      handleClose()
    }, props.duration)
  }
}

// 清除计时器
const clearTimer = () => {
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
}

// 处理关闭
const handleClose = () => {
  visible.value = false
  emit('close')
}

// 处理点击
const handleClick = () => {
  emit('click')
}

// 鼠标悬停暂停自动关闭
const handleMouseEnter = () => {
  clearTimer()
}

const handleMouseLeave = () => {
  startTimer()
}

onMounted(() => {
  visible.value = true
  startTimer()
})

onUnmounted(() => {
  clearTimer()
})

defineExpose({
  close: handleClose
})
</script>

<template>
  <Teleport to="body">
    <transition
      enter-active-class="transition-all duration-300"
      :enter-from-class="position.includes('right') ? 'translate-x-full' : '-translate-x-full'"
      enter-to-class="translate-x-0"
      leave-active-class="transition-all duration-200"
      :leave-to-class="position.includes('right') ? 'translate-x-full' : '-translate-x-full'"
    >
      <div
        v-if="visible"
        class="fixed z-[9999] pointer-events-none"
        :style="positionStyle"
      >
        <div
          :class="notificationClasses"
          @click="handleClick"
          @mouseenter="handleMouseEnter"
          @mouseleave="handleMouseLeave"
        >
          <!-- 图标 -->
          <div v-if="showIcon" class="flex-shrink-0">
            <span :class="[iconClass, iconColorClass, 'text-2xl']" />
          </div>

          <!-- 内容 -->
          <div class="flex-1 min-w-0">
            <h4 v-if="title" class="font-semibold text-[var(--text-default)] mb-1">
              {{ title }}
            </h4>
            <div v-if="message || $slots.default" class="text-sm text-[var(--text-secondary)]">
              <p v-if="message" class="break-words">{{ message }}</p>
              <slot />
            </div>
          </div>

          <!-- 关闭按钮 -->
          <button
            v-if="closable"
            type="button"
            class="flex-shrink-0 text-[var(--text-muted)] hover:text-[var(--text-default)] transition-colors"
            @click.stop="handleClose"
          >
            <span class="i-tabler-x text-lg" />
          </button>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<style scoped>
.wk-notification {
  animation: wk-notification-shake 0.3s ease-out;
}

@keyframes wk-notification-shake {
  0%, 100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-4px);
  }
  75% {
    transform: translateX(4px);
  }
}
</style>
