<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { listAllTopics } from '@/api/topic'
import type { TopicVO } from '@/api/topic'

const { t } = useI18n()

const activeTopic = ref<number | null>(null)
const topics = ref<TopicVO[]>([])
const tags = ref<{ name: string; count: number; color: string }[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

// 默认标签（可以后续从API获取）
const defaultTags = [
  { name: 'Fabric', count: 892, color: 'bg-amber-500/20 text-amber-400' },
  { name: 'Forge', count: 756, color: 'bg-orange-500/20 text-orange-400' },
  { name: 'Quilt', count: 234, color: 'bg-purple-500/20 text-purple-400' },
  { name: 'NeoForge', count: 189, color: 'bg-red-500/20 text-red-400' },
  { name: '1.20.x', count: 1024, color: 'bg-green-500/20 text-green-400' },
  { name: '1.19.x', count: 567, color: 'bg-blue-500/20 text-blue-400' },
]

onMounted(async () => {
  try {
    loading.value = true
    const response = await listAllTopics()
    topics.value = response.data || []
    tags.value = defaultTags
    // 默认选中第一个话题
    if (topics.value.length > 0 && topics.value[0]) {
      activeTopic.value = topics.value[0].topicId
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to load topics'
    console.error('Error loading topics:', e)
  } finally {
    loading.value = false
  }
})

const setTopic = (id: number) => {
  activeTopic.value = id
}
</script>

<template>
  <div class="space-y-4">
    <!-- 加载状态 -->
    <div v-if="loading" class="card">
      <div class="space-y-2">
        <div class="h-8 bg-bg-surface rounded animate-pulse" />
        <div class="h-8 bg-bg-surface rounded animate-pulse" />
        <div class="h-8 bg-bg-surface rounded animate-pulse" />
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="card">
      <div class="text-center text-text-secondary">
        <p>{{ error }}</p>
      </div>
    </div>

    <!-- 话题分类 -->
    <div v-else class="card">
      <h3 class="text-sm font-semibold text-text-secondary uppercase tracking-wide mb-3">
        {{ t('forum.topics') }}
      </h3>
      <nav class="space-y-1">
        <button
          v-for="topic in topics"
          :key="topic.topicId"
          class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-left transition-all duration-200"
          :class="[
            activeTopic === topic.topicId 
              ? 'bg-brand/10' 
              : 'text-text-secondary hover:text-text'
          ]"
          @click="setTopic(topic.topicId)"
        >
          <span :class="['text-lg flex-shrink-0', topic.icon || 'i-tabler-bookmark']" />
          <span class="flex-1 font-medium">{{ topic.topicName }}</span>
          <span 
            class="text-xs px-2 py-0.5 rounded-full"
            :class="activeTopic === topic.topicId ? 'bg-brand/20' : 'bg-bg-surface'"
          >
            {{ topic.postCount }}
          </span>
        </button>
      </nav>
    </div>

    <!-- 热门标签 -->
    <div class="card">
      <h3 class="text-sm font-semibold text-text-secondary uppercase tracking-wide mb-3">
        {{ t('forum.popularTags') }}
      </h3>
      <div class="flex flex-wrap gap-2">
        <button
          v-for="tag in tags"
          :key="tag.name"
          class="px-3 py-1.5 rounded-full text-sm font-medium transition-all duration-200 hover:scale-105"
          :class="tag.color"
        >
          {{ tag.name }}
          <span class="ml-1 opacity-70">{{ tag.count }}</span>
        </button>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="card">
      <h3 class="text-sm font-semibold text-text-secondary uppercase tracking-wide mb-3">
        {{ t('forum.quickActions') }}
      </h3>
      <div class="space-y-2">
        <button class="w-full btn-secondary text-sm justify-start">
          <span class="i-tabler-bookmark text-lg" />
          {{ t('forum.myBookmarks') }}
        </button>
        <button class="w-full btn-secondary text-sm justify-start">
          <span class="i-tabler-message-circle text-lg" />
          {{ t('forum.myPosts') }}
        </button>
        <button class="w-full btn-secondary text-sm justify-start">
          <span class="i-tabler-bell text-lg" />
          {{ t('forum.subscriptions') }}
        </button>
      </div>
    </div>
  </div>
</template>
