<template>
  <div class="comment-item">
    <div class="comment-header">
      <el-avatar :src="comment.author.avatar" :size="36" />
      <div class="comment-info">
        <div class="author-name">{{ comment.author.nickname }}</div>
        <div class="comment-meta">{{ formatTime(comment.createTime) }}</div>
      </div>
      <div class="comment-actions">
        <el-button
          :type="comment.liked ? 'primary' : 'default'"
          size="small"
          link
          @click="toggleLike"
          :loading="likeLoading"
        >
          <el-icon><Star v-if="!comment.liked" /><StarFilled v-else /></el-icon>
          {{ comment.likeCount }}
        </el-button>
        <el-button size="small" link @click="showReplyInput">
          <el-icon><ChatDotRound /></el-icon>
          回复
        </el-button>
        <el-button
          v-if="isAuthor || isAdmin"
          size="small"
          link
          type="danger"
          @click="deleteComment"
        >
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
      </div>
    </div>

    <div class="comment-content">
      <span v-if="comment.replyToUser" class="reply-to">
        @{{ comment.replyToUser.nickname }}
      </span>
      {{ comment.content }}
    </div>

    <!-- 回复输入框 -->
    <div v-if="showReply" class="reply-input-wrapper">
      <el-input
        v-model="replyContent"
        type="textarea"
        :rows="3"
        :placeholder="`回复 @${comment.author.nickname}...`"
        maxlength="500"
        show-word-limit
      />
      <div class="reply-actions">
        <el-button size="small" @click="cancelReply">取消</el-button>
        <el-button size="small" type="primary" @click="submitReply" :loading="replySubmitting">
          发送
        </el-button>
      </div>
    </div>

    <!-- 子评论 -->
    <div v-if="comment.replies?.length" class="replies">
      <CommentTree
        v-for="reply in comment.replies"
        :key="reply.id"
        :comment="reply"
        :post-id="postId"
        @refresh="$emit('refresh')"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled, ChatDotRound, Delete } from '@element-plus/icons-vue'
import { toggleCommentLike, deleteComment as deleteCommentApi, createComment } from '@/api/comment'
import type { CommentVO } from '@/api/comment'

interface Props {
  comment: CommentVO
  postId: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  refresh: []
}>()

const showReply = ref(false)
const replyContent = ref('')
const replySubmitting = ref(false)
const likeLoading = ref(false)

// 当前用户ID（从store获取）
const currentUserId = computed(() => {
  // TODO: 从store获取当前用户ID
  return 1
})

// 是否是作者
const isAuthor = computed(() => {
  return props.comment.author.id === currentUserId.value
})

// 是否是管理员
const isAdmin = computed(() => {
  // TODO: 从store获取是否管理员
  return false
})

// 显示回复输入框
const showReplyInput = () => {
  showReply.value = true
}

// 取消回复
const cancelReply = () => {
  showReply.value = false
  replyContent.value = ''
}

// 提交回复
const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  replySubmitting.value = true
  try {
    await createComment({
      postId: props.postId,
      content: replyContent.value,
      parentId: props.comment.id,
      replyToUserId: props.comment.author.id
    })

    ElMessage.success('回复成功')
    cancelReply()
    emit('refresh')
  } catch (error: any) {
    ElMessage.error(error.message || '回复失败')
  } finally {
    replySubmitting.value = false
  }
}

// 点赞
const toggleLike = async () => {
  likeLoading.value = true
  try {
    const res = await toggleCommentLike(props.comment.id)
    const isLiked = res.data

    ElMessage.success(isLiked ? '点赞成功' : '取消点赞')
    emit('refresh')
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    likeLoading.value = false
  }
}

// 删除评论
const deleteComment = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteCommentApi(props.comment.id)
    ElMessage.success('删除成功')
    emit('refresh')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
  if (diff < day) return `${Math.floor(diff / hour)}小时前`
  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`

  return date.toLocaleDateString()
}
</script>

<style scoped lang="scss">
.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);

  &:last-child {
    border-bottom: none;
  }
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;

  .comment-info {
    flex: 1;
  }

  .author-name {
    font-weight: 500;
    font-size: 14px;
  }

  .comment-meta {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-top: 2px;
  }

  .comment-actions {
    display: flex;
    gap: 8px;
  }
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  margin-left: 48px;

  .reply-to {
    color: var(--el-color-primary);
    font-weight: 500;
    margin-right: 4px;
  }
}

.reply-input-wrapper {
  margin: 12px 0 12px 48px;

  .reply-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 8px;
  }
}

.replies {
  margin-left: 48px;
  margin-top: 12px;
  padding-left: 16px;
  border-left: 2px solid var(--el-border-color-lighter);
}
</style>
