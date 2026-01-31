<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { userApi, type User, type UserRole, type UserStatus, USER_ROLE_MAP, USER_STATUS_MAP } from '@/api/user'
import { getMinecraftAvatarUrl } from '@/utils/minecraft'
import UserFormModal from '@/components/admin/UserFormModal.vue'
import { WkButton, WkInput, WkBadge, WkAlert, WkLoading, WkDialog } from '@/components/common'


const loading = ref(false)
const users = ref<User[]>([])
const total = ref(0)
const errorMessage = ref('')
const successMessage = ref('')

// 确认框状态
const confirmModal = reactive({
  visible: false,
  title: '',
  content: '',
  type: 'info' as 'warning' | 'danger' | 'info' | 'success',
  buttonType: 'both' as 'cancel-only' | 'confirm-only' | 'both',
  loading: false,
  onConfirm: () => {}
})

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined as UserStatus | undefined,
  role: undefined as UserRole | undefined
})

const modalVisible = ref(false)
const modalMode = ref<'create' | 'edit'>('create')
const currentUser = ref<User | null>(null)
const userFormRef = ref<any>(null)

const pageCount = computed(() => Math.ceil(total.value / queryParams.size))

onMounted(() => {
  loadUsers()
})

async function loadUsers() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await userApi.getUserList(queryParams)
    // 安全地访问数据结构
    if (result) {
      users.value = result.records
      total.value = result.total
    } else {
      users.value = []
      total.value = 0
    }
    console.log('API Response:', result) // 调试日志
  } catch (error: any) {
    console.error('Load users error:', error)
    errorMessage.value = '加载用户列表失败: ' + error.message
    users.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 1
  loadUsers()
}

function handleReset() {
  queryParams.keyword = ''
  queryParams.status = undefined
  queryParams.role = undefined
  queryParams.page = 1
  loadUsers()
}

function handlePageChange(page: number) {
  queryParams.page = page
  loadUsers()
}

function handleSizeChange(size: number) {
  queryParams.size = size
  queryParams.page = 1
  loadUsers()
}

function handleCreate() {
  modalMode.value = 'create'
  currentUser.value = null
  modalVisible.value = true
}

function handleEdit(user: User) {
  modalMode.value = 'edit'
  currentUser.value = user
  modalVisible.value = true
}

function handleDelete(user: User) {
  confirmModal.visible = true
  confirmModal.title = '删除用户'
  confirmModal.content = `确定要删除用户"${user.username}"吗？此操作不可恢复。`
  confirmModal.type = 'danger'
  confirmModal.buttonType = 'both'
  confirmModal.onConfirm = async () => {
    confirmModal.loading = true
    try {
      await userApi.deleteUser(user.userId)
      confirmModal.visible = false
      confirmModal.loading = false
      
      // 显示成功提示
      showSuccessMessage('删除成功')
      loadUsers()
    } catch (error: any) {
      confirmModal.loading = false
      confirmModal.visible = false
      showErrorMessage('删除失败: ' + error.message)
    }
  }
}

function handleToggleStatus(user: User) {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  
  confirmModal.visible = true
  confirmModal.title = `${action}用户`
  confirmModal.content = `确定要${action}用户"${user.username}"吗？`
  confirmModal.type = newStatus === 0 ? 'warning' : 'info'
  confirmModal.buttonType = 'both'
  confirmModal.onConfirm = async () => {
    confirmModal.loading = true
    try {
      await userApi.toggleUserStatus(user.userId, newStatus as UserStatus)
      confirmModal.visible = false
      confirmModal.loading = false
      
      // 显示成功提示
      showSuccessMessage(`${action}成功`)
      loadUsers()
    } catch (error: any) {
      confirmModal.loading = false
      confirmModal.visible = false
      showErrorMessage(`${action}失败: ` + error.message)
    }
  }
}

function handleModalSuccess() {
  loadUsers()
}

// 显示成功消息
function showSuccessMessage(message: string) {
  successMessage.value = message
  setTimeout(() => {
    successMessage.value = ''
  }, 3000)
}

// 显示错误消息
function showErrorMessage(message: string) {
  errorMessage.value = message
  setTimeout(() => {
    errorMessage.value = ''
  }, 5000)
}

function getRoleBadgeVariant(role: UserRole): 'danger' | 'warning' | 'success' | '' {
  const variants = {
    ADMIN: 'danger' as const,
    MODERATOR: 'warning' as const,
    USER: '' as const,
    VIP: 'success' as const,
    BANNED: 'danger' as const
  }
  return variants[role] || ''
}


