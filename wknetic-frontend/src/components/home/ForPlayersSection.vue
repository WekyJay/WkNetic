<script setup lang="ts">
import { ref } from 'vue'

const searchQuery = ref('')
const sortBy = ref('Relevance')

const mockResults = [
  {
    name: 'Starter Town Walkthrough',
    author: 'Build Guild',
    description: 'A clear build guide for starter towns with palette tips and layout grids.',
    platforms: ['Guide'],
    downloads: '12.4k',
    followers: '1.3k',
    updated: 'last week',
    icon: '🏘️'
  },
  {
    name: 'Aurora SMP Recruiting',
    author: 'Aurora SMP',
    description: 'Looking for builders, organizers, and event hosts for Season 5.',
    platforms: ['Server'],
    downloads: '2.1k',
    followers: '412',
    updated: '2 days ago',
    icon: '🌌'
  },
  {
    name: 'Medieval Resource Kit',
    author: 'CraftWorks',
    description: 'Shared resource pack and schematic bundle for medieval city builds.',
    platforms: ['Resource Pack'],
    downloads: '6.7k',
    followers: '980',
    updated: '3 days ago',
    icon: '🏰'
  },
]

const notifications = [
  { project: 'Build Jam Weekend', message: 'Theme announced: Cliffside Ports. Submissions open.', time: '3 hours ago', icon: '🏗️' },
  { project: 'Survival Hub', message: 'Recruiting builders and story writers for the next season.', time: '8 hours ago', icon: '🧭' },
  { project: 'Resource Share', message: 'New medieval palette pack and signage kit available.', time: '12 hours ago', icon: '📦' },
]

const launchers = [
  { name: 'Build Guides', icon: '📘' },
  { name: 'Server Recruiting', icon: '📣' },
  { name: 'Resource Library', icon: '🧱' },
  { name: 'Event Calendar', icon: '🗓️' },
]
</script>

<template>
  <section class="py-16 md:py-24 bg-bg">
    <div class="container-main">
      <!-- 标题 -->
      <div class="text-center mb-16">
        <span class="text-brand font-semibold text-sm uppercase tracking-wider mb-3 block">For Players</span>
        <h2 class="text-3xl md:text-4xl lg:text-5xl font-bold text-text mb-4">
          Build, join, and share with Minecraft servers
        </h2>
        <p class="text-text-secondary text-lg max-w-2xl mx-auto">
          Find build tutorials, server recruitment posts, shared resources, and community events in one place.
        </p>
      </div>

      <!-- 功能展示网格 -->
      <div class="grid lg:grid-cols-2 gap-8 lg:gap-12">
        <!-- 左侧: 搜索演示 -->
        <div class="space-y-6">
          <div>
            <h3 class="text-xl font-semibold text-text mb-2">Find the right server or guide fast</h3>
            <p class="text-text-secondary">Search tutorials, recruitment posts, resources, and events with smart filters.</p>
          </div>
          
          <!-- 搜索框模拟 -->
          <div class="card p-6">
            <!-- 搜索输入 -->
            <div class="flex gap-3 mb-4">
              <div class="relative flex-1">
                <span class="i-tabler-search absolute left-3 top-1/2 -translate-y-1/2 text-text-muted"></span>
                <input 
                  v-model="searchQuery"
                  type="text"
                  placeholder="Search"
                  class="w-full input-base pl-10"
                />
              </div>
              <div class="relative">
                <select v-model="sortBy" class="input-base pr-8 appearance-none cursor-pointer">
                  <option>Relevance</option>
                  <option>Downloads</option>
                  <option>Follows</option>
                  <option>Updated</option>
                  <option>Newest</option>
                </select>
                <span class="i-tabler-chevron-down absolute right-3 top-1/2 -translate-y-1/2 text-text-muted pointer-events-none"></span>
              </div>
            </div>

            <!-- 搜索结果 -->
            <div class="space-y-3">
              <div 
                v-for="result in mockResults" 
                :key="result.name"
                class="flex gap-3 p-3 rounded-xl bg-bg-surface hover:bg-bg-hover transition-colors cursor-pointer"
              >
                <div class="w-12 h-12 rounded-lg bg-bg-raised flex-center text-xl flex-shrink-0">
                  {{ result.icon }}
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-start justify-between gap-2">
                    <div>
                      <h4 class="font-medium text-text">{{ result.name }}</h4>
                      <p class="text-xs text-text-muted">by {{ result.author }}</p>
                    </div>
                  </div>
                  <p class="text-sm text-text-secondary mt-1 line-clamp-1">{{ result.description }}</p>
                  <div class="flex items-center gap-3 mt-2 text-xs text-text-muted">
                    <span class="flex items-center gap-1">
                      <span class="i-tabler-download"></span>
                      {{ result.downloads }}
                    </span>
                    <span class="flex items-center gap-1">
                      <span class="i-tabler-heart"></span>
                      {{ result.followers }}
                    </span>
                    <span>Updated {{ result.updated }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧: 通知 + 启动器 -->
        <div class="space-y-8">
          <!-- 关注项目通知 -->
          <div>
            <h3 class="text-xl font-semibold text-text mb-2">Follow servers and events you love</h3>
            <p class="text-text-secondary mb-4">Get notified when servers recruit, guides update, or events go live.</p>
            
            <div class="card p-4">
              <div class="flex items-center gap-2 mb-4">
                <span class="i-tabler-bell text-lg text-brand"></span>
                <span class="font-medium">Notifications</span>
              </div>
              <div class="space-y-3">
                <div 
                  v-for="notif in notifications" 
                  :key="notif.project"
                  class="flex gap-3 p-3 rounded-lg bg-bg-surface"
                >
                  <div class="w-10 h-10 rounded-lg bg-bg-raised flex-center text-lg flex-shrink-0">
                    {{ notif.icon }}
                  </div>
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium text-brand">{{ notif.project }} has been updated!</p>
                    <p class="text-xs text-text-secondary mt-0.5">{{ notif.message }}</p>
                    <p class="text-xs text-text-muted mt-1">Received {{ notif.time }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 启动器支持 -->
          <div>
            <h3 class="text-xl font-semibold text-text mb-2">Everything you need, in one hub</h3>
            <p class="text-text-secondary mb-4">Jump between build guides, recruitment posts, resources, and event calendars.</p>
            
            <div class="card p-6">
              <div class="flex items-center justify-center">
                <!-- Minecraft 窗口模拟 -->
                <div class="relative w-48 h-32 bg-bg-surface rounded-lg border border-border overflow-hidden">
                  <div class="absolute inset-0 flex-center">
                    <div class="w-16 h-16 bg-brand/20 rounded-lg flex-center">
                      <span class="i-tabler-cube text-brand text-3xl"></span>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 启动器图标 -->
              <div class="flex items-center justify-center gap-4 mt-6">
                <div 
                  v-for="launcher in launchers" 
                  :key="launcher.name"
                  class="w-10 h-10 rounded-lg bg-bg-surface hover:bg-bg-hover flex-center text-xl cursor-pointer transition-colors"
                  :title="launcher.name"
                >
                  {{ launcher.icon }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
