<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import UserAvatar from '@/components/user/UserAvatar.vue'
import UserPopover from '@/components/user/UserPopover.vue'

const { t } = useI18n()

interface Post {
  id: number
  title: string
  author: {
    id: number
    name: string
    avatar: string
    badge?: string
  }
  topic: string
  topicColor: string
  content: string
  likes: number
  replies: number
  views: number
  createdAt: string
  isPinned?: boolean
  isHot?: boolean
  tags: string[]
}

const sortBy = ref('latest')
const showComposer = ref(false)
const newPostTitle = ref('')
const newPostContent = ref('')

const getSortOptions = () => [
  { value: 'latest', label: t('forum.latest') },
  { value: 'hot', label: t('forum.hot') },
  { value: 'top', label: t('forum.top') },
  { value: 'unanswered', label: t('forum.unanswered') },
]

const sortOptions = getSortOptions()

const posts: Post[] = [
  {
    id: 1,
    title: '[1.20.4] Sodium + Iris Compatibility Issue - Need Help!',
    author: { id: 1, name: 'MinecraftFan2024', avatar: 'MF', badge: 'Helper' },
    topic: 'Help & Support',
    topicColor: 'bg-blue-500/20 text-blue-400',
    content: 'I\'ve been trying to get Sodium and Iris working together on 1.20.4 but keep getting crashes. Has anyone found a solution?',
    likes: 24,
    replies: 18,
    views: 342,
    createdAt: '2h ago',
    isHot: true,
    tags: ['Fabric', '1.20.4', 'Sodium', 'Iris'],
  },
  {
    id: 2,
    title: 'Modrinth App 1.0 Released! - Download Now',
    author: { id: 2, name: 'Modrinth Team', avatar: 'MT', badge: 'Official' },
    topic: 'Announcements',
    topicColor: 'bg-brand/20 text-text',
    content: 'We\'re excited to announce the official release of the Modrinth App! Download it now and enjoy a seamless modding experience.',
    likes: 567,
    replies: 89,
    views: 12453,
    createdAt: '1d ago',
    isPinned: true,
    tags: ['Official', 'App'],
  },
  {
    id: 3,
    title: 'Showcase: My Medieval Castle Build with Conquest Reforged',
    author: { id: 3, name: 'BuildMaster_X', avatar: 'BX' },
    topic: 'Showcase',
    topicColor: 'bg-purple-500/20 text-purple-400',
    content: 'Check out my latest build! Spent 3 weeks on this medieval castle using Conquest Reforged textures. What do you think?',
    likes: 156,
    replies: 42,
    views: 2891,
    createdAt: '3h ago',
    tags: ['Build', 'Conquest Reforged'],
  },
  {
    id: 4,
    title: 'Best Performance Mods for Low-End PCs in 2024?',
    author: { id: 4, name: 'PixelGamer99', avatar: 'PG' },
    topic: 'Mod Discussion',
    topicColor: 'bg-amber-500/20 text-amber-400',
    content: 'Looking for recommendations on performance mods that work well on older hardware. Currently getting 20-30 FPS on vanilla.',
    likes: 45,
    replies: 31,
    views: 876,
    createdAt: '5h ago',
    tags: ['Performance', 'Optimization'],
  },
  {
    id: 5,
    title: 'Suggestion: Add "Collections" feature for organizing modpacks',
    author: { id: 5, name: 'ModpackCreator', avatar: 'MC', badge: 'Creator' },
    topic: 'Suggestions',
    topicColor: 'bg-cyan-500/20 text-cyan-400',
    content: 'It would be great if we could create collections to organize our favorite mods by theme or purpose. Anyone else want this?',
    likes: 89,
    replies: 23,
    views: 654,
    createdAt: '8h ago',
    tags: ['Feature Request'],
  },
  {
    id: 6,
    title: 'Weekly Off-Topic: What games are you playing besides Minecraft?',
    author: { id: 6, name: 'CommunityMod', avatar: 'CM', badge: 'Moderator' },
    topic: 'Off-Topic',
    topicColor: 'bg-gray-500/20 text-gray-400',
    content: 'Let\'s take a break from Minecraft talk! What other games have you been enjoying lately?',
    likes: 78,
    replies: 156,
    views: 1234,
    createdAt: '12h ago',
    tags: ['Weekly', 'Discussion'],
  },
]