function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<template>
  <div class="p-6 space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-[var(--text-default)]">用户管理</h1>
        <p class="mt-1 text-sm text-[var(--text-secondary)]">
          管理系统用户及其权限
        </p>
      </div>
      <WkButton
        variant="primary"
        icon="i-tabler-user-plus"
        @click="handleCreate"
      >
        新建用户
      </WkButton>
    </div>

    <!-- Success/Error Messages -->
    <WkAlert 
      v-if="successMessage"
      type="success" 
      :message="successMessage"
      :closable="true"
      @close="successMessage = ''"
    />
    
    <WkAlert 
      v-if="errorMessage"
      type="error" 
      :message="errorMessage"
      :closable="true"
      @close="errorMessage = ''"
    />

    <!-- Loading -->
    <WkLoading :loading="loading" fullscreen text="加载中..." />

    <!-- Filters -->
    <div class="bg-[var(--bg-raised)] rounded-lg border border-[var(--border-default)] p-4">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="md:col-span-2">
          <WkInput
            v-model="queryParams.keyword"
            placeholder="搜索用户名、邮箱、MC账号..."
            prefix-icon="i-tabler-search"
            clearable
            @keyup.enter="handleSearch"
          />
        </div>
        <div>
          <ElSelect
            v-model="queryParams.status"
            placeholder="全部状态"
            clearable
            style="width: 100%"
          >
            <ElOption label="启用" :value="1" />
            <ElOption label="禁用" :value="0" />
          </ElSelect>
        </div>
        <div>
          <ElSelect
            v-model="queryParams.role"
            placeholder="全部角色"
            clearable
            style="width: 100%"
          >
            <ElOption label="管理员" value="ADMIN" />
            <ElOption label="版主" value="MODERATOR" />
            <ElOption label="普通用户" value="USER" />
            <ElOption label="VIP会员" value="VIP" />
            <ElOption label="封禁" value="BANNED" />
          </ElSelect>
        </div>
      </div>
      <div class="flex gap-2 mt-4">
        <WkButton
          variant="primary"
          icon="i-tabler-search"
          @click="handleSearch"
        >
          搜索
        </WkButton>
        <WkButton
          variant="secondary"
          icon="i-tabler-refresh"
          @click="handleReset"
        >
          重置
        </WkButton>
      </div>
    </div>

    <!-- Table -->
    <div class="bg-[var(--bg-raised)] rounded-lg border border-[var(--border-default)] overflow-hidden">
      <el-table
        :data="users"
        style="width: 100%"
        :empty-text="'暂无数据'"
        row-key="userId"
        class="!border-none"
      >
        <el-table-column label="用户" min-width="150">
          <template #default="scope">
            <div class="flex items-center gap-3">
              <img
                :src="scope.row.avatar || `https://ui-avatars.com/api/?name=${scope.row.username}`"
                :alt="scope.row.username"
                class="w-10 h-10 rounded-full"
              />
              <div>
                <div class="font-medium text-[var(--text-default)]">{{ scope.row.username }}</div>
                <div class="text-sm text-[var(--text-secondary)]">{{ scope.row.nickname }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="email" label="邮箱" min-width="150">
          <template #default="scope">
            <span class="text-[var(--text-default)]">{{ scope.row.email || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="角色" width="100">
          <template #default="scope">
            <ElTag
              :type="getRoleBadgeVariant(scope.row.role)"
              size="small"
            >
              {{ USER_ROLE_MAP[scope.row.role] }}
            </ElTag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="scope">
            <ElTag
              :type="scope.row.status === 1 ? 'success' : 'info'"
              size="small"
            >
              {{ USER_STATUS_MAP[scope.row.status] }}
            </ElTag>
          </template>
        </el-table-column>

        <el-table-column label="MC账号" min-width="120">
          <template #default="scope">
            <div v-if="scope.row.minecraftUuid" class="flex items-center gap-2">
              <img
                :src="getMinecraftAvatarUrl(scope.row.minecraftUuid, 32)"
                :alt="scope.row.minecraftUsername"
                class="w-6 h-6 rounded"
              />
              <span class="text-sm text-[var(--text-default)]">{{ scope.row.minecraftUsername }}</span>
            </div>
            <span v-else class="text-sm text-[var(--text-secondary)]">-</span>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="200">
          <template #default="scope">
            <span class="text-sm text-[var(--text-secondary)]">
              {{ formatDate(scope.row.createTime) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="scope">
            <div class="flex items-center gap-1">
              <WkButton
                variant="ghost"
                size="sm"
                icon="i-tabler-edit"
                @click="handleEdit(scope.row)"
              />
              <WkButton
                variant="ghost"
                size="sm"
                :icon="scope.row.status === 1 ? 'i-tabler-ban' : 'i-tabler-check'"
                @click="handleToggleStatus(scope.row)"
              />
              <WkButton
                variant="danger"
                size="sm"
                icon="i-tabler-trash"
                @click="handleDelete(scope.row)"
              />
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div v-if="total > 0" class="px-6 py-4 border-t border-[var(--border-default)] flex justify-end">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          class="!bg-transparent"
        />
      </div>
    </div>

    <!-- User Form (inside WkDialog) -->
    <WkDialog v-model="modalVisible" :title="modalMode === 'create' ? '创建用户' : '编辑用户'" size="lg">
      <UserFormModal
        ref="userFormRef"
        :visible="modalVisible"
        :mode="modalMode"
        :user="currentUser"
        :asDialog="true"
        @update:visible="(v) => (modalVisible = v)"
        @success="handleModalSuccess"
      />

      <template #footer>
        <WkButton variant="ghost" @click="modalVisible = false">取消</WkButton>
        <WkButton variant="primary" @click="userFormRef?.submit()">保存</WkButton>
      </template>
    </WkDialog>

    <!-- Confirm Dialog (WkDialog) -->
    <WkDialog v-model="confirmModal.visible" :title="confirmModal.title" size="sm">
      <p class="text-[var(--text-secondary)]">{{ confirmModal.content }}</p>

      <template #footer>
        <WkButton variant="ghost" @click="confirmModal.visible = false">取消</WkButton>
        <WkButton :variant="confirmModal.type === 'danger' ? 'danger' : 'primary'" :loading="confirmModal.loading" @click="confirmModal.onConfirm">确定</WkButton>
      </template>
    </WkDialog>
  </div>
</template>
<style scoped>
</style>