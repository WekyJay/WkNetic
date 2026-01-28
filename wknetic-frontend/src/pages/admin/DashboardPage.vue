<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import AdminLayout from '@/components/layout/admin/AdminLayout.vue'

const { t } = useI18n()

// 统计卡片数据
const getStats = () => [
  { 
    label: t('pages.total_projects'),
    value: '12,847',
    change: '+12.5%',
    trend: 'up',
    icon: 'i-tabler-folders',
    color: 'brand'
  },
  { 
    label: t('pages.active_users'),
    value: '845.2K',
    change: '+8.2%',
    trend: 'up',
    icon: 'i-tabler-users',
    color: 'blue'
  },
  { 
    label: t('pages.downloads_today'),
    value: '2.4M',
    change: '+23.1%',
    trend: 'up',
    icon: 'i-tabler-download',
    color: 'purple'
  },
  { 
    label: t('pages.pending_reviews'),
    value: '156',
    change: '-5.4%',
    trend: 'down',
    icon: 'i-tabler-clock',
    color: 'orange'
  },
]

const stats = ref(getStats())

// 最近活动数据
const recentActivities = ref([
  { 
    type: 'project',
    icon: 'i-tabler-package',
    title: 'New mod submitted',
    description: 'Sodium Extra v0.5.4 submitted for review',
    time: '5 min ago',
    user: 'FlashyReese'
  },
  { 
    type: 'user',
    icon: 'i-tabler-user-plus',
    title: 'New user registered',
    description: 'minecraft_lover_2024 joined WkNetic',
    time: '12 min ago',
    user: null
  },
  { 
    type: 'report',
    icon: 'i-tabler-flag',
    title: 'Content reported',
    description: 'Project "FakeMod" flagged for malware',
    time: '28 min ago',
    user: 'ModReviewer'
  },
  { 
    type: 'update',
    icon: 'i-tabler-refresh',
    title: 'Project updated',
    description: 'Fabric API released version 0.92.0',
    time: '1 hour ago',
    user: 'modmuss50'
  },
  { 
    type: 'verified',
    icon: 'i-tabler-circle-check',
    title: 'Project verified',
    description: 'Create mod verified and published',
    time: '2 hours ago',
    user: 'simibubi'
  },
])

// 热门项目数据
const topProjects = ref([
  { name: 'Sodium', downloads: '45.2M', category: 'Optimization', growth: '+15%' },
  { name: 'Fabric API', downloads: '38.7M', category: 'Library', growth: '+12%' },
  { name: 'Iris Shaders', downloads: '28.4M', category: 'Shaders', growth: '+18%' },
  { name: 'Lithium', downloads: '22.1M', category: 'Optimization', growth: '+8%' },
  { name: 'Create', downloads: '19.8M', category: 'Technology', growth: '+22%' },
])

// 图表数据（简化展示）
const chartData = ref({
  labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
  downloads: [2.1, 2.4, 2.2, 2.8, 3.1, 2.9, 2.4],
  users: [12, 15, 13, 18, 22, 19, 14],
})
</script>

