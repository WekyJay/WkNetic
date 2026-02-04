<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from 'vue-i18n'
import { usePermission } from '@/composables/usePermission'

interface MenuItem {
  name: string
  icon: string
  path?: string
  badge?: string | null
  children?: MenuItem[]
}

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { t } = useI18n()
const { userRoleLabel, userRoleColor } = usePermission()
const user = computed(() => authStore.user)

const isSidebarCollapsed = ref(false)
const isMobileSidebarOpen = ref(false)
const expandedMenus = ref<string[]>([])

const menuItems: MenuItem[] = [
  { 
    name: 'menu.dashboard', 
    icon: 'i-tabler-dashboard', 
    path: '/admin/',
    badge: null 
  },
  { 
    name: 'menu.management', 
    icon: 'i-tabler-layout-list', 
    badge: null,
    children: [
      { 
        name: 'menu.users', 
        icon: 'i-tabler-users', 
        path: '/admin/users',
        badge: null 
      },
      { 
        name: 'menu.roles', 
        icon: 'i-tabler-shield', 
        path: '/admin/roles',
        badge: null 
      }
    ]
  },
  { 
    name: 'menu.forum', 
    icon: 'i-tabler-message', 
    badge: null,
    children: [
      { 
        name: 'menu.forumModeration', 
        icon: 'i-tabler-shield-check', 
        path: '/admin/audit',
        badge: null 
      },
      { 
        name: 'menu.topicManagement', 
        icon: 'i-tabler-layout-list', 
        path: '/admin/topics',
        badge: null 
      }
    ]
  },
  { 
    name: 'menu.serverToken', 
    icon: 'i-tabler-key', 
    path: '/admin/server-tokens',
    badge: null 
  },
  { 
    name: 'menu.serverMonitor', 
    icon: 'i-tabler-server', 
    path: '/admin/server-monitor',
    badge: null 
  },
  { 
    name: 'menu.plugins', 
    icon: 'i-tabler-puzzle', 
    path: '/admin/plugins',
    badge: null 
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

function isActive(item: MenuItem): boolean {
  if (item.path) {
    return currentPath.value === item.path || currentPath.value.startsWith(item.path + '/')
  }
  if (item.children) {
    return item.children.some(child => isActive(child))
  }
  return false
}

function toggleMenu(menuName: string) {
  const index = expandedMenus.value.indexOf(menuName)
  if (index > -1) {
    expandedMenus.value.splice(index, 1)
  } else {
    expandedMenus.value.push(menuName)
  }
}

function isMenuExpanded(menuName: string): boolean {
  return expandedMenus.value.includes(menuName)
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
  <div class="admin-layout min-h-screen bg-bg-darker">
    <!-- Sidebar - 使用固定定位避免滚动条影响 -->
    <aside 
      :class="[
        'admin-sidebar fixed top-0 left-0 z-40 h-screen bg-bg border-r border-border transition-all duration-300',
        isSidebarCollapsed ? 'collapsed' : '',
        isMobileSidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
      ]"
    >
      <!-- Logo -->
      <div class="h-16 flex items-center justify-between px-4 border-b border-border">
        <router-link to="/admin/" class="flex items-center gap-3">
          <div class="w-10 h-10 flex-center flex-shrink-0">
            <img src="/wknetic.svg" alt="WkNetic Logo" class="object-contain w-8 h-8" />
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
        <template v-for="item in menuItems" :key="item.name">
          <!-- Submenu parent item -->
          <div 
            v-if="item.children && item.children.length > 0"
            class="space-y-1"
          >
            <button
              @click="toggleMenu(item.name)"
              :class="[
                'w-full flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors group',
                isActive(item) 
                  ? 'bg-brand/15' 
                  : 'text-text-muted hover:bg-bg-surface hover:text-text'
              ]"
            >
              <span :class="item.icon" class="text-xl flex-shrink-0"></span>
              <span v-if="!isSidebarCollapsed" class="flex-1 font-medium text-left">{{ t(item.name) }}</span>
              <span 
                v-if="!isSidebarCollapsed"
                :class="[
                  'transition-transform duration-200',
                  isMenuExpanded(item.name) ? 'rotate-180' : ''
                ]"
              >
                <span class="i-tabler-chevron-down text-lg"></span>
              </span>
            </button>

            <!-- Submenu items -->
            <transition
              enter-active-class="transition-all duration-200"
              leave-active-class="transition-all duration-200"
              enter-from-class="opacity-0 max-h-0 overflow-hidden"
              enter-to-class="opacity-100 max-h-96"
              leave-from-class="opacity-100 max-h-96"
              leave-to-class="opacity-0 max-h-0 overflow-hidden"
            >
              <div v-show="isMenuExpanded(item.name)" class="space-y-1 pl-4">
                <router-link
                  v-for="child in item.children"
                  :key="child.name"
                  :to="child.path || '/admin/'"
                  :class="[
                    'flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors group',
                    isActive(child) 
                      ? 'text-brand' 
                      : 'text-text-muted hover:bg-bg-surface hover:text-text'
                  ]"
                >
                  <span :class="child.icon" class="text-lg flex-shrink-0"></span>
                  <span v-if="!isSidebarCollapsed" class="flex-1 font-medium">{{ t(child.name) }}</span>
                  <span 
                    v-if="!isSidebarCollapsed && child.badge" 
                    :class="[
                      'px-2 py-0.5 rounded-full text-xs font-medium',
                      isActive(child) ? 'bg-brand text-bg' : 'bg-bg-surface text-text-muted'
                    ]"
                  >
                    {{ child.badge }}
                  </span>
                </router-link>
              </div>
            </transition>
          </div>

          <!-- Single menu item (no children) -->
          <router-link
            v-else
            :to="item.path || '/admin/'"
            :class="[
              'flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors group',
              isActive(item) 
                ? 'bg-brand/15' 
                : 'text-text-muted hover:bg-bg-surface hover:text-text'
            ]"
          >
            <span :class="item.icon" class="text-xl flex-shrink-0"></span>
            <span v-if="!isSidebarCollapsed" class="flex-1 font-medium">{{ t(item.name) }}</span>
            <span 
              v-if="!isSidebarCollapsed && item.badge" 
              :class="[
                'px-2 py-0.5 rounded-full text-xs font-medium',
                isActive(item) ? 'bg-brand text-bg' : 'bg-bg-surface text-text-muted'
              ]"
            >
              {{ item.badge }}
            </span>
          </router-link>
        </template>
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
            <img 
                v-if="user.avatar" 
                :src="user.avatar" 
                :alt="user.nickname || user.username"
                class="w-8 h-8 rounded-full object-cover"
              />
              <div 
                v-else 
                class="w-8 h-8 rounded-full bg-brand/10 flex items-center justify-center"
              >
                <span class="i-tabler-user"></span>
              </div>
          </div>
          <div v-if="!isSidebarCollapsed" class="flex-1 min-w-0">
            <p class="font-medium text-text text-sm truncate">{{ authStore.user?.username }}</p>
            <el-tag size="small" effect="plain" :class="userRoleColor">
              {{ userRoleLabel }}
            </el-tag>
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

    <!-- Main content - 使用固定 margin-left 避免滚动条影响 -->
    <div :class="['admin-main-content flex flex-col min-h-screen', isSidebarCollapsed ? 'collapsed' : '']">
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
/* 后台布局不需要预留滚动条空间 */
.admin-layout {
  scrollbar-gutter: auto;
}

/* 使用固定像素值，完全避免 rem 单位和滚动条的影响 */
.admin-sidebar {
  width: 256px;
}
.admin-sidebar.collapsed {
  width: 80px;
}
.admin-main-content {
  margin-left: 256px;
}
.admin-main-content.collapsed {
  margin-left: 80px;
}

/* 移动端隐藏侧边栏时，主内容无 margin */
@media (max-width: 1023px) {
  .admin-main-content,
  .admin-main-content.collapsed {
    margin-left: 0;
  }
}
</style>