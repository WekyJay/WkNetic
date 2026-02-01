<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import UserAvatar from '@/components/user/UserAvatar.vue'
import UserPopover from '@/components/user/UserPopover.vue'
import { listPosts, createPost } from '@/api/post'
import { listAllTopics } from '@/api/topic'
import type { PostVO } from '@/api/post'
import type { TopicVO } from '@/api/topic'

const { t } = useI18n()
const router = useRouter()

const sortBy = ref('latest')
const posts = ref<PostVO[]>([])
const topics = ref<TopicVO[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const page = ref(1)
const pageSize = ref(10)
const showComposer = ref(false)
const newPostTitle = ref('')
const newPostContent = ref('')
const selectedTopic = ref<number | null>(null)
const totalPages = ref(0)

const getSortOptions = () => [
  { value: 'latest', label: t('forum.latest') },
  { value: 'hot', label: t('forum.hot') },
  { value: 'top', label: t('forum.top') },
  { value: 'unanswered', label: t('forum.unanswered') },
]

const sortOptions = getSortOptions()

const loadTopics = async () => {
  try {
    const response = await listAllTopics()
    topics.value = response.data || []
    if (topics.value.length > 0 && !selectedTopic.value && topics.value[0]) {
      selectedTopic.value = topics.value[0].topicId
    }
  } catch (e) {
    console.error('Error loading topics:', e)
  }
}

const loadPosts = async () => {
  try {
    loading.value = true
    error.value = null
    const response = await listPosts({
      page: page.value,
      size: pageSize.value,
      topicId: selectedTopic.value || undefined,
    })
    posts.value = response.data.records || []
    totalPages.value = response.data.pages || 0
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to load posts'
    console.error('Error loading posts:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTopics()
  loadPosts()
})

watch(() => sortBy.value, () => {
  page.value = 1
  loadPosts()
})

watch(() => selectedTopic.value, () => {
  page.value = 1
  loadPosts()
})

const toggleComposer = () => {
  showComposer.value = !showComposer.value
}

const submitPost = async () => {
  if (!newPostTitle.value.trim() || !newPostContent.value.trim() || !selectedTopic.value) {
    error.value = 'Please fill in all required fields'
    return
  }

  try {
    await createPost({
      title: newPostTitle.value,
      content: newPostContent.value,
      topicId: selectedTopic.value,
      publish: true,
    })
    showComposer.value = false
    newPostTitle.value = ''
    newPostContent.value = ''
    page.value = 1
    await loadPosts()
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to create post'
    console.error('Error creating post:', e)
  }
}

const handlePostClick = (postId: number) => {
  router.push(`/forum/post/${postId}`)
}
</script>

<template>
  <div class="space-y-4">
    <!-- 顶部操作栏 -->
    <div class="card flex-between flex-wrap gap-4">
      <div class="flex items-center gap-2">
        <button
          v-for="option in sortOptions"
          :key="option.value"
          class="px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200"
          :class="[
            sortBy === option.value
              ? 'bg-brand/10'
              : 'text-text-secondary hover:bg-bg-hover hover:text-text'
          ]"
          @click="sortBy = option.value"
        >
          {{ option.label }}
        </button>
      </div>

      <button class="btn-primary" @click="toggleComposer">
        <span class="i-tabler-plus" />
        New Post
      </button>
    </div>

    <!-- 发布新帖子 -->
    <Transition
      enter-active-class="transition-all duration-300 ease-out"
      enter-from-class="opacity-0 -translate-y-4"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition-all duration-200 ease-in"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 -translate-y-4"
    >
      <div v-if="showComposer" class="card border-brand/30">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-text">Create New Post</h3>
          <button
            class="btn-ghost text-sm"
            @click="router.push('/forum/create')"
            title="Open full editor"
          >
            <span class="i-tabler-arrow-up-right" />
            Full Editor
          </button>
        </div>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-text-secondary mb-2">Title</label>
            <input
              v-model="newPostTitle"
              type="text"
              class="w-full input-base"
              placeholder="Enter a descriptive title..."
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-text-secondary mb-2">Content</label>
            <textarea
              v-model="newPostContent"
              class="w-full input-base min-h-32 resize-y"
              placeholder="Write your post content here... Markdown is supported!"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-text-secondary mb-2">Topic</label>
            <select v-model.number="selectedTopic" class="w-full input-base">
              <option v-for="topic in topics" :key="topic.topicId" :value="topic.topicId">
                {{ topic.topicName }}
              </option>
            </select>
          </div>

          <div class="flex items-center justify-end gap-3 pt-2">
            <button class="btn-ghost" @click="showComposer = false">Cancel</button>
            <button class="btn-primary" @click="submitPost">
              <span class="i-tabler-send" />
              Publish Post
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 帖子列表 -->
    <div v-if="loading" class="space-y-3">
      <div v-for="i in 5" :key="i" class="card">
        <div class="h-20 bg-bg-surface rounded animate-pulse" />
      </div>
    </div>

    <div v-else-if="error" class="card text-center text-text-secondary py-8">
      <p>{{ error }}</p>
      <button class="btn-secondary mt-4" @click="loadPosts">
        <span class="i-tabler-refresh" />
        Retry
      </button>
    </div>

    <div v-else-if="posts.length === 0" class="card text-center text-text-secondary py-8">
      <p>No posts found</p>
    </div>

    <div v-else class="space-y-3">
      <article
        v-for="post in posts"
        :key="post.id"
        class="card-hover cursor-pointer group"
        @click="handlePostClick(post.id)"
      >
        <div class="flex gap-4">
          <!-- 作者头像 -->
          <div class="flex-shrink-0" @click.stop>
            <UserPopover
              :user-id="post.userId"
              trigger="hover"
              :delay="500"
              placement="bottom"
            >
              <UserAvatar
                :nickname="post.author?.nickname || 'User'"
                size="lg"
                clickable
              />
            </UserPopover>
          </div>

          <!-- 帖子内容 -->
          <div class="flex-1 min-w-0">
            <!-- 标题行 -->
            <div class="flex items-start gap-2 mb-1">
              <h3 class="text-text font-semibold group-hover:text-brand transition-colors line-clamp-1">
                {{ post.title }}
              </h3>
            </div>

            <!-- 作者信息 -->
            <div class="flex items-center gap-2 mb-2 flex-wrap">
              <span class="text-sm text-text-secondary">{{ post.author?.username || 'Anonymous' }}</span>
              <span class="text-text-muted">·</span>
              <span class="text-xs px-2 py-0.5 rounded-full bg-brand/10 text-brand">
                {{ post.topic?.name || 'Uncategorized' }}
              </span>
              <span class="text-text-muted">·</span>
              <span class="text-sm text-text-muted">{{ post.createTime }}</span>
            </div>

            <!-- 内容预览 -->
            <p class="text-text-secondary text-sm line-clamp-2 mb-3">
              {{ post.intro }}
            </p>

            <!-- 标签和统计 -->
            <div class="flex items-center justify-between">
              <div class="flex flex-wrap gap-1.5">
                <span
                  v-for="tag in (post.tags || []).slice(0, 3)"
                  :key="tag.id"
                  class="text-xs px-2 py-0.5 rounded bg-bg-surface text-text-muted"
                >
                  {{ tag.name }}
                </span>
                <span v-if="(post.tags || []).length > 3" class="text-xs text-text-muted">
                  +{{ (post.tags || []).length - 3 }}
                </span>
              </div>

              <div class="flex items-center gap-4 text-sm text-text-muted">
                <span class="flex items-center gap-1">
                  <span class="i-tabler-heart" />
                  {{ post.likeCount }}
                </span>
                <span class="flex items-center gap-1">
                  <span class="i-tabler-message-circle" />
                  {{ post.commentCount }}
                </span>
                <span class="flex items-center gap-1">
                  <span class="i-tabler-eye" />
                  {{ post.viewCount }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </article>
    </div>

    <!-- 分页 -->
    <div v-if="!loading && posts.length > 0 && totalPages > 1" class="flex-center gap-2 py-6">
      <button 
        v-if="page > 1"
        class="btn-ghost"
        @click="page--; loadPosts()"
      >
        <span class="i-tabler-chevron-left" />
        Previous
      </button>
      <span class="text-text-muted text-sm">
        Page {{ page }} of {{ totalPages }}
      </span>
      <button 
        v-if="page < totalPages"
        class="btn-ghost"
        @click="page++; loadPosts()"
      >
        Next
        <span class="i-tabler-chevron-right" />
      </button>
    </div>
  </div>
</template>
