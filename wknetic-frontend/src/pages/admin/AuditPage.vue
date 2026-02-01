<template>
  <div class="audit-page">
    <div class="page-header">
      <h1>内容审核</h1>
      <el-badge :value="pendingCount" :max="99" class="badge">
        <el-tag type="warning">待审核</el-tag>
      </el-badge>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="待审核" name="pending">
        <el-card v-loading="loading">
          <div v-if="pendingPosts.length > 0" class="posts-list">
            <div v-for="post in pendingPosts" :key="post.id" class="post-item">
              <div class="post-header">
                <div class="post-info">
                  <h3 class="post-title">{{ post.title }}</h3>
                  <div class="post-meta">
                    <el-avatar :src="post.author?.avatar" :size="24" />
                    <span class="author">{{ post.author?.nickname }}</span>
                    <span class="time">{{ formatTime(post.createTime) }}</span>
                    <el-tag size="small" type="info">{{ post.topicName }}</el-tag>
                  </div>
                </div>
              </div>

              <div class="post-content">
                <div class="content-preview">{{ getContentPreview(post.content) }}</div>
                <el-button link @click="viewDetail(post.id)">查看详情</el-button>
              </div>

              <div class="post-actions">
                <el-button type="success" @click="approvePost(post.id)">
                  <el-icon><Check /></el-icon>
                  通过
                </el-button>
                <el-button type="danger" @click="showRejectDialog(post.id)">
                  <el-icon><Close /></el-icon>
                  拒绝
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无待审核内容" />

          <div v-if="pendingTotal > 0" class="pagination">
            <el-pagination
              v-model:current-page="pendingPage"
              v-model:page-size="pageSize"
              :total="pendingTotal"
              layout="total, prev, pager, next"
              @current-change="loadPendingPosts"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="审核历史" name="history">
        <el-card v-loading="loading">
          <div class="filters">
            <el-select v-model="historyStatus" placeholder="审核状态" @change="loadHistory">
              <el-option label="全部" :value="undefined" />
              <el-option label="已通过" :value="2" />
              <el-option label="已拒绝" :value="3" />
            </el-select>
          </div>

          <div v-if="historyPosts.length > 0" class="posts-list">
            <div v-for="post in historyPosts" :key="post.id" class="post-item">
              <div class="post-header">
                <div class="post-info">
                  <h3 class="post-title">{{ post.title }}</h3>
                  <div class="post-meta">
                    <el-avatar :src="post.author?.avatar" :size="24" />
                    <span class="author">{{ post.author?.nickname }}</span>
                    <span class="time">{{ formatTime(post.createTime) }}</span>
                    <el-tag
                      size="small"
                      :type="post.status === 2 ? 'success' : 'danger'"
                    >
                      {{ post.status === 2 ? '已通过' : '已拒绝' }}
                    </el-tag>
                  </div>
                </div>
              </div>

              <div class="post-content">
                <div class="content-preview">{{ getContentPreview(post.content) }}</div>
                <el-button link @click="viewDetail(post.id)">查看详情</el-button>
              </div>

              <div class="audit-info">
                <span>审核人：{{ post.auditorName || '系统' }}</span>
                <span>审核时间：{{ formatTime(post.auditTime) }}</span>
                <span v-if="post.auditNote" class="audit-note">
                  备注：{{ post.auditNote }}
                </span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无审核记录" />

          <div v-if="historyTotal > 0" class="pagination">
            <el-pagination
              v-model:current-page="historyPage"
              v-model:page-size="pageSize"
              :total="historyTotal"
              layout="total, prev, pager, next"
              @current-change="loadHistory"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝原因"
      width="500px"
    >
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请输入拒绝原因，将通知作者"
        maxlength="200"
        show-word-limit
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject" :loading="rejectLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
import {
  getPendingPosts,
  approvePost as approvePostApi,
  rejectPost as rejectPostApi,
  getAuditHistory,
  getPendingPostCount
} from '@/api/audit'
import type { ForumPost } from '@/api/post'

