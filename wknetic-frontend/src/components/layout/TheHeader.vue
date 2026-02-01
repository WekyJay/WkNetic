<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
// import { usePermission } from '@/composables/usePermission'
import { useTheme } from '@/composables/useTheme'
import { useI18n } from 'vue-i18n'
import LoginModal from '@/components/LoginModal.vue'

const router = useRouter()
const authStore = useAuthStore()
// const { userRoleLabel, userRoleColor } = usePermission()
const { themeMode, isDark, toggleTheme } = useTheme()
const { locale } = useI18n()

// 语言切换
const isLangMenuOpen = ref(false)
const languages = [
  { code: 'zh', name: '简体中文', flag: 'i-circle-flags-cn' },
  { code: 'en', name: 'English', flag: 'i-circle-flags-us' }
]

function setLanguage(langCode: string) {
  locale.value = langCode
  localStorage.setItem('app-language', langCode)
  isLangMenuOpen.value = false
}

const currentLanguage = computed(() => {
  return languages.find(l => l.code === locale.value) || languages[0]
})

// 主题图标
const themeIcon = computed(() => {
  if (themeMode.value === 'auto') return 'i-tabler-device-laptop'
  return isDark.value ? 'i-tabler-moon' : 'i-tabler-sun'
})

// 点击外部关闭所有下拉菜单
function closeAllMenus(event: MouseEvent) {
  const target = event.target as HTMLElement
  if (!target.closest('.lang-menu-container')) {
    isLangMenuOpen.value = false
  }
  if (!target.closest('.user-menu-container')) {
    isUserMenuOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', closeAllMenus)
})

onUnmounted(() => {
  document.removeEventListener('click', closeAllMenus)
})

const isMenuOpen = ref(false)
const isSearchOpen = ref(false)
const isLoginModalOpen = ref(false)
const isUserMenuOpen = ref(false)

const navCategories = [
  { name: 'Home', icon: 'i-tabler-home', href: '/' },
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
  const currentUser = authStore.user
  if (currentUser) {
    // UserInfo 中的ID字段是 userId
    const userId = currentUser.userId
    if (userId) {
      router.push(`/user/${userId}`)
    } else {
      console.warn('User ID not found:', currentUser)
      router.push('/login')
    }
  } else {
    router.push('/login')
  }
}

function goToAdmin() {
  isUserMenuOpen.value = false
  router.push('/admin')
}

function goToSettings() {
  isUserMenuOpen.value = false
  router.push('/settings')
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
            <div class="w-28 h-8 rounded-lg flex-center">
              <img src="/wknetic_logo.png" alt="WkNetic Logo" class="object-contain" />

            </div>
            <!-- <span class="hidden sm:block text-text">WkNetic</span> -->
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

          <!-- Get App 按钮 -->
          <button class="hidden sm:flex btn-secondary text-sm">
            <span class="i-tabler-download text-lg"></span>
            <span class="hidden lg:inline">Get WkNetic App</span>
            <span class="lg:hidden">App</span>
          </button>

          <!-- 语言切换 -->
          <div class="relative lang-menu-container">
            <button 
              class="btn-ghost p-2"
              @click.stop="isLangMenuOpen = !isLangMenuOpen"
              :title="currentLanguage?.name"
            >
              <span class="i-tabler-language text-xl"></span>
            </button>
            
            <!-- 语言下拉菜单 -->
            <div 
              v-if="isLangMenuOpen"
              class="absolute right-0 mt-2 w-36 bg-bg border border-border rounded-lg shadow-lg py-1 z-50"
            >
              <button
                v-for="lang in languages"
                :key="lang.code"
                class="w-full px-3 py-2 text-left text-sm bg-transparent hover:bg-bg-surface transition-colors flex items-center gap-2"
                :class="{ 'text-brand': locale === lang.code }"
                @click="setLanguage(lang.code)"
              >
                <span :class="lang.flag" class="w-5 h-5 rounded-full"></span>
                <span>{{ lang.name }}</span>
                <span v-if="locale === lang.code" class="i-tabler-check ml-auto text-brand"></span>
              </button>
            </div>
          </div>

          <!-- 主题切换 -->
          <button 
            class="btn-ghost p-2"
            @click="toggleTheme"
            :title="themeMode === 'auto' ? '自动' : (isDark ? '深色模式' : '浅色模式')"
          >
            <span :class="themeIcon" class="text-xl"></span>
          </button>

          <!-- 登录按钮或用户菜单 -->
          <div v-if="isAuthenticated && user" class="relative user-menu-container">
            <button 
              class="flex items-center gap-2 px-4 py-1 rounded-lg bg-transparent hover:bg-bg-surface transition-colors cursor-pointer"
              @click.stop="isUserMenuOpen = !isUserMenuOpen"
            >
              <img 
                v-if="user.avatar" 
                :src="user.avatar" 
                :alt="user.nickname || user.username"
                class="w-8 h-8 rounded-full object-cover cursor-pointer"
              />
              <div 
                v-else 
                class="w-8 h-8 rounded-full bg-brand/10 flex items-center justify-center cursor-pointer"
              >
                <span class="i-tabler-user text-brand"></span>
              </div>
              <span class="hidden lg:inline text-sm font-medium text-text cursor-pointer">
                {{ user.nickname || user.username }}
              </span>
              <span class="i-tabler-chevron-down text-sm text-text-muted"></span>
            </button>

            <!-- 用户下拉菜单 -->
            <div 
              v-if="isUserMenuOpen"
              class="absolute right-0 mt-2 w-56 bg-bg border border-border rounded-lg shadow-lg py-2 z-50"
            >
              <button
                class="w-full px-4 py-2 text-left bg-transparent hover:bg-bg-surface transition-colors"
                @click="goToProfile"
              >
                <div class="flex items-center gap-3">
                  <img 
                    v-if="user.avatar" 
                    :src="user.avatar" 
                    :alt="user.nickname || user.username"
                    class="w-10 h-10 rounded-full object-cover"
                  />
                  <div 
                    v-else 
                    class="w-10 h-10 rounded-full bg-brand/10 flex items-center justify-center"
                  >
                    <span class="i-tabler-user text-brand"></span>
                  </div>
                  <div>
                    <p class="text-sm font-medium text-text">{{ user.nickname || user.username }}</p>
                    <p class="text-xs text-text-muted">{{ user.email }}</p>
                  </div>
                </div>
              </button>
              
              <div class="border-t border-border my-2"></div>

              <button 
                class="w-full px-4 py-2 text-left text-sm bg-transparent hover:bg-bg-surface transition-colors flex items-center gap-2"
                @click="goToProfile"
              >
                <span class="i-tabler-user"></span>
                个人资料
              </button>
              
              <button 
                class="w-full px-4 py-2 text-left text-sm bg-transparent hover:bg-bg-surface transition-colors flex items-center gap-2"
                @click="goToSettings"
              >
                <span class="i-tabler-settings"></span>
                设置
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
