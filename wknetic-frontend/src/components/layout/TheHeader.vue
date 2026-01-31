<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePermission } from '@/composables/usePermission'
import LoginModal from '@/components/LoginModal.vue'

const router = useRouter()
const authStore = useAuthStore()
const { userRoleLabel, userRoleColor } = usePermission()

const isMenuOpen = ref(false)
const isSearchOpen = ref(false)
const isLoginModalOpen = ref(false)
const isUserMenuOpen = ref(false)

const navCategories = [
  { name: 'Mods', icon: 'i-tabler-puzzle', href: '/mods' },
  { name: 'Plugins', icon: 'i-tabler-plug', href: '/plugins' },
  { name: 'Data Packs', icon: 'i-tabler-database', href: '/datapacks' },
  { name: 'Shaders', icon: 'i-tabler-sun', href: '/shaders' },
  { name: 'Resource Packs', icon: 'i-tabler-photo', href: '/resourcepacks' },
  { name: 'Modpacks', icon: 'i-tabler-package', href: '/modpacks' },
  { name: 'Forum', icon: 'i-tabler-message-circle', href: '/forum' }
]

const isAuthenticated = computed(() => authStore.isAuthenticated)
const user = computed(() => authStore.user)
const isAdmin = computed(() => authStore.isAdmin)

function handleLoginSuccess() {
  isLoginModalOpen.value = false
  // 刷新用户状态已经在 authStore.login 中完成
}

function handleLogout() {
  authStore.logout()
  isUserMenuOpen.value = false
  router.push('/')
}

function goToProfile() {
  isUserMenuOpen.value = false
  router.push('/profile')
}

function goToAdmin() {
  isUserMenuOpen.value = false
  router.push('/admin')
}
</script>

