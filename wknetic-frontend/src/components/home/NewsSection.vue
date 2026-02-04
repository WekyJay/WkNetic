<script setup lang="ts">
const newsItems = [
  {
    title: 'Server Build Guide Week',
    description: 'Daily tutorials on spawns, hubs, and economy areas with creator spotlights.',
    date: 'January 18, 2026',
    image: '📘',
    gradient: 'from-brand/20 to-blue-500/20',
  },
  {
    title: 'Recruiting Fair: Season Launch',
    description: 'Meet new servers, apply to teams, and find long-term builder roles.',
    date: 'January 27, 2026',
    image: '📣',
    gradient: 'from-yellow-500/20 to-orange-500/20',
  },
  {
    title: 'Winter Build Cup Finals',
    description: 'Watch finalists present their cities and vote for the community pick.',
    date: 'February 2, 2026',
    image: '🏆',
    gradient: 'from-pink-500/20 to-purple-500/20',
  },
]
</script>

<template>
  <section class="py-16 md:py-24 bg-bg">
    <div class="container-main">
      <!-- 标题 -->
      <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-12">
        <div>
          <h2 class="text-2xl md:text-3xl font-bold text-text">
            Latest updates from the Minecraft server community
          </h2>
        </div>
        <a href="#" class="btn-secondary text-sm">
          View all updates
          <span class="i-tabler-arrow-right"></span>
        </a>
      </div>

      <!-- 新闻网格 -->
      <div class="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
        <article
            v-for="news in newsItems"
            :key="news.title"
            class="card overflow-hidden group cursor-pointer"
        >
          <!-- 图片区域 -->
          <div
              class="relative h-40 flex-center bg-gradient-to-br overflow-hidden"
              :class="news.gradient"
          >
            <span class="text-5xl group-hover:scale-110 transition-transform duration-300">
              {{ news.image }}
            </span>
            <!-- 悬浮效果 -->
            <div class="absolute inset-0 bg-black/0 group-hover:bg-black/10 transition-colors"></div>
          </div>

          <!-- 内容 -->
          <div class="p-5">
            <h3 class="text-lg font-semibold text-text group-hover:text-brand transition-colors mb-2">
              {{ news.title }}
            </h3>
            <p class="text-text-secondary text-sm mb-4 line-clamp-2">
              {{ news.description }}
            </p>
            <time class="text-xs text-text-muted">{{ news.date }}</time>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
