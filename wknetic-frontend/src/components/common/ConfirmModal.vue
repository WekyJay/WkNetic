<template>
  <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center">
    <!-- 背景遮罩 -->
    <div 
      class="absolute inset-0 bg-black bg-opacity-50"
      @click="handleCancel"
    ></div>
    
    <!-- 确认框 -->
    <div class="relative bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-md w-full mx-4 p-6">
      <!-- 图标 -->
      <div v-if="type" class="flex items-center justify-center mb-4">
        <div 
          :class="[
            'w-12 h-12 rounded-full flex items-center justify-center',
            iconColorClass
          ]"
        >
          <i :class="[iconClass, 'text-2xl']"></i>
        </div>
      </div>
      
      <!-- 标题 -->
      <h3 
        class="text-lg font-semibold text-gray-900 dark:text-white text-center mb-2"
        v-if="title"
      >
        {{ title }}
      </h3>
      
      <!-- 内容 -->
      <div 
        class="text-gray-600 dark:text-gray-300 text-center mb-6"
        v-if="content || $slots.default"
      >
        <slot>{{ content }}</slot>
      </div>
      
      <!-- 按钮组 -->
      <div class="flex gap-3" :class="buttonAlignClass">
        <!-- 取消按钮 -->
        <button
          v-if="showCancel"
          @click="handleCancel"
          class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
          :disabled="loading"
        >
          {{ cancelText }}
        </button>
        
        <!-- 确认按钮 -->
        <button
          v-if="showConfirm"
          @click="handleConfirm"
          :class="[
            'flex-1 px-4 py-2 rounded-lg text-white transition-colors',
            confirmButtonClass,
            loading ? 'opacity-50 cursor-not-allowed' : ''
          ]"
          :disabled="loading"
        >
          <span v-if="loading" class="i-tabler-loader-2 animate-spin mr-2"></span>
          {{ confirmText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface ConfirmModalProps {
  /** 是否显示 */
  visible?: boolean
  /** 标题 */
  title?: string
  /** 内容 */
  content?: string
  /** 按钮类型：cancel-only(仅取消), confirm-only(仅确认), both(都显示) */
  buttonType?: 'cancel-only' | 'confirm-only' | 'both'
  /** 确认按钮文本 */
  confirmText?: string
  /** 取消按钮文本 */
  cancelText?: string
  /** 确认框类型：warning(警告), danger(危险), info(信息), success(成功) */
  type?: 'warning' | 'danger' | 'info' | 'success'
  /** 是否加载中 */
  loading?: boolean
}

const props = withDefaults(defineProps<ConfirmModalProps>(), {
  visible: false,
  title: '确认操作',
  content: '',
  buttonType: 'both',
  confirmText: '确认',
  cancelText: '取消',
  type: undefined,
  loading: false
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'confirm': []
  'cancel': []
}>()

// 是否显示取消按钮
const showCancel = computed(() => {
  return props.buttonType === 'cancel-only' || props.buttonType === 'both'
})

// 是否显示确认按钮
const showConfirm = computed(() => {
  return props.buttonType === 'confirm-only' || props.buttonType === 'both'
})

// 按钮对齐方式
const buttonAlignClass = computed(() => {
  if (props.buttonType === 'both') return 'justify-center'
  return 'justify-center'
})

// 图标类名
const iconClass = computed(() => {
  switch (props.type) {
    case 'warning':
      return 'i-tabler-alert-triangle'
    case 'danger':
      return 'i-tabler-alert-circle'
    case 'info':
      return 'i-tabler-info-circle'
    case 'success':
      return 'i-tabler-circle-check'
    default:
      return 'i-tabler-help-circle'
  }
})

// 图标颜色类名
const iconColorClass = computed(() => {
  switch (props.type) {
    case 'warning':
      return 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/30 dark:text-yellow-400'
    case 'danger':
      return 'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400'
    case 'info':
      return 'bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400'
    case 'success':
      return 'bg-green-100 text-green-600 dark:bg-green-900/30 dark:text-green-400'
    default:
      return 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400'
  }
})

// 确认按钮样式
const confirmButtonClass = computed(() => {
  switch (props.type) {
    case 'danger':
      return 'bg-red-600 hover:bg-red-700'
    case 'warning':
      return 'bg-yellow-600 hover:bg-yellow-700'
    case 'success':
      return 'bg-green-600 hover:bg-green-700'
    default:
      return 'bg-blue-600 hover:bg-blue-700'
  }
})

// 处理确认
const handleConfirm = () => {
  if (props.loading) return
  emit('confirm')
}

// 处理取消
const handleCancel = () => {
  if (props.loading) return
  emit('update:visible', false)
  emit('cancel')
}
</script>

<style scoped>
/* 添加动画效果 */
.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
