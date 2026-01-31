<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

interface Topic {
  id: string
  name: string
  icon: string
  count: number
  color?: string
}

const activeTopic = ref('all')

const topics: Topic[] = [
  { id: 'all', name: 'All Topics', icon: 'i-tabler-list', count: 2847 },
  { id: 'announcements', name: 'Announcements', icon: 'i-tabler-speakerphone', count: 42, color: 'text-brand' },
  { id: 'mods', name: 'Mod Discussion', icon: 'i-tabler-puzzle', count: 1256 },
  { id: 'help', name: 'Help & Support', icon: 'i-tabler-help-circle', count: 534 },
  { id: 'showcase', name: 'Showcase', icon: 'i-tabler-photo', count: 328 },
  { id: 'suggestions', name: 'Suggestions', icon: 'i-tabler-bulb', count: 187 },
  { id: 'offtopic', name: 'Off-Topic', icon: 'i-tabler-coffee', count: 500 },
]

const tags = [
  { name: 'Fabric', count: 892, color: 'bg-amber-500/20 text-amber-400' },
  { name: 'Forge', count: 756, color: 'bg-orange-500/20 text-orange-400' },
  { name: 'Quilt', count: 234, color: 'bg-purple-500/20 text-purple-400' },
  { name: 'NeoForge', count: 189, color: 'bg-red-500/20 text-red-400' },
  { name: '1.20.x', count: 1024, color: 'bg-brand/20 text-brand' },
  { name: '1.19.x', count: 567, color: 'bg-blue-500/20 text-blue-400' },
]

const setTopic = (id: string) => {
  activeTopic.value = id
}
</script>

<template>
  <div class="space-y-4">
    <!-- 话题分类 -->
    <div class="card">
      <h3 class="text-sm font-semibold text-text-secondary uppercase tracking-wide mb-3">
        {{ t('forum.topics') }}
      </h3>
      <nav class="space-y-1">
        <button
          v-for="topic in topics"
          :key="topic.id"
          class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-left transition-all duration-200"
          :class="[
            activeTopic === topic.id 
              ? 'bg-brand/10' 
              : 'text-text-secondary hover:text-text'
          ]"
          @click="setTopic(topic.id)"
        >
          <span :class="[topic.icon, topic.color || '', 'text-lg']" />
          <span class="flex-1 font-medium">{{ topic.name }}</span>
          <span 
            class="text-xs px-2 py-0.5 rounded-full"
            :class="activeTopic === topic.id ? 'bg-brand/20' : 'bg-bg-surface'"
          >
            {{ topic.count }}
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
