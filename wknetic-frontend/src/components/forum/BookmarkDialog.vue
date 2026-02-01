<template>
  <el-dialog
    v-model="visible"
    title="收藏到收藏夹"
    width="500px"
    @close="handleClose"
  >
    <el-form :model="form" label-width="80px">
      <el-form-item label="选择收藏夹">
        <el-select
          v-model="form.categoryId"
          placeholder="请选择收藏夹"
          style="width: 100%"
        >
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          >
            <span>{{ category.name }}</span>
            <span v-if="category.isDefault" style="color: var(--el-color-primary); margin-left: 8px">
              (默认)
            </span>
          </el-option>
        </el-select>
      </el-form-item>

      <el-divider>或创建新收藏夹</el-divider>

      <el-form-item label="收藏夹名称">
        <el-input
          v-model="newCategoryName"
          placeholder="输入新收藏夹名称"
          maxlength="50"
        />
      </el-form-item>

      <el-form-item label="描述">
        <el-input
          v-model="newCategoryDescription"
          type="textarea"
          :rows="2"
          placeholder="选填，描述这个收藏夹的用途"
          maxlength="200"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button
        v-if="newCategoryName"
        type="primary"
        @click="createAndBookmark"
        :loading="loading"
      >
        创建并收藏
      </el-button>
      <el-button
        v-else
        type="primary"
        @click="bookmarkToExisting"
        :loading="loading"
        :disabled="!form.categoryId"
      >
        确定
      </el-button>
    </template>

    <!-- 收藏夹管理 -->
    <el-divider>收藏夹管理</el-divider>
    <div class="category-list">
      <div
        v-for="category in categories.filter(c => !c.isDefault)"
        :key="category.id"
        class="category-item"
      >
        <div class="category-info">
          <span class="category-name">{{ category.name }}</span>
          <span class="category-desc">{{ category.description }}</span>
        </div>
        <div class="category-actions">
          <el-button
            size="small"
            link
            @click="editCategory(category)"
          >
            编辑
          </el-button>
          <el-button
            size="small"
            link
            type="danger"
            @click="deleteCategory(category.id)"
          >
            删除
          </el-button>
        </div>
      </div>
      <el-empty v-if="categories.filter(c => !c.isDefault).length === 0" description="暂无自定义收藏夹" />
    </div>

    <!-- 编辑收藏夹对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑收藏夹"
      width="400px"
      append-to-body
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="editForm.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="2"
            maxlength="200"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateCategory" :loading="editLoading">
          保存
        </el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUserCategories,
  createCategory,
  updateCategory as updateCategoryApi,
  deleteCategory as deleteCategoryApi,
  toggleBookmark
} from '@/api/bookmark'
import type { BookmarkCategory } from '@/api/bookmark'

interface Props {
  modelValue: boolean
  postId: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const visible = ref(false)
const loading = ref(false)
const categories = ref<BookmarkCategory[]>([])

const form = ref({
  categoryId: undefined as number | undefined
})

const newCategoryName = ref('')
const newCategoryDescription = ref('')

// 编辑收藏夹
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editForm = ref({
  id: 0,
  name: '',
  description: ''
})

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadCategories()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 加载收藏夹列表
const loadCategories = async () => {
  try {
    const res = await getUserCategories()
    categories.value = res.data
    
    // 默认选中默认收藏夹
    const defaultCategory = categories.value.find(c => c.isDefault)
    if (defaultCategory) {
      form.value.categoryId = defaultCategory.id
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载收藏夹失败')
  }
}

// 收藏到已有收藏夹
const bookmarkToExisting = async () => {
  if (!form.value.categoryId) {
    ElMessage.warning('请选择收藏夹')
    return
  }

  loading.value = true
  try {
    await toggleBookmark(props.postId, form.value.categoryId)
    ElMessage.success('收藏成功')
    emit('success')
    handleClose()
  } catch (error: any) {
    ElMessage.error(error.message || '收藏失败')
  } finally {
    loading.value = false
  }
}

// 创建并收藏
const createAndBookmark = async () => {
  if (!newCategoryName.value) {
    ElMessage.warning('请输入收藏夹名称')
    return
  }

  loading.value = true
  try {
    // 创建收藏夹
    const createRes = await createCategory({
      name: newCategoryName.value,
      description: newCategoryDescription.value
    })
    const categoryId = createRes.data

    // 收藏到新收藏夹
    await toggleBookmark(props.postId, categoryId)
    
    ElMessage.success('创建收藏夹并收藏成功')
    emit('success')
    handleClose()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

// 编辑收藏夹
const editCategory = (category: BookmarkCategory) => {
  editForm.value = {
    id: category.id,
    name: category.name,
    description: category.description || ''
  }
  editDialogVisible.value = true
}

// 更新收藏夹
const updateCategory = async () => {
  if (!editForm.value.name) {
    ElMessage.warning('请输入收藏夹名称')
    return
  }

  editLoading.value = true
  try {
    await updateCategoryApi(editForm.value.id, {
      name: editForm.value.name,
      description: editForm.value.description
    })
    
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    await loadCategories()
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    editLoading.value = false
  }
}

// 删除收藏夹
const deleteCategory = async (categoryId: number) => {
  try {
    await ElMessageBox.confirm(
      '删除收藏夹后，其中的收藏将移动到默认收藏夹。确定删除吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteCategoryApi(categoryId)
    ElMessage.success('删除成功')
    await loadCategories()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  form.value.categoryId = undefined
  newCategoryName.value = ''
  newCategoryDescription.value = ''
}

onMounted(() => {
  if (visible.value) {
    loadCategories()
  }
})
</script>

<style scoped lang="scss">
.category-list {
  max-height: 300px;
  overflow-y: auto;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  margin-bottom: 8px;

  &:hover {
    background-color: var(--el-fill-color-light);
  }

  .category-info {
    flex: 1;
    min-width: 0;

    .category-name {
      font-weight: 500;
      display: block;
      margin-bottom: 4px;
    }

    .category-desc {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      display: block;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .category-actions {
    display: flex;
    gap: 4px;
  }
}
</style>
