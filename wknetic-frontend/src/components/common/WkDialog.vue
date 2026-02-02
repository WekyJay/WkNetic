<script setup lang="ts">
import { computed, watch } from 'vue'

/**
 * WkDialog - 通用对话框组件
 * 
 * @example
 * <WkDialog v-model="visible" title="Dialog Title">
 *   <p>Dialog content</p>
 * </WkDialog>
 */

export type DialogSize = 'sm' | 'md' | 'lg' | 'xl' | 'full'

interface Props {
  /** 对话框标题 */
  title?: string
  /** 对话框宽度 */
  width?: string
  /** 对话框大小预设 */
  size?: DialogSize
  /** 是否显示关闭按钮 */
  closable?: boolean
  /** 是否显示遮罩层 */
  modal?: boolean
  /** 点击遮罩层是否关闭 */
  closeOnClickModal?: boolean
  /** 按ESC是否关闭 */
  closeOnPressEscape?: boolean
  /** 是否显示底部 */
  showFooter?: boolean
  /** 是否居中显示 */
  center?: boolean
  /** 是否全屏 */
  fullscreen?: boolean
  /** 是否追加到body */
  appendToBody?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  closable: true,
  modal: true,
  closeOnClickModal: true,
  closeOnPressEscape: true,
  showFooter: true,
  center: false,
  fullscreen: false,
  appendToBody: true
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'open': []
  'opened': []
  'close': []
  'closed': []
}>()

const visible = defineModel<boolean>({ default: false })

// 处理关闭
const handleClose = () => {
  emit('close')
  visible.value = false
}

// 处理遮罩点击
const handleMaskClick = () => {
  if (props.closeOnClickModal) {
    handleClose()
  }
}

// ESC键关闭
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && props.closeOnPressEscape && visible.value) {
    handleClose()
  }
}

// 监听visible变化
watch(visible, (val) => {
  if (val) {
    emit('open')
    document.addEventListener('keydown', handleKeydown)
    // 防止body滚动
    document.body.style.overflow = 'hidden'
  } else {
    emit('closed')
    document.removeEventListener('keydown', handleKeydown)
    document.body.style.overflow = ''
  }
})

// 计算对话框样式
const dialogClasses = computed(() => {
  const classes: string[] = [
    'wk-dialog',
    'bg-[var(--bg-raised)] rounded-xl shadow-2xl',
    'flex flex-col max-h-[90vh]'
  ]

  if (props.fullscreen) {
    classes.push('w-full h-full max-h-full rounded-none')
  } else {
    // 尺寸
    if (!props.width) {
      switch (props.size) {
        case 'sm':
          classes.push('w-full max-w-md')
          break
        case 'lg':
          classes.push('w-full max-w-3xl')
          break
        case 'xl':
          classes.push('w-full max-w-5xl')
          break
        case 'full':
          classes.push('w-[95vw]')
          break
        default: // md
          classes.push('w-full max-w-xl')
      }
    }
  }

  return classes
})

const dialogStyle = computed(() => {
  const style: Record<string, string> = {}
  if (props.width && !props.fullscreen) {
    style.width = props.width
  }
  return style
})
</script>

<template>
  <Teleport to="body" :disabled="!appendToBody">
    <transition
      enter-active-class="transition-all duration-300"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-all duration-200"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
        <div
          v-if="visible"
          class="wk-dialog-overlay fixed inset-0 z-[9999] flex items-center justify-center p-4"
          :class="{ 'bg-black/50': modal, 'p-0': fullscreen }"
          @click.self="handleMaskClick"
        >
        <transition
          enter-active-class="transition-all duration-300"
          enter-from-class="opacity-0 scale-95 -translate-y-4"
          enter-to-class="opacity-100 scale-100 translate-y-0"
          leave-active-class="transition-all duration-200"
          leave-from-class="opacity-100 scale-100 translate-y-0"
          leave-to-class="opacity-0 scale-95 -translate-y-4"
        >
          <div
            v-if="visible"
            :class="dialogClasses"
            :style="dialogStyle"
            role="dialog"
            aria-modal="true"
          >
            <!-- 头部 -->
            <div
              v-if="title || $slots.header || closable"
              class="flex items-center justify-between px-6 py-4 border-b border-[var(--border-default)]"
              :class="{ 'justify-center': center && !closable }"
            >
              <slot name="header">
                <h3
                  class="text-lg font-semibold text-[var(--text-default)]"
                  :class="{ 'text-center': center }"
                >
                  {{ title }}
                </h3>
              </slot>

              <button
                v-if="closable"
                type="button"
                class="flex-shrink-0 ml-4 text-[var(--text-secondary)] hover:text-[var(--text-default)] transition-colors bg-transparent  focus:outline-none"
                @click="handleClose"
              >
                <span class="i-tabler-x text-xl" />
              </button>
            </div>

            <!-- 内容 -->
            <div
              class="flex-1 overflow-auto px-6 py-4"
              :class="{ 'text-center': center }"
            >
              <slot />
            </div>

            <!-- 底部 -->
            <div
              v-if="showFooter && $slots.footer"
              class="flex items-center px-6 py-4 border-t border-[var(--border-default)]"
              :class="center ? 'justify-center' : 'justify-end'"
            >
              <slot name="footer" />
            </div>
          </div>
        </transition>
      </div>
    </transition>
  </Teleport>
</template>

<style scoped>
.wk-dialog-overlay {
  backdrop-filter: blur(2px);
}

.wk-dialog {
  position: relative;
  animation: wk-dialog-appear 0.3s ease-out;
}

@keyframes wk-dialog-appear {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(-16px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
</style>
