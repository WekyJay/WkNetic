<template>
  <div class="space-y-6">
    <!-- 页头 -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-[var(--text-default)]">内容审核中心</h1>
        <p class="text-[var(--text-secondary)] mt-1">管理论坛内容，确保社区健康</p>
      </div>
      <div class="flex items-center gap-2">
        <WkBadge variant="warning" size="lg">
          {{ pendingCount }} 待审核
        </WkBadge>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
      <WkCard :hoverable="true">
        <div class="flex items-start justify-between">
          <div class="w-11 h-11 rounded-xl flex items-center justify-center bg-orange-500/15 text-orange-500">
            <i class="i-tabler-inbox text-xl" />
          </div>
        </div>
        <div class="mt-4">
          <p class="text-2xl font-bold text-[var(--text-default)]">{{ statistics.pendingCount }}</p>
          <p class="text-sm text-[var(--text-secondary)] mt-1">待审核</p>
        </div>
      </WkCard>

      <WkCard :hoverable="true">
        <div class="flex items-start justify-between">
          <div class="w-11 h-11 rounded-xl flex items-center justify-center bg-[var(--brand-default)]/15 text-[var(--brand-default)]">
            <i class="i-tabler-check text-xl" />
          </div>
        </div>
        <div class="mt-4">
          <p class="text-2xl font-bold text-[var(--text-default)]">{{ statistics.todayApprovedCount }}</p>
          <p class="text-sm text-[var(--text-secondary)] mt-1">今日通过</p>
        </div>
      </WkCard>

      <WkCard :hoverable="true">
        <div class="flex items-start justify-between">
          <div class="w-11 h-11 rounded-xl flex items-center justify-center bg-red-500/15 text-red-500">
            <i class="i-tabler-x text-xl" />
          </div>
        </div>
        <div class="mt-4">
          <p class="text-2xl font-bold text-[var(--text-default)]">{{ statistics.todayRejectedCount }}</p>
          <p class="text-sm text-[var(--text-secondary)] mt-1">今日拒绝</p>
        </div>
      </WkCard>

      <WkCard :hoverable="true">
        <div class="flex items-start justify-between">
          <div class="w-11 h-11 rounded-xl flex items-center justify-center bg-blue-500/15 text-blue-500">
            <i class="i-tabler-chart-pie text-xl" />
          </div>
        </div>
        <div class="mt-4">
          <p class="text-2xl font-bold text-[var(--text-default)]">{{ statistics.approvalRate }}%</p>
          <p class="text-sm text-[var(--text-secondary)] mt-1">通过率</p>
        </div>
      </WkCard>

      <WkCard :hoverable="true">
        <div class="flex items-start justify-between">
          <div class="w-11 h-11 rounded-xl flex items-center justify-center bg-purple-500/15 text-purple-500">
            <i class="i-tabler-chart-line text-xl" />
          </div>
        </div>
        <div class="mt-4">
          <p class="text-2xl font-bold text-[var(--text-default)]">{{ statistics.weekApprovedCount }}</p>
          <p class="text-sm text-[var(--text-secondary)] mt-1">本周通过</p>
        </div>
      </WkCard>

      <WkCard :hoverable="true">
        <div class="flex items-start justify-between">
          <div class="w-11 h-11 rounded-xl flex items-center justify-center bg-[var(--brand-default)]/15 text-[var(--brand-default)]">
            <i class="i-tabler-list-check text-xl" />
          </div>
        </div>
        <div class="mt-4">
          <p class="text-2xl font-bold text-[var(--text-default)]">{{ statistics.totalAuditCount }}</p>
          <p class="text-sm text-[var(--text-secondary)] mt-1">总审核</p>
        </div>
      </WkCard>
    </div>

    <!-- 标签页 -->
    <WkCard>
      <!-- 标签导航 -->
      <div class="flex gap-0 border-b border-[var(--border-default)] mb-5">
        <button 
          class="px-6 py-3 bg-transparent border-none text-[var(--text-secondary)] text-sm font-medium cursor-pointer transition-all relative hover:text-[var(--brand-default)]"
          :class="activeTab === 'pending' ? 'text-[var(--brand-default)] after:content-empty after:absolute after:bottom-0 after:left-0 after:right-0 after:h-0.5 after:bg-[var(--brand-default)]' : ''"
          @click="activeTab = 'pending'; handleTabChange('pending')"
        >
          待审核
        </button>
        <button 
          class="px-6 py-3 bg-transparent border-none text-[var(--text-secondary)] text-sm font-medium cursor-pointer transition-all relative hover:text-[var(--brand-default)]"
          :class="activeTab === 'history' ? 'text-[var(--brand-default)] after:content-empty after:absolute after:bottom-0 after:left-0 after:right-0 after:h-0.5 after:bg-[var(--brand-default)]' : ''"
          @click="activeTab = 'history'; handleTabChange('history')"
        >
          审核历史
        </button>
      </div>

      <!-- 待审核标签页 -->
      <div v-if="activeTab === 'pending'">
        <!-- 工具栏 -->
        <div class="flex gap-3 mb-5 items-center">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索标题..."
            class="max-w-75"
            clearable
            @keyup.enter="loadPendingPosts"
          />
          <WkButton variant="primary" size="sm" icon="i-tabler-search" @click="loadPendingPosts">
            搜索
          </WkButton>
        </div>

        <!-- 帖子列表 -->
        <div v-loading="loading">
          <div v-if="pendingPosts.length > 0" class="flex flex-col">
            <div
              v-for="post in pendingPosts"
              :key="post.id"
              class="p-5 border-b border-[var(--border-default)] last:border-b-0 transition-all hover:bg-[var(--bg-hover)]"
            >
              <!-- 卡片顶部 -->
              <div class="flex justify-between items-start mb-3">
                <div class="flex gap-3 items-center flex-1">
                  <img 
                    v-if="post.author?.avatar" 
                    :src="post.author.avatar" 
                    class="w-10 h-10 rounded-full object-cover flex-shrink-0" 
                  />
                  <div v-else class="w-10 h-10 rounded-full bg-[var(--bg-surface)] flex-shrink-0"></div>
                  <div class="flex-1">
                    <div class="font-semibold text-[var(--text-default)]">{{ post.author?.nickname }}</div>
                    <div class="text-xs text-[var(--text-muted)] mt-1">{{ formatTime(post.createTime) }}</div>
                  </div>
                </div>
                <WkBadge variant="warning">待审核</WkBadge>
              </div>

              <!-- 标题 -->
              <div class="text-base font-semibold mb-2.5 leading-relaxed text-[var(--text-default)] hover:text-[var(--brand-default)] cursor-pointer">
                {{ post.title }}
              </div>

              <!-- 话题和标签 -->
              <div class="flex gap-2 items-center mb-3 flex-wrap">
                <WkBadge variant="default" size="sm">
                  {{ post.topic?.name || '未分类' }}
                </WkBadge>
                <WkBadge
                  v-for="tag in post.tags?.slice(0, 2)"
                  :key="tag.id"
                  variant="info"
                  size="sm"
                >
                  {{ tag.name }}
                </WkBadge>
                <span v-if="post.tags && post.tags.length > 2" class="text-xs text-[var(--text-muted)]">
                  +{{ post.tags.length - 2 }}
                </span>
              </div>

              <!-- 内容预览 -->
              <div class="mb-3">
                <div class="text-sm leading-relaxed text-[var(--text-secondary)]">{{ getContentPreview(post.content) }}</div>
              </div>

              <!-- 统计数据 -->
              <div class="flex gap-5 mb-3 text-sm text-[var(--text-muted)] py-2 border-t border-b border-[var(--border-light)]">
                <span class="flex items-center gap-1">
                  <i class="i-tabler-eye text-sm" /> {{ post.viewCount || 0 }}
                </span>
                <span class="flex items-center gap-1">
                  <i class="i-tabler-message text-sm" /> {{ post.commentCount || 0 }}
                </span>
                <span class="flex items-center gap-1">
                  <i class="i-tabler-star-filled text-sm" /> {{ post.likeCount || 0 }}
                </span>
              </div>

              <!-- 操作按钮 -->
              <div class="flex gap-3 pt-3">
                <WkButton variant="text" size="sm" @click="viewDetail(post.id)">
                  查看详情
                </WkButton>
                <WkButton variant="primary" size="sm" icon="i-tabler-check" @click="approvePost(post.id)">
                  通过
                </WkButton>
                <WkButton variant="danger" size="sm" icon="i-tabler-x" @click="showRejectDialog(post.id)">
                  拒绝
                </WkButton>
              </div>
            </div>
          </div>
          <div v-else class="flex flex-col items-center justify-center py-15 text-[var(--text-muted)]">
            <i class="i-tabler-inbox-off text-5xl mb-4 opacity-50" />
            <p class="text-sm m-0">暂无待审核内容</p>
          </div>

          <!-- 分页 -->
          <div v-if="pendingTotal > 0" class="flex justify-center p-5 border-t border-[var(--border-default)]">
            <el-pagination
              v-model:current-page="pendingPage"
              v-model:page-size="pageSize"
              :total="pendingTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="loadPendingPosts"
              @size-change="loadPendingPosts"
            />
          </div>
        </div>
      </div>

      <!-- 审核历史标签页 -->
      <div v-if="activeTab === 'history'">
        <!-- 筛选 -->
        <div class="flex gap-3 mb-5 items-center">
          <el-select
            v-model="historyStatus"
            placeholder="审核状态"
            clearable
            @change="loadHistory"
            class="max-w-50"
          >
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="3" />
          </el-select>
        </div>

        <!-- 历史列表 -->
        <div v-loading="loading">
          <div v-if="historyPosts.length > 0" class="flex flex-col">
            <div
              v-for="post in historyPosts"
              :key="post.id"
              class="p-5 border-b border-[var(--border-default)] last:border-b-0 transition-all bg-[var(--bg-raised)] hover:bg-[var(--bg-hover)]"
            >
              <!-- 卡片顶部 -->
              <div class="flex justify-between items-start mb-3">
                <div class="flex gap-3 items-center flex-1">
                  <img 
                    v-if="post.author?.avatar" 
                    :src="post.author.avatar" 
                    class="w-10 h-10 rounded-full object-cover flex-shrink-0" 
                  />
                  <div v-else class="w-10 h-10 rounded-full bg-[var(--bg-surface)] flex-shrink-0"></div>
                  <div class="flex-1">
                    <div class="font-semibold text-[var(--text-default)]">{{ post.author?.nickname }}</div>
                    <div class="text-xs text-[var(--text-muted)] mt-1">{{ formatTime(post.createTime) }}</div>
                  </div>
                </div>
                <WkBadge :variant="post.status === 1 ? 'success' : 'danger'">
                  {{ post.status === 1 ? '已通过' : '已拒绝' }}
                </WkBadge>
              </div>

              <!-- 标题 -->
              <div class="text-base font-semibold mb-2.5 leading-relaxed text-[var(--text-default)]">
                {{ post.title }}
              </div>

              <!-- 内容预览 -->
              <div class="mb-3">
                <div class="text-sm leading-relaxed text-[var(--text-secondary)]">{{ getContentPreview(post.content) }}</div>
              </div>

              <!-- 审核信息 -->
              <div class="bg-[var(--bg-surface)] p-3 rounded-[var(--radius-md)] mt-3 border border-[var(--border-default)]">
                <div class="text-sm mb-2 flex gap-2">
                  <span class="font-medium text-[var(--text-secondary)] min-w-15">审核人：</span>
                  <span class="text-[var(--text-default)]">{{ post.auditorName || '系统' }}</span>
                </div>
                <div class="text-sm mb-2 flex gap-2">
                  <span class="font-medium text-[var(--text-secondary)] min-w-15">审核时间：</span>
                  <span class="text-[var(--text-default)]">{{ formatTime(post.auditTime) }}</span>
                </div>
                <div v-if="post.status === 3 && post.auditRemark" class="text-sm flex gap-2">
                  <span class="font-medium text-[var(--text-secondary)] min-w-15">拒绝原因：</span>
                  <span class="text-red-500 flex-1">{{ post.auditRemark }}</span>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="flex gap-3 pt-3">
                <WkButton variant="text" size="sm" @click="viewDetail(post.id)">
                  查看详情
                </WkButton>
              </div>
            </div>
          </div>
          <div v-else class="flex flex-col items-center justify-center py-15 text-[var(--text-muted)]">
            <i class="i-tabler-inbox-off text-5xl mb-4 opacity-50" />
            <p class="text-sm m-0">暂无审核记录</p>
          </div>

          <!-- 分页 -->
          <div v-if="historyTotal > 0" class="flex justify-center p-5 border-t border-[var(--border-default)]">
            <el-pagination
              v-model:current-page="historyPage"
              v-model:page-size="pageSize"
              :total="historyTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="loadHistory"
              @size-change="loadHistory"
            />
          </div>
        </div>
      </div>
    </WkCard>

    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝原因"
      width="600px"
      align-center
    >
      <el-form>
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明拒绝原因，将通知作者"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 常用模板 -->
        <el-form-item label="快速选择">
          <div class="flex flex-wrap gap-2">
            <button
              v-for="template in rejectTemplates"
              :key="template"
              @click="rejectReason = template"
              class="px-2 py-1 bg-[var(--bg-hover)] text-[var(--text-secondary)] border border-[var(--border-default)] rounded-[var(--radius-sm)] cursor-pointer text-xs transition-all hover:bg-[var(--brand-default)] hover:text-white hover:border-[var(--brand-default)]"
            >
              {{ template }}
            </button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex gap-3 justify-end">
          <WkButton variant="secondary" @click="rejectDialogVisible = false">
            取消
          </WkButton>
          <WkButton variant="danger" @click="confirmReject" :disabled="rejectLoading">
            确定拒绝
          </WkButton>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import WkCard from '@/components/common/WkCard.vue'
