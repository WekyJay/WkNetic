import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { configApi } from '@/api/config'

// 定义主题类型
export type ColorTheme = string  // 支持动态主题，包括 'default', 'grass' 以及自定义主题 ID
export type DarkMode = 'light' | 'dark' | 'auto'

// 主题配置接口
export interface ThemeConfig {
  id: string
  name: string
  displayName: string
  cssFile: string
  preview?: {
    primary: string
    background: string
    text: string
  }
}

// 内置主题配置
const BUILTIN_THEMES: Record<string, ThemeConfig> = {
  default: {
    id: 'default',
    name: 'default',
    displayName: 'Default Orange',
    cssFile: '/styles/theme.css',
    preview: {
      primary: '#ff6b35',
      background: '#ffffff',
      text: '#18181b'
    }
  },
  grass: {
    id: 'grass',
    name: 'grass',
    displayName: 'Minecraft Grass',
    cssFile: '/styles/theme_grass.css',
    preview: {
      primary: '#6DB02C',
      background: '#ffffff',
      text: '#1a3a1a'
    }
  }
}

export const useAppearanceStore = defineStore('appearance', () => {
  // ==================== 状态定义 ====================
  const colorTheme = ref<string>('default')
  const darkMode = ref<DarkMode>('auto')
  const customThemes = ref<ThemeConfig[]>([])
  
  // 系统配置
  const systemDefaultTheme = ref<string>('default')
  const userCanChangeTheme = ref<boolean>(true)
  
  // ==================== 计算属性 ====================
  
  // 获取当前主题配置
  const currentTheme = computed(() => {
    // 先在自定义主题中查找
    const customTheme = customThemes.value.find(t => t.id === colorTheme.value)
    if (customTheme) return customTheme
    
    // 再在内置主题中查找
    if (BUILTIN_THEMES[colorTheme.value]) {
      return BUILTIN_THEMES[colorTheme.value]
    }
    
    // 如果都找不到，使用系统默认主题
    if (BUILTIN_THEMES[systemDefaultTheme.value]) {
      return BUILTIN_THEMES[systemDefaultTheme.value]
    }
    
    // 最后的保底：返回 default 主题
    return BUILTIN_THEMES.default
  })
  
  // 获取所有可用主题（内置 + 自定义）
  const availableThemes = computed(() => {
    const builtin = [BUILTIN_THEMES.default, BUILTIN_THEMES.grass]
    return [...builtin, ...customThemes.value]
  })
  
  // 判断当前是否为暗色模式
  const isDarkMode = computed(() => {
    if (darkMode.value === 'auto') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    return darkMode.value === 'dark'
  })
  
  // ==================== 操作方法 ====================
  
  /**
   * 从后台加载系统配置
   */
  async function loadSystemConfig() {
    try {
      const response = await configApi.getPublicConfigs()
      const configs = response.data || {}
      
      // 读取默认主题
      if (configs['theme.default']) {
        systemDefaultTheme.value = configs['theme.default']
        console.log('[Theme] 系统默认主题:', systemDefaultTheme.value)
      }
      
      // 读取用户是否可以切换主题
      if (configs['theme.user_changeable'] !== undefined) {
        userCanChangeTheme.value = configs['theme.user_changeable'] === '1'
        console.log('[Theme] 用户可切换主题:', userCanChangeTheme.value)
      }
      
      // 读取自定义主题列表
      if (configs['theme.custom_themes']) {
        try {
          const themes = JSON.parse(configs['theme.custom_themes'])
          if (Array.isArray(themes)) {
            customThemes.value = themes
            console.log('[Theme] 自定义主题数量:', themes.length)
          }
        } catch (e) {
          console.error('[Theme] 解析自定义主题失败:', e)
        }
      }
      
      // 如果用户当前主题不在可用列表中，重置为默认主题
      const currentThemeExists = availableThemes.value.find(t => t.id === colorTheme.value)
      if (!currentThemeExists) {
        console.warn('[Theme] 当前主题不可用，切换到系统默认主题:', systemDefaultTheme.value)
        colorTheme.value = systemDefaultTheme.value
      }
    } catch (error) {
      console.error('[Theme] 加载系统配置失败:', error)
    }
  }
  
  /**
   * 设置颜色主题
   */
  function setColorTheme(theme: string) {
    if (!userCanChangeTheme.value) {
      console.warn('[Theme] 用户没有切换主题的权限')
      return
    }
    
    // 检查是否为可用主题
    const themeConfig = availableThemes.value.find(t => t.id === theme)
    if (themeConfig) {
      colorTheme.value = theme
      console.log('[Theme] 切换主题:', theme)
      applyTheme()
    } else {
      console.warn('[Theme] 主题不存在:', theme)
    }
  }
  
  /**
   * 设置明暗模式
   */
  function setDarkMode(mode: DarkMode) {
    darkMode.value = mode
    applyDarkMode()
  }
  
  /**
   * 切换明暗模式（循环：light -> dark -> auto）
   */
  function toggleDarkMode() {
    if (darkMode.value === 'light') {
      setDarkMode('dark')
    } else if (darkMode.value === 'dark') {
      setDarkMode('auto')
    } else {
      setDarkMode('light')
    }
  }
  
  /**
   * 注册自定义主题
   */
  function registerCustomTheme(theme: ThemeConfig) {
    const existingIndex = customThemes.value.findIndex(t => t.id === theme.id)
    if (existingIndex >= 0) {
      customThemes.value[existingIndex] = theme
    } else {
      customThemes.value.push(theme)
    }
  }
  
  /**
   * 移除自定义主题
   */
  function removeCustomTheme(themeId: string) {
    const index = customThemes.value.findIndex(t => t.id === themeId)
    if (index >= 0) {
      customThemes.value.splice(index, 1)
      if (colorTheme.value === 'custom' && customThemes.value.length === 0) {
        setColorTheme('default')
      }
    }
  }
  
  /**
   * 应用颜色主题（切换 CSS 文件）
   */
  function applyTheme() {
    const themeConfig = currentTheme.value
    
    // 查找现有的主题 link 标签
    let themeLink = document.querySelector('#theme-stylesheet') as HTMLLinkElement
    
    if (!themeLink) {
      // 如果不存在，创建新的 link 标签
      themeLink = document.createElement('link')
      themeLink.id = 'theme-stylesheet'
      themeLink.rel = 'stylesheet'
      document.head.appendChild(themeLink)
    }
    
    // 更新 href（注意：这里使用相对路径，Vite 会处理）
    themeLink.href = themeConfig.cssFile
    
    // 设置 data-theme 属性（用于 CSS 选择器或未来扩展）
    document.documentElement.setAttribute('data-theme', themeConfig.id)
  }
  
  /**
   * 应用明暗模式
   */
  function applyDarkMode() {
    const isDark = isDarkMode.value
    
    if (isDark) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }
  
  /**
   * 初始化主题系统
   */
  async function initialize() {
    console.log('[Theme] 初始化主题系统...')
    
    // 先加载系统配置
    await loadSystemConfig()
    
    // 如果用户不能切换主题，强制使用系统默认主题
    if (!userCanChangeTheme.value) {
      console.log('[Theme] 强制使用系统默认主题')
      colorTheme.value = systemDefaultTheme.value
    }
    
    // 应用主题
    applyTheme()
    applyDarkMode()
    
    console.log('[Theme] 当前主题:', colorTheme.value)
    console.log('[Theme] 当前明暗模式:', darkMode.value)
    
    // 监听系统颜色方案变化
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    mediaQuery.addEventListener('change', () => {
      if (darkMode.value === 'auto') {
        applyDarkMode()
      }
    })
    
    // 监听主题变化
    watch([colorTheme, darkMode], () => {
      applyTheme()
      applyDarkMode()
    })
  }
  
  return {
    // State
    colorTheme,
    darkMode,
    customThemes,
    systemDefaultTheme,
    userCanChangeTheme,
    
    // Computed
    currentTheme,
    availableThemes,
    isDarkMode,
    
    // Actions
    setColorTheme,
    setDarkMode,
    toggleDarkMode,
    registerCustomTheme,
    removeCustomTheme,
    loadSystemConfig,
    initialize
  }
}, {
  persist: {
    key: 'wknetic-appearance',
    storage: localStorage,
    pick: ['colorTheme', 'darkMode']  // 只持久化用户选择，不持久化系统配置
  }
})
