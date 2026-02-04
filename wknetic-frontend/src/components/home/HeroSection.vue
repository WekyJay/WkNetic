<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const words = ['build guides', 'server recruiting', 'resource sharing', 'events', 'community hubs', 'server showcases']
const currentWordIndex = ref(0)
const isAnimating = ref(false)
const authStore = useAuthStore()

// 判断用户是否已登录
const isUserRegistered = computed(() => !!authStore.token && !!authStore.user)

let interval: ReturnType<typeof setInterval>

onMounted(() => {
  interval = setInterval(() => {
    isAnimating.value = true
    setTimeout(() => {
      currentWordIndex.value = (currentWordIndex.value + 1) % words.length
      isAnimating.value = false
    }, 300)
  }, 2500)
})

onUnmounted(() => {
  clearInterval(interval)
})
</script>

<template>
  <section class="relative overflow-hidden">
    <!-- 背景装饰 -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 w-96 h-96 bg-brand/10 rounded-full blur-3xl"></div>
      <div class="absolute -bottom-40 -left-40 w-96 h-96 bg-brand/5 rounded-full blur-3xl"></div>
    </div>

    <div class="container-main relative z-10 py-16 md:py-24 lg:py-32">
      <div class="max-w-4xl mx-auto text-center">
        <!-- 主标题 -->
        <h1 class="text-4xl sm:text-5xl md:text-6xl lg:text-7xl font-bold leading-tight mb-6">
          <span class="text-text">A server community</span>
          <br />
          <span class="text-text">built for Minecraft servers</span>
          <br />
          <!-- 动态文字 -->
          <span class="relative inline-block h-[1.2em] overflow-hidden">
            <span
                class="inline-block text-brand transition-all duration-300"
                :class="isAnimating ? 'opacity-0 -translate-y-4' : 'opacity-100 translate-y-0'"
            >
              {{ words[currentWordIndex] }}
            </span>
          </span>
        </h1>

        <!-- 副标题 -->
        <p class="text-lg sm:text-xl text-text-secondary max-w-2xl mx-auto mb-10">
          Learn with build tutorials, recruit for your server, share resources, and run community events in one place.
        </p>

        <!-- CTA 按钮 -->
        <div class="flex flex-col sm:flex-row items-center justify-center gap-4">
          <!-- 未注册用户状态 -->
          <template v-if="!isUserRegistered">
            <router-link to="/search" class="btn-primary text-base px-8 py-3">
              <span class="i-tabler-compass text-lg"></span>
              Explore the community
            </router-link>
            <router-link to="/register" class="btn-secondary text-base px-8 py-3">
              <span class="i-tabler-user-plus text-lg"></span>
              Join now
            </router-link>
          </template>

          <!-- 已注册用户状态 -->
          <template v-else>
            <a href="/docs/server-guide" target="_blank" class="btn-primary text-base px-8 py-3">
              <span class="i-tabler-book text-lg"></span>
              Server Guide
            </a>
            <a href="/forum" target="_blank" class="btn-secondary text-base px-8 py-3">
              <span class="i-tabler-message-circle text-lg"></span>
              Forum
            </a>
          </template>
        </div>
      </div>
    </div>
  </section>
</template>
