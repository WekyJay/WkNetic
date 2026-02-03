<template>
  <div class="p-5">
    <div class="flex justify-between items-center mb-8">
      <h2 class="text-2xl font-semibold text-gray-900 dark:text-gray-100">服务器监控</h2>
      <div class="flex items-center gap-4">
        <div class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
          <span :class="[
            'w-2.5 h-2.5 rounded-full',
            isConnected 
              ? 'bg-green-500 shadow-[0_0_8px_rgba(34,197,94,0.5)]' 
              : 'bg-gray-400 dark:bg-gray-600'
          ]" />
          {{ isConnected ? '已连接' : '未连接' }}
        </div>
        <div class="text-sm text-gray-600 dark:text-gray-400 px-3 py-1 bg-gray-100 dark:bg-gray-800 rounded-2">
          连接计数: <span class="font-semibold text-brand">{{ connectionRefCount }}</span>
          上次连接：<span class="font-semibold text-brand">
            {{ lastMessageTime ? new Date(lastMessageTime).toLocaleTimeString() : '无消息' }}
          </span>
        </div>
      </div>
    </div>

    <div v-if="onlineServers.length === 0" class="flex flex-col items-center justify-center min-h-100">
      <i class="i-carbon-server text-6xl text-gray-300 dark:text-gray-700" />
      <p class="text-gray-500 dark:text-gray-400 mt-4">暂无在线服务器</p>
    </div>

    <div v-else class="grid grid-cols-[repeat(auto-fill,minmax(350px,1fr))] gap-5">
      <div
        v-for="server in onlineServers"
        :key="server.sessionId"
        class="bg-bg-raised rounded-3 cursor-pointer transition-all duration-200 card hover:(-translate-y-1 shadow-lg)"
        @click="goToDetail(server.sessionId)"
      >
        <div class="flex justify-between items-center mb-2">
          <h3 class="text-lg font-semibold m-0 text-gray-900 dark:text-gray-100">
            {{ server.serverName || '未命名服务器' }}
          </h3>
          <span class="px-3 py-1 rounded-3 text-xs font-medium bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-400">
            在线
          </span>
        </div>
         <h2 class="text-sm mb-4 text-gray-600 dark:text-gray-400">
            {{ server.motd || '暂无MOTD' }}
          </h2>

        <div class="flex flex-col gap-3 mb-4">
          <div class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
            <i class="i-carbon-user text-lg" />
            <span>{{ server.onlinePlayers }} / {{ server.maxPlayers }}</span>
          </div>

          <div class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
            <i class="i-carbon-dashboard text-lg" />
            <span>TPS: {{ server.tps?.toFixed(2) || 'N/A' }}</span>
          </div>

          <div class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
            <i class="i-carbon-chip text-lg" />
            <span>
              {{ formatMemory(server.ramUsage) }} / {{ formatMemory(server.maxRam) }}
            </span>
          </div>
        </div>

        <div class="mb-3">
          <div class="w-full h-2 bg-gray-100 dark:bg-gray-700 rounded-1 overflow-hidden mb-1">
            <div
              class="h-full transition-all duration-300"
              :style="{
                width: `${(server.ramUsage / server.maxRam) * 100}%`,
                background: getMemoryColor(server.ramUsage / server.maxRam)
              }"
            />
          </div>
          <span class="text-xs text-gray-400 dark:text-gray-500">内存占用率 {{ ((server.ramUsage / server.maxRam) * 100).toFixed(2) }}%</span>
        </div>

        <div class="flex justify-between items-center mt-4 pt-4 border-t border-gray-100 dark:border-gray-700">
          <span class="text-sm text-gray-500 dark:text-gray-400">
            插件: {{ server.pluginList?.length || 0 }}
          </span>
          <i class="i-carbon-arrow-right text-gray-400 dark:text-gray-600" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useServerMonitor } from '@/composables/useServerMonitor'
import { get } from '@vueuse/core'

const router = useRouter()
const { servers, isConnected, connectionRefCount, lastMessageTime, getOnlineServers } = useServerMonitor()



const onlineServers = computed(() => getOnlineServers())


onMounted(() => {
    console.log('在线服务器数量:', onlineServers.value.length)
})

const formatMemory = (mb: number) => {
  if (mb >= 1024) {
    return `${(mb / 1024).toFixed(1)} GB`
  }
  return `${mb} MB`
}

const getPlayerColor = (ratio: number) => {
  if (ratio >= 0.8) return '#f44336'
  if (ratio >= 0.5) return '#ff9800'
  return '#4caf50'
}

const getMemoryColor = (ratio: number) => {
  if (ratio >= 0.9) return '#f44336'
  if (ratio >= 0.7) return '#ff9800'
  return '#4caf50'
}

const goToDetail = (sessionId: string) => {
  router.push(`/admin/server-monitor/${sessionId}`)
}
</script>
