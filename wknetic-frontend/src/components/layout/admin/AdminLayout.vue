<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { t } = useI18n()

const isSidebarCollapsed = ref(false)
const isMobileSidebarOpen = ref(false)

const menuItems = [
  { 
    name: 'menu.dashboard', 
    icon: 'i-tabler-dashboard', 
    path: '/admin/dashboard',
    badge: null 
  },
  { 
    name: 'menu.projects', 
    icon: 'i-tabler-folders', 
    path: '/admin/projects',
    badge: '1.2k' 
  },
  { 
    name: 'menu.users', 
    icon: 'i-tabler-users', 
    path: '/admin/users',
    badge: null 
  },
  { 
    name: 'menu.plugins', 
    icon: 'i-tabler-puzzle', 
    path: '/admin/plugins',
    badge: null 
  },
  { 
    name: 'menu.roles', 
    icon: 'i-tabler-shield', 
    path: '/admin/roles',
    badge: null 
  },
  { 
    name: 'menu.moderation', 
    icon: 'i-tabler-shield-check', 
    path: '/admin/moderation',
    badge: '23' 
  },
  { 
    name: 'menu.reports', 
    icon: 'i-tabler-flag', 
    path: '/admin/reports',
    badge: '5' 
  },
  { 
    name: 'menu.analytics', 
    icon: 'i-tabler-chart-bar', 
    path: '/admin/analytics',
    badge: null 
  },
  { 
    name: 'menu.settings', 
    icon: 'i-tabler-settings', 
    path: '/admin/settings',
    badge: null 
  },
]

const currentPath = computed(() => route.path)

function isActive(path: string): boolean {
  return currentPath.value === path || currentPath.value.startsWith(path + '/')
}

function handleLogout() {
  authStore.logout()
  router.push('/admin/login')
}

function toggleSidebar() {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}
</script>

<template>
  <div class="admin-layout min-h-screen bg-bg-darker flex">
    <!-- Sidebar -->
    <aside 
      :class="[
        'fixed lg:sticky top-0 left-0 z-40 h-screen bg-bg border-r border-border transition-all duration-300',
        isSidebarCollapsed ? 'w-20' : 'w-64',
        isMobileSidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
      ]"
    >
      <!-- Logo -->
      <div class="h-16 flex items-center justify-between px-4 border-b border-border">
        <router-link to="/admin/dashboard" class="flex items-center gap-3">
          <div class="w-10 h-10 bg-brand rounded-xl flex-center flex-shrink-0">
            <span class="i-tabler-cube text-bg text-xl"></span>
          </div>
          <div v-if="!isSidebarCollapsed" class="flex flex-col">
            <span class="font-bold text-text">WkNetic</span>
            <span class="text-xs text-text-muted">{{ t('admin.panel') }}</span>
          </div>
        </router-link>
        <button 
          v-if="!isSidebarCollapsed"
          class="hidden lg:flex btn-ghost p-1.5"
          @click="toggleSidebar"
        >
          <span class="i-tabler-layout-sidebar-left-collapse text-lg"></span>
        </button>
      </div>

      <!-- Navigation -->
      <nav class="p-3 space-y-1">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          :class="[
            'flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors group',
            isActive(item.path) 
              ? 'bg-brand/15 text-brand' 
              : 'text-text-muted hover:bg-bg-surface hover:text-text'
          ]"
        >
          <span :class="item.icon" class="text-xl flex-shrink-0"></span>
          <span v-if="!isSidebarCollapsed" class="flex-1 font-medium">{{ t(item.name) }}</span>
          <span 
            v-if="!isSidebarCollapsed && item.badge" 
            :class="[
              'px-2 py-0.5 rounded-full text-xs font-medium',
              isActive(item.path) ? 'bg-brand text-bg' : 'bg-bg-surface text-text-muted'
            ]"
          >
            {{ item.badge }}
          </span>
        </router-link>
      </nav>

      <!-- Expand button (collapsed state) -->
      <button 
        v-if="isSidebarCollapsed"
        class="absolute bottom-20 left-1/2 -translate-x-1/2 btn-ghost p-2"
        @click="toggleSidebar"
      >
        <span class="i-tabler-layout-sidebar-right-collapse text-lg"></span>
      </button>

      <!-- User section -->
      <div class="absolute bottom-0 left-0 right-0 p-3 border-t border-border">
        <div 
          :class="[
            'flex items-center gap-3 p-2 rounded-lg bg-bg-surface',
            isSidebarCollapsed ? 'justify-center' : ''
          ]"
        >
          <div class="w-9 h-9 rounded-full bg-brand/20 flex-center flex-shrink-0">
            <span class="i-tabler-user text-brand"></span>
          </div>
          <div v-if="!isSidebarCollapsed" class="flex-1 min-w-0">
            <p class="font-medium text-text text-sm truncate">{{ authStore.user?.username }}</p>
            <p class="text-xs text-text-muted truncate">{{ authStore.user?.role }}</p>
          </div>
          <button 
            v-if="!isSidebarCollapsed"
            class="btn-ghost p-1.5 text-text-muted hover:text-danger"
            @click="handleLogout"
            :title="t('admin.logout')"
          >
            <span class="i-tabler-logout text-lg"></span>
          </button>
        </div>
      </div>
    </aside>

    <!-- Mobile overlay -->
    <div 
      v-if="isMobileSidebarOpen"
      class="fixed inset-0 bg-black/50 z-30 lg:hidden"
      @click="isMobileSidebarOpen = false"
    ></div>

    <!-- Main content -->
    <div class="flex-1 flex flex-col min-h-screen">
      <!-- Top header -->
      <header class="sticky top-0 z-20 h-16 bg-bg/80 backdrop-blur-xl border-b border-border">
        <div class="h-full px-4 lg:px-6 flex items-center justify-between">
          <!-- Mobile menu button -->
          <button 
            class="lg:hidden btn-ghost p-2"
            @click="isMobileSidebarOpen = true"
          >
            <span class="i-tabler-menu-2 text-xl"></span>
          </button>

          <!-- Breadcrumb / Search -->
          <div class="hidden sm:flex items-center gap-4 flex-1">
            <div class="relative max-w-md w-full">
              <span class="i-tabler-search absolute left-3 top-1/2 -translate-y-1/2 text-text-muted"></span>
              <input 
                type="text"
                :placeholder="t('admin.searchPlaceholder')"
                class="w-full input-base pl-10 pr-4 bg-bg-surface"
              />
            </div>
          </div>

          <!-- Right actions -->
          <div class="flex items-center gap-2">
            <button class="btn-ghost p-2 relative">
              <span class="i-tabler-bell text-xl"></span>
              <span class="absolute top-1 right-1 w-2 h-2 bg-danger rounded-full"></span>
            </button>
            <button class="btn-ghost p-2">
              <span class="i-tabler-help text-xl"></span>
            </button>
            <router-link to="/" class="btn-secondary text-sm">
              <span class="i-tabler-external-link"></span>
              <span class="hidden sm:inline">{{ t('admin.viewSite') }}</span>
            </router-link>
          </div>
        </div>
      </header>

      <!-- Page content -->
      <main class="flex-1 p-4 lg:p-6 bg-bg-DEFAULT">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  /* Admin 样式 */
}
</style>
