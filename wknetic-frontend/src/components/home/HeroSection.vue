<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const words = ['mods', 'resource packs', 'data packs', 'shaders', 'modpacks', 'plugins', 'servers']
const currentWordIndex = ref(0)
const isAnimating = ref(false)

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
          <span class="text-text">The place for Minecraft</span>
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
          Discover, play, and share Minecraft content through our open-source platform built for the community.
        </p>

        <!-- CTA 按钮 -->
        <div class="flex flex-col sm:flex-row items-center justify-center gap-4">
          <router-link to="/mods" class="btn-primary text-base px-8 py-3">
            <span class="i-tabler-compass text-lg"></span>
            Discover mods
          </router-link>
          <button class="btn-secondary text-base px-8 py-3">
            <span class="i-tabler-user-plus text-lg"></span>
            Sign up
          </button>
        </div>
      </div>
    </div>
  </section>
</template>
