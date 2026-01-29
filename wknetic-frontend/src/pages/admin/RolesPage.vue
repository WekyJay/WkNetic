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

    <!-- 数据表格 -->
    <DataTable
      :columns="columns"
      :data="roles"
      :loading="tableLoading"
      :empty-text="t('role.noRoles')"
      :loading-text="t('common.loading')"
      :actions-label="t('common.actions')"
      row-key="roleId"
    >
      <!-- 角色编码列 -->
      <template #column-roleCode="{ value }">
        <span
          class="inline-block px-3 py-1 rounded-full text-xs font-medium"
          :class="getRoleBadgeClass(value)"
        >
          {{ value }}
        </span>
      </template>

      <!-- 排序权重列 -->
      <template #column-sortOrder="{ value }">
        <span class="font-mono text-[var(--text-secondary)]">{{ value }}</span>
      </template>

      <!-- 默认角色列 -->
      <template #column-isDefault="{ value }">
        <span v-if="value" class="inline-block px-2 py-1 bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200 rounded text-xs font-medium">
          {{ t('common.yes') }}
        </span>
        <span v-else class="text-[var(--text-muted)]">{{ t('common.no') }}</span>
      </template>

      <!-- 状态列 -->
      <template #column-status="{ value }">
        <span
          v-if="value === 1"
          class="inline-block px-2 py-1 bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200 rounded text-xs font-medium"
        >
          {{ t('common.enabled') }}
        </span>
        <span v-else class="inline-block px-2 py-1 bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300 rounded text-xs font-medium">
          {{ t('common.disabled') }}
        </span>
      </template>

      <!-- 创建时间列 -->
      <template #column-createTime="{ value }">
        <span class="text-sm text-[var(--text-secondary)]">{{ formatDate(value) }}</span>
      </template>

      <!-- 操作列 -->
      <template #actions="{ row }">
        <div class="flex items-center justify-end gap-2">
          <button
            class="p-2 text-[var(--brand-default)] hover:bg-[var(--bg-hover)] rounded transition-colors"
            @click="showEditDialog(row)"
            :title="t('common.edit')"
          >
            <i class="i-tabler-edit text-lg"></i>
          </button>
          <button
            class="p-2 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            @click="confirmDelete(row)"
            :disabled="row.isDefault"
            :title="row.isDefault ? t('role.cannotDeleteDefault') : t('common.delete')"
          >
            <i class="i-tabler-trash text-lg"></i>
          </button>
        </div>
      </template>

      <!-- 空状态图标 -->
      <template #empty-icon>
        <i class="i-tabler-shield-off"></i>
      </template>
    </DataTable>

    <!-- 创建/编辑对话框 -->
    <div
      v-if="showDialog"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
      @click.self="closeDialog"
    >
      <div class="bg-[var(--bg-raised)] rounded-xl w-full max-w-2xl max-h-[90vh] overflow-auto shadow-xl">
        <!-- 对话框头部 -->
        <div class="flex justify-between items-center p-6 border-b border-[var(--border-default)]">
          <h3 class="text-xl font-semibold text-[var(--text-default)]">
            {{ isEditing ? t('role.editRole') : t('role.createRole') }}
          </h3>
          <button
            class="text-[var(--text-secondary)] hover:text-[var(--text-default)] transition-colors"
            @click="closeDialog"
          >
            <i class="i-tabler-x text-2xl"></i>
          </button>
        </div>

        <!-- 对话框内容 -->
        <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
          <!-- 角色编码 -->
          <div>
            <label class="block text-sm font-medium text-[var(--text-default)] mb-2">
              {{ t('role.roleCode') }} *
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
          <div>
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
          <div>
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
          <div class="grid grid-cols-2 gap-4">
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
          <div>
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

          <!-- 对话框底部 -->
          <div class="flex justify-end gap-3 pt-4 border-t border-[var(--border-default)]">
            <button
              type="button"
              class="px-4 py-2 bg-[var(--bg-surface)] text-[var(--text-default)] rounded-lg hover:bg-[var(--bg-hover)] transition-colors"
              @click="closeDialog"
            >
              {{ t('common.cancel') }}
            </button>
            <button
              type="submit"
              class="px-4 py-2 bg-[var(--brand-default)] text-white rounded-lg hover:opacity-90 transition-opacity disabled:opacity-50"
              :disabled="submitLoading"
            >
              {{ submitLoading ? t('common.saving') : t('common.save') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import DataTable, { type Column } from '@/components/common/DataTable.vue'
import { roleApi, type Role, type RoleFormData } from '@/api/role'

const { t } = useI18n()

const roles = ref<Role[]>([])
const showDialog = ref(false)
const isEditing = ref(false)
const tableLoading = ref(false)
const submitLoading = ref(false)

const formData = ref<RoleFormData>({
  roleCode: '',
  roleName: '',
  roleDesc: '',
  sortOrder: 10,
  isDefault: false,
  status: 1
})

// 表格列定义
const columns: Column[] = [
  { key: 'roleId', label: t('role.roleId'), sortable: true },
  { key: 'roleCode', label: t('role.roleCode'), sortable: true },
  { key: 'roleName', label: t('role.roleName'), sortable: false },
  { key: 'roleDesc', label: t('role.roleDesc'), sortable: false },
  { key: 'sortOrder', label: t('role.sortOrder'), sortable: true },
  { key: 'isDefault', label: t('role.isDefault'), sortable: false },
  { key: 'status', label: t('role.status'), sortable: true },
  { key: 'createTime', label: t('role.createTime'), sortable: true }
]

// 角色徽章样式
const getRoleBadgeClass = (code: string) => {
  const classes: Record<string, string> = {
    ADMIN: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
    MODERATOR: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
    VIP: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
    USER: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300',
    BANNED: 'bg-black text-white dark:bg-gray-900 dark:text-gray-100'
  }
  return classes[code] || 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200'
}

// 日期格式化
const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString()
}

// 加载角色列表
const loadRoles = async () => {
  tableLoading.value = true
  try {
    const data = await roleApi.getAllRoles()
    roles.value = data || []
  } catch (error) {
    console.error('Failed to load roles:', error)
    alert(t('role.loadFailed'))
  } finally {
    tableLoading.value = false
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

// 显示编辑对话框
const showEditDialog = (role: Role) => {
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
      alert(t('common.updateSuccess'))
    } else {
      await roleApi.createRole(formData.value)
      alert(t('common.createSuccess'))
    }
    closeDialog()
    loadRoles()
  } catch (error: any) {
    console.error('Failed to save role:', error)
    const message = error.response?.data?.message || t('common.saveFailed')
    alert(message)
  } finally {
    submitLoading.value = false
  }
}

// 确认删除
const confirmDelete = async (role: Role) => {
  if (role.isDefault) {
    alert(t('role.cannotDeleteDefault'))
    return
  }

  if (!confirm(t('role.confirmDelete', { name: role.roleName }))) {
    return
  }

  try {
    await roleApi.deleteRole(role.roleId)
    alert(t('common.deleteSuccess'))
    loadRoles()
  } catch (error: any) {
    console.error('Failed to delete role:', error)
    const message = error.response?.data?.message || t('common.deleteFailed')
    alert(message)
  }
}

onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
/* 移除了所有硬编码的颜色样式，完全使用 UnoCSS 和 CSS 变量 */
</style>
