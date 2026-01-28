<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TheHeader from '@/components/layout/TheHeader.vue'
import TheFooter from '@/components/layout/TheFooter.vue'
import MarkdownRenderer from '@/components/common/MarkdownRenderer.vue'
import MarkdownEditor from '@/components/common/MarkdownEditor.vue'

interface Author {
  id: number
  name: string
  avatar: string
  badge?: string
  joinDate: string
  postCount: number
  reputation: number
}

interface Comment {
  id: number
  author: Author
  content: string
  likes: number
  createdAt: string
  isLiked?: boolean
  replies?: Comment[]
}

interface Post {
  id: number
  title: string
  author: Author
  topic: string
  topicColor: string
  content: string
  likes: number
  views: number
  createdAt: string
  updatedAt?: string
  isPinned?: boolean
  isLiked?: boolean
  isBookmarked?: boolean
  tags: string[]
  comments: Comment[]
}

const route = useRoute()
const router = useRouter()
const postId = computed(() => route.params.id)

const isLoading = ref(true)
const newComment = ref('')
const replyingTo = ref<number | null>(null)
const replyContent = ref('')
const showShareMenu = ref(false)

// 模拟帖子数据
const post = ref<Post>({
  id: 1,
  title: '[1.20.4] Sodium + Iris Compatibility Issue - Need Help!',
  author: {
    id: 1,
    name: 'MinecraftFan2024',
    avatar: 'MF',
    badge: 'Helper',
    joinDate: 'Jan 2023',
    postCount: 156,
    reputation: 1240,
  },
  topic: 'Help & Support',
  topicColor: 'bg-blue-500/20 text-blue-400',
  content: `# The Problem

I've been trying to get **Sodium** and **Iris** working together on Minecraft 1.20.4, but I keep running into crashes. Here's what I've tried so far:

## My Setup

- Minecraft: 1.20.4
- Fabric Loader: 0.15.6
- Sodium: 0.5.8
- Iris: 1.6.17

## Error Log

\`\`\`
java.lang.RuntimeException: Could not execute entrypoint stage 'client' due to errors!
  at net.fabricmc.loader.impl.entrypoint.EntrypointUtils.lambda$invoke
  at net.fabricmc.loader.impl.entrypoint.EntrypointUtils.invoke(EntrypointUtils.java:34)
  ...
Caused by: java.lang.NoSuchMethodError: 'void net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter'
\`\`\`

## What I've Tried

1. Reinstalling both mods
2. Clearing the shader cache
3. Using different shader packs
4. Downgrading to older versions

> None of these solutions have worked for me.

## Screenshots

The game crashes immediately when I try to enable any shader pack.

![Crash Screenshot](https://img.cdn1.vip/i/6979a510b85cc_1769579792.webp)

---

Has anyone else encountered this issue? Any help would be greatly appreciated!

**System Specs:**
| Component | Specification |
| --- | --- |
| CPU | AMD Ryzen 5 5600X |
| GPU | NVIDIA RTX 3060 |
| RAM | 16GB DDR4 |
| OS | Windows 11 |`,
  likes: 24,
  views: 342,
  createdAt: '2 hours ago',
  updatedAt: '1 hour ago',
  isPinned: false,
  isLiked: false,
  isBookmarked: false,
  tags: ['Fabric', '1.20.4', 'Sodium', 'Iris', 'Shaders'],
  comments: [
    {
      id: 1,
      author: {
        id: 2,
        name: 'ShaderExpert',
        avatar: 'SE',
        badge: 'Moderator',
        joinDate: 'Mar 2021',
        postCount: 892,
        reputation: 5420,
      },
      content: `This is a known issue with the latest Sodium version. Try using **Sodium 0.5.7** instead - it's compatible with Iris 1.6.17.

You can download it from the [Modrinth page](https://modrinth.com/mod/sodium/versions).

\`\`\`bash
# Quick fix command
rm sodium-fabric-0.5.8.jar
# Then download 0.5.7
\`\`\`

Let me know if this helps!`,
      likes: 18,
      createdAt: '1 hour ago',
      isLiked: true,
      replies: [
        {
          id: 3,
          author: {
            id: 1,
            name: 'MinecraftFan2024',
            avatar: 'MF',
            badge: 'Helper',
            joinDate: 'Jan 2023',
            postCount: 156,
            reputation: 1240,
          },
          content: `Thank you so much! Downgrading to 0.5.7 fixed the issue. I should have tried that specific version earlier.`,
          likes: 5,
          createdAt: '45 mins ago',
        },
      ],
    },
    {
      id: 2,
      author: {
        id: 3,
        name: 'FabricDev',
        avatar: 'FD',
        badge: 'Creator',
        joinDate: 'Jun 2020',
        postCount: 2341,
        reputation: 12500,
      },
      content: `For future reference, you can check compatibility between mods using the [Fabric Compatibility Checker](https://example.com).

Also, make sure to always check the mod's changelog before updating - breaking changes are usually documented there.

> Pro tip: Keep a backup of working mod configurations!`,
      likes: 12,
      createdAt: '30 mins ago',
    },
  ],
})

