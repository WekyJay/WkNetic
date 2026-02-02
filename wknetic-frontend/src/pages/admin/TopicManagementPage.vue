<template>
  <div>
    <div class="space-y-6">
      <!-- 页头 -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold text-[var(--text-default)]">板块管理</h1>
          <p class="text-[var(--text-secondary)] mt-1">管理论坛板块，设置名称、图标和颜色</p>
        </div>
        <WkButton variant="primary" icon="i-tabler-plus" @click="handleCreate">
          新建板块
        </WkButton>
      </div>

    <!-- 板块列表 -->
    <WkCard>
      <!-- 搜索和过滤 -->
      <div class="flex gap-3 mb-6 items-center">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索板块名称..."
          class="max-w-75"
          clearable
          @keyup.enter="loadTopics"
        >
          <template #prefix>
            <i class="i-tabler-search" />
          </template>
        </el-input>
        <WkButton variant="primary" size="sm" icon="i-tabler-search" @click="loadTopics">
          搜索
        </WkButton>
        <WkButton variant="secondary" size="sm" icon="i-tabler-refresh" @click="loadTopics">
          刷新
        </WkButton>
      </div>

      <!-- 加载状态 -->
      <div v-loading="loading" class="min-h-40">
        <!-- 空状态 -->
        <div v-if="!loading && topics.length === 0" class="flex flex-col items-center justify-center py-12 text-[var(--text-muted)]">
          <i class="i-tabler-layout-list text-5xl mb-4 opacity-50" />
          <p class="text-sm m-0">暂无板块数据</p>
          <WkButton variant="text" size="sm" class="mt-4" @click="handleCreate">
            创建第一个板块
          </WkButton>
        </div>

        <!-- 板块网格 -->
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          <div
            v-for="topic in topics"
            :key="topic.topicId"
            class="group bg-white dark:bg-[var(--bg-raised)] border border-[var(--border-default)] rounded-lg p-4 transition-all hover:shadow-md hover:border-[var(--brand-default)]"
          >
            <!-- 图标和标题 -->
            <div class="flex items-start justify-between mb-3">
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 rounded-lg flex items-center justify-center flex-shrink-0" :style="{ backgroundColor: topic.color ? `${topic.color}15` : 'var(--brand/15)' }">
                  <span :class="topic.icon || 'i-tabler-layout'" class="text-xl" :style="{ color: topic.color || 'var(--brand-default)' }"></span>
                </div>
                <div class="flex-1 min-w-0">
                  <h3 class="font-semibold text-[var(--text-default)] text-base truncate" :title="topic.topicName">
                    {{ topic.topicName }}
                  </h3>
                  <p v-if="topic.topicDesc" class="text-sm text-[var(--text-secondary)] mt-1 line-clamp-2" :title="topic.topicDesc">
                    {{ topic.topicDesc }}
                  </p>
                </div>
              </div>
              <div class="opacity-0 group-hover:opacity-100 transition-opacity">
                <div class="flex gap-1">
                  <button
                    class="p-1.5 hover:bg-[var(--bg-hover)] rounded text-[var(--text-muted)] hover:text-[var(--brand-default)]"
                    @click="handleEdit(topic)"
                    title="编辑"
                  >
                    <i class="i-tabler-edit text-sm" />
                  </button>
                  <button
                    class="p-1.5 hover:bg-[var(--bg-hover)] rounded text-[var(--text-muted)] hover:text-[var(--danger)]"
                    @click="handleDelete(topic.topicId)"
                    title="删除"
                  >
                    <i class="i-tabler-trash text-sm" />
                  </button>
                </div>
              </div>
            </div>

            <!-- 统计信息 -->
            <div class="flex items-center justify-between text-sm text-[var(--text-muted)] border-t border-[var(--border-light)] pt-3">
              <div class="flex items-center gap-1">
                <i class="i-tabler-file-text text-xs" />
                <span>{{ topic.postCount || 0 }} 帖子</span>
              </div>
              <div class="text-xs text-[var(--text-muted)]">
                创建于 {{ formatDate(topic.createTime) }}
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="total > 0 && total > pageSize" class="flex justify-center p-5 border-t border-[var(--border-default)]">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[8, 16, 32]"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="loadTopics"
            @size-change="handlePageSizeChange"
          />
        </div>
      </div>
    </WkCard>
    </div>

    <!-- 编辑/创建对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.topicId ? '编辑板块' : '新建板块'"
      width="500px"
      align-center
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="板块名称" prop="topicName">
          <el-input
            v-model="form.topicName"
            placeholder="请输入板块名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="板块描述" prop="topicDesc">
          <el-input
            v-model="form.topicDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入板块描述"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="图标设置">
          <div class="flex items-center gap-4">
            <!-- 图标预览 -->
            <div class="w-16 h-16 rounded-lg flex items-center justify-center border border-[var(--border-default)]" :style="{ backgroundColor: form.color ? `${form.color}15` : 'var(--brand/15)' }">
              <span :class="form.icon || 'i-tabler-layout'" class="text-2xl" :style="{ color: form.color || 'var(--brand-default)' }"></span>
            </div>
            
            <!-- 选择按钮 -->
            <div class="flex-1">
              <div class="flex items-center gap-2 mb-2">
                <span class="text-sm text-[var(--text-secondary)]">当前图标: </span>
                <span class="text-sm font-medium">{{ form.icon || '默认图标' }}</span>
              </div>
              <div class="flex gap-2">
                <WkButton variant="secondary" size="sm" @click="showIconPicker = true">
                  <i class="i-tabler-palette mr-1" />
                  选择图标
                </WkButton>
                <WkButton variant="text" size="sm" @click="form.icon = ''; form.color = ''">
                  重置为默认
                </WkButton>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex gap-3 justify-end">
          <WkButton variant="secondary" @click="dialogVisible = false">
            取消
          </WkButton>
          <WkButton variant="primary" @click="handleSubmit" :loading="submitting">
            {{ form.topicId ? '保存' : '创建' }}
          </WkButton>
        </div>
      </template>
    </el-dialog>

    <!-- 图标选择器弹窗 -->
    <IconPickerDialog
      v-model="showIconPicker"
      :selected-icon="form.icon"
      :selected-color="form.color || '#1890ff'"
      @select="handleIconSelect"
      @update:selectedIcon="form.icon = $event"
      @update:selectedColor="form.color = $event"
    />

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="400px"
      align-center
    >
      <p class="text-[var(--text-default)]">
        确定要删除板块 "<span class="font-semibold text-[var(--danger)]">{{ deletingTopicName }}</span>" 吗？
      </p>
      <p class="text-sm text-[var(--text-secondary)] mt-2">
        此操作将永久删除该板块，且无法恢复。该板块下的帖子将转移到默认板块。
      </p>

      <template #footer>
        <div class="flex gap-3 justify-end">
          <WkButton variant="secondary" @click="deleteDialogVisible = false">
            取消
          </WkButton>
          <WkButton variant="danger" @click="confirmDelete" :loading="deleting">
            确认删除
          </WkButton>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import WkCard from '@/components/common/WkCard.vue'
