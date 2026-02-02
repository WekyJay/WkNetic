<script setup lang="ts">
import { ref, computed } from 'vue'
import WkMarkdownRenderer from './WkMarkdownRenderer.vue'

const props = withDefaults(defineProps<{
  modelValue: string
  placeholder?: string
  minHeight?: string
  maxHeight?: string
  mode?: 'tabs' | 'split' | 'write-only' | 'preview-only'
}>(), {
  placeholder: 'Write your content here... Markdown is supported!',
  minHeight: '200px',
  maxHeight: '500px',
  mode: 'tabs',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const activeTab = ref<'write' | 'preview'>('write')
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const showWrite = ref(true) // split 模式下控制编辑器显示
const showPreview = ref(true) // split 模式下控制预览显示

const content = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value),
})

// split模式下切换显示
const toggleWrite = () => {
  if (props.mode !== 'split') return
  if (!showWrite.value && !showPreview.value) {
    showWrite.value = true
  } else {
    showWrite.value = !showWrite.value
  }
}

const togglePreview = () => {
  if (props.mode !== 'split') return
  if (!showWrite.value && !showPreview.value) {
    showPreview.value = true
  } else {
    showPreview.value = !showPreview.value
  }
}

// 工具栏按钮
const toolbarButtons = [
  { icon: 'i-tabler-bold', title: 'Bold', prefix: '**', suffix: '**', placeholder: 'bold text' },
  { icon: 'i-tabler-italic', title: 'Italic', prefix: '_', suffix: '_', placeholder: 'italic text' },
  { icon: 'i-tabler-strikethrough', title: 'Strikethrough', prefix: '~~', suffix: '~~', placeholder: 'strikethrough' },
  { icon: 'i-tabler-code', title: 'Inline Code', prefix: '`', suffix: '`', placeholder: 'code' },
  { type: 'divider' },
  { icon: 'i-tabler-h-1', title: 'Heading 1', prefix: '# ', suffix: '', placeholder: 'Heading', newLine: true },
  { icon: 'i-tabler-h-2', title: 'Heading 2', prefix: '## ', suffix: '', placeholder: 'Heading', newLine: true },
  { icon: 'i-tabler-h-3', title: 'Heading 3', prefix: '### ', suffix: '', placeholder: 'Heading', newLine: true },
  { type: 'divider' },
  { icon: 'i-tabler-list', title: 'Unordered List', prefix: '- ', suffix: '', placeholder: 'List item', newLine: true },
  { icon: 'i-tabler-list-numbers', title: 'Ordered List', prefix: '1. ', suffix: '', placeholder: 'List item', newLine: true },
  { icon: 'i-tabler-checkbox', title: 'Task List', prefix: '- [ ] ', suffix: '', placeholder: 'Task item', newLine: true },
  { type: 'divider' },
  { icon: 'i-tabler-quote', title: 'Blockquote', prefix: '> ', suffix: '', placeholder: 'Quote', newLine: true },
  { icon: 'i-tabler-link', title: 'Link', prefix: '[', suffix: '](url)', placeholder: 'link text' },
  { icon: 'i-tabler-photo', title: 'Image', prefix: '![', suffix: '](url)', placeholder: 'alt text' },
  { icon: 'i-tabler-code-dots', title: 'Code Block', prefix: '```\n', suffix: '\n```', placeholder: 'code', newLine: true },
  { type: 'divider' },
  { icon: 'i-tabler-table', title: 'Table', prefix: '| Header 1 | Header 2 |\n| --- | --- |\n| Cell 1 | Cell 2 |', suffix: '', placeholder: '', newLine: true },
  { icon: 'i-tabler-separator', title: 'Horizontal Rule', prefix: '\n---\n', suffix: '', placeholder: '', newLine: true },
]

const insertMarkdown = (button: typeof toolbarButtons[number]) => {
  if (button.type === 'divider' || !textareaRef.value) return
  
  const textarea = textareaRef.value
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = content.value
  const selectedText = text.substring(start, end)
  
  const prefix = button.prefix || ''
  const suffix = button.suffix || ''
  const placeholder = button.placeholder || ''
  
  let insertText = ''
  let newCursorPos = 0
  
  if (selectedText) {
    insertText = prefix + selectedText + suffix
    newCursorPos = start + insertText.length
  } else {
    insertText = prefix + placeholder + suffix
    newCursorPos = start + prefix.length + placeholder.length
  }
  
  // 如果需要新行，检查当前位置
  if (button.newLine && start > 0 && text[start - 1] !== '\n') {
    insertText = '\n' + insertText
    newCursorPos += 1
  }
  
  content.value = text.substring(0, start) + insertText + text.substring(end)
  
  // 设置光标位置
  setTimeout(() => {
    textarea.focus()
    if (selectedText) {
      textarea.setSelectionRange(newCursorPos, newCursorPos)
    } else {
      textarea.setSelectionRange(start + prefix.length + (button.newLine && start > 0 ? 1 : 0), newCursorPos)
    }
  }, 0)
}

