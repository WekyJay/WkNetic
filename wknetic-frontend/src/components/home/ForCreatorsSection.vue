<script setup lang="ts">
const features = [
  {
    title: 'Discovery',
    description: 'Get your project discovered by thousands of users through search, our home page, Discord server, and more ways to come in the future!',
    icon: 'i-tabler-compass',
    color: 'text-brand',
    bgColor: 'bg-brand/10',
  },
  {
    title: 'Team Management',
    description: 'Invite your teammates and manage roles and permissions with ease',
    icon: 'i-tabler-users-group',
    color: 'text-blue-400',
    bgColor: 'bg-blue-400/10',
  },
  {
    title: 'Monetization',
    description: 'Get paid ad revenue from your project pages and withdraw your funds at any time',
    icon: 'i-tabler-currency-dollar',
    color: 'text-yellow-400',
    bgColor: 'bg-yellow-400/10',
  },
  {
    title: 'Diverse Ecosystem',
    description: 'Integrate with your build tools through Minotaur for automatic uploads right when you release a new version',
    icon: 'i-tabler-puzzle',
    color: 'text-purple-400',
    bgColor: 'bg-purple-400/10',
  },
  {
    title: 'Data and Statistics',
    description: 'Get detailed reports on page views, download counts, and revenue',
    icon: 'i-tabler-chart-line',
    color: 'text-pink-400',
    bgColor: 'bg-pink-400/10',
  },
  {
    title: 'Constantly Evolving',
    description: 'Get the best modding experience possible with constant updates from the WkNetic team',
    icon: 'i-tabler-rocket',
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
          Share your content with the world
        </h2>
        <p class="text-text-secondary text-lg max-w-2xl mx-auto">
          Give an online home to your creations and reach a massive audience of dedicated players.
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
          Start creating
        </button>
      </div>
    </div>
  </section>
</template>