import WkButton from '@/components/common/WkButton.vue'
import IconPickerDialog, { type IconItem } from '@/components/common/IconPickerDialog.vue'
import {
  listTopics,
  createTopic,
  updateTopic,
  deleteTopic,
  type TopicVO
} from '@/api/topic'

const loading = ref(false)
const submitting = ref(false)
const deleting = ref(false)
const dialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const showIconPicker = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const deletingTopicId = ref<number | null>(null)
const deletingTopicName = ref('')
const formRef = ref<FormInstance>()

const topics = ref<TopicVO[]>([])

const form = reactive({
  topicId: undefined as number | undefined,
  topicName: '',
  topicDesc: '',
  icon: 'i-tabler-layout',
  color: '#1890ff'
})

const rules = {
  topicName: [
    { required: true, message: '请输入板块名称', trigger: 'blur' },
    { min: 2, max: 20, message: '名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  topicDesc: [
    { max: 100, message: '描述不能超过 100 个字符', trigger: 'blur' }
  ]
}

/**
 * 加载板块列表
 */
async function loadTopics() {
  try {
    loading.value = true
    const res = await listTopics({
      page: currentPage.value,
      size: pageSize.value
    })
    topics.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载板块列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 处理分页大小变化
 */
function handlePageSizeChange(val: number) {
  pageSize.value = val
  currentPage.value = 1
  loadTopics()
}

/**
 * 打开创建对话框
 */
function handleCreate() {
  resetForm()
  dialogVisible.value = true
}

/**
 * 打开编辑对话框
 */
function handleEdit(topic: TopicVO) {
  resetForm()
  form.topicId = topic.topicId
  form.topicName = topic.topicName
  form.topicDesc = topic.topicDesc
  form.icon = topic.icon || 'i-tabler-layout'
  form.color = topic.color || '#1890ff'
  dialogVisible.value = true
}

/**
 * 重置表单
 */
function resetForm() {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.topicId = undefined
  form.topicName = ''
  form.topicDesc = ''
  form.icon = 'i-tabler-layout'
  form.color = '#1890ff'
}

/**
 * 处理图标选择
 */
function handleIconSelect(iconClass: string, color: string) {
  form.icon = iconClass
  form.color = color
}

/**
 * 提交表单
 */
async function handleSubmit() {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  try {
    submitting.value = true
    
    const requestData = {
      name: form.topicName,
      description: form.topicDesc,
      icon: form.icon === 'i-tabler-layout' ? undefined : form.icon
    }

    if (form.topicId) {
      // 编辑
      await updateTopic(form.topicId, requestData)
      ElMessage.success('更新成功')
    } else {
      // 创建
      await createTopic(requestData)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadTopics()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
    console.error(error)
  } finally {
    submitting.value = false
  }
}

/**
 * 打开删除确认对话框
 */
function handleDelete(topicId: number) {
  const topic = topics.value.find(t => t.topicId === topicId)
  if (!topic) return
  
  deletingTopicId.value = topicId
  deletingTopicName.value = topic.topicName
  deleteDialogVisible.value = true
}

/**
 * 确认删除
 */
async function confirmDelete() {
  if (!deletingTopicId.value) return

  try {
    deleting.value = true
    await deleteTopic(deletingTopicId.value)
    ElMessage.success('删除成功')
    deleteDialogVisible.value = false
    loadTopics()
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
    console.error(error)
  } finally {
    deleting.value = false
    deletingTopicId.value = null
    deletingTopicName.value = ''
  }
}

/**
 * 格式化日期
 */
function formatDate(dateStr: string | undefined): string {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN')
  } catch {
    return dateStr
  }
}

// 生命周期
onMounted(() => {
  loadTopics()
})
</script>

<style scoped>
.line-clamp-2 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>