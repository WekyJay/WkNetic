<script setup lang="ts">
import type { PostSearchVO } from '@/api/search'

interface Props {
  post: PostSearchVO
}

const props = defineProps<Props>()

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 跳转到帖子详情
const goToPost = () => {
  window.location.href = `/forum/post/${props.post.postId}`
}
</script>

<template>
  <article 
    class="post-search-card border-b border-gray-200 dark:border-gray-700 p-4 hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors cursor-pointer"
    @click="goToPost"
  >
    <!-- 标题 -->
    <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2 line-clamp-2">
      <span v-if="post.isPinned" class="text-red-500 mr-1">[置顶]</span>
      <span v-if="post.isHot" class="text-orange-500 mr-1">[热门]</span>
      <span v-html="post.title"></span>
    </h3>
    
    <!-- 摘要 -->
    <p class="text-sm text-gray-600 dark:text-gray-400 mb-3 line-clamp-2" v-html="post.excerpt"></p>
    
    <!-- 元信息 -->
    <div class="flex flex-wrap items-center gap-4 text-xs text-gray-500 dark:text-gray-400">
      <!-- 作者 -->
      <span class="flex items-center gap-1">
        <span>@{{ post.username }}</span>
      </span>
      
      <!-- 话题 -->
      <span class="px-2 py-0.5 bg-blue-100 dark:bg-blue-900 text-blue-600 dark:text-blue-300 rounded">
        {{ post.topicName }}
      </span>
      
      <!-- 标签 -->
      <span 
        v-for="tag in post.tags.slice(0, 3)" 
        :key="tag"
        class="px-2 py-0.5 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 rounded"
      >
        #{{ tag }}
      </span>
      
      <!-- 时间 -->
      <span class="flex items-center gap-1">
        <span class="i-tabler-clock w-4 h-4"></span>
        {{ formatTime(post.updateTime) }}
      </span>
      
      <!-- 分隔符 -->
      <span class="flex-1"></span>
      
      <!-- 统计信息 -->
      <span class="flex items-center gap-1">
        <span class="i-tabler-eye w-4 h-4"></span>
        {{ post.viewCount }}
      </span>
      
      <span class="flex items-center gap-1">
        <span class="i-tabler-message-circle w-4 h-4"></span>
        {{ post.commentCount }}
      </span>
      
      <span class="flex items-center gap-1">
        <span class="i-tabler-star w-4 h-4"></span>
        {{ post.likeCount }}
      </span>
    </div>
  </article>
</template>

<style scoped>
.post-search-card {
  transition: all 0.2s ease;
}

.post-search-card:hover {
  transform: translateY(-2px);
}

/* 高亮样式 */
:deep(em) {
  font-style: normal;
  color: #f56c6c;
  font-weight: 600;
  background-color: rgba(245, 108, 108, 0.1);
  padding: 0 2px;
  border-radius: 2px;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
