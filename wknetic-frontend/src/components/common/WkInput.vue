<script setup lang="ts">
import { ref, computed } from 'vue'
/**
 * WkInput - 统一的输入框组件
 * 
 * @example
 * <WkInput v-model="username" placeholder="Enter username" />
 * <WkInput v-model="password" type="password" prefix-icon="i-tabler-lock" />
 * <WkInput v-model="email" :error="emailError" suffix-icon="i-tabler-mail" />
 */

export type InputType = 'text' | 'password' | 'email' | 'number' | 'tel' | 'url' | 'search'
export type InputSize = 'sm' | 'md' | 'lg'

interface Props {
  /** v-model 绑定值 */
  modelValue?: string | number
  /** 输入框类型 */
  type?: InputType
  /** 占位文本 */
  placeholder?: string
  /** 是否禁用 */
  disabled?: boolean
  /** 是否只读 */
  readonly?: boolean
  /** 输入框大小 */
  size?: InputSize
  /** 错误信息 */
  error?: string
  /** 前缀图标 */
  prefixIcon?: string
  /** 后缀图标 */
  suffixIcon?: string
  /** 是否显示清空按钮 */
  clearable?: boolean
  /** 最大长度 */
  maxlength?: number
  /** 是否显示字符计数 */
  showCount?: boolean
  /** 自动聚焦 */
  autofocus?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  size: 'md',
  clearable: false,
  showCount: false,
  autofocus: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  'focus': [event: FocusEvent]
  'blur': [event: FocusEvent]
  'enter': [event: KeyboardEvent]
  'clear': []
}>()

const inputRef = ref<HTMLInputElement>()
const isFocused = ref(false)

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

const handleFocus = (event: FocusEvent) => {
  isFocused.value = true
  emit('focus', event)
}

const handleBlur = (event: FocusEvent) => {
  isFocused.value = false
  emit('blur', event)
}

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter') {
    emit('enter', event)
  }
}

const handleClear = () => {
  emit('update:modelValue', '')
  emit('clear')
  inputRef.value?.focus()
}

// 计算样式类
const containerClasses = computed(() => {
  const classes: string[] = [
    'wk-input-container',
    'relative flex items-center gap-2',
    'border rounded-lg transition-all duration-200',
    'bg-[var(--bg-raised)]'
  ]

  // 尺寸
  switch (props.size) {
    case 'sm':
      classes.push('text-sm px-2 py-1')
      break
    case 'lg':
      classes.push('text-lg px-4 py-3')
      break
    default: // md
      classes.push('text-base px-3 py-2')
  }

  // 状态
  if (props.error) {
    classes.push('border-red-500 focus-within:ring-2 focus-within:ring-red-200')
  } else if (isFocused.value) {
    classes.push('border-[var(--brand-default)] ring-2 ring-[var(--brand-default)]/20')
  } else {
    classes.push('border-[var(--border-default)] hover:border-[var(--brand-default)]')
  }

  if (props.disabled) {
    classes.push('opacity-50 cursor-not-allowed bg-[var(--bg-surface)]')
  }

  return classes
})

const inputClasses = computed(() => {
  return [
    'flex-1 bg-transparent outline-none',
    'text-[var(--text-default)] placeholder:text-[var(--text-muted)]',
    'disabled:cursor-not-allowed'
  ]
})

const iconClasses = computed(() => {
  const size = props.size === 'sm' ? 'text-base' : props.size === 'lg' ? 'text-xl' : 'text-lg'
  return [size, 'text-[var(--text-secondary)]']
})

// 字符计数
const characterCount = computed(() => {
  const value = String(props.modelValue || '')
  return `${value.length}${props.maxlength ? `/${props.maxlength}` : ''}`
})

const showClearButton = computed(() => {
  return props.clearable && props.modelValue && !props.disabled && !props.readonly
})

// 暴露聚焦方法
defineExpose({
  focus: () => inputRef.value?.focus(),
  blur: () => inputRef.value?.blur()
})
</script>

<template>
  <div class="wk-input-wrapper">
    <div :class="containerClasses">
      <!-- 前缀图标 -->
      <span v-if="prefixIcon" :class="[prefixIcon, iconClasses]" />
      
      <!-- 前缀插槽 -->
      <slot name="prefix" />
      
      <!-- 输入框 -->
      <input
        ref="inputRef"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :maxlength="maxlength"
        :autofocus="autofocus"
        :class="inputClasses"
        @input="handleInput"
        @focus="handleFocus"
        @blur="handleBlur"
        @keydown="handleKeydown"
      />
      
      <!-- 字符计数 -->
      <span v-if="showCount" class="text-xs text-[var(--text-muted)] whitespace-nowrap">
        {{ characterCount }}
      </span>
      
      <!-- 清空按钮 -->
      <button
        v-if="showClearButton"
        type="button"
        class="i-tabler-x cursor-pointer text-[var(--text-muted)] hover:text-[var(--text-default)] transition-colors"
        :class="iconClasses"
        @click="handleClear"
      />
      
      <!-- 后缀插槽 -->
      <slot name="suffix" />
      
      <!-- 后缀图标 -->
      <span v-if="suffixIcon" :class="[suffixIcon, iconClasses]" />
    </div>
    
    <!-- 错误信息 -->
    <transition
      enter-active-class="transition-all duration-200"
      enter-from-class="opacity-0 -translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition-all duration-150"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 -translate-y-1"
    >
      <p v-if="error" class="mt-1 text-sm text-red-500 flex items-center gap-1">
        <span class="i-tabler-alert-circle" />
        {{ error }}
      </p>
    </transition>
    
    <!-- 提示文本插槽 -->
    <slot name="hint" />
  </div>
</template>

<style scoped>
.wk-input-container {
  min-height: 2.5rem;
}

input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

input[type="number"] {
  -moz-appearance: textfield;
  appearance: textfield;
}
</style>
