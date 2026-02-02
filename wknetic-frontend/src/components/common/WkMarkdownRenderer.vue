<script setup lang="ts">
import { ref, h, defineComponent, watch, resolveComponent } from 'vue'
import { marked } from 'marked'
import 'highlight.js/styles/atom-one-dark.css' // 引入代码高亮样式
import DOMPurify from 'dompurify'

const props = defineProps<{
  content: string
  class?: string
}>()

// 存储代码块数据
const codeBlocks = ref<Array<{ code: string; language?: string }>>([])
const contentWithPlaceholders = ref('')

// 配置 marked
const renderer = new marked.Renderer()

// 自定义代码块渲染 - 使用 Vue 组件标签作为占位符
renderer.code = (code: { text: string; lang?: string; escaped?: boolean; type: 'code' }) => {
  const blockIndex = codeBlocks.value.length
  console.log('Code block detected:', { text: code.text?.substring(0, 50), lang: code.lang, type: code.type })
  codeBlocks.value.push({ code: code.text, language: code.lang })
  // 返回一个特殊标记，我们稍后会替换
  return `<code-block data-index="${blockIndex}"></code-block>`
}

// 自定义blockquote渲染 - 支持特殊标签
renderer.blockquote = ({ text }: { text: string }) => {
  // 检测特殊标签 [!TIP], [!INFO], [!WARNING], [!NOTE], [!DANGER] (不区分大小写)
  const alertMatch = text.match(/^\[!(TIP|INFO|WARNING|NOTE|DANGER)\]\s*\n?/i)
  
  if (alertMatch) {
    const type = alertMatch[1]!.toLowerCase()
    const content = text.replace(alertMatch[0], '')
    const icons: Record<string, string> = {
      tip: 'i-tabler-bulb',
      info: 'i-tabler-info-circle',
      warning: 'i-tabler-alert-triangle',
      note: 'i-tabler-note',
      danger: 'i-tabler-alert-octagon'
    }
    const titles: Record<string, string> = {
      tip: 'Tip',
      info: 'Info',
      warning: 'Warning',
      note: 'Note',
      danger: 'Danger'
    }
    
    return `<blockquote class="alert alert-${type}">
      <div class="alert-title"><span class="alert-icon ${icons[type] || ''}"></span>${titles[type] || type.toUpperCase()}</div>
      <div class="alert-content">${content}</div>
    </blockquote>`
  }
  
  return `<blockquote>${text}</blockquote>`
}

// 自定义链接渲染
renderer.link = ({ href, title, text }: { href: string; title?: string | null; text: string }) => {
  const isExternal = href?.startsWith('http')
  const titleAttr = title ? ` title="${title}"` : ''
  const targetAttr = isExternal ? ' target="_blank" rel="noopener noreferrer"' : ''
  return `<a href="${href}"${titleAttr}${targetAttr}>${text}</a>`
}

// 自定义图片渲染
renderer.image = ({ href, title, text }: { href: string; title?: string | null; text: string }) => {
  const titleAttr = title ? ` title="${title}"` : ''
  return `<figure class="md-image"><img src="${href}" alt="${text}"${titleAttr} loading="lazy" />${text ? `<figcaption>${text}</figcaption>` : ''}</figure>`
}

marked.setOptions({
  renderer,
  gfm: true,
  breaks: true,
})

// 创建动态内容组件
const DynamicMarkdown = defineComponent({
  props: {
    content: String,
    codeBlocks: Array as any,
  },
  setup(props) {
    const copyCode = (code: string, event: Event) => {
      navigator.clipboard.writeText(code)
      const btn = event.target as HTMLElement
      const originalText = btn.textContent
      btn.textContent = '✓ Copied!'
      setTimeout(() => {
        btn.textContent = originalText
      }, 2000)
    }
    
    const renderContent = () => {
      if (!props.content) return []
      
      const parts = props.content.split(/(<code-block[^>]*><\/code-block>)/g)
      
      return parts.map((part, idx) => {
        if (part.match(/<code-block/)) {
          const match = part.match(/data-index="(\d+)"/)
          const index = match ? parseInt(match[1]!, 10) : 0
          const block = props.codeBlocks?.[index]
          
          if (!block) return null
          
          return h('div', { class: 'code-block-wrapper', key: `code-${idx}` }, [
            // 代码块头部（语言标签 + 复制按钮）
            h('div', { class: 'code-block-header' }, [
              h('span', { class: 'code-block-language' }, block.language?.toUpperCase() || 'PLAINTEXT'),
              h('button', {
                class: 'code-block-copy-btn',
                onClick: (e: Event) => copyCode(block.code, e),
              }, 'Copy'),
            ]),
            // 代码块内容
            h('div', { class: 'rendered-code-block' },
              h(resolveComponent('highlightjs'), {
                autodetect: false,
                language: block.language || 'plaintext',
                code: block.code || '',
              })
            ),
          ])
        } else if (part.trim()) {
          const sanitized = DOMPurify.sanitize(part, {
            // ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'u', 's', 'code', 'a', 'ul', 'ol', 'li', 'blockquote', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'table', 'thead', 'tbody', 'tr', 'th', 'td', 'hr','element', 'figure', 'figcaption', 'img'],
            // ALLOWED_ATTR: ['href', 'title', 'target', 'rel', 'src', 'alt', 'loading'],
            FORBID_TAGS: ['script', 'style', 'iframe', 'object', 'embed'],
            FORBID_ATTR: ['onerror', 'onload', 'onclick', 'onmouseover'],
          })
          return h('div', {
            innerHTML: sanitized,
            key: `html-${idx}`,
          })
        }
        return null
      }).filter(Boolean)
    }
    
    return () => h('div', { class: 'markdown-body-content' }, renderContent())
  },
})

