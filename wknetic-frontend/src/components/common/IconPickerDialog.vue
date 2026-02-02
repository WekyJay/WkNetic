<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { Icon } from '@iconify/vue'
import { useVirtualList } from '@vueuse/core'
import WkDialog, { type DialogSize } from './WkDialog.vue'
import WkInput from './WkInput.vue'
import WkButton from './WkButton.vue'

// 直接导入 tabler 图标集的 icons.json
import tablerIcons from '@iconify-json/tabler/icons.json'

/**
 * 图标选择器弹窗组件
 * 使用 @iconify/vue 渲染图标，支持虚拟滚动优化性能
 */

export interface IconItem {
  name: string        // 图标名称 (如 'home')
  icon: string        // 完整图标名 (如 'tabler:home')
  category: string    // 分类
}

interface Props {
  /** 对话框标题 */
  title?: string
  /** 对话框大小 */
  size?: DialogSize
  /** 是否显示 */
  modelValue?: boolean
  /** 选中的图标 (格式: 'tabler:home' 或 'i-tabler-home') */
  selectedIcon?: string
  /** 显示颜色选择器 */
  showColorPicker?: boolean
  /** 选中的颜色 */
  selectedColor?: string
  /** 是否显示分类 */
  showCategories?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: '选择图标',
  size: 'lg',
  modelValue: false,
  selectedIcon: '',
  selectedColor: '#1890ff',
  showColorPicker: true,
  showCategories: true
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'update:selectedIcon': [icon: string]
  'update:selectedColor': [color: string]
  'select': [icon: string, color: string]
}>()

const visible = defineModel<boolean>('modelValue')
const searchQuery = ref('')
const selectedIcon = ref(props.selectedIcon)
const selectedColor = ref(props.selectedColor)
const activeCategory = ref('all')

// 图标分类映射 - 根据图标名称前缀自动分类
const categoryKeywords: Record<string, string[]> = {
  common: ['home', 'dashboard', 'settings', 'user', 'users', 'message', 'bell', 'bookmark', 'heart', 'star', 'menu', 'dots'],
  arrow: ['arrow', 'chevron', 'caret', 'corner', 'direction'],
  social: ['brand-'],
  file: ['file', 'folder', 'document', 'download', 'upload', 'copy', 'clipboard'],
  media: ['photo', 'video', 'music', 'camera', 'microphone', 'headphone', 'volume', 'speaker', 'player'],
  weather: ['sun', 'moon', 'cloud', 'rain', 'snow', 'wind', 'droplet', 'umbrella', 'temperature'],
  tool: ['search', 'edit', 'trash', 'plus', 'minus', 'check', 'x', 'help', 'question', 'code', 'terminal', 'tool', 'hammer', 'wrench'],
  status: ['info', 'alert', 'error', 'success', 'warning', 'loader', 'ban', 'mood', 'thumb', 'hand'],
  business: ['briefcase', 'building', 'handshake', 'target', 'trophy', 'medal', 'wallet', 'credit-card', 'coin', 'money', 'receipt', 'discount', 'percent', 'barcode', 'qrcode'],
  chart: ['chart', 'trending', 'graph', 'axis', 'report', 'analytics'],
  communication: ['mail', 'phone', 'at', 'hash', 'comment', 'send', 'share', 'inbox', 'antenna', 'broadcast'],
  time: ['clock', 'hourglass', 'calendar', 'date', 'history', 'alarm', 'stopwatch', 'timer'],
  game: ['dice', 'gamepad', 'cards', 'puzzle', 'ball', 'tennis', 'chess', 'go', 'play', 'pause', 'stop'],
  security: ['lock', 'unlock', 'shield', 'key', 'fingerprint', 'eye', 'password', 'auth', 'encrypt'],
  network: ['wifi', 'router', 'database', 'server', 'cloud', 'globe', 'world', 'link', 'network', 'rss', 'api'],
  editor: ['pencil', 'pen', 'palette', 'highlight', 'eraser', 'paint', 'brush', 'crop', 'zoom', 'focus', 'rotate', 'flip', 'brightness', 'contrast'],
  layout: ['layout', 'grid', 'list', 'table', 'columns', 'rows', 'sidebar', 'navbar'],
  shape: ['square', 'circle', 'rectangle', 'triangle', 'polygon', 'hexagon', 'octagon']
}

// 分类标签
const iconCategories: Record<string, string> = {
  all: '全部图标',
  common: '常用',
  arrow: '箭头',
  social: '社交品牌',
  file: '文件',
  media: '媒体',
  weather: '天气',
  tool: '工具',
  status: '状态',
  business: '业务',
  chart: '图表',
  communication: '通讯',
  time: '时间',
  game: '游戏',
  security: '安全',
  network: '网络',
  editor: '编辑',
  layout: '布局',
  shape: '形状',
  other: '其他'
}

