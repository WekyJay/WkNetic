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
            <!-- 已保存状态指示 -->
            <div v-if="isSaved && !hasUnsavedChanges" class="flex items-center gap-2 text-green-600 px-3 py-2 bg-green-50 rounded-md">
              <span class="i-tabler-circle-check text-lg" />
              <span class="text-sm font-medium">Saved</span>
            </div>
            <!-- 未保存状态指示 -->
            <div v-else-if="hasUnsavedChanges" class="flex items-center gap-2 text-yellow-600 px-3 py-2 bg-yellow-50 rounded-md">
              <span class="i-tabler-circle-dotted text-lg animate-spin" />
              <span class="text-sm font-medium">Unsaved</span>
            </div>
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

    <!-- 下方区域：使用split模式的编辑器 -->
    <div class="flex-1 overflow-hidden">
      <WkMarkdownEditor
        v-model="form.content"
        mode="split"
        placeholder="Write your content in Markdown..."
      />
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

        <el-form-item label="Excerpt" prop="excerpt">
          <el-input
            v-model="form.excerpt"
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

    <!-- 草稿确认对话框 -->
    <el-dialog
      v-model="draftConfirmVisible"
      title="Draft Found"
      width="480px"
      align-center
      @close="discardDraft"
      :close-on-click-modal="false"
    >
      <div class="space-y-4">
        <div class="flex items-start gap-3">
          <span class="i-tabler-file-text text-2xl text-blue-600 flex-shrink-0 mt-1" />
          <div class="flex-1">
            <h3 class="font-semibold text-text mb-1">Draft: {{ draftToUse?.title }}</h3>
            <p class="text-sm text-text-muted mb-2">
              {{ draftToUse?.content?.substring(0, 100) }}{{ draftToUse?.content?.length > 100 ? '...' : '' }}
            </p>
            <p class="text-xs text-text-muted">
              Last saved: {{ new Date(draftToUse?.updateTime).toLocaleString() }}
            </p>
          </div>
        </div>
        <p class="text-sm text-text-secondary">
          Do you want to use this draft or start fresh?
        </p>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="draftConfirmVisible = false">Start Fresh</el-button>
          <el-button type="primary" @click="useDraft">Use Draft</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { listAllTopics } from '@/api/topic'
import { createPost, updatePost, getPostDetail, getLatestDraft } from '@/api/post'
import type { CreatePostDTO, UpdatePostDTO } from '@/api/post'
import type { TopicVO } from '@/api/topic'
import type { FormInstance, FormRules } from 'element-plus'
import WkMarkdownEditor from '@/components/common/WkMarkdownEditor.vue'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.params.id)
const postId = computed(() => route.params.id as string)

const form = reactive({
  title: '',
  content: '',
  excerpt: '',
  topicId: null as number | null,
  tags: [] as string[],
  changeSummary: ''
})

// 保存初始表单状态用于检测变化
const initialForm = reactive({
  title: '',
  content: '',
  excerpt: '',
  topicId: null as number | null,
  tags: [] as string[],
  changeSummary: ''
})

const topics = ref<TopicVO[]>([])
const saving = ref(false)
const publishing = ref(false)
const publishDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const publishedPostId = ref<number | null>(null)
const publishFormRef = ref<FormInstance>()

// 状态管理
const isSaved = ref(true) // 是否已保存
const draftPostId = ref<number | null>(null) // 草稿ID，用于复用旧草稿
const hasUnsavedChanges = computed(() => {
  return JSON.stringify(form) !== JSON.stringify(initialForm)
})

// 草稿确认对话框
const draftConfirmVisible = ref(false)
const draftToUse = ref<any>(null)