const getBadgeClass = (badge?: string) => {
  switch (badge) {
    case 'Official': return 'bg-brand/20 text-brand'
    case 'Moderator': return 'bg-red-500/20 text-red-400'
    case 'Helper': return 'bg-blue-500/20 text-blue-400'
    case 'Creator': return 'bg-purple-500/20 text-purple-400'
    default: return 'bg-bg-surface text-text-secondary'
  }
}

const toggleLike = () => {
  post.value.isLiked = !post.value.isLiked
  post.value.likes += post.value.isLiked ? 1 : -1
}

const toggleBookmark = () => {
  post.value.isBookmarked = !post.value.isBookmarked
}

const toggleCommentLike = (comment: Comment) => {
  comment.isLiked = !comment.isLiked
  comment.likes += comment.isLiked ? 1 : -1
}

const submitComment = () => {
  if (!newComment.value.trim()) return
  
  const comment: Comment = {
    id: Date.now(),
    author: {
      id: 999,
      name: 'CurrentUser',
      avatar: 'CU',
      joinDate: 'Jan 2024',
      postCount: 5,
      reputation: 50,
    },
    content: newComment.value,
    likes: 0,
    createdAt: 'Just now',
  }
  
  post.value.comments.push(comment)
  newComment.value = ''
}

const submitReply = (parentComment: Comment) => {
  if (!replyContent.value.trim()) return
  
  const reply: Comment = {
    id: Date.now(),
    author: {
      id: 999,
      name: 'CurrentUser',
      avatar: 'CU',
      joinDate: 'Jan 2024',
      postCount: 5,
      reputation: 50,
    },
    content: replyContent.value,
    likes: 0,
    createdAt: 'Just now',
  }
  
  if (!parentComment.replies) {
    parentComment.replies = []
  }
  parentComment.replies.push(reply)
  replyContent.value = ''
  replyingTo.value = null
}

const copyLink = () => {
  navigator.clipboard.writeText(window.location.href)
  showShareMenu.value = false
}

const goBack = () => {
  router.push('/forum')
}

onMounted(() => {
  // 模拟加载
  setTimeout(() => {
    isLoading.value = false
  }, 500)
})
</script>

