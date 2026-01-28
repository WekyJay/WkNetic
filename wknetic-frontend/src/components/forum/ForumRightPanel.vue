<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'

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

const today = new Date().getDate()
const isToday = (day: number | null) => {
  if (!day) return false
  const now = new Date()
  return day === now.getDate() && 
         currentDate.value.getMonth() === now.getMonth() && 
         currentDate.value.getFullYear() === now.getFullYear()
}

// 签到活动日（模拟数据）
const checkedDays = [1, 2, 3, 5, 8, 9, 10, 12, 15, 16, 18, 20, 22, 23, 25]
const isChecked = (day: number | null) => day && checkedDays.includes(day)

const prevMonth = () => {
  currentDate.value = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth() - 1, 1)
}

const nextMonth = () => {
  currentDate.value = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth() + 1, 1)
}

// 签到相关
const isCheckedIn = ref(false)
const streak = ref(7)
const totalCheckIns = ref(23)

const checkIn = () => {
  isCheckedIn.value = true
  streak.value++
  totalCheckIns.value++
}

// 活跃用户
const activeUsers = [
  { name: 'ShadowCrafter', avatar: 'SC', posts: 156, online: true },
  { name: 'PixelMaster', avatar: 'PM', posts: 134, online: true },
  { name: 'ModdingGuru', avatar: 'MG', posts: 98, online: false },
  { name: 'CraftKing99', avatar: 'CK', posts: 87, online: true },
  { name: 'BlockBuilder', avatar: 'BB', posts: 76, online: false },
]

// 论坛统计
const getForumStats = () => [
  { label: t('forum.totalPosts'), value: '12,847', icon: 'i-tabler-message-2' },
  { label: t('forum.members'), value: '45,231', icon: 'i-tabler-users' },
  { label: t('forum.onlineNow'), value: '1,234', icon: 'i-tabler-wifi', highlight: true },
  { label: t('forum.todayPosts'), value: '328', icon: 'i-tabler-calendar-event' },
]

const forumStats = getForumStats()
</script>

<template>
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
            isChecked(day) && !isToday(day) ? 'bg-brand/20 text-brand' : '',
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

    <!-- 活跃用户 -->
    <div class="card">
      <h3 class="text-sm font-semibold text-text-secondary uppercase tracking-wide mb-4">
        Top Contributors
      </h3>
      <div class="space-y-3">
        <div
          v-for="(user, index) in activeUsers"
          :key="user.name"
          class="flex items-center gap-3"
        >
          <div class="relative">
            <div 
              class="w-10 h-10 rounded-full flex-center text-sm font-bold"
              :class="[
                index === 0 ? 'bg-gradient-to-br from-amber-400 to-amber-600 text-white' :
                index === 1 ? 'bg-gradient-to-br from-gray-300 to-gray-500 text-white' :
                index === 2 ? 'bg-gradient-to-br from-orange-400 to-orange-600 text-white' :
                'bg-bg-surface text-text-secondary'
              ]"
            >
              {{ user.avatar }}
            </div>
            <div 
              v-if="user.online"
              class="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-brand rounded-full border-2 border-bg-raised"
            />
          </div>
          <div class="flex-1 min-w-0">
            <div class="text-sm font-medium text-text truncate">{{ user.name }}</div>
            <div class="text-xs text-text-muted">{{ user.posts }} posts</div>
          </div>
          <div 
            class="text-lg font-bold"
            :class="[
              index === 0 ? 'text-amber-400' :
              index === 1 ? 'text-gray-400' :
              index === 2 ? 'text-orange-400' :
              'text-text-muted'
            ]"
          >
            #{{ index + 1 }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
