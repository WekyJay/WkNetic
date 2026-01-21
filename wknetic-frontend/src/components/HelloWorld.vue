<script setup lang="ts">
import { useDark, useToggle } from '@vueuse/core'

// 使用 VueUse 快速实现暗黑模式切换
const isDark = useDark()
const toggleDark = useToggle(isDark)

</script>

<template>
  <div class="h-screen w-full flex flex-col items-center justify-center bg-bg-base text-text-base transition-colors">

    <div class="card w-full md:w-96 flex flex-col gap-4">

      <div class="flex justify-between items-center">
        <h2 class="text-xl text-text font-bold flex items-center gap-2">
          <div class="i-carbon-code text-pretty text-2xl" />
          UnoCSS 演示
        </h2>
        <span class="text-xs text-text-muted">v1.0</span>
      </div>

      <p class="text-text-muted">
        这是一个包含响应式、暗黑模式和主题系统的基础卡片。
        试着缩放浏览器窗口查看响应式效果。
      </p>

      <div class="grid grid-cols-2 gap-3">
        <button
            class="btn-secondary border-gray-300"
            @click="toggleDark()"
        >
          <div class="i-carbon-sun dark:i-carbon-moon inline-block mr-1" />
          {{ isDark ? '切换亮色' : '切换暗色' }}
        </button>

        <button class="btn btn-primary" @click="changeThemeColor">
          变更为紫色主题
        </button>
      </div>
    </div>

    <div class="mt-8 text-sm text-text-muted">
      当前断点样式:
      <span class="text-primary font-bold block md:hidden">Mobile (Default)</span>
      <span class="text-primary font-bold hidden md:block">Desktop (MD+)</span>
    </div>

  </div>
</template>