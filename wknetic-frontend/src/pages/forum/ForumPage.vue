<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import ForumSidebar from '@/components/forum/ForumSidebar.vue'
import ForumContent from '@/components/forum/ForumContent.vue'
import ForumRightPanel from '@/components/forum/ForumRightPanel.vue'

const { t } = useI18n()
</script>

<template>
  <div class="min-h-screen bg-bg py-6">
    <div class="container-main">
      <!-- 页面标题 -->
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-text">{{ t('forum.title') }}</h1>
        <p class="text-text-secondary mt-1">{{ t('forum.subtitle') }}</p>
      </div>

      <!-- 三栏布局 -->
      <div class="flex gap-6">
        <!-- 左侧：话题选择 -->
        <aside class="w-64 flex-shrink-0 hidden lg:block">
          <ForumSidebar />
        </aside>

        <!-- 中间：内容区域 -->
        <main class="flex-1 min-w-0">
          <ForumContent />
        </main>

        <!-- 右侧：统计信息 -->
        <aside class="w-80 flex-shrink-0 hidden xl:block">
          <ForumRightPanel />
        </aside>
      </div>
    </div>
  </div>
</template>
