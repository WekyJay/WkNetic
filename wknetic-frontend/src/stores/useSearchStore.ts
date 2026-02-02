import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getHotSearchKeywords } from '@/api/search'

const SEARCH_HISTORY_KEY = 'search_history'
const MAX_HISTORY_SIZE = 10

export const useSearchStore = defineStore('search', () => {
  // 搜索历史
  const searchHistory = ref<string[]>([])
  
  // 热门搜索词
  const hotKeywords = ref<string[]>([])
  
  // 从 localStorage 加载搜索历史
  const loadSearchHistory = () => {
    try {
      const stored = localStorage.getItem(SEARCH_HISTORY_KEY)
      if (stored) {
        searchHistory.value = JSON.parse(stored)
      }
    } catch (error) {
      console.error('加载搜索历史失败:', error)
    }
  }
  
  // 保存搜索历史到 localStorage
  const saveSearchHistory = () => {
    try {
      localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(searchHistory.value))
    } catch (error) {
      console.error('保存搜索历史失败:', error)
    }
  }
  
  // 添加搜索记录
  const addSearchHistory = (keyword: string) => {
    if (!keyword || !keyword.trim()) {
      return
    }
    
    const trimmedKeyword = keyword.trim()
    
    // 移除已存在的相同关键词
    const index = searchHistory.value.indexOf(trimmedKeyword)
    if (index > -1) {
      searchHistory.value.splice(index, 1)
    }
    
    // 添加到开头
    searchHistory.value.unshift(trimmedKeyword)
    
    // 限制最大数量
    if (searchHistory.value.length > MAX_HISTORY_SIZE) {
      searchHistory.value = searchHistory.value.slice(0, MAX_HISTORY_SIZE)
    }
    
    saveSearchHistory()
  }
  
  // 删除单条搜索历史
  const removeSearchHistory = (keyword: string) => {
    const index = searchHistory.value.indexOf(keyword)
    if (index > -1) {
      searchHistory.value.splice(index, 1)
      saveSearchHistory()
    }
  }
  
  // 清空搜索历史
  const clearSearchHistory = () => {
    searchHistory.value = []
    localStorage.removeItem(SEARCH_HISTORY_KEY)
  }
  
  // 加载热门搜索词
  const loadHotKeywords = async () => {
    try {
      const response = await getHotSearchKeywords()
      hotKeywords.value = response.data
    } catch (error) {
      console.error('加载热门搜索词失败:', error)
    }
  }
  
  // 初始化时加载历史记录
  loadSearchHistory()
  
  return {
    searchHistory,
    hotKeywords,
    addSearchHistory,
    removeSearchHistory,
    clearSearchHistory,
    loadHotKeywords
  }
})
