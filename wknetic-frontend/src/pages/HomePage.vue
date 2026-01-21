<script setup lang="ts">
import HeroSection from '@/components/home/HeroSection.vue'
import ProjectCarousel from '@/components/home/ProjectCarousel.vue'
import ForPlayersSection from '@/components/home/ForPlayersSection.vue'
import ForCreatorsSection from '@/components/home/ForCreatorsSection.vue'
import NewsSection from '@/components/home/NewsSection.vue'
</script>

<template>
  <div class="min-h-screen">
    <!-- Hero 区域 -->
    <HeroSection />

    <!-- 项目卡片轮播 -->
    <ProjectCarousel />

    <!-- For Players 区域 -->
    <ForPlayersSection />

    <!-- For Creators 区域 -->
    <ForCreatorsSection />

    <!-- 新闻区域 -->
    <NewsSection />
  </div>
</template>
