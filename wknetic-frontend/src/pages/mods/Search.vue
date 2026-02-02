<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchPosts, type SearchPostDTO, type PostSearchVO } from '@/api/search'
import { useSearchStore } from '@/stores/useSearchStore'
import PostSearchCard from '@/components/forum/PostSearchCard.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const searchStore = useSearchStore()

// 搜索参数
const searchParams = reactive<SearchPostDTO>({
  keyword: '',
  topicId: undefined,
  tags: [],
  status: 1, // 默认只搜索已发布的帖子
  sortBy: '_score',
  sortOrder: 'desc',
  page: 0,
  size: 20
})

// 搜索结果
const searchResults = ref<PostSearchVO[]>([])
const total = ref(0)
const loading = ref(false)

// 过滤器显示状态
const showFilters = ref(false)

// 排序选项
const sortOptions = [
  { label: '相关性', value: '_score' },
  { label: '最新发布', value: 'createTime' },
  { label: '最多点赞', value: 'likeCount' },
  { label: '最多评论', value: 'commentCount' },
  { label: '最多浏览', value: 'viewCount' }
]

// 执行搜索
const doSearch = async () => {
  if (!searchParams.keyword || !searchParams.keyword.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  loading.value = true
  
  try {
    const result = await searchPosts(searchParams)
    searchResults.value = result.data.records
    total.value = result.data.total
    
    // 添加到搜索历史
    searchStore.addSearchHistory(searchParams.keyword)
    
    // 更新 URL 查询参数
    router.push({
      query: {
        q: searchParams.keyword,
        topic: searchParams.topicId,
        sortBy: searchParams.sortBy,
        page: searchParams.page
      }
    })
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 清空过滤器
const clearFilters = () => {
  searchParams.topicId = undefined
  searchParams.tags = []
  searchParams.startTime = undefined
  searchParams.endTime = undefined
  doSearch()
}

// 改变排序
const changeSortBy = (value: string) => {
  searchParams.sortBy = value
  searchParams.page = 0
  doSearch()
}

// 翻页
const changePage = (page: number) => {
  searchParams.page = page - 1
  doSearch()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 从 URL 初始化搜索参数
const initFromQuery = () => {
  const q = route.query.q as string
  if (q) {
    searchParams.keyword = q
    searchParams.topicId = route.query.topic ? Number(route.query.topic) : undefined
    searchParams.sortBy = (route.query.sortBy as string) || '_score'
    searchParams.page = route.query.page ? Number(route.query.page) : 0
    doSearch()
  }
}

// 监听路由变化
watch(() => route.query.q, (newVal) => {
  if (newVal && newVal !== searchParams.keyword) {
    initFromQuery()
  }
})

// 组件挂载时初始化
onMounted(() => {
  initFromQuery()
  searchStore.loadHotKeywords()
})
</script>

<template>
  <div class="search-page min-h-screen bg-gray-50 dark:bg-gray-900">
    <div class="container mx-auto px-4 py-8">
      <!-- 搜索头部 -->
      <div class="search-header bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6 mb-6">
        <div class="flex gap-4">
          <el-input
            v-model="searchParams.keyword"
            placeholder="搜索帖子、文章..."
            size="large"
            clearable
            @keyup.enter="doSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-button 
            type="primary" 
            size="large"
            :loading="loading"
            @click="doSearch"
          >
            搜索
          </el-button>
          
          <el-button 
            size="large"
            @click="showFilters = !showFilters"
          >
            <span class="i-tabler-filter w-5 h-5 mr-1"></span>
            过滤器
          </el-button>
        </div>
        
        <!-- 过滤器面板 -->
        <el-collapse-transition>
          <div v-show="showFilters" class="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
            <div class="flex gap-4 items-center">
              <span class="text-sm text-gray-600 dark:text-gray-400 whitespace-nowrap">过滤条件：</span>
              
              <el-select 
                v-model="searchParams.topicId" 
                placeholder="选择话题"
                clearable
                class="w-40"
              >
                <!-- TODO: 从 API 加载话题列表 -->
                <el-option label="全部话题" :value="undefined" />
              </el-select>
              
              <div class="flex-1"></div>
              
              <el-button size="small" @click="clearFilters">
                <span class="i-tabler-refresh w-4 h-4 mr-1"></span>
                清空过滤
              </el-button>
            </div>
          </div>
        </el-collapse-transition>
      </div>
      
      <!-- 搜索结果区域 -->
      <div class="search-content flex gap-6">
        <!-- 主内容区 -->
        <div class="flex-1">
          <!-- 结果头部 -->
          <div class="flex items-center justify-between mb-4">
            <div class="text-sm text-gray-600 dark:text-gray-400">
              找到 <span class="text-primary font-semibold">{{ total }}</span> 个结果
            </div>
            
            <el-select 
              :model-value="searchParams.sortBy" 
              @change="changeSortBy"
              size="small"
              style="width: 130px"
            >
              <el-option 
                v-for="option in sortOptions" 
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </div>
          
          <!-- 搜索结果列表 -->
          <div v-if="loading" class="space-y-4">
            <el-skeleton v-for="i in 5" :key="i" :rows="3" animated />
          </div>
          
          <div v-else-if="searchResults.length > 0" class="bg-white dark:bg-gray-800 rounded-lg shadow-sm overflow-hidden">
            <post-search-card 
              v-for="post in searchResults" 
              :key="post.postId"
              :post="post"
            />
            
            <!-- 分页 -->
            <div class="p-4 flex justify-center">
              <el-pagination
                :current-page="searchParams.page! + 1"
                :page-size="searchParams.size"
                :total="total"
                layout="prev, pager, next, jumper"
                @current-change="changePage"
              />
            </div>
          </div>
          
          <!-- 空状态 -->
          <el-empty 
            v-else 
            description="没有找到相关结果"
            :image-size="200"
          >
            <template #default>
              <p class="text-gray-500 dark:text-gray-400 mb-4">
                试试调整搜索关键词或清除过滤条件
              </p>
              <el-button type="primary" @click="clearFilters">清除过滤</el-button>
            </template>
          </el-empty>
        </div>
        
        <!-- 侧边栏 -->
        <aside class="hidden lg:block w-64 space-y-6">
          <!-- 搜索历史 -->
          <div v-if="searchStore.searchHistory.length > 0" class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-4">
            <div class="flex items-center justify-between mb-3">
              <h3 class="text-sm font-semibold text-gray-900 dark:text-gray-100">搜索历史</h3>
              <el-button 
                text 
                size="small"
                @click="searchStore.clearSearchHistory"
              >
                清空
              </el-button>
            </div>
            <div class="flex flex-wrap gap-2">
              <el-tag
                v-for="keyword in searchStore.searchHistory"
                :key="keyword"
                size="small"
                class="cursor-pointer"
                closable
                @click="searchParams.keyword = keyword; doSearch()"
                @close="searchStore.removeSearchHistory(keyword)"
              >
                {{ keyword }}
              </el-tag>
            </div>
          </div>
          
          <!-- 热门搜索 -->
          <div v-if="searchStore.hotKeywords.length > 0" class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-4">
            <h3 class="text-sm font-semibold text-gray-900 dark:text-gray-100 mb-3">热门搜索</h3>
            <div class="flex flex-wrap gap-2">
              <el-tag
                v-for="(keyword, index) in searchStore.hotKeywords"
                :key="keyword"
                size="small"
                :type="index < 3 ? 'danger' : 'info'"
                class="cursor-pointer"
                @click="searchParams.keyword = keyword; doSearch()"
              >
                {{ index + 1 }}. {{ keyword }}
              </el-tag>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<style scoped>
.search-page {
  min-height: calc(100vh - 64px);
}

.container {
  max-width: 1280px;
}
</style>
