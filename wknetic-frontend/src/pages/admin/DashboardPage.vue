<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { WkCard, WkButton, WkBadge } from '@/components/common'
import { VueUiXy, type VueUiXyDatasetItem, type VueUiXyConfig } from 'vue-data-ui'
import 'vue-data-ui/style.css'
import { 
  getDashboardStatistics, 
  getPostTrend, 
  getRecentActivities, 
  getUserQuickActions,
  getAvailableQuickActions,
  saveUserQuickActions,
  type DashboardStatistics,
  type PostTrendItem,
  type RecentActivity,
  type QuickAction,
  type AvailableQuickActionOption
} from '@/api/dashboard'

const { t } = useI18n()

// Loading states
const loading = ref(true)
const trendLoading = ref(false)
const activityLoading = ref(false)

// Data refs
const statistics = ref<DashboardStatistics | null>(null)
const postTrends = ref<PostTrendItem[]>([])
const recentActivities = ref<RecentActivity[]>([])
const quickActions = ref<QuickAction[]>([])
const availableQuickActions = ref<AvailableQuickActionOption[]>([])

// Trend days filter
const trendDays = ref(7)

// Stats cards computed
const statsCards = computed(() => {
  if (!statistics.value) return []
  
  return [
    { 
      label: t('pages.total_users'),
      value: statistics.value.totalUserCount.toLocaleString(),
      change: `${statistics.value.totalUserChangeRate > 0 ? '+' : ''}${statistics.value.totalUserChangeRate.toFixed(2)}%`,
      trend: statistics.value.totalUserChangeRate >= 0 ? 'up' : 'down',
      icon: 'i-tabler-users',
      color: 'brand'
    },
    { 
      label: t('pages.online_users'),
      value: statistics.value.onlineUserCount.toLocaleString(),
      change: t('pages.now'),
      trend: 'stable',
      icon: 'i-tabler-user-check',
      color: 'blue'
    },
    { 
      label: t('pages.total_posts'),
      value: statistics.value.totalPostCount.toLocaleString(),
      change: `${statistics.value.totalPostChangeRate > 0 ? '+' : ''}${statistics.value.totalPostChangeRate.toFixed(2)}%`,
      trend: statistics.value.totalPostChangeRate >= 0 ? 'up' : 'down',
      icon: 'i-tabler-file-text',
      color: 'purple'
    },
    { 
      label: t('pages.pending_audit'),
      value: statistics.value.pendingAuditCount.toLocaleString(),
      change: `${statistics.value.pendingAuditChangeRate > 0 ? '+' : ''}${statistics.value.pendingAuditChangeRate.toFixed(2)}%`,
      trend: statistics.value.pendingAuditChangeRate <= 0 ? 'up' : 'down',
      icon: 'i-tabler-clock',
      color: 'orange'
    },
  ]
})

const selectedXIndex = ref<number | undefined>(undefined)

// Chart labels
const chartLabels = computed(() => {
  if (!postTrends.value || !Array.isArray(postTrends.value) || postTrends.value.length === 0) return []
  // 遍历日期
  const validTrends = postTrends.value.filter(item => item && item.date)
  if (validTrends.length === 0) return []
  return validTrends.map(item => {
    try {
      const date = new Date(item.date)
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
    } catch {
      return ''
    }
  })
})

const brandColor = getComputedStyle(document.documentElement)
    .getPropertyValue('--brand-default')
    .trim();

console.log('Brand color:', brandColor)

// Chart dataset for vue-data-ui
const chartDataset = computed<VueUiXyDatasetItem[]>(() => {
  if (!postTrends.value || !Array.isArray(postTrends.value) || postTrends.value.length === 0) return []
  const validTrends = postTrends.value.filter(item => item && item.date && item.postCount != null)
  if (validTrends.length === 0) return []
  return [
    {
      name: t('dashboard.posts'),
      series: validTrends.map(item => Number(item.postCount) || 0),
      type: 'bar',
      color: brandColor,
      // color: 'var(--brand-default)'
    }
  ]
})