const publishRules: FormRules = {
  topicId: [{ required: true, message: 'Please select a topic', trigger: 'change' }],
  excerpt: [
    { required: true, message: 'Please enter an excerpt', trigger: 'blur' },
    { min: 10, message: 'Excerpt must be at least 10 characters', trigger: 'blur' }
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

// 更新已保存状态（监听表单变化）
watch(() => form.title, () => { isSaved.value = false }, { deep: true })
watch(() => form.content, () => { isSaved.value = false }, { deep: true })
watch(() => form.excerpt, () => { isSaved.value = false }, { deep: true })
watch(() => form.topicId, () => { isSaved.value = false }, { deep: true })
watch(() => form.tags, () => { isSaved.value = false }, { deep: true })

// 加载板块列表
const loadTopics = async () => {
  try {
    const res = await listAllTopics()
    topics.value = res.data
  } catch (e) {
    console.error('Failed to load topics:', e)
  }
}

// 加载草稿
const loadDraft = async () => {
  try {
    const res = await getLatestDraft()
    if (res.data && res.data.postId) {
      draftToUse.value = res.data
      draftConfirmVisible.value = true
    }
  } catch (e) {
    console.error('Failed to load draft:', e)
    // 没有草稿是正常的，不需要显示错误
  }
}

// 使用草稿
const useDraft = () => {
  if (!draftToUse.value) return
  
  const draft = draftToUse.value
  form.title = draft.title || ''
  form.content = draft.content || ''
  form.excerpt = draft.excerpt || ''
  form.topicId = draft.topicId ? Number(draft.topicId) : null
  form.tags = draft.tags?.map((t: any) => t?.tagName || '') || []
  
  // 保存草稿ID，后续保存时使用该ID
  draftPostId.value = draft.postId
  
  // 更新初始状态
  Object.assign(initialForm, form)
  isSaved.value = true
  
  draftConfirmVisible.value = false
  draftToUse.value = null
}

// 丢弃草稿
const discardDraft = () => {
  // 强制使用草稿id,但不加载内容
  console.log('Discarding draft , use id only.')
  draftPostId.value = draftToUse.value?.postId || null
  draftConfirmVisible.value = false
  draftToUse.value = null
}

// 加载帖子详情（编辑模式）
const loadPostDetail = async () => {
  if (!isEdit.value) return

  try {
    const res = await getPostDetail(Number(postId.value))
    const post = res.data

    form.title = post.title
    form.content = post.content || ''
    form.excerpt = post.excerpt
    form.topicId = Number(post.topicId)
    form.tags = post.tags?.map(t => t != null ? t.tagName : '') || []
    
    // 保存初始状态
    Object.assign(initialForm, form)
    isSaved.value = true
  } catch (e) {
    console.error('Failed to load post:', e)
    router.push('/forum')
  }
}

const openPublishDialog = () => {
  publishDialogVisible.value = true
}

// 保存草稿
const saveDraft = async () => {
  if (!form.title.trim()) {
    // 使用 ElMessage 作为临时通知方案
    const { ElMessage } = await import('element-plus')
    ElMessage.warning('Please enter a title')
    return
  }

  try {
    saving.value = true

    if (isEdit.value) {
      // 编辑已发布的帖子
      await updatePost(Number(postId.value), {
        title: form.title,
        content: form.content,
        excerpt: form.excerpt,
        topicId: form.topicId ?? undefined,
        tags: form.tags,
        changeSummary: form.changeSummary
      } as UpdatePostDTO)
    } else if (draftPostId.value) {
      // 复用旧草稿：使用草稿的ID进行更新，避免数据冗余
      await updatePost(draftPostId.value, {
        title: form.title,
        content: form.content,
        excerpt: form.excerpt,
        topicId: form.topicId ?? undefined,
        tags: form.tags
      } as UpdatePostDTO)
    } else {
      // 创建新草稿
      const draftData: any = {
        title: form.title,
        content: form.content,
        excerpt: form.excerpt,
        tags: form.tags,
        publish: false
      }
      
      // 只有选择了有效的话题才包含在请求中
      if (form.topicId && form.topicId > 0) {
        draftData.topicId = form.topicId
      }
      
      const res = await createPost(draftData as CreatePostDTO)
      // 保存新创建的草稿ID，后续保存将复用此ID
      draftPostId.value = res.data
    }

    // 更新初始状态和保存标记
    Object.assign(initialForm, form)
    isSaved.value = true
    
    const { ElMessage } = await import('element-plus')
    ElMessage.success('Draft saved successfully!')
  } catch (e) {
    console.error('Failed to save draft:', e)
    const { ElMessage } = await import('element-plus')
    const errorMsg = (e as any)?.response?.data?.message || (e as any)?.message || 'Unknown error'
    ElMessage.error(`Failed to save draft: ${errorMsg}`)
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
    const { ElMessage } = await import('element-plus')
    ElMessage.error(errors.join('\n'))
    return
  }

  try {
    publishing.value = true

    if (isEdit.value) {
      await updatePost(Number(postId.value), {
        title: form.title,
        content: form.content,
        excerpt: form.excerpt,
        topicId: form.topicId ?? 0,
        tags: form.tags,
        changeSummary: form.changeSummary
      } as UpdatePostDTO)
      publishedPostId.value = Number(postId.value)
    } else {
      const res = await createPost({
        title: form.title,
        content: form.content,
        excerpt: form.excerpt,
        topicId: form.topicId ?? 0,
        tags: form.tags,
        publish: true
      } as CreatePostDTO)
      publishedPostId.value = Number(res.data)
    }

    auditDialogVisible.value = true
  } catch (e) {
    console.error('Failed to publish post:', e)
    const { ElMessage } = await import('element-plus')
    ElMessage.error('Failed to publish post')
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

const goBack = async () => {
  // 如果有未保存的变更，提示用户
  if (hasUnsavedChanges.value) {
    const { ElMessageBox } = await import('element-plus')
    try {
      await ElMessageBox.confirm(
        'You have unsaved changes. Are you sure you want to leave?',
        'Warning',
        {
          confirmButtonText: 'Leave',
          cancelButtonText: 'Cancel',
          type: 'warning',
        }
      )
      router.back()
    } catch {
      // 用户取消了离开
    }
  } else {
    router.back()
  }
}

onMounted(async () => {
  await loadTopics()
  if (isEdit.value) {
    await loadPostDetail()
  } else {
    // 非编辑模式下，检查是否有草稿
    await loadDraft()
  }
})
</script>

<style scoped>
</style>