const getBadgeClass = (badge?: string) => {
  switch (badge) {
    case 'Official': return 'bg-brand/20 text-text'
    case 'Moderator': return 'bg-red-500/20 text-red-400'
    case 'Helper': return 'bg-blue-500/20 text-blue-400'
    case 'Creator': return 'bg-purple-500/20 text-purple-400'
    default: return 'bg-bg-surface text-text-secondary'
  }
}

const toggleComposer = () => {
  showComposer.value = !showComposer.value
}

const submitPost = () => {
  // 模拟提交
  console.log('Submitting:', { title: newPostTitle.value, content: newPostContent.value })
  showComposer.value = false
  newPostTitle.value = ''
  newPostContent.value = ''
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
        <h3 class="text-lg font-semibold text-text mb-4">Create New Post</h3>
        
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
            <select class="w-full input-base">
              <option>Mod Discussion</option>
              <option>Help & Support</option>
              <option>Showcase</option>
              <option>Suggestions</option>
              <option>Off-Topic</option>
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
    <div class="space-y-3">
      <article
        v-for="post in posts"
        :key="post.id"
        class="card-hover cursor-pointer group"
        :class="{ 'border-l-4 border-l-brand': post.isPinned }"
      >
        <div class="flex gap-4">
          <!-- 作者头像 -->
          <div class="flex-shrink-0" @click.stop>
            <UserPopover
              :user-id="post.author.id"
              trigger="hover"
              :delay="500"
              placement="bottom"
            >
              <UserAvatar
                :nickname="post.author.name"
                size="lg"
                clickable
              />
            </UserPopover>
          </div>

          <!-- 帖子内容 -->
          <div class="flex-1 min-w-0">
            <!-- 标题行 -->
            <div class="flex items-start gap-2 mb-1">
              <span v-if="post.isPinned" class="i-tabler-pin text-brand flex-shrink-0 mt-1" />
              <span v-if="post.isHot" class="i-tabler-flame text-orange-400 flex-shrink-0 mt-1" />
              <h3 class="text-text font-semibold group-hover:text-brand transition-colors line-clamp-1">
                {{ post.title }}
              </h3>
            </div>

            <!-- 作者信息 -->
            <div class="flex items-center gap-2 mb-2">
              <span class="text-sm text-text-secondary">{{ post.author.name }}</span>
              <span 
                v-if="post.author.badge" 
                class="text-xs px-2 py-0.5 rounded-full font-medium"
                :class="getBadgeClass(post.author.badge)"
              >
                {{ post.author.badge }}
              </span>
              <span class="text-text-muted">·</span>
              <span class="text-xs px-2 py-0.5 rounded-full" :class="post.topicColor">
                {{ post.topic }}
              </span>
              <span class="text-text-muted">·</span>
              <span class="text-sm text-text-muted">{{ post.createdAt }}</span>
            </div>

            <!-- 内容预览 -->
            <p class="text-text-secondary text-sm line-clamp-2 mb-3">
              {{ post.content }}
            </p>

            <!-- 标签和统计 -->
            <div class="flex items-center justify-between">
              <div class="flex flex-wrap gap-1.5">
                <span
                  v-for="tag in post.tags.slice(0, 3)"
                  :key="tag"
                  class="text-xs px-2 py-0.5 rounded bg-bg-surface text-text-muted"
                >
                  {{ tag }}
                </span>
                <span v-if="post.tags.length > 3" class="text-xs text-text-muted">
                  +{{ post.tags.length - 3 }}
                </span>
              </div>

              <div class="flex items-center gap-4 text-sm text-text-muted">
                <span class="flex items-center gap-1">
                  <span class="i-tabler-heart" />
                  {{ post.likes }}
                </span>
                <span class="flex items-center gap-1">
                  <span class="i-tabler-message-circle" />
                  {{ post.replies }}
                </span>
                <span class="flex items-center gap-1">
                  <span class="i-tabler-eye" />
                  {{ post.views }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </article>
    </div>

    <!-- 加载更多 -->
    <div class="flex-center py-6">
      <button class="btn-secondary">
        <span class="i-tabler-loader-2" />
        Load More Posts
      </button>
    </div>
  </div>
</template>
