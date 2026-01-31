<script setup lang="ts">
import { computed } from 'vue'
/**
 * WkCard - 通用卡片容器组件
 * 
 * @example
 * <WkCard title="Card Title">Card content</WkCard>
 * <WkCard :hoverable="true" :shadow="'lg'">
 *   <template #header>Custom header</template>
 *   Content
 * </WkCard>
 */

export type CardShadow = 'none' | 'sm' | 'md' | 'lg'
export type CardPadding = 'none' | 'sm' | 'md' | 'lg'

interface Props {
  /** 卡片标题 */
  title?: string
  /** 阴影大小 */
  shadow?: CardShadow
  /** 内边距大小 */
  padding?: CardPadding
  /** 是否可悬停（hover效果） */
  hoverable?: boolean
  /** 是否显示边框 */
  bordered?: boolean
  /** 是否加载中 */
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  shadow: 'sm',
  padding: 'md',
  hoverable: false,
  bordered: true,
  loading: false
})

const cardClasses = computed(() => {
  const classes: string[] = [
    'wk-card',
    'bg-[var(--bg-raised)] rounded-lg transition-all duration-200'
  ]

  // 阴影
  switch (props.shadow) {
    case 'none':
      break
    case 'sm':
      classes.push('shadow-sm')
      break
    case 'md':
      classes.push('shadow-md')
      break
    case 'lg':
      classes.push('shadow-lg')
      break
  }

  // 边框
  if (props.bordered) {
    classes.push('border border-[var(--border-default)]')
  }

  // 悬停效果
  if (props.hoverable) {
    classes.push('hover:shadow-md hover:translate-y-[-2px] cursor-pointer')
  }

  return classes
})

const contentClasses = computed(() => {
  const classes: string[] = []

  // 内边距
  switch (props.padding) {
    case 'none':
      break
    case 'sm':
      classes.push('p-3')
      break
    case 'md':
      classes.push('p-4')
      break
    case 'lg':
      classes.push('p-6')
      break
  }

  return classes
})
</script>

<template>
  <div :class="cardClasses">
    <!-- 加载状态 -->
    <div 
      v-if="loading" 
      class="flex items-center justify-center py-12"
      :class="contentClasses"
    >
      <div class="flex flex-col items-center gap-3">
        <span class="i-tabler-loader-2 animate-spin text-3xl text-[var(--brand-default)]" />
        <span class="text-sm text-[var(--text-muted)]">Loading...</span>
      </div>
    </div>

    <template v-else>
      <!-- 头部 -->
      <div 
        v-if="$slots.header || title"
        class="border-b border-[var(--border-default)]"
        :class="contentClasses"
      >
        <slot name="header">
          <h3 class="text-lg font-semibold text-[var(--text-default)]">{{ title }}</h3>
        </slot>
      </div>

      <!-- 内容区域 -->
      <div :class="contentClasses">
        <slot />
      </div>

      <!-- 底部 -->
      <div 
        v-if="$slots.footer"
        class="border-t border-[var(--border-default)]"
        :class="contentClasses"
      >
        <slot name="footer" />
      </div>
    </template>
  </div>
</template>

<style scoped>
.wk-card {
  position: relative;
  overflow: hidden;
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