// 根据图标名称获取分类
function getIconCategory(iconName: string): string {
  const lowerName = iconName.toLowerCase()
  for (const [category, keywords] of Object.entries(categoryKeywords)) {
    if (keywords.some(keyword => lowerName.includes(keyword))) {
      return category
    }
  }
  return 'other'
}

// 从 @iconify-json/tabler 解析所有图标
const allIcons = computed<IconItem[]>(() => {
  const icons: IconItem[] = []
  const iconNames = Object.keys(tablerIcons.icons)
  
  for (const name of iconNames) {
    icons.push({
      name,
      icon: `tabler:${name}`,
      category: getIconCategory(name)
    })
  }
  
  return icons
})

// 颜色选项
const colorOptions = [
  { name: '蓝色', value: '#1890ff' },
  { name: '绿色', value: '#52c41a' },
  { name: '橙色', value: '#fa8c16' },
  { name: '金色', value: '#faad14' },
  { name: '红色', value: '#f5222d' },
  { name: '紫色', value: '#722ed1' },
  { name: '粉色', value: '#eb2f96' },
  { name: '青色', value: '#13c2c2' },
  { name: '灰色', value: '#666666' },
  { name: '黑色', value: '#000000' }
]

// 过滤后的图标
const filteredIcons = computed(() => {
  let result = allIcons.value
  
  // 按搜索词过滤
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(icon => icon.name.toLowerCase().includes(query))
  }
  
  // 按分类过滤
  if (activeCategory.value !== 'all') {
    result = result.filter(icon => icon.category === activeCategory.value)
  }
  
  return result
})

// 计算每行显示的图标数量 (用于虚拟滚动)
const ICONS_PER_ROW = 8
const ROW_HEIGHT = 80 // 每行高度 (px)

// 将图标按行分组
const iconRows = computed(() => {
  const rows: IconItem[][] = []
  const icons = filteredIcons.value
  for (let i = 0; i < icons.length; i += ICONS_PER_ROW) {
    rows.push(icons.slice(i, i + ICONS_PER_ROW))
  }
  return rows
})

// 使用 @vueuse/core 的虚拟滚动
const containerRef = ref<HTMLElement | null>(null)
const { list: virtualRows, containerProps, wrapperProps } = useVirtualList(
  iconRows,
  {
    itemHeight: ROW_HEIGHT,
    overscan: 5
  }
)

// 转换图标格式: 'tabler:home' <-> 'i-tabler-home'
function toIconifyFormat(icon: string): string {
  if (icon.startsWith('i-')) {
    // i-tabler-home -> tabler:home
    const parts = icon.slice(2).split('-')
    if (parts.length >= 2) {
      const prefix = parts[0]
      const name = parts.slice(1).join('-')
      return `${prefix}:${name}`
    }
  }
  return icon
}

function toUnocssFormat(icon: string): string {
  if (icon.includes(':')) {
    // tabler:home -> i-tabler-home
    return 'i-' + icon.replace(':', '-')
  }
  return icon
}

// 处理图标选择 - 存储为 iconify 格式
const handleIconSelect = (icon: string) => {
  selectedIcon.value = icon
}

// 检查图标是否被选中
const isIconSelected = (icon: string): boolean => {
  if (!selectedIcon.value) return false
  const normalizedSelected = toIconifyFormat(selectedIcon.value)
  const normalizedIcon = toIconifyFormat(icon)
  return normalizedSelected === normalizedIcon
}

// 处理颜色选择
const handleColorSelect = (color: string) => {
  selectedColor.value = color
}

// 确认选择 - 输出为 unocss 格式 (i-tabler-xxx)
const handleConfirm = () => {
  if (selectedIcon.value) {
    const unocssIcon = toUnocssFormat(selectedIcon.value)
    emit('update:selectedIcon', unocssIcon)
    emit('update:selectedColor', selectedColor.value)
    emit('select', unocssIcon, selectedColor.value)
    visible.value = false
  }
}

// 取消
const handleCancel = () => {
  visible.value = false
}

// 获取分类下的图标数量
const getCategoryCount = (category: string): number => {
  if (category === 'all') return allIcons.value.length
  return allIcons.value.filter(icon => icon.category === category).length
}

// 初始化选中状态
onMounted(() => {
  selectedIcon.value = props.selectedIcon ? toIconifyFormat(props.selectedIcon) : ''
  selectedColor.value = props.selectedColor
})

// 监听props变化
watch(() => props.selectedIcon, (val) => {
  selectedIcon.value = val ? toIconifyFormat(val) : ''
})

watch(() => props.selectedColor, (val) => {
  selectedColor.value = val
})

// 切换分类时重置虚拟滚动位置
watch(activeCategory, async () => {
  await nextTick()
  if (containerRef.value) {
    containerRef.value.scrollTop = 0
  }
})

watch(searchQuery, async () => {
  await nextTick()
  if (containerRef.value) {
    containerRef.value.scrollTop = 0
  }
})
</script>

