<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { WkCard, WkButton, WkBadge } from '@/components/common'

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
          <h1 class="text-2xl font-bold text-[var(--text-default)]">{{ $t('dashboard') }}</h1>
          <p class="text-[var(--text-secondary)] mt-1">{{ $t('welcome') }}</p>
        </div>
        <div class="flex items-center gap-2">
          <WkButton variant="secondary" size="sm" icon="i-tabler-download">
            {{ t('pages.export') }}
          </WkButton>
          <WkButton variant="primary" size="sm" icon="i-tabler-plus">
            {{ t('pages.new_project') }}
          </WkButton>
        </div>
      </div>

      <!-- Stats cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <WkCard 
          v-for="stat in stats"
          :key="stat.label"
          :hoverable="true"
        >
          <div class="flex items-start justify-between">
            <div 
              :class="[
                'w-11 h-11 rounded-xl flex items-center justify-center',
                stat.color === 'brand' ? 'bg-[var(--brand-default)]/15 text-[var(--brand-default)]' :
                stat.color === 'blue' ? 'bg-blue-500/15 text-blue-500' :
                stat.color === 'purple' ? 'bg-purple-500/15 text-purple-500' :
                'bg-orange-500/15 text-orange-500'
              ]"
            >
              <span :class="stat.icon" class="text-xl"></span>
            </div>
            <WkBadge 
              :variant="stat.trend === 'up' ? 'success' : 'danger'"
              size="sm"
            >
              {{ stat.change }}
            </WkBadge>
          </div>
          <div class="mt-4">
            <p class="text-2xl font-bold text-[var(--text-default)]">{{ stat.value }}</p>
            <p class="text-sm text-[var(--text-secondary)] mt-1">{{ stat.label }}</p>
          </div>
        </WkCard>
      </div>

      <!-- Charts and Activity row -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Downloads chart -->
        <WkCard class="lg:col-span-2">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-lg font-semibold text-[var(--text-default)]">Downloads Overview</h2>
            <div class="flex items-center gap-2">
              <WkButton variant="primary" size="sm">7 Days</WkButton>
              <WkButton variant="ghost" size="sm">30 Days</WkButton>
              <WkButton variant="ghost" size="sm">90 Days</WkButton>
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
                class="w-full bg-[var(--brand-default)]/80 rounded-t-lg transition-all hover:bg-[var(--brand-default)]"
                :style="{ height: `${(value / 3.5) * 100}%` }"
              ></div>
              <span class="text-xs text-[var(--text-muted)]">{{ chartData.labels[index] }}</span>
            </div>
          </div>
          <div class="mt-4 pt-4 border-t border-[var(--border-default)] flex items-center justify-between text-sm">
            <span class="text-[var(--text-secondary)]">Total this week: <span class="text-[var(--text-default)] font-medium">17.9M downloads</span></span>
            <WkBadge variant="success">+15.3% vs last week</WkBadge>
          </div>
        </WkCard>

        <!-- Recent activity -->
        <WkCard>
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-[var(--text-default)]">Recent Activity</h2>
            <WkButton variant="text" size="sm">View all</WkButton>
          </div>
          <div class="space-y-4">
            <div 
              v-for="activity in recentActivities"
              :key="activity.title"
              class="flex gap-3"
            >
              <div 
                :class="[
                  'w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0',
                  activity.type === 'project' ? 'bg-[var(--brand-default)]/15 text-[var(--brand-default)]' :
                  activity.type === 'user' ? 'bg-blue-500/15 text-blue-500' :
                  activity.type === 'report' ? 'bg-red-500/15 text-red-500' :
                  activity.type === 'verified' ? 'bg-[var(--brand-default)]/15 text-[var(--brand-default)]' :
                  'bg-purple-500/15 text-purple-500'
                ]"
              >
                <span :class="activity.icon" class="text-lg"></span>
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-[var(--text-default)] truncate">{{ activity.title }}</p>
                <p class="text-xs text-[var(--text-secondary)] truncate">{{ activity.description }}</p>
                <p class="text-xs text-[var(--text-muted)] mt-1">{{ activity.time }}</p>
              </div>
            </div>
          </div>
        </WkCard>
      </div>

      <!-- Bottom row -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Top projects table -->
        <WkCard>
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-[var(--text-default)]">Top Projects</h2>
            <WkButton variant="text" size="sm">View all</WkButton>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead>
                <tr class="text-left text-sm text-[var(--text-secondary)] border-b border-[var(--border-default)]">
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
                  class="border-b border-[var(--border-default)] last:border-0 hover:bg-[var(--bg-surface)]/50"
                >
                  <td class="py-3">
                    <div class="flex items-center gap-3">
                      <div class="w-8 h-8 rounded-lg bg-[var(--bg-surface)] flex items-center justify-center">
                        <span class="i-tabler-package text-[var(--text-muted)]"></span>
                      </div>
                      <span class="font-medium text-[var(--text-default)]">{{ project.name }}</span>
                    </div>
                  </td>
                  <td class="py-3">
                    <WkBadge variant="default" size="sm">
                      {{ project.category }}
                    </WkBadge>
                  </td>
                  <td class="py-3 text-right text-[var(--text-default)]">{{ project.downloads }}</td>
                  <td class="py-3 text-right">
                    <WkBadge variant="success" size="sm">{{ project.growth }}</WkBadge>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </WkCard>

        <!-- Quick actions -->
        <WkCard>
          <h2 class="text-lg font-semibold text-[var(--text-default)] mb-4">Quick Actions</h2>
          <div class="grid grid-cols-2 gap-3">
            <WkCard 
              :hoverable="true"
              :padding="'md'"
              class="cursor-pointer"
            >
              <div class="w-10 h-10 rounded-lg bg-[var(--brand-default)]/15 text-[var(--brand-default)] flex items-center justify-center mb-3">
                <span class="i-tabler-shield-check text-xl"></span>
              </div>
              <p class="font-medium text-[var(--text-default)]">Review Queue</p>
              <div class="flex items-center gap-2 mt-1">
                <p class="text-sm text-[var(--text-secondary)]">23 pending</p>
              </div>
            </WkCard>
            
            <WkCard 
              :hoverable="true"
              :padding="'md'"
              class="cursor-pointer"
            >
              <div class="w-10 h-10 rounded-lg bg-red-500/15 text-red-500 flex items-center justify-center mb-3">
                <span class="i-tabler-flag text-xl"></span>
              </div>
              <p class="font-medium text-[var(--text-default)]">Reports</p>
              <div class="flex items-center gap-2 mt-1">
                <p class="text-sm text-[var(--text-secondary)]">5 new</p>
              </div>
            </WkCard>
            
            <WkCard 
              :hoverable="true"
              :padding="'md'"
              class="cursor-pointer"
            >
              <div class="w-10 h-10 rounded-lg bg-blue-500/15 text-blue-500 flex items-center justify-center mb-3">
                <span class="i-tabler-users text-xl"></span>
              </div>
              <p class="font-medium text-[var(--text-default)]">User Management</p>
              <div class="flex items-center gap-2 mt-1">
                <p class="text-sm text-[var(--text-secondary)]">845K users</p>
              </div>
            </WkCard>
            
            <WkCard 
              :hoverable="true"
              :padding="'md'"
              class="cursor-pointer"
            >
              <div class="w-10 h-10 rounded-lg bg-purple-500/15 text-purple-500 flex items-center justify-center mb-3">
                <span class="i-tabler-settings text-xl"></span>
              </div>
              <p class="font-medium text-[var(--text-default)]">Settings</p>
              <div class="flex items-center gap-2 mt-1">
                <p class="text-sm text-[var(--text-secondary)]">Configure</p>
              </div>
            </WkCard>
          </div>
        </WkCard>
      </div>
    </div>
</template>
