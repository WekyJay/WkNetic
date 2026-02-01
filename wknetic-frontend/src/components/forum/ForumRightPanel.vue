<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import ExtensionSlot from '@/components/ExtensionSlot.vue'
import { listPosts } from '@/api/post'
import { listAllTopics } from '@/api/topic'
import type { PostVO } from '@/api/post'

const { t } = useI18n()

// 日历相关
const currentDate = ref(new Date())
const selectedDate = ref<number | null>(null)

const monthNames = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'
]

const weekDays = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa']

const currentMonth = computed(() => monthNames[currentDate.value.getMonth()])
const currentYear = computed(() => currentDate.value.getFullYear())

const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  
  const firstDay = new Date(year, month, 1).getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  
  const days: (number | null)[] = []
  
  // 填充空白
  for (let i = 0; i < firstDay; i++) {
    days.push(null)
  }
  
  // 填充日期
  for (let i = 1; i <= daysInMonth; i++) {
    days.push(i)
  }
  
  return days
})

const isToday = (day: number | null) => {
  if (!day) return false
  const now = new Date()
  return day === now.getDate() && 
         currentDate.value.getMonth() === now.getMonth() && 
         currentDate.value.getFullYear() === now.getFullYear()
}

// 签到活动日（从本地存储或API获取）
const checkedDays = ref<number[]>([])
const isChecked = (day: number | null) => day && checkedDays.value.includes(day)

const prevMonth = () => {
  currentDate.value = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth() - 1, 1)
}

const nextMonth = () => {
  currentDate.value = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth() + 1, 1)
}

// 签到相关
const isCheckedIn = ref(false)
const streak = ref(0)
const totalCheckIns = ref(0)
const latestPosts = ref<PostVO[]>([])
const forumStats = ref<any[]>([])

const checkIn = () => {
  isCheckedIn.value = true
  streak.value++
  totalCheckIns.value++
  // TODO: 调用API保存签到记录
}

const loadForumData = async () => {
  try {
    // 获取最新帖子
    const postsResponse = await listPosts({ page: 1, size: 5 })
    latestPosts.value = postsResponse.data.records || []
    
    // 获取论坛统计
    const topicsResponse = await listAllTopics()
    const topics = topicsResponse.data || []
    
    let totalPosts = 0
    topics.forEach(topic => {
      totalPosts += topic.postCount || 0
    })
    
    forumStats.value = [
      { label: t('forum.totalPosts'), value: totalPosts.toLocaleString(), icon: 'i-tabler-message-2' },
      { label: t('forum.members'), value: '45,231', icon: 'i-tabler-users' },
      { label: t('forum.onlineNow'), value: '1,234', icon: 'i-tabler-wifi', highlight: true },
      { label: t('forum.todayPosts'), value: '328', icon: 'i-tabler-calendar-event' },
    ]
  } catch (e) {
    console.error('Error loading forum data:', e)
    forumStats.value = [
      { label: t('forum.totalPosts'), value: '--', icon: 'i-tabler-message-2' },
      { label: t('forum.members'), value: '--', icon: 'i-tabler-users' },
      { label: t('forum.onlineNow'), value: '--', icon: 'i-tabler-wifi', highlight: true },
      { label: t('forum.todayPosts'), value: '--', icon: 'i-tabler-calendar-event' },
    ]
  }
}

onMounted(() => {
  loadForumData()
})
</script>

