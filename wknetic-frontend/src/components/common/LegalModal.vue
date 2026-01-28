<script setup lang="ts">
import { computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { LegalContent } from '@/data/legal'

const props = defineProps<{
  isOpen: boolean
  content: {
    en: LegalContent
    zh: LegalContent
  }
}>()

const emit = defineEmits<{
  close: []
}>()

const { locale } = useI18n()

// 根据当前语言选择内容
const currentContent = computed(() => {
  const lang = locale.value as 'en' | 'zh'
  return props.content[lang] || props.content.en
})

// 监听弹窗打开/关闭，处理body滚动
watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

function handleClose() {
  emit('close')
}

function handleBackdropClick(e: MouseEvent) {
  if (e.target === e.currentTarget) {
    handleClose()
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    handleClose()
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="isOpen"
        class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4"
        @click="handleBackdropClick"
        @keydown="handleKeydown"
      >
        <Transition
          enter-active-class="transition-all duration-200"
          enter-from-class="opacity-0 scale-95"
          enter-to-class="opacity-100 scale-100"
          leave-active-class="transition-all duration-150"
          leave-from-class="opacity-100 scale-100"
          leave-to-class="opacity-0 scale-95"
        >
          <div
            v-if="isOpen"
            class="bg-bg-raised border border-border rounded-xl shadow-2xl w-full max-w-3xl max-h-[85vh] flex flex-col"
            @click.stop
          >
            <!-- 头部 -->
            <div class="flex items-center justify-between px-6 py-4 border-b border-border">
              <div>
                <h2 class="text-xl font-bold text-text">{{ currentContent.title }}</h2>
                <p class="text-sm text-text-muted mt-1">{{ currentContent.lastUpdated }}</p>
              </div>
              <button
                class="p-2 rounded-lg text-text-secondary hover:bg-bg-hover hover:text-text transition-colors"
                @click="handleClose"
              >
                <span class="i-tabler-x text-xl" />
              </button>
            </div>

            <!-- 内容区域 -->
            <div class="flex-1 overflow-y-auto px-6 py-6">
              <div class="space-y-6">
                <div
                  v-for="(section, index) in currentContent.sections"
                  :key="index"
                  class="space-y-3"
                >
                  <h3 class="text-lg font-semibold text-text">{{ section.title }}</h3>
                  <div class="space-y-2">
                    <p
                      v-for="(paragraph, pIndex) in section.content"
                      :key="pIndex"
                      class="text-text-secondary leading-relaxed"
                    >
                      {{ paragraph }}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 底部 -->
            <div class="px-6 py-4 border-t border-border flex justify-end">
              <button
                class="btn-primary"
                @click="handleClose"
              >
                <span class="i-tabler-check" />
                Close
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* 自定义滚动条 */
.overflow-y-auto::-webkit-scrollbar {
  width: 8px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: var(--bg-surface);
  border-radius: 4px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: var(--border-default);
  border-radius: 4px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: var(--text-muted);
}
</style>
