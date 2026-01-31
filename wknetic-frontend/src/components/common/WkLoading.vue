<script setup lang="ts">
import { computed } from 'vue'

/**
 * WkLoading - 加载状态组件
 * 
 * @example
 * <WkLoading :loading="true" text="Loading..." />
 * <WkLoading :loading="true" fullscreen />
 */

export type LoadingSize = 'sm' | 'md' | 'lg'

interface Props {
  /** 是否显示加载 */
  loading?: boolean
  /** 加载文本 */
  text?: string
  /** 加载器大小 */
  size?: LoadingSize
  /** 是否全屏 */
  fullscreen?: boolean
  /** 背景色透明度 */
  background?: string
  /** 自定义spinner图标 */
  spinner?: string
}

const props = withDefaults(defineProps<Props>(), {
  loading: true,
  text: 'Loading...',
  size: 'md',
  fullscreen: false,
  background: 'rgba(255, 255, 255, 0.9)'
})

// 计算容器样式类
const containerClasses = computed(() => {
  const classes: string[] = [
    'wk-loading',
    'flex flex-col items-center justify-center gap-3'
  ]

  if (props.background) {
    classes.push('bg-[var(--loading-bg)]')
  } else {
    classes.push('bg-bg-DEFAULT/70')
  }

  if (props.fullscreen) {
    classes.push('fixed inset-0 z-[9999]')
  } else {
    classes.push('absolute inset-0')
  }

  return classes
})

// Spinner大小
const spinnerSizeClass = computed(() => {
  switch (props.size) {
    case 'sm':
      return 'text-2xl'
    case 'lg':
      return 'text-5xl'
    default: // md
      return 'text-4xl'
  }
})

const textSizeClass = computed(() => {
  switch (props.size) {
    case 'sm':
      return 'text-xs'
    case 'lg':
      return 'text-base'
    default: // md
      return 'text-sm'
  }
})

const spinnerIcon = computed(() => {
  return props.spinner || 'i-tabler-loader-2'
})
</script>

<template>
  <Teleport :to="fullscreen ? 'body' : undefined" :disabled="!fullscreen">
    <transition
      enter-active-class="transition-opacity duration-300"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-200"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="loading"
        :class="containerClasses"
      >
        <div class="flex flex-col items-center gap-3">
          <!-- Spinner -->
          <span
            :class="[spinnerIcon, spinnerSizeClass, 'text-[var(--brand-default)] animate-spin']"
          />

          <!-- 加载文本 -->
          <p
            v-if="text || $slots.default"
            :class="[textSizeClass, 'text-[var(--text-secondary)] font-medium']"
          >
            <slot>{{ text }}</slot>
          </p>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<style scoped>
.wk-loading {
  backdrop-filter: blur(2px);
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