// 处理快捷键
const handleKeydown = (e: KeyboardEvent) => {
  if (e.ctrlKey || e.metaKey) {
    switch (e.key) {
      case 'b':
        e.preventDefault()
        insertMarkdown(toolbarButtons[0]!)
        break
      case 'i':
        e.preventDefault()
        insertMarkdown(toolbarButtons[1]!)
        break
      case 'k':
        e.preventDefault()
        insertMarkdown(toolbarButtons.find(b => b.title === 'Link')!)
        break
    }
  }
  
  // Enter 键 - 自动补全 > 用于引用块、列表项
  if (e.key === 'Enter') {
    const textarea = textareaRef.value
    if (!textarea) return
    
    const start = textarea.selectionStart
    const text = content.value
    const lineStart = text.lastIndexOf('\n', start - 1) + 1
    const currentLine = text.substring(lineStart, start)
    
    // 检查当前行是否以 > 开头（可能有空格）- 引用块
    const quoteMatch = currentLine.match(/^>\s*/)
    if (quoteMatch) {
      e.preventDefault()
      const indent = quoteMatch[0]
      content.value = text.substring(0, start) + '\n' + indent + text.substring(start)
      
      setTimeout(() => {
        const newPos = start + indent.length + 1
        textarea.setSelectionRange(newPos, newPos)
      }, 0)
      return
    }
    
    // 检查当前行是否是无序列表项 (-, *, +) 可能有缩进
    const unorderedMatch = currentLine.match(/^(\s*)[-*+]\s+/)
    if (unorderedMatch) {
      e.preventDefault()
      const indent = unorderedMatch[1] || ''
      const prefix = currentLine.match(/^(\s*)[-*+]/)?.[0] || '-'
      content.value = text.substring(0, start) + '\n' + indent + prefix.replace(/\s+$/, '') + ' ' + text.substring(start)
      
      setTimeout(() => {
        const newPos = start + indent.length + prefix.length + 1
        textarea.setSelectionRange(newPos, newPos)
      }, 0)
      return
    }
    
    // 检查当前行是否是有序列表项 (1., 2., 等等)
    const orderedMatch = currentLine.match(/^(\s*)(\d+)\.\s+/)
    if (orderedMatch) {
      e.preventDefault()
      const indent = orderedMatch[1] || ''
      const currentNum = parseInt(orderedMatch[2]!, 10)
      const nextNum = currentNum + 1
      content.value = text.substring(0, start) + '\n' + indent + nextNum + '. ' + text.substring(start)
      
      setTimeout(() => {
        const newPos = start + indent.length + nextNum.toString().length + 2
        textarea.setSelectionRange(newPos, newPos)
      }, 0)
      return
    }
    
    // 检查当前行是否是任务列表项 (- [ ] 或 - [x])
    const checkboxMatch = currentLine.match(/^(\s*)\-\s+\[.\]\s+/)
    if (checkboxMatch) {
      e.preventDefault()
      const indent = checkboxMatch[1] || ''
      content.value = text.substring(0, start) + '\n' + indent + '- [ ] ' + text.substring(start)
      
      setTimeout(() => {
        const newPos = start + indent.length + 6
        textarea.setSelectionRange(newPos, newPos)
      }, 0)
      return
    }
  }
  
  // Tab 键插入空格
  if (e.key === 'Tab') {
    e.preventDefault()
    const textarea = textareaRef.value
    if (!textarea) return
    
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    
    content.value = content.value.substring(0, start) + '  ' + content.value.substring(end)
    
    setTimeout(() => {
      textarea.setSelectionRange(start + 2, start + 2)
    }, 0)
  }
}

// 字数统计
const wordCount = computed(() => {
  const text = content.value.trim()
  if (!text) return { chars: 0, words: 0, lines: 0 }
  
  return {
    chars: text.length,
    words: text.split(/\s+/).filter(Boolean).length,
    lines: text.split('\n').length,
  }
})
</script>

