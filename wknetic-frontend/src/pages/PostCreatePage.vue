<template>
  <!-- 全屏编辑器布局 -->
  <div class="fixed inset-0 z-50 bg-bg flex flex-col overflow-hidden">
    <!-- 顶部区域：标题、板块、标签等设置 -->
    <div class="flex-shrink-0 border-b border-border bg-bg-surface sticky top-0 z-20">
      <div class="w-full px-6 py-4">
        <!-- 操作栏 -->
        <div class="flex-between mb-4">
          <div class="flex items-center gap-3">
            <button class="btn-ghost" @click="goBack">
              <span class="i-tabler-arrow-left" />
              Back
            </button>
            <h1 class="text-xl font-bold text-text">
              {{ isEdit ? 'Edit Post' : 'Create New Post' }}
            </h1>
          </div>
          <div class="flex gap-3">
            <button class="btn-secondary" @click="saveDraft" :disabled="saving">
              <span v-if="saving" class="i-tabler-loader-2 animate-spin" />
              <span v-else class="i-tabler-bookmark" />
              {{ saving ? 'Saving...' : 'Save Draft' }}
            </button>
            <button class="btn-primary" @click="openPublishDialog" :disabled="publishing">
              <span v-if="publishing" class="i-tabler-loader-2 animate-spin" />
              <span v-else class="i-tabler-send" />
              {{ publishing ? 'Publishing...' : 'Publish' }}
            </button>
          </div>
        </div>

        <!-- 表单设置 -->
        <div class="grid grid-cols-12 gap-4 items-start text-left">
          <!-- 标题 -->
          <div class="col-span-12">
            <input
              v-model="form.title"
              type="text"
              class="w-full text-2xl font-bold border-0 bg-transparent focus:outline-none text-text placeholder:text-text-muted"
              placeholder="Enter post title..."
              maxlength="100"
            />
          </div>

          <!-- 变更摘要（编辑时） -->
          <div v-if="isEdit" class="col-span-12">
            <input
              v-model="form.changeSummary"
              type="text"
              class="w-full input-base text-sm"
              placeholder="Change summary (optional)"
              maxlength="100"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 下方区域：左侧编辑器 + 右侧预览 -->
    <div class="flex-1 grid grid-cols-2 overflow-hidden">
      <!-- 左侧编辑器 -->
      <div class="flex flex-col border-r border-border">
        <!-- 工具栏 -->
        <div class="flex-shrink-0 px-4 py-2 border-b border-border bg-bg-raised flex flex-wrap gap-2 h-10">
          <button
            class="btn-ghost-sm"
            @click="insertMarkdown('**bold**')"
            title="Bold (Ctrl+B)"
          >
            <span class="i-tabler-bold" />
          </button>
          <button
            class="btn-ghost-sm"
            @click="insertMarkdown('*italic*')"
            title="Italic (Ctrl+I)"
          >
            <span class="i-tabler-italic" />
          </button>
          <button
            class="btn-ghost-sm"
            @click="insertMarkdown('~~strikethrough~~')"
            title="Strikethrough"
          >
            <span class="i-tabler-strikethrough" />
          </button>
          <div class="w-px bg-border mx-1" />
          <button
            class="btn-ghost-sm"
            @click="insertMarkdown('# Heading 1')"
            title="Heading 1"
          >
            H1
          </button>
          <button
            class="btn-ghost-sm"
            @click="insertMarkdown('## Heading 2')"
            title="Heading 2"
          >
            H2
          </button>
          <button
            class="btn-ghost-sm"
            @click="insertMarkdown('### Heading 3')"
            title="Heading 3"
          >
            H3
          </button>
          <div class="w-px bg-border mx-1" />
          <button
            class="btn-ghost-sm"
            @click="insertMarkdown('\n> Quote\n')"
            title="Quote"
          >
            <span class="i-tabler-quote" />
          </button>
          <button
            class="btn-ghost-sm"
            @click="insertMarkdown('\n```\ncode\n```\n')"
            title="Code Block"
          >
            <span class="i-tabler-code" />
          </button>
          <button
            class="btn-ghost-sm"
            @click="insertMarkdown('`code`')"
            title="Inline Code"
          >
            <span class="i-tabler-file-code" />
          </button>
          <div class="w-px bg-border mx-1" />
          <button
            class="btn-ghost-sm"
            @click="insertMarkdown('\n- List item\n')"
            title="Unordered List"
          >
            <span class="i-tabler-list" />
          </button>
          <button
            class="btn-ghost-sm"
            @click="insertMarkdown('\n1. Numbered item\n')"
            title="Ordered List"
          >
            <span class="i-tabler-list-numbers" />
          </button>
          <button
            class="btn-ghost-sm"
            @click="insertMarkdown('[link text](url)')"
            title="Link"
          >
            <span class="i-tabler-link" />
          </button>
          <button
            class="btn-ghost-sm"
            @click="insertMarkdown('![image alt](url)')"
            title="Image"
          >
            <span class="i-tabler-photo" />
          </button>
          <div class="w-px bg-border mx-1" />
          <button
            class="btn-ghost-sm"
            @click="insertMarkdown('\n| Header | Header |\n|--------|--------|\n| Cell   | Cell   |\n')"
            title="Table"
          >
            <span class="i-tabler-table" />
          </button>
        </div>

        <!-- 编辑区 -->
        <textarea
          v-model="form.content"
          class="flex-1 resize-none p-6 font-mono text-sm bg-bg focus:outline-none text-text"
          placeholder="Write your content in Markdown..."
          @input="updatePreview"
        />

        <!-- 底部状态栏 -->
        <div class="flex-shrink-0 px-4 py-2 border-t border-border bg-bg-surface text-xs text-text-muted flex justify-between">
          <span>Markdown Editor</span>
          <span>{{ form.content.length }} characters</span>
        </div>
      </div>

      <!-- 右侧预览 -->
      <div class="flex flex-col bg-bg-surface overflow-hidden">
        <!-- 预览头部 -->
        <div class="flex-shrink-0 px-4 py-2 border-b border-border bg-bg-raised text-xs text-text-muted flex justify-between items-center h-10">
          <span>Live Preview</span>
          <span v-if="form.content" class="text-brand">● Rendering</span>
        </div>

        <!-- 预览内容 -->
        <div class="flex-1 overflow-y-auto p-8">
          <div
            v-if="form.content"
            class="prose prose-sm dark:prose-invert max-w-none"
            v-html="renderedContent"
          />
          <div v-else class="h-full flex items-center justify-center text-text-muted">
            <div class="text-center">
              <span class="i-tabler-eye-off text-4xl mb-2 block opacity-30" />
              <p class="text-sm">Preview will appear here as you type...</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 发布弹窗：填写板块/简介/标签 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="Post Settings"
      width="640px"
      align-center
      :close-on-click-modal="false"
    >
      <el-form
        ref="publishFormRef"
        :model="form"
        :rules="publishRules"
        label-position="top"
      >
        <el-form-item label="Topic" prop="topicId">
          <el-select
            v-model="form.topicId"
            class="w-full"
            placeholder="Select a topic"
            filterable
            clearable
          >
            <el-option
              v-for="topic in topics"
              :key="topic.topicId"
              :label="`${topic.icon ? topic.icon + ' ' : ''}${topic.topicName}`"
              :value="Number(topic.topicId)"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Intro" prop="intro">
          <el-input
            v-model="form.intro"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="Brief summary"
          />
        </el-form-item>

        <el-form-item label="Tags" prop="tags">
          <el-select
            v-model="form.tags"
            class="w-full"
            multiple
            filterable
            allow-create
            default-first-option
            collapse-tags
            collapse-tags-tooltip
            placeholder="Add tags (press Enter)"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="publishDialogVisible = false">Cancel</el-button>
          <el-button type="primary" :loading="publishing" @click="confirmPublish">
            Confirm Publish
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 发布成功审核提示（全屏） -->
    <el-dialog
      v-model="auditDialogVisible"
      fullscreen
      align-center
      :close-on-click-modal="false"
      :show-close="false"
    >
      <div class="h-full flex flex-col items-center justify-center text-center px-6">
        <span class="i-tabler-shield-check text-6xl text-brand mb-4" />
        <h2 class="text-2xl font-bold text-text mb-2">Your post is under review</h2>
        <p class="text-text-muted max-w-xl">
          The post has been submitted for moderation. You (and admins) can access it while it is pending.
        </p>
        <div class="mt-8 flex gap-3">
          <button class="btn-secondary" @click="goToForum">Back to Forum</button>
          <button class="btn-primary" @click="goToPost">View Post</button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { listAllTopics } from '@/api/topic'
