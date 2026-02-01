<template>
  <div class="post-create-page">
    <div class="page-header">
      <h1>{{ isEdit ? '编辑帖子' : '发布新帖' }}</h1>
      <div class="actions">
        <el-button @click="saveDraft" :loading="saving">保存草稿</el-button>
        <el-button type="primary" @click="publishPost" :loading="publishing">
          {{ isEdit ? '更新帖子' : '发布帖子' }}
        </el-button>
      </div>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" class="post-form">
      <!-- 标题 -->
      <el-form-item label="标题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入帖子标题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <!-- 板块选择 -->
      <el-form-item label="板块" prop="topicId">
        <el-select v-model="form.topicId" placeholder="请选择板块" style="width: 100%">
          <el-option
            v-for="topic in topics"
            :key="topic.id"
            :label="topic.name"
            :value="topic.id"
          >
            <span>{{ topic.name }}</span>
            <span style="color: var(--el-text-color-secondary); float: right">
              {{ topic.postCount }} 帖
            </span>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- 标签 -->
      <el-form-item label="标签">
        <el-tag
          v-for="tag in form.tags"
          :key="tag"
          closable
          @close="removeTag(tag)"
          style="margin-right: 8px"
        >
          {{ tag }}
        </el-tag>
        <el-input
          v-if="tagInputVisible"
          ref="tagInputRef"
          v-model="tagInput"
          size="small"
          style="width: 100px"
          @blur="handleTagInputConfirm"
          @keyup.enter="handleTagInputConfirm"
        />
        <el-button v-else size="small" @click="showTagInput">+ 添加标签</el-button>
      </el-form-item>

      <!-- Markdown编辑器 -->
      <el-form-item label="内容" prop="content">
        <div class="editor-container">
          <div class="editor-toolbar">
            <el-button-group>
              <el-button size="small" @click="insertMarkdown('**粗体**')">
                <i class="i-carbon-text-bold" />
              </el-button>
              <el-button size="small" @click="insertMarkdown('*斜体*')">
                <i class="i-carbon-text-italic" />
              </el-button>
              <el-button size="small" @click="insertMarkdown('\n```\n代码\n```\n')">
                <i class="i-carbon-code" />
              </el-button>
              <el-button size="small" @click="insertMarkdown('\n> 引用\n')">
                <i class="i-carbon-quotes" />
              </el-button>
              <el-button size="small" @click="insertMarkdown('[链接](url)')">
                <i class="i-carbon-link" />
              </el-button>
            </el-button-group>
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleImageUpload"
              accept="image/*"
            >
              <el-button size="small">
                <i class="i-carbon-image" /> 上传图片
              </el-button>
            </el-upload>
          </div>

          <div class="editor-body">
            <div class="editor-input">
              <el-input
                ref="contentRef"
                v-model="form.content"
                type="textarea"
                placeholder="请输入帖子内容，支持Markdown格式"
                :rows="20"
                @input="updatePreview"
              />
            </div>
            <div class="editor-preview">
              <div class="preview-header">预览</div>
              <div class="markdown-body" v-html="renderedContent"></div>
            </div>
          </div>
        </div>
      </el-form-item>

      <!-- 简介（可选） -->
      <el-form-item label="简介">
        <el-input
          v-model="form.intro"
          type="textarea"
          placeholder="帖子简介，留空将自动从内容中提取"
          :rows="3"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 变更摘要（编辑时） -->
      <el-form-item v-if="isEdit" label="变更说明">
        <el-input
          v-model="form.changeSummary"
          placeholder="描述本次修改的内容（可选）"
          maxlength="100"
        />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { marked } from 'marked'
import { listAllTopics } from '@/api/topic'
import { createPost, updatePost, getPostDetail } from '@/api/post'
import { uploadImage } from '@/api/upload'
import type { CreatePostDTO, UpdatePostDTO } from '@/api/post'
import type { TopicVO } from '@/api/topic'
import type { TopicVO } from '@/api/topic'

const router = useRouter()
const route = useRoute()
const isEdit = computed(() => !!route.params.id)

const formRef = ref<FormInstance>()
const contentRef = ref()
const tagInputRef = ref()

const form = reactive<CreatePostDTO & UpdatePostDTO>({
  title: '',
  content: '',
  intro: '',
  topicId: 0,
  tags: [],
  changeSummary: ''
})

const topics = ref<TopicVO[]>([])
const renderedContent = ref('')
const tagInputVisible = ref(false)
const tagInput = ref('')
const saving = ref(false)
const publishing = ref(false)

const rules: FormRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度在5-100个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 20, message: '内容至少20个字符', trigger: 'blur' }
  ],
  topicId: [
    { required: true, message: '请选择板块', trigger: 'change' }
  ]
}