// 处理 markdown 解析
watch(
  () => props.content,
  (newContent) => {
    codeBlocks.value = []
    if (!newContent) {
      contentWithPlaceholders.value = ''
      return
    }
    contentWithPlaceholders.value = marked.parse(newContent) as string
  },
  { immediate: true }
)
</script>

<template>
  <div class="markdown-body" :class="props.class">
    <DynamicMarkdown :content="contentWithPlaceholders" :code-blocks="codeBlocks" />
  </div>
</template>


<style>
/* Markdown 渲染样式 */
.markdown-body {
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 0.95rem;
}

.markdown-body-content h1,
.markdown-body-content h2,
.markdown-body-content h3,
.markdown-body-content h4,
.markdown-body-content h5,
.markdown-body-content h6 {
  color: var(--text-default);
  font-weight: 600;
  margin-top: 1em;
  margin-bottom: 0.5em;
  line-height: 1.3;
}

.markdown-body-content h1 { font-size: 1.75rem; border-bottom: 1px solid var(--color-border); padding-bottom: 0.5rem; }
.markdown-body-content h2 { font-size: 1.5rem; border-bottom: 1px solid var(--color-border); padding-bottom: 0.4rem; }
.markdown-body-content h3 { font-size: 1.25rem; }
.markdown-body-content h4 { font-size: 1.1rem; }
.markdown-body-content h5 { font-size: 1rem; }
.markdown-body-content h6 { font-size: 0.9rem; color: var(--color-text-secondary); }

.markdown-body-content p {
  margin-bottom: 1em;
}

.markdown-body-content a {
  color: var(--color-brand);
  text-decoration: none;
  transition: all 0.2s;
}

.markdown-body-content a:hover {
  text-decoration: underline;
}

.markdown-body-content ul,
.markdown-body-content ol {
  margin-bottom: 1em;
  padding-left: 1.5em;
}

.markdown-body-content ul {
  list-style-type: disc;
}

.markdown-body-content ol {
  list-style-type: decimal;
}

.markdown-body-content li {
  margin-bottom: 0.4em;
}

.markdown-body-content li > ul,
.markdown-body-content li > ol {
  margin-top: 0.4em;
  margin-bottom: 0;
}

.markdown-body-content blockquote {
  position: relative;
  border-left: 4px solid var(--color-brand);
  padding: 1em 1.25em 1em 2.5em;
  margin: 1.5em 0;
  background: var(--color-bg-surface);
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  color: var(--color-text-secondary);
  font-style: italic;
}

.markdown-body-content blockquote::before {
  content: '"';
  position: absolute;
  left: 0.75em;
  top: 0.25em;
  font-size: 2.5em;
  line-height: 1;
  color: var(--color-brand);
  opacity: 0.3;
  font-family: Georgia, serif;
}

.markdown-body-content blockquote p:first-child {
  margin-top: 0;
}

.markdown-body-content blockquote p:last-child {
  margin-bottom: 0;
}

.markdown-body-content blockquote cite {
  display: block;
  text-align: right;
  margin-top: 0.5em;
  font-size: 0.875em;
  color: var(--color-text-muted);
  font-style: normal;
}

.markdown-body-content blockquote cite::before {
  content: '— ';
}

/* Alert blockquotes (tip, info, warning, note, danger) */
.markdown-body-content .alert {
  position: relative;
  border-left: 4px solid;
  padding: 1em 1.25em;
  margin: 1.5em 0;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  font-style: normal;
}

.markdown-body-content .alert::before {
  display: none;
}

.markdown-body-content .alert-title {
  font-weight: 600;
  margin-bottom: 0.5em;
  font-size: 1em;
  display: flex;
  align-items: center;
  gap: 0.5em;
}