import { createPost, updatePost, getPostDetail } from '@/api/post'
import type { CreatePostDTO, UpdatePostDTO } from '@/api/post'
import type { TopicVO } from '@/api/topic'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.params.id)
const postId = computed(() => route.params.id as string)

const form = reactive({
  title: '',
  content: '',
  intro: '',
  topicId: null as number | null,
  tags: [] as string[],
  changeSummary: ''
})

const topics = ref<TopicVO[]>([])
const renderedContent = ref('')
const saving = ref(false)
const publishing = ref(false)
const publishDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const publishedPostId = ref<number | null>(null)
const publishFormRef = ref<FormInstance>()

const publishRules: FormRules = {
  topicId: [{ required: true, message: 'Please select a topic', trigger: 'change' }],
  intro: [
    { required: true, message: 'Please enter an intro', trigger: 'blur' },
    { min: 10, message: 'Intro must be at least 10 characters', trigger: 'blur' }
  ],
  tags: [
    {
      validator: (_rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('Please add at least one tag'))
        } else if (value.length > 5) {
          callback(new Error('You can add up to 5 tags'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 加载板块列表
const loadTopics = async () => {
  try {
    const res = await listAllTopics()
    topics.value = res.data
  } catch (e) {
    console.error('Failed to load topics:', e)
  }
}

// 加载帖子详情（编辑模式）
const loadPostDetail = async () => {
  if (!isEdit.value) return

  try {
    const res = await getPostDetail(Number(postId.value))
    const post = res.data

    form.title = post.title
    form.content = post.content || ''
    form.intro = post.intro
    form.topicId = Number(post.topicId)
    form.tags = post.tags?.map(t => t.name) || []

    updatePreview()
  } catch (e) {
    console.error('Failed to load post:', e)
    router.push('/forum')
  }
}

// 更新预览
const updatePreview = async () => {
  try {
    const html = await marked.parse(form.content || '')
    renderedContent.value = DOMPurify.sanitize(html)
  } catch (e) {
    console.error('Failed to render markdown:', e)
  }
}

// 插入Markdown
const insertMarkdown = (text: string) => {
  const textarea = document.querySelector('textarea') as HTMLTextAreaElement
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const before = form.content.substring(0, start)
  const after = form.content.substring(end)

  form.content = before + text + after

  // 重新设置焦点和光标位置
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + text.length, start + text.length)
  }, 0)

  updatePreview()
}

const removeTag = (index: number) => {
  form.tags.splice(index, 1)
}

const openPublishDialog = () => {
  publishDialogVisible.value = true
}

// 保存草稿
const saveDraft = async () => {
  if (!form.title.trim()) {
    alert('Please enter a title')
    return
  }

  try {
    saving.value = true

    if (isEdit.value) {
      await updatePost(Number(postId.value), {
        title: form.title,
        content: form.content,
        intro: form.intro,
        topicId: form.topicId ?? 0,
        tags: form.tags,
        changeSummary: form.changeSummary
      } as UpdatePostDTO)
    } else {
      await createPost({
        title: form.title,
        content: form.content,
        intro: form.intro,
        topicId: form.topicId ?? 0,
        tags: form.tags,
        publish: false
      } as CreatePostDTO)
    }

    alert('Draft saved successfully!')
  } catch (e) {
    console.error('Failed to save draft:', e)
    alert('Failed to save draft')
  } finally {
    saving.value = false
  }
}

// 发布帖子
const publishPost = async () => {
  const errors = []

  if (!form.title.trim() || form.title.length < 5) {
    errors.push('Title must be 5-100 characters')
  }
  if (!form.content.trim() || form.content.length < 20) {
    errors.push('Content must be at least 20 characters')
  }
  if (errors.length > 0) {
    alert(errors.join('\n'))
    return
  }

  try {
    publishing.value = true

    if (isEdit.value) {
      await updatePost(Number(postId.value), {
        title: form.title,
        content: form.content,
        intro: form.intro,
        topicId: form.topicId ?? 0,
        tags: form.tags,
        changeSummary: form.changeSummary
      } as UpdatePostDTO)
      publishedPostId.value = Number(postId.value)
    } else {
      const res = await createPost({
        title: form.title,
        content: form.content,
        intro: form.intro,
        topicId: form.topicId ?? 0,
        tags: form.tags,
        publish: true
      } as CreatePostDTO)
      publishedPostId.value = Number(res.data)
    }

    auditDialogVisible.value = true
  } catch (e) {
    console.error('Failed to publish post:', e)
    alert('Failed to publish post')
  } finally {
    publishing.value = false
  }
}

const confirmPublish = async () => {
  if (!publishFormRef.value) return
  try {
    await publishFormRef.value.validate()
    publishDialogVisible.value = false
    await publishPost()
  } catch (e) {
    console.error('Publish validation failed:', e)
  }
}

const goToForum = () => {
  auditDialogVisible.value = false
  router.push('/forum')
}

const goToPost = () => {
  auditDialogVisible.value = false
  const id = publishedPostId.value || Number(postId.value)
  if (id) {
    router.push(`/forum/post/${id}`)
  } else {
    router.push('/forum')
  }
}

const goBack = () => {
  if (confirm('Are you sure you want to leave? Unsaved changes will be lost.')) {
    router.back()
  }
}

onMounted(() => {
  loadTopics()
  loadPostDetail()
})
</script>

<style scoped>
:deep(.prose) {
  color: inherit;
}

:deep(.prose h1),
:deep(.prose h2),
:deep(.prose h3),
:deep(.prose h4),
:deep(.prose h5),
:deep(.prose h6) {
  color: inherit;
  font-weight: 600;
  margin-top: 1em;
  margin-bottom: 0.5em;
}

:deep(.prose p) {
  margin: 0.5em 0;
}

:deep(.prose code) {
  background-color: rgb(31, 41, 55);
  color: rgb(229, 231, 235);
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-size: 0.9em;
}

:deep(.prose pre) {
  background-color: rgb(31, 41, 55);
  padding: 1em;
  border-radius: 6px;
  overflow-x: auto;
  margin: 1em 0;
}

:deep(.prose pre code) {
  background-color: transparent;
  color: rgb(229, 231, 235);
  padding: 0;
}

:deep(.prose a) {
  color: rgb(59, 130, 246);
  text-decoration: none;
}

:deep(.prose a:hover) {
  text-decoration: underline;
}

:deep(.prose blockquote) {
  border-left: 4px solid rgb(107, 114, 128);
  padding-left: 1em;
  color: rgb(107, 114, 128);
  margin: 1em 0;
}

:deep(.prose ul),
:deep(.prose ol) {
  margin: 1em 0;
  padding-left: 2em;
}

:deep(.prose li) {
  margin: 0.5em 0;
}

:deep(.prose img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  margin: 1em 0;
}

:deep(.prose table) {
  border-collapse: collapse;
  width: 100%;
  margin: 1em 0;
}

:deep(.prose th),
:deep(.prose td) {
  border: 1px solid rgb(107, 114, 128);
  padding: 0.5em 1em;
  text-align: left;
}

:deep(.prose th) {
  background-color: rgb(31, 41, 55);
  font-weight: 600;
}
</style>