import WkButton from '@/components/common/WkButton.vue'
import WkBadge from '@/components/common/WkBadge.vue'
import {
  getPendingPosts,
  approvePost as approvePostApi,
  rejectPost as rejectPostApi,
  getAuditHistory,
  getPendingPostCount,
  getAuditStatistics,
  type AuditStatistics,
  type AuditPostVO
} from '@/api/audit'

const router = useRouter()

// 状态管理
const activeTab = ref<'pending' | 'history'>('pending')
const loading = ref(false)
const rejectLoading = ref(false)

// 待审核列表
const pendingPosts = ref<AuditPostVO[]>([])
const pendingPage = ref(1)
const pageSize = ref(10)
const pendingTotal = ref(0)
const pendingCount = ref(0)
const searchKeyword = ref('')

// 审核历史列表
const historyPosts = ref<AuditPostVO[]>([])
const historyPage = ref(1)
const historyTotal = ref(0)
const historyStatus = ref<number | undefined>(undefined)

// 审核统计
const statistics = ref<AuditStatistics>({
  pendingCount: 0,
  todayApprovedCount: 0,
  todayRejectedCount: 0,
  weekApprovedCount: 0,
  weekRejectedCount: 0,
  approvalRate: 0,
  averageAuditTime: 0,
  totalAuditCount: 0
})