// Chart configuration for vue-data-ui VueUiXy
const chartConfig = computed(():VueUiXyConfig => ({
  theme: "dark",
  chart: {
    backgroundColor: 'transparent',
    color: '#CCCCCC',
    userOptions: {
      show: false
    },
    height: 500,
    grid: {
      stroke: '#3A3A3A',
      showVerticalLines: false,
      labels: {
        color: '#888888',
        show: true,
        fontSize: 16,
        yAxis: {
          showBaseline: true
        },
        xAxis: {
          showBaseline: true
        },
        xAxisLabels:{
          color: '#cccccc',
          values: chartLabels.value
        }
      }
    },
    bar: {
      borderRadius: 16,
      useGradient: true,
      labels: {
        show: true,
        color: '#cccccc',
        offsetY: -6,
      }
    },
    title: {
      show: true
    },
    legend: {
      show: true
    },
    tooltip: {
      show: true,
      backgroundColor: '#1F1F1F',
      color: '#FFFFFF',
      fontSize: 12,
      borderRadius: 6,
      borderColor: '#3A3A3A',
      borderWidth: 1
    },
    zoom: {
      show: false
    },
    padding: {
      top: 24,
      right: 24,
      bottom: 48,
      left: 48
    }
  }
}))

// Fetch all dashboard data
const loadDashboardData = async () => {
  try {
    loading.value = true
    
    // Fetch statistics
    const statsResponse = await getDashboardStatistics()
    statistics.value = statsResponse.data || null
    
    // Fetch post trends
    await loadPostTrends()
    
    // Fetch recent activities
    await loadRecentActivities()
    
    // Fetch user quick actions
    await loadQuickActions()
    
    // Fetch available quick actions options
    try {
      const availableResponse = await getAvailableQuickActions()
      availableQuickActions.value = Array.isArray(availableResponse.data) ? availableResponse.data : []
    } catch (e) {
      console.error('Failed to load available quick actions:', e)
      availableQuickActions.value = []
    }
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
    // Reset all data to safe defaults
    statistics.value = null
    postTrends.value = []
    recentActivities.value = []
    quickActions.value = []
    availableQuickActions.value = []
  } finally {
    loading.value = false
  }
}

// Load post trends
const loadPostTrends = async () => {
  try {
    trendLoading.value = true
    const response = await getPostTrend(trendDays.value)
    postTrends.value = response.data ?? []

  } catch (error) {
    console.error('Failed to load post trends:', error)
    postTrends.value = []
  } finally {
    trendLoading.value = false
  }
}

// Load recent activities
const loadRecentActivities = async () => {
  try {
    activityLoading.value = true
    const response = await getRecentActivities(10)
    recentActivities.value = response.data || []
  } catch (error) {
    console.error('Failed to load recent activities:', error)
    recentActivities.value = []
  } finally {
    activityLoading.value = false
  }
}

// Load quick actions
const loadQuickActions = async () => {
  try {
    const response = await getUserQuickActions()
    quickActions.value = response.data || []
  } catch (error) {
    console.error('Failed to load quick actions:', error)
    quickActions.value = []
  }
}

// Handle trend days change
const changeTrendDays = async (days: number) => {
  trendDays.value = days
  await loadPostTrends()
}

// Handle quick action click (navigate to action URL)
const handleQuickAction = (action: QuickAction) => {
  if (action.actionUrl) {
    window.location.href = action.actionUrl
  }
}

// Activity type icon mapping
const getActivityIcon = (businessType: number): string => {
  const iconMap: { [key: number]: string } = {
    1: 'i-tabler-plus', // Add
    2: 'i-tabler-pencil', // Edit
    3: 'i-tabler-trash', // Delete
    4: 'i-tabler-check', // Approve
    5: 'i-tabler-download', // Export
    6: 'i-tabler-upload', // Import
  }
  return iconMap[businessType] || 'i-tabler-help'
}

