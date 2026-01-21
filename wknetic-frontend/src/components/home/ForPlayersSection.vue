<script setup lang="ts">
import { ref } from 'vue'

const searchQuery = ref('')
const sortBy = ref('Relevance')

const mockResults = [
  {
    name: 'Leave My Bars Alone',
    author: 'Fuzs',
    description: 'Makes your food and experience bars visible when riding on a horse.',
    platforms: ['Fabric', 'Forge', 'NeoForge'],
    downloads: '2.03M',
    followers: '672',
    updated: 'last month',
    icon: '🐴'
  },
  {
    name: 'Leave My Hotbar Alone!',
    author: 'MiladMoro88',
    description: 'Reverts the hotbar textures from any GUI modification resourcepacks.',
    platforms: ['Resource Pack'],
    downloads: '1,224',
    followers: '2',
    updated: 'last month',
    icon: '🎨'
  },
  {
    name: 'Leave Bind!',
    author: 'TutlaMC',
    description: 'LeaveBind! Lets you leave a server with a keybind',
    platforms: ['Fabric'],
    downloads: '501',
    followers: '19',
    updated: '2 months ago',
    icon: '⌨️'
  },
]

const notifications = [
  { project: 'Caelesti Fenestra', message: 'Version 1.1.2 has been released for 1.21.8', time: '21 hours ago', icon: '🌌' },
  { project: 'Bloom and Doom', message: 'Version 1.0.3 has been released for 1.21.1', time: '21 hours ago', icon: '🌸' },
  { project: 'Cannons Revamped', message: 'Version 15.1 has been released for 1.21.11', time: '22 hours ago', icon: '💥' },
]

const launchers = [
  { name: 'WkNetic App', icon: '🟢' },
  { name: 'ATLauncher', icon: '🔷' },
  { name: 'MultiMC', icon: '🟣' },
  { name: 'Prism Launcher', icon: '🔶' },
]
</script>

<template>
  <section class="py-16 md:py-24 bg-bg">
    <div class="container-main">
      <!-- 标题 -->
      <div class="text-center mb-16">
        <span class="text-brand font-semibold text-sm uppercase tracking-wider mb-3 block">For Players</span>
        <h2 class="text-3xl md:text-4xl lg:text-5xl font-bold text-text mb-4">
          Discover over 75,000 creations
        </h2>
        <p class="text-text-secondary text-lg max-w-2xl mx-auto">
          From magical biomes to cursed dungeons, you can be sure to find content to bring your gameplay to the next level.
        </p>
      </div>

      <!-- 功能展示网格 -->
      <div class="grid lg:grid-cols-2 gap-8 lg:gap-12">
        <!-- 左侧: 搜索演示 -->
        <div class="space-y-6">
          <div>
            <h3 class="text-xl font-semibold text-text mb-2">Find what you want, quickly and easily</h3>
            <p class="text-text-secondary">WkNetic's lightning-fast search and powerful filters let you find what you want as you type.</p>
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
            <h3 class="text-xl font-semibold text-text mb-2">Follow projects you love</h3>
            <p class="text-text-secondary mb-4">Get notified every time your favorite projects update and stay in the loop.</p>
            
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
            <h3 class="text-xl font-semibold text-text mb-2">Play with your favorite launcher</h3>
            <p class="text-text-secondary mb-4">WkNetic's open-source API lets launchers add deep integration with WkNetic.</p>
            
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