<template>
  <header class="sticky top-0 z-50 bg-bg/80 backdrop-blur-xl border-b border-border">
    <!-- 主导航栏 -->
    <div class="container-main">
      <div class="flex-between h-16">
        <!-- Logo -->
        <div class="flex items-center gap-6">
          <router-link to="/" class="flex items-center gap-2 text-xl font-bold">
            <div class="w-8 h-8 bg-brand rounded-lg flex-center">
              <span class="i-tabler-cube text-bg text-lg"></span>
            </div>
            <span class="hidden sm:block text-text">WkNetic</span>
          </router-link>

          <!-- 桌面端导航 -->
          <nav class="hidden lg:flex items-center gap-1">
            <button class="nav-link flex items-center gap-1.5">
              Discover
              <span class="i-tabler-chevron-down text-sm"></span>
            </button>
          </nav>
        </div>

        <!-- 搜索栏 - 桌面端 -->
        <div class="hidden md:flex flex-1 max-w-xl mx-8">
          <div class="relative w-full">
            <span class="i-tabler-search absolute left-3 top-1/2 -translate-y-1/2 text-text-muted"></span>
            <input
                type="text"
                placeholder="Search mods, plugins, and more..."
                class="w-full input-base pl-10 pr-4"
            />
            <kbd class="absolute right-3 top-1/2 -translate-y-1/2 px-2 py-0.5 bg-bg-surface rounded text-xs text-text-muted border border-border">
              /
            </kbd>
          </div>
        </div>

        <!-- 右侧操作区 -->
        <div class="flex items-center gap-2">
          <!-- 移动端搜索按钮 -->
          <button
              class="md:hidden btn-ghost p-2"
              @click="isSearchOpen = !isSearchOpen"
          >
            <span class="i-tabler-search text-xl"></span>
          </button>

          <!-- Host a server -->
          <a href="#" class="hidden sm:flex nav-link text-sm">
            Host a server
          </a>

          <!-- Get App 按钮 -->
          <button class="hidden sm:flex btn-secondary text-sm">
            <span class="i-tabler-download text-lg"></span>
            <span class="hidden lg:inline">Get WkNetic App</span>
            <span class="lg:hidden">App</span>
          </button>

          <!-- 登录按钮或用户菜单 -->
          <div v-if="isAuthenticated && user" class="relative">
            <button 
              class="flex items-center gap-2 px-4 py-1 rounded-lg bg-transparent hover:bg-bg-surface transition-colors"
              @click="isUserMenuOpen = !isUserMenuOpen"
            >
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
                <span class="i-tabler-user text-brand"></span>
              </div>
              <span class="hidden lg:inline text-sm font-medium text-text">
                {{ user.nickname || user.username }}
              </span>
              <span class="i-tabler-chevron-down text-sm text-text-muted"></span>
            </button>

            <!-- 用户下拉菜单 -->
            <div 
              v-if="isUserMenuOpen"
              class="absolute right-0 mt-2 w-56 bg-bg border border-border rounded-lg shadow-lg py-2 z-50"
            >
              <div class="px-4 py-2  border-border">
                <p class="text-sm font-medium text-text">{{ user.nickname || user.username }}</p>
                <p class="text-xs text-text-muted">{{ user.email }}</p>
                <div class="mt-1.5 flex items-center gap-1.5">
                  <span class="i-tabler-shield text-xs" :class="userRoleColor"></span>
                  <span class="text-xs font-medium" :class="userRoleColor">{{ userRoleLabel }}</span>
                </div>
              </div>
              
              <div class="border-t border-border my-2"></div>

              <button 
                class="w-full px-4 py-2 text-left text-sm bg-transparent hover:bg-bg-surface transition-colors flex items-center gap-2"
                @click="goToProfile"
              >
                <span class="i-tabler-user"></span>
                个人资料
              </button>
              
              <button 
                v-if="isAdmin"
                class="w-full px-4 py-2 text-left text-sm bg-transparent hover:bg-bg-surface transition-colors flex items-center gap-2"
                @click="goToAdmin"
              >
                <span class="i-tabler-dashboard"></span>
                管理后台
              </button>
              
              <div class="border-t border-border my-2"></div>
              
              <button 
                class="w-full px-4 py-2 text-left text-sm bg-transparent hover:bg-bg-surface transition-colors flex items-center gap-2 text-danger"
                @click="handleLogout"
              >
                <span class="i-tabler-logout"></span>
                退出登录
              </button>
            </div>
          </div>
          
          <button 
            v-else
            class="btn-primary text-sm" 
            @click="isLoginModalOpen = true"
          >
            Sign in
          </button>

          <!-- 移动端菜单按钮 -->
          <button
              class="lg:hidden btn-ghost p-2"
              @click="isMenuOpen = !isMenuOpen"
          >
            <span :class="isMenuOpen ? 'i-tabler-x' : 'i-tabler-menu-2'" class="text-xl"></span>
          </button>
        </div>
      </div>
    </div>

    <!-- 分类导航栏 -->
    <div class="border-t border-border">
      <div class="container-main">
        <nav class="flex items-center gap-1 py-2 overflow-x-auto scrollbar-hide">
          <router-link
              v-for="cat in navCategories"
              :key="cat.name"
              :to="cat.href"
              class="nav-link flex items-center gap-2 whitespace-nowrap text-sm"
          >
            <span :class="cat.icon"></span>
            {{ cat.name }}
          </router-link>
        </nav>
      </div>
    </div>

    <!-- 移动端搜索框 -->
    <div v-if="isSearchOpen" class="md:hidden border-t border-border p-4 bg-bg">
      <div class="relative">
        <span class="i-tabler-search absolute left-3 top-1/2 -translate-y-1/2 text-text-muted"></span>
        <input
            type="text"
            placeholder="Search..."
            class="w-full input-base pl-10"
            autofocus
        />
      </div>
    </div>

    <!-- 移动端菜单 -->
    <div v-if="isMenuOpen" class="lg:hidden border-t border-border bg-bg">
      <div class="container-main py-4">
        <nav class="flex flex-col gap-1">
          <router-link
              v-for="cat in navCategories"
              :key="cat.name"
              :to="cat.href"
              class="nav-link flex items-center gap-3 py-3"
              @click="isMenuOpen = false"
          >
            <span :class="cat.icon" class="text-xl"></span>
            {{ cat.name }}
          </router-link>
          <hr class="my-2 border-border" />
          <a href="#" class="nav-link py-3">Host a server</a>
          <a href="#" class="nav-link py-3">Settings</a>
        </nav>
      </div>
    </div>

    <!-- 登录弹窗 -->
    <LoginModal
      :is-open="isLoginModalOpen"
      @close="isLoginModalOpen = false"
      @success="handleLoginSuccess"
    />
  </header>
</template>

<style scoped>
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
</style>