// Activity type color mapping
const getActivityColor = (businessType: number): string => {
  const colorMap: { [key: number]: string } = {
    1: 'bg-blue-500/15 text-blue-500', // Add
    2: 'bg-purple-500/15 text-purple-500', // Edit
    3: 'bg-red-500/15 text-red-500', // Delete
    4: 'bg-green-500/15 text-green-500', // Approve
    5: 'bg-yellow-500/15 text-yellow-500', // Export
    6: 'bg-indigo-500/15 text-indigo-500', // Import
  }
  return colorMap[businessType] || 'bg-gray-500/15 text-gray-500'
}

// Format time for display
const formatTime = (dateTimeStr: string): string => {
  try {
    const date = new Date(dateTimeStr)
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMs / 3600000)
    const diffDays = Math.floor(diffMs / 86400000)
    
    if (diffMins < 1) return t('dashboard.just_now')
    if (diffMins < 60) return t('dashboard.min_ago', { n: diffMins })
    if (diffHours < 24) return t('dashboard.hour_ago', { n: diffHours })
    if (diffDays < 7) return t('dashboard.day_ago', { n: diffDays })
    
    return date.toLocaleDateString()
  } catch {
    return dateTimeStr
  }
}

// Lifecycle
onMounted(() => {
  loadDashboardData()
})
</script>

