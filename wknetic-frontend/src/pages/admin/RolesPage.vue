<template>
  <div class="roles-page p-8">
    <!-- 页面头部 -->
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-semibold text-[var(--text-default)]">{{ t('menu.roles') }}</h2>
      <button
        class="flex items-center gap-2 px-4 py-2 bg-[var(--brand-default)] text-white rounded-lg hover:opacity-90 transition-opacity"
        @click="showCreateDialog"
      >
        <i class="i-tabler-plus"></i>
        {{ t('common.create') }}
      </button>
    </div>

    <!-- 搜索和过滤器 -->
    <div class="bg-[var(--bg-default)] rounded-lg p-4 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <el-input
          v-model="searchQuery"
          :placeholder="t('role.searchPlaceholder')"
          clearable
          @change="handleSearch"
        >
          <template #prefix>
            <i class="i-tabler-search"></i>
          </template>
        </el-input>
        
        <el-select
          v-model="statusFilter"
          :placeholder="t('role.filterByStatus')"
          clearable
          @change="handleStatusFilter"
        >
          <el-option :label="t('common.enabled')" :value="1" />
          <el-option :label="t('common.disabled')" :value="0" />
        </el-select>
        
        <el-button type="primary" @click="handleRefresh">
          <i class="i-tabler-refresh mr-1"></i>
          {{ t('common.refresh') }}
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="bg-[var(--bg-default)] rounded-lg">
      <el-table
        :data="tableData"
        style="width: 100%"
        :loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column 
          prop="roleCode" 
          :label="t('role.roleCode')" 
          min-width="120"
          show-overflow-tooltip
        />
        
        <el-table-column 
          prop="roleName" 
          :label="t('role.roleName')" 
          min-width="120"
          show-overflow-tooltip
        />
        
        <el-table-column 
          prop="roleDesc" 
          :label="t('role.roleDesc')" 
          min-width="220"
          show-overflow-tooltip
        />
        
        <el-table-column 
          prop="sortOrder" 
          :label="t('role.sortOrder')" 
          width="130px"
          align="center"
          sortable
        />
        
        <el-table-column 
          prop="status" 
          :label="t('role.status')" 
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'danger'"
              size="small"
            >
              {{ row.status === 1 ? t('common.enabled') : t('common.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column 
          prop="isDefault" 
          :label="t('role.isDefault')" 
          width="120px"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              v-if="row.isDefault"
              type="warning"
              size="small"
            >
              {{ t('common.yes') }}
            </el-tag>
            <span v-else class="text-[var(--text-muted)]">{{ t('common.no') }}</span>
          </template>
        </el-table-column>
        
        <el-table-column 
          :label="t('common.operations')" 
          width="200"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              @click="editRole(row)"
            >
              {{ t('common.edit') }}
            </el-button>
            
            <el-button
              type="danger"
              size="small"
              link
              @click="confirmDeleteRole(row)"
              :disabled="row.isDefault"
            >
              {{ t('common.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="flex justify-between items-center p-4">
        <div class="text-sm text-[var(--text-muted)]">
          {{ t('table.showing', { 
            start: (currentPage - 1) * pageSize + 1,
            end: Math.min(currentPage * pageSize, total),
            total 
          }) }}
        </div>
        
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          :background="true"
          layout="sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <WkDialog
      v-model="showDialog"
      :title="isEditing ? t('role.editRole') : t('role.createRole')"
      size="lg"
    >
      <!-- 角色编码 -->
      <div>
        <label class="block text-sm font-medium text-[var(--text-default)] mb-2">
          {{ t('role.roleCode') }} 
        </label>
        <input
          v-model="formData.roleCode"
          type="text"
          class="w-full px-3 py-2 bg-[var(--bg-default)] border border-[var(--border-default)] rounded-lg text-[var(--text-default)] focus:outline-none focus:ring-2 focus:ring-[var(--brand-default)] disabled:opacity-50"
          :disabled="isEditing"
          required
          placeholder="CUSTOM_ROLE"
        />
        <p class="mt-1 text-xs text-[var(--text-muted)]">{{ t('role.roleCodeHint') }}</p>
      </div>

      <!-- 角色名称 -->
      <div class="mt-4">
        <label class="block text-sm font-medium text-[var(--text-default)] mb-2">
          {{ t('role.roleName') }} *
        </label>
        <input
          v-model="formData.roleName"
          type="text"
          class="w-full px-3 py-2 bg-[var(--bg-default)] border border-[var(--border-default)] rounded-lg text-[var(--text-default)] focus:outline-none focus:ring-2 focus:ring-[var(--brand-default)]"
          required
          :placeholder="t('role.roleNamePlaceholder')"
        />
      </div>

      <!-- 角色描述 -->
      <div class="mt-4">
        <label class="block text-sm font-medium text-[var(--text-default)] mb-2">
          {{ t('role.roleDesc') }}
        </label>
        <textarea
          v-model="formData.roleDesc"
          class="w-full px-3 py-2 bg-[var(--bg-default)] border border-[var(--border-default)] rounded-lg text-[var(--text-default)] focus:outline-none focus:ring-2 focus:ring-[var(--brand-default)]"
          rows="3"
          :placeholder="t('role.roleDescPlaceholder')"
        ></textarea>
      </div>

      <!-- 排序和状态 -->
      <div class="grid grid-cols-2 gap-4 mt-4">
        <div>
          <label class="block text-sm font-medium text-[var(--text-default)] mb-2">
            {{ t('role.sortOrder') }} *
          </label>
          <input
            v-model.number="formData.sortOrder"
            type="number"
            class="w-full px-3 py-2 bg-[var(--bg-default)] border border-[var(--border-default)] rounded-lg text-[var(--text-default)] focus:outline-none focus:ring-2 focus:ring-[var(--brand-default)]"
            required
            min="0"
            max="100"
          />
          <p class="mt-1 text-xs text-[var(--text-muted)]">{{ t('role.sortOrderHint') }}</p>
        </div>

        <div>
          <label class="block text-sm font-medium text-[var(--text-default)] mb-2">
            {{ t('role.status') }}
          </label>
          <select
            v-model.number="formData.status"
            class="w-full px-3 py-2 bg-[var(--bg-default)] border border-[var(--border-default)] rounded-lg text-[var(--text-default)] focus:outline-none focus:ring-2 focus:ring-[var(--brand-default)]"
          >
            <option :value="1">{{ t('common.enabled') }}</option>
            <option :value="0">{{ t('common.disabled') }}</option>
          </select>
        </div>
      </div>

      <!-- 默认角色 -->
      <div class="mt-4">
        <label class="flex items-center gap-2 cursor-pointer">
          <input
            v-model="formData.isDefault"
            type="checkbox"
            class="w-5 h-5 text-[var(--brand-default)] border-[var(--border-default)] rounded focus:ring-2 focus:ring-[var(--brand-default)]"
          />
          <span class="text-sm font-medium text-[var(--text-default)]">
            {{ t('role.setAsDefault') }}
          </span>
        </label>
        <p class="mt-1 ml-7 text-xs text-[var(--text-muted)]">{{ t('role.isDefaultHint') }}</p>
      </div>

      <template #footer>
        <WkButton variant="ghost" @click="closeDialog">
          {{ t('common.cancel') }}
        </WkButton>
        <WkButton 
          variant="primary" 
          :loading="submitLoading"
          @click="handleSubmit"
        >
          {{ t('common.save') }}
        </WkButton>
      </template>
    </WkDialog>

    <!-- 删除确认对话框 -->
    <WkDialog
      v-model="deleteDialog.visible"
      :title="t('common.confirmDelete')"
      size="sm"
    >
      <p class="text-[var(--text-secondary)]">
        {{ t('role.confirmDelete', { name: deleteDialog.roleToDelete?.roleName || '' }) }}
      </p>
      
      <template #footer>
        <WkButton variant="ghost" @click="deleteDialog.visible = false">
          {{ t('common.cancel') }}
        </WkButton>
        <WkButton variant="danger" @click="handleDelete">
          {{ t('common.delete') }}
        </WkButton>
      </template>
    </WkDialog>

    <!-- 消息提示 -->
    <WkAlert
      v-if="alertMessage.visible"
      :type="alertMessage.type"
      :message="alertMessage.message"
      :closable="true"
      @close="alertMessage.visible = false"
      class="fixed top-4 right-4 z-[60] min-w-80"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { WkDialog, WkAlert, WkButton } from '@/components/common'
import { roleApi, type Role, type RoleFormData } from '@/api/role'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

// 表格数据
const tableData = ref<Role[]>([])
const loading = ref(false)
const selectedRoles = ref<Role[]>([])

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索和过滤
const searchQuery = ref('')
const statusFilter = ref<number | undefined>(undefined)

const showDialog = ref(false)
const isEditing = ref(false)
const submitLoading = ref(false)

// 确认删除对话框
const deleteDialog = reactive({
  visible: false,
  roleToDelete: null as Role | null
})

// 消息提示
const alertMessage = reactive({
  visible: false,
  type: 'info' as 'info' | 'success' | 'warning' | 'error',
  message: ''
})


const formData = ref<RoleFormData>({
  roleCode: '',
  roleName: '',
  roleDesc: '',
  sortOrder: 10,
  isDefault: false,
  status: 1
})

// 表格选择变化
const handleSelectionChange = (selection: Role[]) => {
  selectedRoles.value = selection
}

// 分页相关函数
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadRoles()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadRoles()
}

// 搜索功能
const handleSearch = () => {
  currentPage.value = 1
  loadRoles()
}

// 状态过滤
const handleStatusFilter = () => {
  currentPage.value = 1
  loadRoles()
}

// 刷新数据
const handleRefresh = () => {
  currentPage.value = 1
  searchQuery.value = ''
  statusFilter.value = undefined
  loadRoles()
}

// 加载角色列表
const loadRoles = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      search: searchQuery.value || undefined,
      status: statusFilter.value
    }
    
    const response = await roleApi.getAllRoles(params)
    const data = response.data
    
    // 处理不同的数据格式：直接数组或分页对象
    if (Array.isArray(data)) {
      // 后端返回直接数组，前端模拟分页
      let filteredData = [...data]
      
      // 应用搜索过滤
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filteredData = filteredData.filter(role => 
          role.roleName.toLowerCase().includes(query) ||
          role.roleCode.toLowerCase().includes(query) ||
          role.roleDesc?.toLowerCase().includes(query)
        )
      }
      
      // 应用状态过滤
      if (statusFilter.value !== undefined) {
        filteredData = filteredData.filter(role => role.status === statusFilter.value)
      }
      
      total.value = filteredData.length
      
      // 前端分页
      const startIndex = (currentPage.value - 1) * pageSize.value
      const endIndex = startIndex + pageSize.value
      tableData.value = filteredData.slice(startIndex, endIndex)
    } else {
      // 后端已处理分页
      tableData.value = data?.list || []
      total.value = data?.total || 0
    }
    
    console.log('Loaded roles:', tableData.value)
  } catch (error) {
    console.error('Failed to load roles:', error)
    ElMessage.error(t('role.loadFailed'))
  } finally {
    loading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEditing.value = false
  formData.value = {
    roleCode: '',
    roleName: '',
    roleDesc: '',
    sortOrder: 10,
    isDefault: false,
    status: 1
  }
  showDialog.value = true
}

// 编辑角色
const editRole = (role: Role) => {
  isEditing.value = true
  formData.value = {
    roleId: role.roleId,
    roleCode: role.roleCode,
    roleName: role.roleName,
    roleDesc: role.roleDesc,
    sortOrder: role.sortOrder,
    isDefault: role.isDefault,
    status: role.status
  }
  showDialog.value = true
}

// 关闭对话框
const closeDialog = () => {
  showDialog.value = false
}

// 提交表单
const handleSubmit = async () => {
  submitLoading.value = true
  try {
    if (isEditing.value) {
      await roleApi.updateRole(formData.value)
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await roleApi.createRole(formData.value)
      ElMessage.success(t('common.createSuccess'))
    }
    closeDialog()
    loadRoles()
  } catch (error: any) {
    console.error('Failed to save role:', error)
    const message = error.response?.data?.message || t('common.saveFailed')
    ElMessage.error(message)
  } finally {
    submitLoading.value = false
  }
}

// 确认删除角色
const confirmDeleteRole = (role: Role) => {
  if (role.isDefault) {
    ElMessage.warning(t('role.cannotDeleteDefault'))
    return
  }
  
  deleteDialog.roleToDelete = role
  deleteDialog.visible = true
}

// 执行删除
const handleDelete = async () => {
  if (!deleteDialog.roleToDelete) return
  
  try {
    await roleApi.deleteRole(deleteDialog.roleToDelete.roleId)
    ElMessage.success(t('common.deleteSuccess'))
    deleteDialog.visible = false
    deleteDialog.roleToDelete = null
    loadRoles()
  } catch (error: any) {
    console.error('Failed to delete role:', error)
    const message = error.response?.data?.message || t('common.deleteFailed')
    ElMessage.error(message)
  }
}

onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
.table-container {
  background-color: var(--bg-raised);
  border-radius: 8px;
  border: 1px solid var(--border-default);
  padding: 16px;
  overflow-x: auto;
  overflow-y: hidden;
}

/* 移除了所有硬编码的颜色样式，完全使用 UnoCSS 和 CSS 变量 */
</style>