<template>
  <WkDialog
    v-model="visible"
    :title="title"
    :size="size"
    :show-footer="true"
    @close="handleCancel"
    class="wk-icon-picker-dialog"
  >
    <div class="flex flex-col gap-4">
      <!-- 搜索框 -->
      <div class="flex flex-col gap-2">
        <label class="text-sm font-medium text-[var(--text-secondary)]">搜索图标</label>
        <WkInput
          v-model="searchQuery"
          placeholder="输入图标名称搜索 (如 home, user, settings...)"
          class="w-full"
          :clearable="true"
        >
          <template #prefix>
            <Icon icon="tabler:search" class="w-4 h-4" />
          </template>
        </WkInput>
      </div>

      <!-- 分类筛选 -->
      <div v-if="showCategories" class="flex flex-col gap-2">
        <label class="text-sm font-medium text-[var(--text-secondary)]">图标分类</label>
        <div class="flex flex-wrap gap-2 max-h-50 overflow-y-auto">
          <WkButton
            v-for="(categoryName, categoryKey) in iconCategories"
            :key="categoryKey"
            :variant="activeCategory === categoryKey ? 'primary' : 'secondary'"
            size="sm"
            @click="activeCategory = categoryKey"
          >
            {{ categoryName }} ({{ getCategoryCount(categoryKey) }})
          </WkButton>
        </div>
      </div>

      <!-- 颜色选择器 -->
      <div v-if="showColorPicker" class="flex flex-col gap-2">
        <label class="text-sm font-medium text-[var(--text-secondary)]">图标颜色</label>
        <div class="flex flex-wrap gap-2">
          <div
            v-for="color in colorOptions"
            :key="color.value"
            class="w-8 h-8 rounded-full cursor-pointer border-2 transition-all hover:scale-110"
            :style="{
              backgroundColor: color.value,
              borderColor: selectedColor === color.value ? 'var(--primary)' : 'transparent'
            }"
            :title="color.name"
            @click="handleColorSelect(color.value)"
          />
        </div>
      </div>

      <!-- 图标预览 -->
      <div class="flex flex-col gap-2">
        <div class="flex items-center justify-between">
          <label class="text-sm font-medium text-[var(--text-secondary)]">
            图标库 ({{ filteredIcons.length }} / {{ allIcons.length }})
          </label>
          <div v-if="selectedIcon" class="flex items-center gap-2 text-sm">
            <span>已选择:</span>
            <div class="flex items-center gap-1">
              <Icon 
                :icon="selectedIcon" 
                :style="{ color: selectedColor }" 
                class="text-xl" 
              />
              <span class="text-[var(--text-secondary)]">{{ toUnocssFormat(selectedIcon) }}</span>
            </div>
          </div>
        </div>
        
        <!-- 虚拟滚动图标网格 -->
        <div 
          v-bind="containerProps"
          class="h-[320px] overflow-y-auto border border-bg-surface rounded-lg icon-grid-container"
          @scroll="containerRef = $event.target as HTMLElement"
        >
          <div v-bind="wrapperProps" class="p-2">
            <div 
              v-for="{ data: row, index } in virtualRows" 
              :key="index"
              class="grid grid-cols-8 gap-2"
              :style="{ height: ROW_HEIGHT + 'px' }"
            >
              <div
                v-for="icon in row"
                :key="icon.icon"
                class="flex flex-col items-center justify-center p-2 rounded-lg cursor-pointer transition-all hover:bg-[var(--bg-hover)] border"
                :class="{
                  'border-[var(--primary)] bg-[var(--primary-light)]': isIconSelected(icon.icon),
                  'border-transparent': !isIconSelected(icon.icon)
                }"
                :title="icon.name"
                @click="handleIconSelect(icon.icon)"
              >
                <Icon 
                  :icon="icon.icon" 
                  class="text-2xl mb-1"
                  :style="{ color: isIconSelected(icon.icon) ? selectedColor : 'var(--text-default)' }"
                />
                <span class="text-xs text-[var(--text-secondary)] truncate w-full text-center">
                  {{ icon.name }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex items-center gap-2">
        <WkButton variant="secondary" @click="handleCancel">
          取消
        </WkButton>
        <WkButton variant="primary" :disabled="!selectedIcon" @click="handleConfirm">
          确认选择
        </WkButton>
      </div>
    </template>
  </WkDialog>
</template>

<style scoped>
:deep(.wk-dialog) {
  max-height: 85vh;
}

/* 确保图标选择器显示在父级对话框之上 */
.wk-icon-picker-dialog :deep(.wk-dialog-overlay) {
  z-index: 10000 !important;
}

/* 虚拟滚动容器滚动条样式 */
.icon-grid-container::-webkit-scrollbar {
  width: 6px;
}

.icon-grid-container::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.icon-grid-container::-webkit-scrollbar-thumb {
  background: var(--border);
  border-radius: 3px;
}

.icon-grid-container::-webkit-scrollbar-thumb:hover {
  background: var(--text-muted);
}
</style>