<template>
    <div class="space-y-6">
      <!-- Page header -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold text-text">{{ $t('dashboard') }}</h1>
          <p class="text-text-muted mt-1">{{ $t('welcome') }}</p>
        </div>
        <div class="flex items-center gap-2">
          <button class="btn-secondary text-sm">
            <span class="i-tabler-download"></span>
            {{ t('pages.export') }}
          </button>
          <button class="btn-primary text-sm">
            <span class="i-tabler-plus"></span>
            {{ t('pages.new_project') }}
          </button>
        </div>
      </div>

      <!-- Stats cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div 
          v-for="stat in stats"
          :key="stat.label"
          class="bg-bg rounded-xl border border-border p-5 hover:border-brand/30 transition-colors"
        >
          <div class="flex items-start justify-between">
            <div 
              :class="[
                'w-11 h-11 rounded-xl flex-center',
                stat.color === 'brand' ? 'bg-brand/15 text-brand' :
                stat.color === 'blue' ? 'bg-blue-500/15 text-blue-500' :
                stat.color === 'purple' ? 'bg-purple-500/15 text-purple-500' :
                'bg-orange-500/15 text-orange-500'
              ]"
            >
              <span :class="stat.icon" class="text-xl"></span>
            </div>
            <span 
              :class="[
                'text-xs font-medium px-2 py-1 rounded-full',
                stat.trend === 'up' 
                  ? 'text-brand text-green-600/90 bg-green-600/15' 
                  : 'text-danger text-red-600/90 bg-red-600/15'
              ]"
            >
              {{ stat.change }}
            </span>
          </div>
          <div class="mt-4">
            <p class="text-2xl font-bold text-text">{{ stat.value }}</p>
            <p class="text-sm text-text-muted mt-1">{{ stat.label }}</p>
          </div>
        </div>
      </div>

      <!-- Charts and Activity row -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Downloads chart -->
        <div class="lg:col-span-2 bg-bg rounded-xl border border-border p-5">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-lg font-semibold text-text">Downloads Overview</h2>
            <div class="flex items-center gap-2">
              <button class="px-3 py-1.5 text-sm rounded-lg bg-brand/15 text-brand">7 Days</button>
              <button class="px-3 py-1.5 text-sm rounded-lg text-text-muted hover:bg-bg-surface">30 Days</button>
              <button class="px-3 py-1.5 text-sm rounded-lg text-text-muted hover:bg-bg-surface">90 Days</button>
            </div>
          </div>
          <!-- Simple bar chart visualization -->
          <div class="h-64 flex items-end justify-between gap-4 px-4">
            <div 
              v-for="(value, index) in chartData.downloads"
              :key="index"
              class="flex-1 flex flex-col items-center gap-2"
            >
              <div 
                class="w-full bg-brand/80 rounded-t-lg transition-all hover:bg-brand"
                :style="{ height: `${(value / 3.5) * 100}%` }"
              ></div>
              <span class="text-xs text-text-muted">{{ chartData.labels[index] }}</span>
            </div>
          </div>
          <div class="mt-4 pt-4 border-t border-border flex items-center justify-between text-sm">
            <span class="text-text-muted">Total this week: <span class="text-text font-medium">17.9M downloads</span></span>
            <span class="text-brand">+15.3% vs last week</span>
          </div>
        </div>

        <!-- Recent activity -->
        <div class="bg-bg rounded-xl border border-border p-5">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-text">Recent Activity</h2>
            <button class="text-sm text-brand hover:underline">View all</button>
          </div>
          <div class="space-y-4">
            <div 
              v-for="activity in recentActivities"
              :key="activity.title"
              class="flex gap-3"
            >
              <div 
                :class="[
                  'w-9 h-9 rounded-lg flex-center flex-shrink-0',
                  activity.type === 'project' ? 'bg-brand/15 text-brand' :
                  activity.type === 'user' ? 'bg-blue-500/15 text-blue-500' :
                  activity.type === 'report' ? 'bg-danger/15 text-danger' :
                  activity.type === 'verified' ? 'bg-brand/15 text-brand' :
                  'bg-purple-500/15 text-purple-500'
                ]"
              >
                <span :class="activity.icon" class="text-lg"></span>
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-text truncate">{{ activity.title }}</p>
                <p class="text-xs text-text-muted truncate">{{ activity.description }}</p>
                <p class="text-xs text-text-muted mt-1">{{ activity.time }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Bottom row -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Top projects table -->
        <div class="bg-bg rounded-xl border border-border p-5">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-text">Top Projects</h2>
            <button class="text-sm text-brand hover:underline">View all</button>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead>
                <tr class="text-left text-sm text-text-muted border-b border-border">
                  <th class="pb-3 font-medium">Project</th>
                  <th class="pb-3 font-medium">Category</th>
                  <th class="pb-3 font-medium text-right">Downloads</th>
                  <th class="pb-3 font-medium text-right">Growth</th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="project in topProjects"
                  :key="project.name"
                  class="border-b border-border last:border-0 hover:bg-bg-surface/50"
                >
                  <td class="py-3">
                    <div class="flex items-center gap-3">
                      <div class="w-8 h-8 rounded-lg bg-bg-surface flex-center">
                        <span class="i-tabler-package text-text-muted"></span>
                      </div>
                      <span class="font-medium text-text">{{ project.name }}</span>
                    </div>
                  </td>
                  <td class="py-3">
                    <span class="px-2 py-1 text-xs rounded-full bg-bg-surface text-text-muted">
                      {{ project.category }}
                    </span>
                  </td>
                  <td class="py-3 text-right text-text">{{ project.downloads }}</td>
                  <td class="py-3 text-right text-brand">{{ project.growth }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Quick actions -->
        <div class="bg-bg rounded-xl border border-border p-5">
          <h2 class="text-lg font-semibold text-text mb-4">Quick Actions</h2>
          <div class="grid grid-cols-2 gap-3">
            <button class="p-4 rounded-xl bg-bg-surface border border-border hover:border-brand/30 transition-colors text-left group">
              <div class="w-10 h-10 rounded-lg bg-brand/15 text-brand flex-center mb-3 group-hover:bg-brand group-hover:text-bg transition-colors">
                <span class="i-tabler-shield-check text-xl"></span>
              </div>
              <p class="font-medium text-text">Review Queue</p>
              <p class="text-sm text-text-muted mt-1">23 pending</p>
            </button>
            <button class="p-4 rounded-xl bg-bg-surface border border-border hover:border-brand/30 transition-colors text-left group">
              <div class="w-10 h-10 rounded-lg bg-danger/15 text-danger flex-center mb-3 group-hover:bg-danger group-hover:text-white transition-colors">
                <span class="i-tabler-flag text-xl"></span>
              </div>
              <p class="font-medium text-text">Reports</p>
              <p class="text-sm text-text-muted mt-1">5 new</p>
            </button>
            <button class="p-4 rounded-xl bg-bg-surface border border-border hover:border-brand/30 transition-colors text-left group">
              <div class="w-10 h-10 rounded-lg bg-blue-500/15 text-blue-500 flex-center mb-3 group-hover:bg-blue-500 group-hover:text-white transition-colors">
                <span class="i-tabler-users text-xl"></span>
              </div>
              <p class="font-medium text-text">User Management</p>
              <p class="text-sm text-text-muted mt-1">845K users</p>
            </button>
            <button class="p-4 rounded-xl bg-bg-surface border border-border hover:border-brand/30 transition-colors text-left group">
              <div class="w-10 h-10 rounded-lg bg-purple-500/15 text-purple-500 flex-center mb-3 group-hover:bg-purple-500 group-hover:text-white transition-colors">
                <span class="i-tabler-settings text-xl"></span>
              </div>
              <p class="font-medium text-text">Settings</p>
              <p class="text-sm text-text-muted mt-1">Configure</p>
            </button>
          </div>
        </div>
      </div>
    </div>
</template>