// 拒绝对话框
const rejectDialogVisible = ref(false)
const rejectingPostId = ref<number | null>(null)
const rejectReason = ref('')
const rejectTemplates = [
  '内容与社区主题无关',
  '含有不当言论或骚扰',
  '营销/广告内容',
  '低质量内容',
  '标题不符合要求',
]

/**
 * 加载待审核帖子
 */
async function loadPendingPosts() {
  try {
    loading.value = true
    const res = await getPendingPosts({
      page: pendingPage.value,
      size: pageSize.value
    })
    pendingPosts.value = res.data?.records || []
    pendingTotal.value = res.data?.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载待审核内容失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载审核历史
 */
async function loadHistory() {
  try {
    loading.value = true
    const res = await getAuditHistory({
      page: historyPage.value,
      size: pageSize.value,
      status: historyStatus.value
    })
    historyPosts.value = res.data?.records || []
    historyTotal.value = res.data?.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载审核历史失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载待审核数量
 */
async function loadPendingCount() {
  try {
    const res = await getPendingPostCount()
    pendingCount.value = res.data || 0
  } catch (error) {
    console.error('加载待审核数量失败', error)
  }
}

/**
 * 加载审核统计
 */
async function loadStatistics() {
  try {
    const res = await getAuditStatistics()
    statistics.value = res.data || {}
  } catch (error: any) {
    console.error('加载审核统计失败', error)
  }
}

/**
 * 处理标签页切换
 */
function handleTabChange(tab: string) {
  if (tab === 'history') {
    loadHistory()
  } else {
    loadPendingCount()
  }
}

/**
 * 批准帖子
 */
async function approvePost(postId: number) {
  try {
    loading.value = true
    await approvePostApi(postId)
    ElMessage.success('已通过审核')
    await loadPendingPosts()
    await loadPendingCount()
    await loadStatistics()
  } catch (error: any) {
    ElMessage.error(error.message || '审核失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 显示拒绝对话框
 */
function showRejectDialog(postId: number) {
  rejectingPostId.value = postId
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

/**
 * 确认拒绝
 */
async function confirmReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  try {
    rejectLoading.value = true
    await rejectPostApi(rejectingPostId.value!, rejectReason.value)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    await loadPendingPosts()
    await loadPendingCount()
    await loadStatistics()
  } catch (error: any) {
    ElMessage.error(error.message || '拒绝失败')
    console.error(error)
  } finally {
    rejectLoading.value = false
  }
}

/**
 * 查看详情
 */
function viewDetail(postId: number) {
  router.push(`/post/${postId}`)
}

/**
 * 获取内容预览
 */
function getContentPreview(content: string | undefined, length = 150): string {
  if (!content) return '暂无内容'
  const text = content.replace(/[#*`>\-\[\]]/g, '').trim()
  return text.length > length
    ? text.substring(0, length) + '...'
    : text
}

/**
 * 格式化时间
 */
function formatTime(timeStr: string | undefined): string {
  if (!timeStr) return '-'
  try {
    const date = new Date(timeStr)
    return date.toLocaleString('zh-CN')
  } catch {
    return timeStr
  }
}

// 生命周期
onMounted(() => {
  loadPendingCount()
  loadPendingPosts()
  loadStatistics()
})
</script>