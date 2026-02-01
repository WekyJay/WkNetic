<template>
  <div class="topic-page">
    <div v-if="topic" class="topic-header">
      <div class="topic-info">
        <div class="topic-icon">
          <img v-if="topic.icon" :src="topic.icon" :alt="topic.name" />
          <div v-else class="default-icon">{{ topic.name[0] }}</div>
        </div>
        <div class="topic-details">
          <h1 class="topic-name">{{ topic.name }}</h1>
          <p class="topic-description">{{ topic.description }}</p>
          <div class="topic-stats">
            <span>{{ topic.postCount }} 帖子</span>
            <span>创建于 {{ formatTime(topic.createTime) }}</span>
          </div>
        </div>
      </div>
      <div class="topic-actions">
        <el-button type="primary" @click="createPost">
          <el-icon><Edit /></el-icon>
          发帖
        </el-button>
      </div>
    </div>

    <el-card v-loading="loading" class="posts-card">
      <template #header>
        <div class="card-header">
          <div class="filters">
            <el-radio-group v-model="sortType" @change="handleSortChange">
              <el-radio-button label="latest">最新</el-radio-button>
              <el-radio-button label="hot">热门</el-radio-button>
            </el-radio-group>
          </div>
          <div class="search">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索帖子..."
              clearable
              @change="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <div v-if="posts.length > 0" class="posts-list">
        <div
          v-for="post in posts"
          :key="post.id"
          class="post-item"
          @click="goToPost(post.id)"
        >
          <div class="post-main">
            <div class="post-content">
              <h3 class="post-title">
                <el-tag
                  v-if="post.status === 1"
                  type="warning"
                  size="small"
                  style="margin-right: 8px"
                >
                  审核中
                </el-tag>
                {{ post.title }}
              </h3>
              <div class="post-meta">
                <el-avatar :src="post.author?.avatar" :size="20" />
                <span class="author">{{ post.author?.nickname }}</span>
                <span class="separator">·</span>
                <span class="time">{{ formatTime(post.createTime) }}</span>
                <span v-if="post.tags?.length" class="tags">
                  <el-tag
                    v-for="tag in post.tags.slice(0, 3)"
                    :key="tag"
                    size="small"
                    type="info"
                  >
                    {{ tag }}
                  </el-tag>
                </span>
              </div>
            </div>
            <div class="post-stats">
              <div class="stat-item">
                <el-icon><View /></el-icon>
                <span>{{ formatCount(post.viewCount) }}</span>
              </div>
              <div class="stat-item">
                <el-icon><Star /></el-icon>
                <span>{{ formatCount(post.likeCount) }}</span>
              </div>
              <div class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                <span>{{ formatCount(post.commentCount) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无帖子" />

      <div v-if="total > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="loadPosts"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit, Search, View, Star, ChatDotRound } from '@element-plus/icons-vue'
import { getTopicDetail } from '@/api/topic'
import { listPosts } from '@/api/post'
import type { TopicVO, PostVO } from '@/api/post'

const route = useRoute()
const router = useRouter()
const topicId = ref(Number(route.params.id))

const loading = ref(false)
const topic = ref<TopicVO | null>(null)
const posts = ref<PostVO[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const sortType = ref('latest')
const searchKeyword = ref('')

// 加载板块信息
const loadTopic = async () => {
  try {
    const res = await getTopicDetail(topicId.value)
    topic.value = res.data
  } catch (error: any) {
    ElMessage.error(error.message || '加载板块信息失败')
  }
}

// 加载帖子列表
const loadPosts = async () => {
  loading.value = true
  try {
    const res = await listPosts({
      page: currentPage.value,
      size: pageSize.value,
      topicId: topicId.value,
      status: 2 // 只显示已发布的帖子
    })
    posts.value = res.data.records
    total.value = res.data.total
  } catch (error: any) {
    ElMessage.error(error.message || '加载帖子列表失败')
  } finally {
    loading.value = false
  }
}

// 切换排序
const handleSortChange = () => {
  currentPage.value = 1
  loadPosts()
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadPosts()
}

// 跳转到帖子详情
const goToPost = (postId: number) => {
  router.push(`/post/${postId}`)
}

// 创建帖子
const createPost = () => {
  router.push({
    path: '/post/create',
    query: { topicId: topicId.value }
  })
}

// 格式化数字
const formatCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  }
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count.toString()
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return ''
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

onMounted(() => {
  loadTopic()
  loadPosts()
})
</script>

<style scoped lang="scss">
.topic-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.topic-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30px;
  background: linear-gradient(135deg, var(--el-color-primary-light-9) 0%, var(--el-color-primary-light-8) 100%);
  border-radius: 8px;
  margin-bottom: 20px;

  .topic-info {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .topic-icon {
    width: 80px;
    height: 80px;
    border-radius: 12px;
    overflow: hidden;
    background-color: var(--el-color-primary);
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .default-icon {
      font-size: 32px;
      font-weight: bold;
      color: white;
    }
  }

  .topic-details {
    flex: 1;
  }

  .topic-name {
    font-size: 28px;
    font-weight: bold;
    margin: 0 0 8px 0;
  }

  .topic-description {
    font-size: 14px;
    color: var(--el-text-color-secondary);
    margin: 0 0 12px 0;
  }

  .topic-stats {
    display: flex;
    gap: 16px;
    font-size: 14px;
    color: var(--el-text-color-regular);

    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.posts-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;

    .search {
      width: 250px;
    }
  }

  .posts-list {
    .post-item {
      padding: 20px;
      border-bottom: 1px solid var(--el-border-color-lighter);
      cursor: pointer;
      transition: background-color 0.2s;

      &:hover {
        background-color: var(--el-fill-color-light);
      }

      &:last-child {
        border-bottom: none;
      }
    }

    .post-main {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 20px;
    }

    .post-content {
      flex: 1;
      min-width: 0;
    }

    .post-title {
      margin: 0 0 8px 0;
      font-size: 16px;
      font-weight: 500;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .post-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      color: var(--el-text-color-secondary);

      .author {
        font-weight: 500;
      }

      .separator {
        color: var(--el-text-color-placeholder);
      }

      .tags {
        display: flex;
        gap: 4px;
        margin-left: 8px;
      }
    }

    .post-stats {
      display: flex;
      gap: 16px;
      font-size: 13px;
      color: var(--el-text-color-secondary);

      .stat-item {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid var(--el-border-color-lighter);
  }
}
</style>
