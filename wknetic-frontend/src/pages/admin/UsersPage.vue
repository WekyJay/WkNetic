<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { userApi, type User, type UserRole, type UserStatus, USER_ROLE_MAP, USER_STATUS_MAP } from '@/api/user'
import { getMinecraftAvatarUrl } from '@/utils/minecraft'
import UserFormModal from '@/components/admin/UserFormModal.vue'
import ConfirmModal from '@/components/common/ConfirmModal.vue'

const loading = ref(false)
const users = ref<User[]>([])
const total = ref(0)

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

const pageCount = computed(() => Math.ceil(total.value / queryParams.size))

onMounted(() => {
  loadUsers()
})

async function loadUsers() {
  loading.value = true
  try {
    const result = await userApi.getUserList(queryParams)
    users.value = result.records
    total.value = result.total
  } catch (error: any) {
    alert('加载用户列表失败: ' + error.message)
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
  confirmModal.visible = true
  confirmModal.title = '操作成功'
  confirmModal.content = message
  confirmModal.type = 'success'
  confirmModal.buttonType = 'confirm-only'
  confirmModal.onConfirm = () => {
    confirmModal.visible = false
  }
}

// 显示错误消息
function showErrorMessage(message: string) {
  confirmModal.visible = true
  confirmModal.title = '操作失败'
  confirmModal.content = message
  confirmModal.type = 'danger'
  confirmModal.buttonType = 'confirm-only'
  confirmModal.onConfirm = () => {
    confirmModal.visible = false
  }
}

function getRoleBadgeColor(role: UserRole): string {
  const colors = {
    ADMIN: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
    MODERATOR: 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200',
    USER: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
    VIP: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
    BANNED: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200'
  }
  return colors[role] || colors.USER
}

function getStatusBadgeColor(status: UserStatus): string {
  return status === 1
    ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
    : 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200'
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
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">用户管理</h1>
        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
          管理系统用户及其权限
        </p>
      </div>
      <button
        @click="handleCreate"
        class="flex items-center gap-2 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
      >
        <i class="i-tabler-user-plus w-5 h-5" />
        新建用户
      </button>
    </div>

    <!-- Filters -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="md:col-span-2">
          <input
            v-model="queryParams.keyword"
            type="text"
            placeholder="搜索用户名、邮箱、MC账号..."
            class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            @keyup.enter="handleSearch"
          />
        </div>
        <div>
          <select
            v-model="queryParams.status"
            class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          >
            <option :value="undefined">全部状态</option>
            <option :value="1">启用</option>
            <option :value="0">禁用</option>
          </select>
        </div>
        <div>
          <select
            v-model="queryParams.role"
            class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          >
            <option :value="undefined">全部角色</option>
            <option value="ADMIN">管理员</option>
            <option value="MODERATOR">版主</option>
            <option value="USER">普通用户</option>
            <option value="VIP">VIP会员</option>
            <option value="BANNED">封禁</option>
          </select>
        </div>
      </div>
      <div class="flex gap-2 mt-4">
        <button
          @click="handleSearch"
          class="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
        >
          <i class="i-tabler-search w-4 h-4" />
          搜索
        </button>
        <button
          @click="handleReset"
          class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600"
        >
          <i class="i-tabler-refresh w-4 h-4" />
          重置
        </button>
      </div>
    </div>

    <!-- Table -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 dark:bg-gray-700">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">用户</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">邮箱</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">角色</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">状态</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">MC账号</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">创建时间</th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
            <tr v-if="loading" class="animate-pulse">
              <td colspan="7" class="px-6 py-4 text-center text-gray-500 dark:text-gray-400">加载中...</td>
            </tr>
            <tr v-else-if="users.length === 0">
              <td colspan="7" class="px-6 py-4 text-center text-gray-500 dark:text-gray-400">暂无数据</td>
            </tr>
            <tr v-else v-for="user in users" :key="user.userId" class="hover:bg-gray-50 dark:hover:bg-gray-700">
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center gap-3">
                  <img
                    :src="user.avatar || `https://ui-avatars.com/api/?name=${user.username}`"
                    :alt="user.username"
                    class="w-10 h-10 rounded-full"
                  />
                  <div>
                    <div class="font-medium text-gray-900 dark:text-white">{{ user.username }}</div>
                    <div class="text-sm text-gray-500 dark:text-gray-400">{{ user.nickname }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ user.email || '-' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getRoleBadgeColor(user.role)" class="px-2 py-1 text-xs font-semibold rounded-full">
                  {{ USER_ROLE_MAP[user.role] }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getStatusBadgeColor(user.status)" class="px-2 py-1 text-xs font-semibold rounded-full">
                  {{ USER_STATUS_MAP[user.status] }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div v-if="user.minecraftUuid" class="flex items-center gap-2">
                  <img
                    :src="getMinecraftAvatarUrl(user.minecraftUuid, 32)"
                    :alt="user.minecraftUsername"
                    class="w-6 h-6 rounded"
                  />
                  <span class="text-sm text-gray-900 dark:text-white">{{ user.minecraftUsername }}</span>
                </div>
                <span v-else class="text-sm text-gray-500 dark:text-gray-400">-</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                {{ formatDate(user.createTime) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <div class="flex items-center justify-end gap-2">
                  <button
                    @click="handleEdit(user)"
                    class="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300"
                    title="编辑"
                  >
                    <i class="i-tabler-edit w-5 h-5" />
                  </button>
                  <button
                    @click="handleToggleStatus(user)"
                    :class="user.status === 1 ? 'text-yellow-600 hover:text-yellow-800' : 'text-green-600 hover:text-green-800'"
                    :title="user.status === 1 ? '禁用' : '启用'"
                  >
                    <i :class="user.status === 1 ? 'i-tabler-ban' : 'i-tabler-check'" class="w-5 h-5" />
                  </button>
                  <button
                    @click="handleDelete(user)"
                    class="text-red-600 hover:text-red-800 dark:text-red-400 dark:hover:text-red-300"
                    title="删除"
                  >
                    <i class="i-tabler-trash w-5 h-5" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="total > 0" class="px-6 py-4 border-t border-gray-200 dark:border-gray-700 flex items-center justify-between">
        <div class="text-sm text-gray-700 dark:text-gray-300">
          显示 {{ (queryParams.page - 1) * queryParams.size + 1 }} - {{ Math.min(queryParams.page * queryParams.size, total) }} 
          / 共 {{ total }} 条
        </div>
        <div class="flex items-center gap-2">
          <select
            v-model="queryParams.size"
            @change="handleSizeChange(queryParams.size)"
            class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm"
          >
            <option :value="10">10 / 页</option>
            <option :value="20">20 / 页</option>
            <option :value="50">50 / 页</option>
            <option :value="100">100 / 页</option>
          </select>
          <button
            @click="handlePageChange(queryParams.page - 1)"
            :disabled="queryParams.page === 1"
            class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded bg-white dark:bg-gray-700 text-gray-900 dark:text-white disabled:opacity-50"
          >
            <i class="i-tabler-chevron-left w-4 h-4" />
          </button>
          <span class="px-3 py-1 text-sm text-gray-700 dark:text-gray-300">
            {{ queryParams.page }} / {{ pageCount }}
          </span>
          <button
            @click="handlePageChange(queryParams.page + 1)"
            :disabled="queryParams.page >= pageCount"
            class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded bg-white dark:bg-gray-700 text-gray-900 dark:text-white disabled:opacity-50"
          >
            <i class="i-tabler-chevron-right w-4 h-4" />
          </button>
        </div>
      </div>
    </div>

    <!-- User Form Modal -->
    <UserFormModal
      v-model:visible="modalVisible"
      :mode="modalMode"
      :user="currentUser"
      @success="handleModalSuccess"
    />

    <!-- Confirm Modal -->
    <ConfirmModal
      v-model:visible="confirmModal.visible"
      :title="confirmModal.title"
      :content="confirmModal.content"
      :type="confirmModal.type"
      :button-type="confirmModal.buttonType"
      :loading="confirmModal.loading"
      @confirm="confirmModal.onConfirm"
    />
  </div>
</template>