// 加载板块列表
const loadTopics = async () => {
  try {
    const res = await listAllTopics()
    topics.value = res.data
  } catch (error) {
    ElMessage.error('加载板块失败')
  }
}

// 加载帖子详情（编辑模式）
const loadPostDetail = async () => {
  if (!isEdit.value) return
  
  try {
    const res = await getPostDetail(Number(route.params.id))
    const post = res.data
    
    form.title = post.title
    form.content = post.content
    form.intro = post.intro
    form.topicId = post.topicId
    form.tags = post.tags?.map(t => t.name) || []
    
    updatePreview()
  } catch (error) {
    ElMessage.error('加载帖子失败')
    router.back()
  }
}

// 更新预览
const updatePreview = async () => {
  const result = await marked.parse(form.content || '')
  renderedContent.value = result
}

// 插入Markdown
const insertMarkdown = (text: string) => {
  const textarea = contentRef.value?.textarea
  if (!textarea) return
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = form.content.substring(start, end)
  
  if (text.includes('**') || text.includes('*')) {
    // 处理加粗/斜体
    const wrapped = text.replace(/(\*+)[^*]+(\*+)/, `$1${selectedText}$2`)
    form.content = form.content.substring(0, start) + wrapped + form.content.substring(end)
  } else {
    form.content = form.content.substring(0, start) + text + form.content.substring(end)
  }
  
  updatePreview()
}

// 上传图片
const handleImageUpload = async (file: UploadFile) => {
  if (!file.raw) return
  
  try {
    const res = await uploadImage(file.raw)
    const imageUrl = res.data.url
    
    // 插入Markdown图片语法
    const markdown = `\n![${file.name}](${imageUrl})\n`
    const textarea = contentRef.value?.textarea
    const start = textarea.selectionStart
    
    form.content = form.content.substring(0, start) + markdown + form.content.substring(start)
    updatePreview()
    
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error('图片上传失败')
  }
}

// 标签操作
const showTagInput = () => {
  tagInputVisible.value = true
  nextTick(() => {
    tagInputRef.value?.focus()
  })
}

const handleTagInputConfirm = () => {
  const tag = tagInput.value.trim()
  if (tag && !form.tags?.includes(tag)) {
    if (!form.tags) form.tags = []
    form.tags.push(tag)
  }
  tagInputVisible.value = false
  tagInput.value = ''
}

const removeTag = (tag: string) => {
  form.tags = form.tags?.filter(t => t !== tag)
}

// 保存草稿
const saveDraft = async () => {
  try {
    saving.value = true
    
    if (isEdit.value) {
      await updatePost(Number(route.params.id), form as UpdatePostDTO)
      ElMessage.success('草稿保存成功')
    } else {
      const data: CreatePostDTO = { ...form, publish: false }
      const res = await createPost(data)
      ElMessage.success('草稿保存成功')
      router.replace(`/post/edit/${res.data}`)
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 发布帖子
const publishPost = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    publishing.value = true
    
    if (isEdit.value) {
      await updatePost(Number(route.params.id), form as UpdatePostDTO)
      ElMessage.success('帖子更新成功')
    } else {
      const data: CreatePostDTO = { ...form, publish: true }
      const res = await createPost(data)
      
      ElMessageBox.alert(
        '您的帖子已提交审核，审核通过后将自动发布',
        '发布成功',
        {
          confirmButtonText: '查看帖子',
          callback: () => {
            router.push(`/post/${res.data}`)
          }
        }
      )
    }
  } catch (error) {
    console.error('发布失败', error)
  } finally {
    publishing.value = false
  }
}

onMounted(() => {
  loadTopics()
  loadPostDetail()
})
</script>

<style scoped lang="scss">
.post-create-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h1 {
    font-size: 24px;
    font-weight: 600;
  }
}

.editor-container {
  width: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
}

.editor-toolbar {
  display: flex;
  gap: 8px;
  padding: 8px;
  border-bottom: 1px solid var(--el-border-color);
  background: var(--el-fill-color-light);
}

.editor-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1px;
  background: var(--el-border-color);
  min-height: 500px;
}

.editor-input,
.editor-preview {
  background: white;
}

.editor-input {
  :deep(.el-textarea) {
    height: 100%;
    
    .el-textarea__inner {
      height: 100%;
      border: none;
      box-shadow: none;
      resize: none;
      font-family: 'Monaco', 'Menlo', 'Courier New', monospace;
    }
  }
}

.editor-preview {
  overflow-y: auto;
  
  .preview-header {
    padding: 8px 12px;
    background: var(--el-fill-color-light);
    border-bottom: 1px solid var(--el-border-color);
    font-weight: 500;
  }
  
  .markdown-body {
    padding: 12px;
    min-height: 450px;
  }
}
</style>
