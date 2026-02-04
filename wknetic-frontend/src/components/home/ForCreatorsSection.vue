<script setup lang="ts">
const features = [
  {
    title: 'Build Tutorials',
    description: 'Publish step-by-step guides, schematics, and palettes for builders of every level.',
    icon: 'i-tabler-book',
    color: 'text-brand',
    bgColor: 'bg-brand/10',
  },
  {
    title: 'Server Recruiting',
    description: 'Post recruitment listings, manage applications, and grow your server team.',
    icon: 'i-tabler-users-group',
    color: 'text-blue-400',
    bgColor: 'bg-blue-400/10',
  },
  {
    title: 'Resource Sharing',
    description: 'Share resource packs, builds, and templates with clear licensing and credits.',
    icon: 'i-tabler-stack-2',
    color: 'text-yellow-400',
    bgColor: 'bg-yellow-400/10',
  },
  {
    title: 'Event Hosting',
    description: 'Create build competitions, seasonal events, and community challenges.',
    icon: 'i-tabler-trophy',
    color: 'text-purple-400',
    bgColor: 'bg-purple-400/10',
  },
  {
    title: 'Insightful Analytics',
    description: 'Track guide views, recruitment clicks, and resource downloads.',
    icon: 'i-tabler-chart-line',
    color: 'text-pink-400',
    bgColor: 'bg-pink-400/10',
  },
  {
    title: 'Creator Tools',
    description: 'Organize projects, update posts quickly, and keep your community informed.',
    icon: 'i-tabler-tool',
    color: 'text-orange-400',
    bgColor: 'bg-orange-400/10',
  },
]
</script>

<template>
  <section class="py-16 md:py-24 bg-bg-raised">
    <div class="container-main">
      <!-- 标题 -->
      <div class="text-center mb-16">
        <span class="text-brand font-semibold text-sm uppercase tracking-wider mb-3 block">For Creators</span>
        <h2 class="text-3xl md:text-4xl lg:text-5xl font-bold text-text mb-4">
          Build a thriving Minecraft server community
        </h2>
        <p class="text-text-secondary text-lg max-w-2xl mx-auto">
          Publish tutorials, recruit teammates, share resources, and host events for your server.
        </p>
      </div>

      <!-- 功能网格 -->
      <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div 
          v-for="feature in features" 
          :key="feature.title"
          class="card p-6 hover:border-border-light transition-all duration-300 group"
        >
          <!-- 图标 -->
          <div 
            class="w-12 h-12 rounded-xl flex-center mb-4 transition-transform group-hover:scale-110"
            :class="feature.bgColor"
          >
            <span :class="[feature.icon, feature.color]" class="text-2xl"></span>
          </div>
          
          <!-- 内容 -->
          <h3 class="text-lg font-semibold text-text mb-2">{{ feature.title }}</h3>
          <p class="text-text-secondary text-sm leading-relaxed">{{ feature.description }}</p>
        </div>
      </div>

      <!-- 底部 CTA -->
      <div class="text-center mt-12">
        <button class="btn-primary text-base px-8 py-3">
          <span class="i-tabler-upload text-lg"></span>
          Start building
        </button>
      </div>
    </div>
  </section>
</template>