<template>
    <div class="space-y-6">
      <!-- Page header -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold text-[var(--text-default)]">{{ $t('menu.dashboard') }}</h1>
          <p class="text-[var(--text-secondary)] mt-1">{{ t('welcome_back') }}</p>
        </div>
        <div class="flex items-center gap-2">
          <WkButton variant="secondary" size="sm" icon="i-tabler-refresh" @click="loadDashboardData" :disabled="loading">
            {{ t('refresh') }}
          </WkButton>
        </div>
      </div>

      <!-- Loading skeleton or Stats cards -->
      <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div v-for="i in 4" :key="i" class="h-32 bg-[var(--bg-surface)] rounded-lg animate-pulse"></div>
      </div>
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <WkCard 
          v-for="stat in statsCards"
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
              v-if="stat.trend !== 'stable'"
              :variant="stat.trend === 'up' ? 'success' : 'danger'"
              size="sm"
            >
              {{ stat.change }}
            </WkBadge>
            <WkBadge v-else variant="default" size="sm">
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
        <!-- Posts trend chart -->
        <WkCard class="lg:col-span-2">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-lg font-semibold text-[var(--text-default)]">{{ t('post_trend_overview') }}</h2>
            <div class="flex items-center gap-2">
              <WkButton 
                :variant="trendDays === 7 ? 'primary' : 'ghost'"
                size="sm"
                @click="changeTrendDays(7)"
                :disabled="trendLoading"
              >
                {{ t('dashboard.7_days') }}
              </WkButton>
              <WkButton 
                :variant="trendDays === 30 ? 'primary' : 'ghost'"
                size="sm"
                @click="changeTrendDays(30)"
                :disabled="trendLoading"
              >
                {{ t('dashboard.30_days') }}
              </WkButton>
            </div>
          </div>
          
          <!-- Chart -->
          <div v-if="trendLoading" class="h-64 bg-[var(--bg-surface)] rounded animate-pulse"></div>
          <div v-else-if="(chartDataset?.length ?? 0) > 0 && (chartLabels?.length ?? 0) > 0" class="h-80">
            <VueUiXy 
              v-if="chartDataset && chartDataset.length"
              :dataset="chartDataset || []" 
              :config="chartConfig"
              :selectedXIndex="selectedXIndex"
            />
            <div v-else>Loading charts...</div>
          </div>
          <div v-else class="h-64 flex items-center justify-center text-[var(--text-secondary)]">
            {{ t('dashboard.no_data') }}
          </div>
          
          <!-- Stats -->
          <div class="mt-4 pt-4 border-t border-[var(--border-default)] flex items-center justify-between text-sm">
            <span class="text-[var(--text-secondary)]">
              {{ t('dashboard.total_this_period') }}: 
              <span class="text-[var(--text-default)] font-medium">{{ (postTrends ?? []).reduce((sum, item) => sum + item.postCount, 0) }} {{ t('dashboard.posts') }}</span>
            </span>
          </div>
        </WkCard>

        <!-- Recent activity -->
        <WkCard>
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-[var(--text-default)]">{{ t('recent_activity') }}</h2>
            <WkButton variant="text" size="sm" @click="loadRecentActivities">{{ t('refresh') }}</WkButton>
          </div>
          <div v-if="activityLoading" class="space-y-4">
            <div v-for="i in 5" :key="i" class="h-12 bg-[var(--bg-surface)] rounded animate-pulse"></div>
          </div>
          <div v-else class="space-y-3 max-h-96 overflow-y-auto">
            <div 
              v-for="activity in recentActivities.slice(0, 8)"
              :key="activity.logId"
              class="flex gap-3 pb-3 border-b border-[var(--border-default)] last:border-0 last:pb-0"
            >
              <div 
                :class="[
                  'w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0',
                  getActivityColor(activity.businessType)
                ]"
              >
                <span :class="getActivityIcon(activity.businessType)" class="text-lg"></span>
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-[var(--text-default)] truncate">{{ activity.title }}</p>
                <div class="flex items-center gap-2 mt-1">
                  <p class="text-xs text-[var(--text-secondary)]">{{ activity.operName }}</p>
                  <span class="text-xs text-[var(--text-muted)]">•</span>
                  <p class="text-xs text-[var(--text-muted)]">{{ formatTime(activity.operTime) }}</p>
                </div>
                <WkBadge v-if="activity.status === 0" variant="danger" size="sm" class="mt-1">
                  {{ t('dashboard.failed') }}
                </WkBadge>
              </div>
            </div>
          </div>
        </WkCard>
      </div>

      <!-- Quick actions -->
      <div class="grid grid-cols-1 gap-6">
        <WkCard>
          <h2 class="text-lg font-semibold text-[var(--text-default)] mb-4">{{ t('quick_actions') }}</h2>
          
          <!-- Quick action buttons -->
          <div v-if="quickActions.length > 0" class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3 mb-6">
            <div 
              v-for="action in quickActions"
              :key="action.actionId"
              @click="handleQuickAction(action)"
              class="group cursor-pointer"
            >
              <WkCard 
                :hoverable="true"
                :padding="'md'"
                class="h-full"
              >
                <div v-if="action.icon" :class="['w-10 h-10 rounded-lg flex items-center justify-center mb-3', 'bg-[var(--brand-default)]/15 text-[var(--brand-default)]']">
                <span :class="[action.icon, 'text-xl']"></span>
                </div>
                <p class="font-medium text-[var(--text-default)] truncate">{{ action.actionName }}</p>
                <p class="text-xs text-[var(--text-secondary)] truncate mt-1">{{ action.actionUrl }}</p>
              </WkCard>
            </div>
          </div>

          <!-- All available actions -->
          <div v-if="availableQuickActions.length > 0" class="pt-4 border-t border-[var(--border-default)]">
            <p class="text-sm text-[var(--text-secondary)] mb-3">{{ t('dashboard.available_quick_actions') }}:</p>
            <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
              <div 
                v-for="action in availableQuickActions"
                :key="action.actionKey"
                class="p-3 rounded-lg border border-[var(--border-default)] hover:border-[var(--brand-default)]/50 cursor-pointer transition-colors"
              >
                <div :class="['w-8 h-8 rounded flex items-center justify-center mb-2', 'bg-[var(--brand-default)]/10 text-[var(--brand-default)]']">
                  <span :class="[action.icon, 'text-base']"></span>
                </div>
                <p class="text-xs font-medium text-[var(--text-default)] truncate">{{ action.actionName }}</p>
              </div>
            </div>
          </div>
        </WkCard>
      </div>
    </div>
</template>