const router = useRouter()

const activeTab = ref('pending')
const loading = ref(false)
const pendingPosts = ref<ForumPost[]>([])
const historyPosts = ref<ForumPost[]>([])
const pendingPage = ref(1)
const historyPage = ref(1)
const pageSize = ref(10)
const pendingTotal = ref(0)
const historyTotal = ref(0)
const pendingCount = ref(0)
const historyStatus = ref<number | undefined>(undefined)

const rejectDialogVisible = ref(false)
const rejectPostId = ref(0)
const rejectReason = ref('')
const rejectLoading = ref(false)

// 加载待审核数量
const loadPendingCount = async () => {
  try {
    const res = await getPendingPostCount()
    pendingCount.value = res.data
  } catch (error) {
    console.error('加载待审核数量失败', error)
  }
}

// 加载待审核帖子
const loadPendingPosts = async () => {
  loading.value = true
  try {
    const res = await getPendingPosts({
      page: pendingPage.value,
      size: pageSize.value
    })
    pendingPosts.value = res.data.records
    pendingTotal.value = res.data.total
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 加载审核历史
const loadHistory = async () => {
  loading.value = true
  try {
    const res = await getAuditHistory({
      page: historyPage.value,
      size: pageSize.value,
      status: historyStatus.value
    })
    historyPosts.value = res.data.records
    historyTotal.value = res.data.total
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 切换标签页
const handleTabChange = (tabName: string) => {
  if (tabName === 'pending') {
    loadPendingPosts()
  } else {
    loadHistory()
  }
}

// 查看详情
const viewDetail = (postId: number) => {
  window.open(`/post/${postId}`, '_blank')
}

// 通过审核
const approvePost = async (postId: number) => {
  try {
    await approvePostApi(postId)
    ElMessage.success('审核通过')
    await loadPendingPosts()
    await loadPendingCount()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 显示拒绝对话框
const showRejectDialog = (postId: number) => {
  rejectPostId.value = postId
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  rejectLoading.value = true
  try {
    await rejectPostApi(rejectPostId.value, rejectReason.value)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    await loadPendingPosts()
    await loadPendingCount()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    rejectLoading.value = false
  }
}

// 获取内容预览
const getContentPreview = (content: string) => {
  if (!content) return ''
  const text = content.replace(/[#*`>\-\[\]]/g, '').trim()
  return text.length > 150 ? text.substring(0, 150) + '...' : text
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadPendingCount()
  loadPendingPosts()
})
</script>

<style scoped lang="scss">
.audit-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;

  h1 {
    margin: 0;
    font-size: 24px;
  }

  .badge {
    :deep(.el-badge__content) {
      font-weight: bold;
    }
  }
}

.filters {
  margin-bottom: 16px;
}

.posts-list {
  .post-item {
    padding: 20px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
    margin-bottom: 16px;
    background-color: var(--el-bg-color);

    &:hover {
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    }
  }

  .post-header {
    margin-bottom: 12px;
  }

  .post-title {
    margin: 0 0 8px 0;
    font-size: 18px;
    font-weight: 600;
  }

  .post-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 14px;
    color: var(--el-text-color-secondary);

    .author {
      font-weight: 500;
    }
  }

  .post-content {
    margin-bottom: 16px;

    .content-preview {
      line-height: 1.6;
      color: var(--el-text-color-regular);
      margin-bottom: 8px;
    }
  }

  .post-actions {
    display: flex;
    gap: 12px;
  }

  .audit-info {
    display: flex;
    gap: 16px;
    padding-top: 12px;
    margin-top: 12px;
    border-top: 1px solid var(--el-border-color-lighter);
    font-size: 13px;
    color: var(--el-text-color-secondary);

    .audit-note {
      flex: 1;
      color: var(--el-color-danger);
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
