import { ref, watchEffect, computed } from 'vue'
import { useAppearanceStore } from '@/stores/appearance'

// 定义三种模式类型
type ThemeMode = 'light' | 'dark' | 'auto'

// 全局状态（单例模式，保证多组件共享）
const themeMode = ref<ThemeMode>('auto')
const isDark = ref(false)

/**
 * @deprecated 使用 useAppTheme 代替
 * 这个函数保留用于向后兼容
 */
export function useTheme() {
    // 1. 获取系统偏好
    const systemDarkQuery = window.matchMedia('(prefers-color-scheme: dark)')

    // 2. 初始化：从 localStorage 读取上次设置
    const savedTheme = localStorage.getItem('app-theme') as ThemeMode | null
    if (savedTheme) {
        themeMode.value = savedTheme
    }

    // 3. 核心逻辑：计算当前应该显示什么颜色
    // 只要系统或者模式变了，这里立即重新计算，无需刷新
    const updateState = () => {
        const systemIsDark = systemDarkQuery.matches

        if (themeMode.value === 'auto') {
            isDark.value = systemIsDark
        } else {
            isDark.value = themeMode.value === 'dark'
        }

        // 应用到 HTML 标签
        if (isDark.value) {
            document.documentElement.classList.add('dark')
        } else {
            document.documentElement.classList.remove('dark')
        }
    }

    // 4. 监听器：当系统外观改变时（比如日落自动变黑），触发更新
    // addEventListener 是“不刷新就生效”的关键
    systemDarkQuery.addEventListener('change', updateState)

    // 5. 监听器：当用户手动修改模式时，触发更新
    watchEffect(updateState)

    // 6. 提供给外部的方法
    const setTheme = (mode: ThemeMode) => {
        themeMode.value = mode
        localStorage.setItem('app-theme', mode)
    }

    // 切换逻辑：在 Light -> Dark -> Auto 之间循环，或者你可以做成下拉菜单
    const toggleTheme = () => {
        if (themeMode.value === 'light') setTheme('dark')
        else if (themeMode.value === 'dark') setTheme('auto')
        else setTheme('light')
    }

    return {
        themeMode, // 当前模式 ('light' | 'dark' | 'auto')
        isDark,    // 当前实际表现是否是暗色 (true/false)
        setTheme,  // 手动设置模式
        toggleTheme // 循环切换
    }
}

/**
 * 新版主题系统 Hook - 推荐使用
 * 集成了颜色主题和明暗模式的完整管理
 */
export function useAppTheme() {
    const appearanceStore = useAppearanceStore()
    
    return {
        // 颜色主题
        colorTheme: computed(() => appearanceStore.colorTheme),
        setColorTheme: appearanceStore.setColorTheme,
        currentTheme: computed(() => appearanceStore.currentTheme),
        availableThemes: computed(() => appearanceStore.availableThemes),
        
        // 明暗模式
        darkMode: computed(() => appearanceStore.darkMode),
        isDarkMode: computed(() => appearanceStore.isDarkMode),
        setDarkMode: appearanceStore.setDarkMode,
        toggleDarkMode: appearanceStore.toggleDarkMode,
        
        // 权限和系统配置
        userCanChangeTheme: computed(() => appearanceStore.userCanChangeTheme),
        systemDefaultTheme: computed(() => appearanceStore.systemDefaultTheme),
        
        // 自定义主题
        registerCustomTheme: appearanceStore.registerCustomTheme,
        removeCustomTheme: appearanceStore.removeCustomTheme,
        
        // 初始化
        initialize: appearanceStore.initialize
    }
}