<template>
  <div class="markdown-editor border border-border bg-bg-raised overflow-hidden" :class="{ 'flex h-full': props.mode === 'split' }">
    <!-- tabs 模式：标签切换 -->
    <template v-if="props.mode === 'tabs'">
      <!-- 标签页切换 -->
      <div class="flex items-center justify-between border-b border-border bg-bg-surface">
      <div class="flex">
        <button
          class="px-4 py-2.5 text-sm font-medium transition-colors relative"
          :class="[
            activeTab === 'write'
              ? 'text-brand'
              : 'text-text-secondary hover:text-text'
          ]"
          @click="activeTab = 'write'"
        >
          <span class="flex items-center gap-1.5">
            <span class="i-tabler-pencil text-base" />
            Write
          </span>
          <span 
            v-if="activeTab === 'write'" 
            class="absolute bottom-0 left-0 right-0 h-0.5 bg-brand"
          />
        </button>
        <button
          class="px-4 py-2.5 text-sm font-medium transition-colors relative"
          :class="[
            activeTab === 'preview'
              ? 'text-brand'
              : 'text-text-secondary hover:text-text'
          ]"
          @click="activeTab = 'preview'"
        >
          <span class="flex items-center gap-1.5">
            <span class="i-tabler-eye text-base" />
            Preview
          </span>
          <span 
            v-if="activeTab === 'preview'" 
            class="absolute bottom-0 left-0 right-0 h-0.5 bg-brand"
          />
        </button>
      </div>
      
      <!-- 字数统计 -->
      <div class="px-4 text-xs text-text-muted flex items-center gap-3">
        <span>{{ wordCount.chars }} chars</span>
        <span>{{ wordCount.words }} words</span>
        <span>{{ wordCount.lines }} lines</span>
      </div>
    </div>
    
    <!-- 工具栏 -->
    <div v-show="activeTab === 'write'" class="flex flex-wrap items-center gap-0.5 p-2 border-b border-border bg-bg-surface/50">
      <template v-for="(button, index) in toolbarButtons" :key="index">
        <div v-if="button.type === 'divider'" class="w-px h-5 bg-border mx-1" />
        <button
          v-else
          class="p-1.5 rounded-md text-text-secondary hover:text-text hover:bg-bg-hover transition-colors"
          :title="button.title"
          @click="insertMarkdown(button)"
        >
          <span :class="button.icon" class="text-lg" />
        </button>
      </template>
    </div>
    
    <!-- 编辑区域 -->
    <div v-show="activeTab === 'write'" class="relative">
      <textarea
        ref="textareaRef"
        v-model="content"
        class="w-full p-4 bg-transparent text-text resize-y outline-none font-mono text-sm leading-relaxed placeholder:text-text-muted"
        :style="{ minHeight: minHeight, maxHeight: maxHeight }"
        :placeholder="placeholder"
        @keydown="handleKeydown"
      />
    </div>
    
    <!-- 预览区域 -->
    <div 
      v-show="activeTab === 'preview'" 
      class="p-4 overflow-auto"
      :style="{ minHeight: minHeight, maxHeight: maxHeight }"
    >
      <WkMarkdownRenderer 
        v-if="content.trim()" 
        :content="content" 
      />
      <div v-else class="h-full flex flex-col items-center justify-center text-text-muted">
        <span class="i-tabler-markdown text-4xl mt-5 mb-2 opacity-50" />
        <p class="text-sm">Nothing to preview yet...</p>
      </div>
    </div>
    
    <!-- 快捷键提示 -->
    <div class="px-4 py-2 border-t border-border bg-bg-surface/30 text-xs text-text-muted flex items-center gap-4">
      <span><kbd class="kbd">Ctrl</kbd> + <kbd class="kbd">B</kbd> Bold</span>
      <span><kbd class="kbd">Ctrl</kbd> + <kbd class="kbd">I</kbd> Italic</span>
      <span><kbd class="kbd">Ctrl</kbd> + <kbd class="kbd">K</kbd> Link</span>
      <span><kbd class="kbd">Tab</kbd> Indent</span>
    </div>
    </template>

    <!-- split 模式：左编辑右预览 -->
    <template v-else-if="props.mode === 'split'">
      <!-- 左侧编辑器 -->
      <div v-show="showWrite" class="flex-1 flex flex-col border-r border-border min-w-0">
        <!-- 编辑器头部 -->
        <div class="flex items-center justify-between border-b border-border bg-bg-surface px-4 py-2 flex-shrink-0">
          <div class="flex items-center gap-2 text-sm font-medium text-text">
            <span class="i-tabler-pencil text-base" />
            Write
          </div>
          <div class="flex items-center gap-2">
            <div class="text-xs text-text-muted flex items-center gap-3">
              <span>{{ wordCount.chars }} chars</span>
              <span>{{ wordCount.words }} words</span>
            </div>
            <button 
              v-if="showPreview"
              class="p-1.5 rounded-md text-text-secondary hover:text-text hover:bg-bg-hover transition-colors"
              title="Hide editor"
              @click="toggleWrite"
            >
              <span class="i-tabler-layout-sidebar-left-collapse text-lg" />
            </button>
          </div>
        </div>
        
        <!-- 工具栏 -->
        <div class="flex flex-wrap items-center gap-0.5 p-2 border-b border-border bg-bg-surface/50 flex-shrink-0">
          <template v-for="(button, index) in toolbarButtons" :key="index">
            <div v-if="button.type === 'divider'" class="w-px h-5 bg-border mx-1" />
            <button
              v-else
              class="p-1.5 rounded-md text-text-secondary hover:text-text hover:bg-bg-hover transition-colors"
              :title="button.title"
              @click="insertMarkdown(button)"
            >
              <span :class="button.icon" class="text-lg" />
            </button>
          </template>
        </div>
        
        <!-- 编辑区 -->
        <textarea
          ref="textareaRef"
          v-model="content"
          class="flex-1 p-4 bg-transparent text-text resize-none outline-none font-mono text-sm leading-relaxed placeholder:text-text-muted overflow-auto"
          :placeholder="placeholder"
          @keydown="handleKeydown"
        />
        
        <!-- 快捷键提示 -->
        <div class="px-4 py-2 border-t border-border bg-bg-surface/30 text-xs text-text-muted flex items-center gap-4 flex-shrink-0">
          <span><kbd class="kbd">Ctrl</kbd> + <kbd class="kbd">B</kbd> Bold</span>
          <span><kbd class="kbd">Ctrl</kbd> + <kbd class="kbd">I</kbd> Italic</span>
          <span><kbd class="kbd">Ctrl</kbd> + <kbd class="kbd">K</kbd> Link</span>
        </div>
      </div>

      <!-- 右侧预览 -->
      <div v-show="showPreview" class="flex-1 flex flex-col bg-bg-surface min-w-0">
        <!-- 预览头部 -->
        <div class="flex items-center justify-between border-b border-border bg-bg-surface px-4 py-2 flex-shrink-0">
          <div class="flex items-center gap-2 text-sm font-medium text-text">
            <span class="i-tabler-eye text-base" />
            Preview
          </div>
          <button 
            v-if="showWrite"
            class="p-1.5 rounded-md text-text-secondary hover:text-text hover:bg-bg-hover transition-colors"
            title="Hide preview"
            @click="togglePreview"
          >
            <span class="i-tabler-layout-sidebar-right-collapse text-lg" />
          </button>
        </div>
        
        <!-- 预览内容 -->
        <div class="flex-1 overflow-auto p-8">
          <WkMarkdownRenderer 
            v-if="content.trim()" 
            :content="content" 
          />
          <div v-else class="h-full flex flex-col items-center justify-center text-text-muted">
            <span class="i-tabler-markdown text-4xl mb-2 opacity-50" />
            <p class="text-sm">Nothing to preview yet...</p>
          </div>
        </div>
      </div>

      <!-- 恢复按钮（当编辑器或预览被隐藏时） -->
      <div v-if="!showWrite" class="flex items-center border-r border-border bg-bg-surface px-2">
        <button 
          class="p-2 rounded-md text-text-secondary hover:text-text hover:bg-bg-hover transition-colors"
          title="Show editor"
          @click="toggleWrite"
        >
          <span class="i-tabler-layout-sidebar-left-expand text-xl" />
        </button>
      </div>
      <div v-if="!showPreview" class="flex items-center bg-bg-surface px-2">
        <button 
          class="p-2 rounded-md text-text-secondary hover:text-text hover:bg-bg-hover transition-colors"
          title="Show preview"
          @click="togglePreview"
        >
          <span class="i-tabler-layout-sidebar-right-expand text-xl" />
        </button>
      </div>
    </template>

    <!-- write-only 和 preview-only模式保持原样 -->
  </div>
</template>

<style scoped>
.kbd {
  display: inline-block;
  padding: 0.15em 0.4em;
  font-size: 0.75rem;
  font-family: inherit;
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: 0.25rem;
  box-shadow: 0 1px 0 var(--color-border);
}

textarea::-webkit-scrollbar {
  width: 8px;
}

textarea::-webkit-scrollbar-track {
  background: transparent;
}

textarea::-webkit-scrollbar-thumb {
  background: var(--color-border);
  border-radius: 4px;
}

textarea::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-muted);
}
</style>