<template>
  <ExtensionSlot 
      name="user-profile-header" 
      :context="{ userId: 1001, name: 'WekyJay' }" 
    />
  <div class="space-y-4">
    <!-- 签到卡片 -->
    <div class="card">
      <div class="flex-between mb-4">
        <h3 class="text-sm font-semibold text-text-secondary uppercase tracking-wide">
          {{ t('forum.daily') }}
        </h3>
        <span class="i-tabler-flame text-orange-400" />
      </div>

      <div v-if="!isCheckedIn" class="text-center py-4">
        <div class="w-16 h-16 mx-auto mb-3 rounded-full bg-gradient-to-br from-brand/20 to-brand/5 flex-center">
          <span class="i-tabler-calendar-check text-3xl text-brand" />
        </div>
        <p class="text-text-secondary text-sm mb-4">
          You haven't checked in today!
        </p>
        <button class="btn-primary w-full" @click="checkIn">
          <span class="i-tabler-check" />
          Check In Now
        </button>
      </div>

      <div v-else class="text-center py-4">
        <div class="w-16 h-16 mx-auto mb-3 rounded-full bg-brand/20 flex-center animate-pulse">
          <span class="i-tabler-circle-check text-3xl text-brand" />
        </div>
        <p class="text-brand font-semibold mb-1">Checked In!</p>
        <p class="text-text-muted text-sm">See you tomorrow</p>
      </div>

      <!-- 签到统计 -->
      <div class="grid grid-cols-2 gap-3 mt-4 pt-4 border-t border-border">
        <div class="text-center">
          <div class="text-2xl font-bold text-brand">{{ streak }}</div>
          <div class="text-xs text-text-muted">Day Streak</div>
        </div>
        <div class="text-center">
          <div class="text-2xl font-bold text-text">{{ totalCheckIns }}</div>
          <div class="text-xs text-text-muted">Total Check-ins</div>
        </div>
      </div>
    </div>

    <!-- 日历 -->
    <div class="card">
      <div class="flex-between mb-4">
        <h3 class="text-sm font-semibold text-text-secondary uppercase tracking-wide">
          Activity Calendar
        </h3>
        <div class="flex items-center gap-1">
          <button 
            class="p-1 rounded hover:bg-bg-hover text-text-muted hover:text-text transition-colors"
            @click="prevMonth"
          >
            <span class="i-tabler-chevron-left" />
          </button>
          <button 
            class="p-1 rounded hover:bg-bg-hover text-text-muted hover:text-text transition-colors"
            @click="nextMonth"
          >
            <span class="i-tabler-chevron-right" />
          </button>
        </div>
      </div>

      <div class="text-center mb-3">
        <span class="font-semibold text-text">{{ currentMonth }} {{ currentYear }}</span>
      </div>

      <!-- 星期标题 -->
      <div class="grid grid-cols-7 gap-1 mb-2">
        <div 
          v-for="day in weekDays" 
          :key="day" 
          class="text-center text-xs text-text-muted py-1"
        >
          {{ day }}
        </div>
      </div>

      <!-- 日期网格 -->
      <div class="grid grid-cols-7 gap-1">
        <div
          v-for="(day, index) in calendarDays"
          :key="index"
          class="aspect-square flex-center rounded-lg text-sm cursor-pointer transition-all duration-200"
          :class="[
            day ? 'hover:bg-bg-hover' : '',
            isToday(day) ? 'bg-brand text-bg font-bold' : '',
            isChecked(day) && !isToday(day) ? 'bg-brand/20 text-text' : '',
            !day ? 'invisible' : '',
            selectedDate === day ? 'ring-2 ring-brand' : ''
          ]"
          @click="day && (selectedDate = day)"
        >
          {{ day }}
        </div>
      </div>

      <!-- 图例 -->
      <div class="flex items-center justify-center gap-4 mt-4 pt-3 border-t border-border">
        <div class="flex items-center gap-1.5">
          <div class="w-3 h-3 rounded bg-brand" />
          <span class="text-xs text-text-muted">Today</span>
        </div>
        <div class="flex items-center gap-1.5">
          <div class="w-3 h-3 rounded bg-brand/20" />
          <span class="text-xs text-text-muted">Active</span>
        </div>
      </div>
    </div>

    <!-- 论坛统计 -->
    <div class="card">
      <h3 class="text-sm font-semibold text-text-secondary uppercase tracking-wide mb-4">
        Forum Statistics
      </h3>
      <div class="space-y-3">
        <div 
          v-for="stat in forumStats" 
          :key="stat.label"
          class="flex items-center justify-between py-2 border-b border-border/50 last:border-0"
        >
          <div class="flex items-center gap-2">
            <span :class="[stat.icon, stat.highlight ? 'text-brand' : 'text-text-muted']" />
            <span class="text-sm text-text-secondary">{{ stat.label }}</span>
          </div>
          <span 
            class="font-semibold"
            :class="stat.highlight ? 'text-brand' : 'text-text'"
          >
            {{ stat.value }}
          </span>
        </div>
      </div>
    </div>

    <!-- 最新帖子 -->
    <div class="card">
      <h3 class="text-sm font-semibold text-text-secondary uppercase tracking-wide mb-4">
        Latest Posts
      </h3>
      <div v-if="latestPosts.length === 0" class="text-sm text-text-muted text-center py-4">
        No posts yet
      </div>
      <div v-else class="space-y-3">
        <div
          v-for="(post, index) in latestPosts"
          :key="post.id"
          class="flex items-start gap-2"
        >
          <div class="text-lg font-bold text-text-muted">
            #{{ index + 1 }}
          </div>
          <div class="flex-1 min-w-0">
            <div class="text-sm font-medium text-text truncate hover:text-brand transition-colors cursor-pointer">
              {{ post.title }}
            </div>
            <div class="text-xs text-text-muted mt-1">
              by {{ post.author?.username || 'Anonymous' }}
            </div>
            <div class="text-xs text-text-muted">
              {{ post.createTime }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