<template>
  <div class="min-h-screen bg-bg flex flex-col">
    
    <main class="flex-1 container mx-auto px-4 py-6 max-w-5xl">
      <!-- 返回按钮 -->
      <button 
        class="flex items-center gap-2 text-text-secondary hover:text-brand transition-colors mb-4"
        @click="goBack"
      >
        <span class="i-tabler-arrow-left" />
        Back to Forum
      </button>
      
      <!-- 加载状态 -->
      <div v-if="isLoading" class="flex-center py-20">
        <span class="i-tabler-loader-2 text-4xl text-brand animate-spin" />
      </div>
      
      <template v-else>
        <!-- 帖子主体 -->
        <article class="card mb-6">
          <!-- 帖子头部 -->
          <header class="mb-6">
            <!-- 话题和状态 -->
            <div class="flex items-center gap-2 mb-3">
              <span class="text-xs px-2 py-1 rounded-full" :class="post.topicColor">
                {{ post.topic }}
              </span>
              <span v-if="post.isPinned" class="flex items-center gap-1 text-xs text-brand">
                <span class="i-tabler-pin" />
                Pinned
              </span>
            </div>
            
            <!-- 标题 -->
            <h1 class="text-2xl font-bold text-text mb-4">{{ post.title }}</h1>
            
            <!-- 作者信息 -->
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-full bg-gradient-to-br from-brand to-brand-dark flex-center text-bg font-bold">
                {{ post.author.avatar }}
              </div>
              <div>
                <div class="flex items-center gap-2">
                  <span class="font-medium text-text">{{ post.author.name }}</span>
                  <span 
                    v-if="post.author.badge" 
                    class="text-xs px-2 py-0.5 rounded-full"
                    :class="getBadgeClass(post.author.badge)"
                  >
                    {{ post.author.badge }}
                  </span>
                </div>
                <div class="text-sm text-text-muted flex items-center gap-2">
                  <span>Posted {{ post.createdAt }}</span>
                  <span v-if="post.updatedAt" class="text-text-muted">
                    (edited {{ post.updatedAt }})
                  </span>
                </div>
              </div>
            </div>
          </header>
          
          <!-- 帖子内容 (Markdown 渲染) -->
          <div class="border-t border-border pt-6 mb-6">
            <MarkdownRenderer :content="post.content" />
          </div>
          
          <!-- 标签 -->
          <div class="flex flex-wrap gap-2 mb-6">
            <span
              v-for="tag in post.tags"
              :key="tag"
              class="text-sm px-3 py-1 rounded-full bg-bg-surface text-text-secondary hover:bg-bg-hover cursor-pointer transition-colors"
            >
              #{{ tag }}
            </span>
          </div>
          
          <!-- 帖子底部操作 -->
          <footer class="flex items-center justify-between pt-4 border-t border-border">
            <div class="flex items-center gap-4">
              <!-- 点赞 -->
              <button 
                class="flex items-center gap-2 px-3 py-1.5 rounded-lg transition-colors"
                :class="[
                  post.isLiked 
                    ? 'text-red-400 bg-red-500/10' 
                    : 'text-text-secondary hover:bg-bg-hover'
                ]"
                @click="toggleLike"
              >
                <span :class="post.isLiked ? 'i-tabler-heart-filled' : 'i-tabler-heart'" />
                {{ post.likes }}
              </button>
              
              <!-- 评论数 -->
              <span class="flex items-center gap-2 text-text-secondary">
                <span class="i-tabler-message-circle" />
                {{ post.comments.length }} Comments
              </span>
              
              <!-- 浏览数 -->
              <span class="flex items-center gap-2 text-text-secondary">
                <span class="i-tabler-eye" />
                {{ post.views }} Views
              </span>
            </div>
            
            <div class="flex items-center gap-2">
              <!-- 收藏 -->
              <button 
                class="p-2 rounded-lg transition-colors"
                :class="[
                  post.isBookmarked 
                    ? 'text-amber-400 bg-amber-500/10' 
                    : 'text-text-secondary hover:bg-bg-hover'
                ]"
                :title="post.isBookmarked ? 'Remove bookmark' : 'Bookmark'"
                @click="toggleBookmark"
              >
                <span :class="post.isBookmarked ? 'i-tabler-bookmark-filled' : 'i-tabler-bookmark'" />
              </button>
              
              <!-- 分享 -->
              <div class="relative">
                <button 
                  class="p-2 rounded-lg text-text-secondary hover:bg-bg-hover transition-colors"
                  title="Share"
                  @click="showShareMenu = !showShareMenu"
                >
                  <span class="i-tabler-share" />
                </button>
                
                <Transition
                  enter-active-class="transition-all duration-200"
                  enter-from-class="opacity-0 scale-95"
                  enter-to-class="opacity-100 scale-100"
                  leave-active-class="transition-all duration-150"
                  leave-from-class="opacity-100 scale-100"
                  leave-to-class="opacity-0 scale-95"
                >
                  <div 
                    v-if="showShareMenu" 
                    class="absolute right-0 top-full mt-2 w-48 py-2 bg-bg-raised border border-border rounded-lg shadow-xl z-10"
                  >
                    <button 
                      class="w-full px-4 py-2 text-left text-sm text-text-secondary hover:bg-bg-hover hover:text-text flex items-center gap-2"
                      @click="copyLink"
                    >
                      <span class="i-tabler-link" />
                      Copy link
                    </button>
                    <button class="w-full px-4 py-2 text-left text-sm text-text-secondary hover:bg-bg-hover hover:text-text flex items-center gap-2">
                      <span class="i-tabler-brand-twitter" />
                      Share on Twitter
                    </button>
                    <button class="w-full px-4 py-2 text-left text-sm text-text-secondary hover:bg-bg-hover hover:text-text flex items-center gap-2">
                      <span class="i-tabler-brand-discord" />
                      Share on Discord
                    </button>
                  </div>
                </Transition>
              </div>
              
              <!-- 更多操作 -->
              <button 
                class="p-2 rounded-lg text-text-secondary hover:bg-bg-hover transition-colors"
                title="More options"
              >
                <span class="i-tabler-dots" />
              </button>
            </div>
          </footer>
        </article>
        
        <!-- 评论区 -->
        <section class="space-y-4">
          <h2 class="text-lg font-semibold text-text flex items-center gap-2">
            <span class="i-tabler-messages" />
            Comments ({{ post.comments.length }})
          </h2>
          
          <!-- 发表评论 -->
          <div class="card">
            <h3 class="text-sm font-medium text-text-secondary mb-3">Write a comment</h3>
            <MarkdownEditor 
              v-model="newComment" 
              placeholder="Share your thoughts... Markdown is supported!"
              min-height="150px"
            />
            <div class="flex justify-end mt-3">
              <button 
                class="btn-primary"
                :disabled="!newComment.trim()"
                @click="submitComment"
              >
                <span class="i-tabler-send" />
                Post Comment
              </button>
            </div>
          </div>
          
          <!-- 评论列表 -->
          <div class="space-y-4">
            <div 
              v-for="comment in post.comments" 
              :key="comment.id"
              class="card"
            >
              <!-- 评论内容 -->
              <div class="flex gap-4">
                <!-- 作者头像 -->
                <div class="flex-shrink-0">
                  <div class="w-10 h-10 rounded-full bg-gradient-to-br from-brand to-brand-dark flex-center text-bg font-bold text-sm">
                    {{ comment.author.avatar }}
                  </div>
                </div>
                
                <!-- 评论主体 -->
                <div class="flex-1 min-w-0">
                  <!-- 作者信息 -->
                  <div class="flex items-center gap-2 mb-2">
                    <span class="font-medium text-text">{{ comment.author.name }}</span>
                    <span 
                      v-if="comment.author.badge" 
                      class="text-xs px-2 py-0.5 rounded-full"
                      :class="getBadgeClass(comment.author.badge)"
                    >
                      {{ comment.author.badge }}
                    </span>
                    <span class="text-text-muted">·</span>
                    <span class="text-sm text-text-muted">{{ comment.createdAt }}</span>
                  </div>
                  
                  <!-- 评论内容 -->
                  <MarkdownRenderer :content="comment.content" class="text-sm" />
                  
                  <!-- 评论操作 -->
                  <div class="flex items-center gap-4 mt-3">
                    <button 
                      class="flex items-center gap-1 text-sm transition-colors"
                      :class="[
                        comment.isLiked 
                          ? 'text-red-400' 
                          : 'text-text-muted hover:text-text'
                      ]"
                      @click="toggleCommentLike(comment)"
                    >
                      <span :class="comment.isLiked ? 'i-tabler-heart-filled' : 'i-tabler-heart'" />
                      {{ comment.likes }}
                    </button>
                    <button 
                      class="flex items-center gap-1 text-sm text-text-muted hover:text-text transition-colors"
                      @click="replyingTo = replyingTo === comment.id ? null : comment.id"
                    >
                      <span class="i-tabler-message-circle" />
                      Reply
                    </button>
                  </div>
                  
                  <!-- 回复输入框 -->
                  <Transition
                    enter-active-class="transition-all duration-300"
                    enter-from-class="opacity-0 -translate-y-2"
                    enter-to-class="opacity-100 translate-y-0"
                    leave-active-class="transition-all duration-200"
                    leave-from-class="opacity-100 translate-y-0"
                    leave-to-class="opacity-0 -translate-y-2"
                  >
                    <div v-if="replyingTo === comment.id" class="mt-4">
                      <MarkdownEditor 
                        v-model="replyContent" 
                        placeholder="Write a reply..."
                        min-height="100px"
                      />
                      <div class="flex justify-end gap-2 mt-2">
                        <button 
                          class="btn-ghost text-sm"
                          @click="replyingTo = null; replyContent = ''"
                        >
                          Cancel
                        </button>
                        <button 
                          class="btn-primary text-sm"
                          :disabled="!replyContent.trim()"
                          @click="submitReply(comment)"
                        >
                          Reply
                        </button>
                      </div>
                    </div>
                  </Transition>
                  
                  <!-- 回复列表 -->
                  <div v-if="comment.replies?.length" class="mt-4 pl-4 border-l-2 border-border space-y-4">
                    <div 
                      v-for="reply in comment.replies" 
                      :key="reply.id"
                      class="flex gap-3"
                    >
                      <div class="w-8 h-8 rounded-full bg-gradient-to-br from-brand to-brand-dark flex-center text-bg font-bold text-xs flex-shrink-0">
                        {{ reply.author.avatar }}
                      </div>
                      <div class="flex-1 min-w-0">
                        <div class="flex items-center gap-2 mb-1">
                          <span class="font-medium text-text text-sm">{{ reply.author.name }}</span>
                          <span 
                            v-if="reply.author.badge" 
                            class="text-xs px-1.5 py-0.5 rounded-full"
                            :class="getBadgeClass(reply.author.badge)"
                          >
                            {{ reply.author.badge }}
                          </span>
                          <span class="text-text-muted">·</span>
                          <span class="text-xs text-text-muted">{{ reply.createdAt }}</span>
                        </div>
                        <MarkdownRenderer :content="reply.content" class="text-sm" />
                        <button 
                          class="flex items-center gap-1 text-xs text-text-muted hover:text-text mt-2 transition-colors"
                        >
                          <span class="i-tabler-heart" />
                          {{ reply.likes }}
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </template>
    </main>
  </div>
</template>