.markdown-body-content .alert-icon {
  display: inline-block;
  width: 1.25em;
  height: 1.25em;
  flex-shrink: 0;
}

.markdown-body-content .alert-content {
  color: var(--color-text-secondary);
}

.markdown-body-content .alert-content p:first-child {
  margin-top: 0;
}

.markdown-body-content .alert-content p:last-child {
  margin-bottom: 0;
}

/* Alert types */
.markdown-body-content .alert-tip {
  border-left-color: #3b82f6;
  background: rgba(59, 130, 246, 0.1);
}

.markdown-body-content .alert-tip .alert-title {
  color: #3b82f6;
}

.markdown-body-content .alert-tip .alert-icon {
  color: #3b82f6;
}

.markdown-body-content .alert-info {
  border-left-color: #06b6d4;
  background: rgba(6, 182, 212, 0.1);
}

.markdown-body-content .alert-info .alert-title {
  color: #06b6d4;
}

.markdown-body-content .alert-info .alert-icon {
  color: #06b6d4;
}

.markdown-body-content .alert-warning {
  border-left-color: #f59e0b;
  background: rgba(245, 158, 11, 0.1);
}

.markdown-body-content .alert-warning .alert-title {
  color: #f59e0b;
}

.markdown-body-content .alert-warning .alert-icon {
  color: #f59e0b;
}

.markdown-body-content .alert-note {
  border-left-color: #8b5cf6;
  background: rgba(139, 92, 246, 0.1);
}

.markdown-body-content .alert-note .alert-title {
  color: #8b5cf6;
}

.markdown-body-content .alert-note .alert-icon {
  color: #8b5cf6;
}

.markdown-body-content .alert-danger {
  border-left-color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.markdown-body-content .alert-danger .alert-title {
  color: #ef4444;
}

.markdown-body-content .alert-danger .alert-icon {
  color: #ef4444;
}

/* 行内代码样式 */
.markdown-body-content :not(pre) > code {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  padding: 0.2em 0.4em;
  border-radius: var(--radius-sm);
  font-size: 0.85em;
  color: var(--color-brand);
  border: 1px solid var(--color-border);
}


/* 代码块包装器 */
.code-block-wrapper {
  margin: 1.5em 0;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid #292e35;
  background: #282c34;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 代码块头部 */
.code-block-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 1rem;
  background: #21252b;
  border-bottom: 1px solid #27292d;
}

.code-block-language {
  font-size: 0.75rem;
  color: #abb2bf;
  font-weight: 600;
  letter-spacing: 0.05em;
}

.code-block-copy-btn {
  font-size: 0.75rem;
  padding: 0.25rem 0.75rem;
  color: #abb2bf;
  background: transparent;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.code-block-copy-btn:hover {
  background: #2c313a;
  color: #61afef;
}

.code-block-copy-btn:active {
  transform: scale(0.95);
}

/* 代码块内容容器 */
.rendered-code-block {
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace !important;
  background: #282c34;
  font-size: smaller;
}

/* 代码块 pre 元素样式 */
.rendered-code-block :deep(pre) {
  overflow-x: auto;
  padding: 1rem;
  margin: 0;
  background: #282c34 !important;
}


.markdown-body-content table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  margin: 1em 0;
  border: 1px solid #d0d7de;
  border-radius: var(--radius-md);
  overflow: hidden;
}

.markdown-body-content th,
.markdown-body-content td {
  padding: 0.6em 1em;
  border-bottom: 1px solid #d0d7de;
  border-right: 1px solid #d0d7de;
}

.markdown-body-content th:last-child,
.markdown-body-content td:last-child {
  border-right: none;
}

.markdown-body-content tr:last-child td {
  border-bottom: none;
}

.markdown-body-content th {
  background: #f6f8fa;
  font-weight: 600;
  color: #24292f;
}

.markdown-body-content tbody tr:nth-child(even) {
  background: #f6f8fa;
}

.markdown-body-content tbody tr:hover {
  background: #eef2f5;
}

.markdown-body-content tbody tr:hover {
  background: #eef2f5;
}

.markdown-body-content hr {
  border: none;
  height: 1px;
  width: 100%;
  background: var(--border-default);
  margin: 1em 0;
}

.markdown-body-content img {
  max-width: 100%;
  height: auto;
  border-radius: var(--radius-md);
}

.markdown-body-content .md-image {
  margin: 1.5em 0;
  text-align: center;
}

.markdown-body-content .md-image figcaption {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin-top: 0.5em;
  text-align: center;
  display: block;
}

/* 任务列表 */
.markdown-body-content input[type="checkbox"] {
  margin-right: 0.5em;
}

.markdown-body-content li:has(> input[type="checkbox"]) {
  list-style: none;
}
</style>

