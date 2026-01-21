<script setup lang="ts">
interface Project {
  id: number
  name: string
  description: string
  icon: string
  downloads: string
  category: string
}

defineProps<{
  project: Project
}>()
</script>

<template>
  <article class="card-hover flex-shrink-0 w-72 p-4 cursor-pointer group">
    <div class="flex gap-3">
      <!-- 图标 -->
      <div class="w-12 h-12 rounded-xl bg-bg-surface flex-center text-2xl flex-shrink-0">
        {{ project.icon }}
      </div>
      
      <!-- 内容 -->
      <div class="flex-1 min-w-0">
        <h3 class="font-semibold text-text group-hover:text-brand transition-colors truncate">
          {{ project.name }}
        </h3>
        <p class="text-sm text-text-secondary line-clamp-2 mt-1">
          {{ project.description }}
        </p>
      </div>
    </div>
    
    <!-- 底部信息 -->
    <div class="flex items-center justify-between mt-3 pt-3 border-t border-border">
      <span class="badge-green text-xs">
        {{ project.category }}
      </span>
      <span class="text-xs text-text-muted flex items-center gap-1">
        <span class="i-tabler-download text-sm"></span>
        {{ project.downloads }}
      </span>
    </div>
  </article>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
