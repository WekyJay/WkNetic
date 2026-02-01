<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import UserCard from './UserCard.vue'

interface Props {
  /** 用户 ID */
  userId: number
  /** 触发方式 */
  trigger?: 'hover' | 'click'
  /** hover 延迟（毫秒） */
  delay?: number
  /** popover 位置 */
  placement?: 'top' | 'bottom' | 'left' | 'right'
}

const props = withDefaults(defineProps<Props>(), {
  trigger: 'hover',
  delay: 500,
  placement: 'bottom'
})

const visible = ref(false)
const hoverTimer = ref<number | null>(null)
const hideTimer = ref<number | null>(null)
const wrapperRef = ref<HTMLElement | null>(null)
const popoverStyle = ref({
  top: '-9999px',
  left: '-9999px'
})

/** 计算 popover 位置（同步计算，不依赖 visible 状态） */
const calculatePosition = () => {
  if (!wrapperRef.value) return

  const wrapper = wrapperRef.value
  const rect = wrapper.getBoundingClientRect()
  const popoverWidth = 320 // w-80 = 320px
  const popoverHeight = 300 // 估计高度

  const spacing = 8 // mt-2, mb-2 等对应的像素

  let top = 0
  let left = 0

  switch (props.placement) {
    case 'bottom':
      top = rect.bottom + spacing
      left = rect.left + rect.width / 2 - popoverWidth / 2
      break
    case 'top':
      top = rect.top - popoverHeight - spacing
      left = rect.left + rect.width / 2 - popoverWidth / 2
      break
    case 'left':
      top = rect.top + rect.height / 2 - popoverHeight / 2
      left = rect.left - popoverWidth - spacing
      break
    case 'right':
      top = rect.top + rect.height / 2 - popoverHeight / 2
      left = rect.right + spacing
      break
  }

  // 防止超出窗口边界
  left = Math.max(8, Math.min(left, window.innerWidth - popoverWidth - 8))
  top = Math.max(8, top)

  popoverStyle.value = {
    top: `${top}px`,
    left: `${left}px`
  }
}

/** 显示 popover */
const show = () => {
  // 清除隐藏定时器
  if (hideTimer.value) {
    clearTimeout(hideTimer.value)
    hideTimer.value = null
  }
  
  // 如果已经显示，不需要重新处理
  if (visible.value) return
  
  if (props.trigger === 'hover') {
    if (hoverTimer.value) clearTimeout(hoverTimer.value)
    hoverTimer.value = window.setTimeout(() => {
      // 先计算位置，再显示
      calculatePosition()
      visible.value = true
    }, props.delay)
  } else {
    calculatePosition()
    visible.value = true
  }
}

/** 隐藏 popover */
const hide = () => {
  if (hoverTimer.value) {
    clearTimeout(hoverTimer.value)
    hoverTimer.value = null
  }
  
  // 添加延迟隐藏，给用户时间移动到 popover 上
  if (hideTimer.value) clearTimeout(hideTimer.value)
  hideTimer.value = window.setTimeout(() => {
    visible.value = false
  }, 100)
}

/** 处理点击 */
const handleClick = () => {
  if (props.trigger === 'click') {
    visible.value = !visible.value
    if (visible.value) {
      calculatePosition()
    }
  }
}

/** 监听窗口大小变化 */
const handleResize = () => {
  if (visible.value) {
    calculatePosition()
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  window.addEventListener('scroll', handleResize, true)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('scroll', handleResize, true)
  if (hoverTimer.value) clearTimeout(hoverTimer.value)
  if (hideTimer.value) clearTimeout(hideTimer.value)
})
</script>

<template>
  <div
    ref="wrapperRef"
    class="user-popover-wrapper inline-block"
    @mouseenter="trigger === 'hover' ? show() : null"
    @mouseleave="trigger === 'hover' ? hide() : null"
    @click="handleClick"
  >
    <!-- 触发元素插槽 -->
    <slot />
    
    <!-- Popover 内容（使用 fixed 定位以突破 overflow:hidden） -->
    <Transition name="popover">
      <div
        v-if="visible"
        :class="[
          'user-popover-content',
          'fixed z-50 w-80'
        ]"
        :style="popoverStyle"
        @mouseenter="trigger === 'hover' ? show() : null"
        @mouseleave="trigger === 'hover' ? hide() : null"
      >
        <UserCard
          :user-id="userId"
          :detailed="true"
          :clickable="false"
        />
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.user-popover-wrapper {
  position: relative;
}

.user-popover-content {
  filter: drop-shadow(0 10px 25px rgba(0, 0, 0, 0.15));
}

/* Popover 动画 */
.popover-enter-active,
.popover-leave-active {
  transition: all 0.2s ease;
}

.popover-enter-from,
.popover-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.popover-enter-to,
.popover-leave-from {
  opacity: 1;
  transform: translateY(0);
}
</style>
