<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

// 漂浮方块动画
const blocks = ref<{ id: number; x: number; y: number; size: number; delay: number; duration: number }[]>([])

onMounted(() => {
  // 生成随机漂浮方块
  for (let i = 0; i < 12; i++) {
    blocks.value.push({
      id: i,
      x: Math.random() * 100,
      y: Math.random() * 100,
      size: Math.random() * 20 + 10,
      delay: Math.random() * 5,
      duration: Math.random() * 10 + 15,
    })
  }
})
</script>

<template>
  <div class="min-h-[calc(100vh-4rem)] flex-center relative overflow-hidden">
    <!-- 背景装饰 -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute top-1/4 -right-20 w-80 h-80 bg-brand/5 rounded-full blur-3xl"></div>
      <div class="absolute bottom-1/4 -left-20 w-80 h-80 bg-brand/5 rounded-full blur-3xl"></div>
      
      <!-- 漂浮的 Minecraft 风格方块 -->
      <div 
        v-for="block in blocks" 
        :key="block.id"
        class="absolute opacity-10 rounded-sm bg-brand animate-float"
        :style="{
          left: `${block.x}%`,
          top: `${block.y}%`,
          width: `${block.size}px`,
          height: `${block.size}px`,
          animationDelay: `${block.delay}s`,
          animationDuration: `${block.duration}s`,
        }"
      ></div>
    </div>

    <!-- 主内容 -->
    <div class="container-main relative z-10 text-center py-16">
      <!-- 404 数字 -->
      <div class="relative inline-block mb-8">
        <h1 class="text-[8rem] sm:text-[12rem] md:text-[16rem] font-bold leading-none select-none">
          <span class="text-bg-surface">4</span>
          <span class="relative inline-block">
            <!-- Creeper 脸形状代替 0 -->
            <span class="text-brand">0</span>
            <div class="absolute inset-0 flex-center">
              <div class="w-16 h-20 sm:w-24 sm:h-28 md:w-32 md:h-36 relative">
                <!-- Creeper 简化脸 -->
                <div class="absolute top-2 sm:top-3 left-1 sm:left-2 w-4 h-4 sm:w-6 sm:h-6 md:w-8 md:h-8 bg-bg rounded-sm"></div>
                <div class="absolute top-2 sm:top-3 right-1 sm:right-2 w-4 h-4 sm:w-6 sm:h-6 md:w-8 md:h-8 bg-bg rounded-sm"></div>
                <div class="absolute bottom-2 sm:bottom-3 left-1/2 -translate-x-1/2 w-3 h-3 sm:w-4 sm:h-5 md:w-6 md:h-6 bg-bg rounded-sm"></div>
                <div class="absolute bottom-0 left-1/2 -translate-x-1/2 w-6 h-4 sm:w-8 sm:h-6 md:w-10 md:h-8 bg-bg rounded-sm"></div>
              </div>
            </div>
          </span>
          <span class="text-bg-surface">4</span>
        </h1>
        
        <!-- 光晕效果 -->
        <div class="absolute inset-0 flex-center pointer-events-none">
          <div class="w-48 h-48 sm:w-64 sm:h-64 bg-brand/10 rounded-full blur-3xl"></div>
        </div>
      </div>

      <!-- 错误信息 -->
      <div class="space-y-4 mb-10">
        <h2 class="text-2xl sm:text-3xl md:text-4xl font-bold text-text">
          Page not found
        </h2>
        <p class="text-text-secondary text-lg max-w-md mx-auto">
          Looks like a Creeper blew up this page. The content you're looking for doesn't exist or has been moved.
        </p>
      </div>

      <!-- 操作按钮 -->
      <div class="flex flex-col sm:flex-row items-center justify-center gap-4">
        <router-link to="/" class="btn-primary text-base px-8 py-3">
          <span class="i-tabler-home text-lg"></span>
          Back to home
        </router-link>
        <router-link to="/mods" class="btn-secondary text-base px-8 py-3">
          <span class="i-tabler-compass text-lg"></span>
          Discover mods
        </router-link>
      </div>

      <!-- 搜索建议 -->
      <div class="mt-16 max-w-xl mx-auto">
        <p class="text-text-muted text-sm mb-4">Or try searching for what you need:</p>
        <div class="relative">
          <span class="absolute left-4 top-1/2 -translate-y-1/2 i-tabler-search text-text-muted"></span>
          <input 
            type="text" 
            placeholder="Search mods, plugins, resource packs..."
            class="w-full bg-bg-raised border border-border rounded-xl pl-12 pr-4 py-3 text-text placeholder-text-muted focus:border-brand focus:outline-none transition-colors"
          />
        </div>
      </div>

      <!-- 快速链接 -->
      <div class="mt-12 flex flex-wrap items-center justify-center gap-3">
        <span class="text-text-muted text-sm">Quick links:</span>
        <router-link to="/mods" class="badge-green hover:bg-brand/30 transition-colors">
          Mods
        </router-link>
        <router-link to="/plugins" class="badge-green hover:bg-brand/30 transition-colors">
          Plugins
        </router-link>
        <router-link to="/resourcepacks" class="badge-green hover:bg-brand/30 transition-colors">
          Resource Packs
        </router-link>
        <router-link to="/shaders" class="badge-green hover:bg-brand/30 transition-colors">
          Shaders
        </router-link>
        <router-link to="/modpacks" class="badge-green hover:bg-brand/30 transition-colors">
          Modpacks
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  25% {
    transform: translateY(-20px) rotate(5deg);
  }
  50% {
    transform: translateY(-10px) rotate(-3deg);
  }
  75% {
    transform: translateY(-25px) rotate(2deg);
  }
}

.animate-float {
  animation: float ease-in-out infinite;
}
</style>
