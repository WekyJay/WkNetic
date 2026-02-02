<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import WkDialog, { type DialogSize } from './WkDialog.vue'
import WkInput from './WkInput.vue'
import WkButton from './WkButton.vue'

/**
 * 图标选择器弹窗组件
 * 用于选择Unocss图标
 */

export interface IconItem {
  id: string
  name: string
  class: string
  category: string
}

interface Props {
  /** 对话框标题 */
  title?: string
  /** 对话框大小 */
  size?: DialogSize
  /** 是否显示 */
  modelValue?: boolean
  /** 选中的图标 */
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

// 图标配置 - 按分类组织，支持动态生成
const iconConfig: Record<string, { label: string; icons: Array<[string, string]> }> = {
  common: {
    label: '常用',
    icons: [
      ['home', '首页'],
      ['dashboard', '仪表板'],
      ['settings', '设置'],
      ['user', '用户'],
      ['users', '用户组'],
      ['message', '消息'],
      ['bell', '通知'],
      ['bookmark', '收藏'],
      ['heart', '收心'],
      ['star', '星标'],
    ]
  },
  ui: {
    label: '界面元素',
    icons: [
      ['layout', '布局'],
      ['menu', '菜单'],
      ['menu-2', '菜单2'],
      ['grid', '网格'],
      ['list', '列表'],
      ['layout-grid', '网格布局'],
      ['layout-list', '列表布局'],
      ['layout-navbar', '导航栏布局'],
      ['layout-sidebar', '侧边栏布局'],
      ['square', '方块'],
      ['circle', '圆形'],
      ['rectangle', '矩形'],
    ]
  },
  arrow: {
    label: '箭头',
    icons: [
      ['arrow-up', '上箭头'],
      ['arrow-down', '下箭头'],
      ['arrow-left', '左箭头'],
      ['arrow-right', '右箭头'],
      ['chevron-up', '上折角'],
      ['chevron-down', '下折角'],
      ['chevron-left', '左折角'],
      ['chevron-right', '右折角'],
      ['arrows-up-down', '上下箭头'],
      ['arrows-left-right', '左右箭头'],
      ['corner-up-left', '左上角'],
      ['corner-up-right', '右上角'],
      ['corner-down-left', '左下角'],
      ['corner-down-right', '右下角'],
    ]
  },
  social: {
    label: '社交',
    icons: [
      ['brand-facebook', 'Facebook'],
      ['brand-twitter', 'Twitter'],
      ['brand-github', 'GitHub'],
      ['brand-discord', 'Discord'],
      ['brand-youtube', 'YouTube'],
      ['brand-instagram', 'Instagram'],
      ['brand-linkedin', 'LinkedIn'],
      ['brand-telegram', 'Telegram'],
      ['brand-whatsapp', 'WhatsApp'],
      ['brand-slack', 'Slack'],
      ['brand-weibo', '微博'],
      ['brand-qq', 'QQ'],
      ['brand-wechat', '微信'],
    ]
  },
  file: {
    label: '文件',
    icons: [
      ['file', '文件'],
      ['file-text', '文本文件'],
      ['file-code', '代码文件'],
      ['file-image', '图片文件'],
      ['file-music', '音乐文件'],
      ['file-video', '视频文件'],
      ['file-pdf', 'PDF文件'],
      ['file-zip', '压缩文件'],
      ['folder', '文件夹'],
      ['folder-open', '打开文件夹'],
      ['folder-plus', '新建文件夹'],
      ['folder-minus', '删除文件夹'],
      ['download', '下载'],
      ['upload', '上传'],
      ['copy', '复制'],
      ['cut', '剪切'],
      ['paste', '粘贴'],
    ]
  },
  media: {
    label: '媒体',
    icons: [
      ['photo', '图片'],
      ['photo-plus', '添加图片'],
      ['photo-off', '关闭图片'],
      ['video', '视频'],
      ['video-plus', '添加视频'],
      ['music', '音乐'],
      ['music-plus', '添加音乐'],
      ['camera', '相机'],
      ['camera-off', '关闭相机'],
      ['microphone', '麦克风'],
      ['microphone-off', '关闭麦克风'],
      ['headphones', '耳机'],
      ['volume', '音量'],
      ['volume-off', '关闭声音'],
      ['speaker', '扬声器'],
      ['artboard', '画板'],
      ['frame', '框架'],
    ]
  },
  weather: {
    label: '天气',
    icons: [
      ['sun', '太阳'],
      ['moon', '月亮'],
      ['cloud', '云'],
      ['cloud-rain', '雨'],
      ['cloud-snow', '雪'],
      ['wind', '风'],
      ['droplets', '水滴'],
      ['snowflake', '雪花'],
      ['umbrella', '伞'],
      ['sun-moon', '日月'],
    ]
  },
  tool: {
    label: '工具',
    icons: [
      ['search', '搜索'],
      ['search-off', '关闭搜索'],
      ['edit', '编辑'],
      ['trash', '删除'],
      ['trash-off', '取消删除'],
      ['plus', '添加'],
      ['minus', '减少'],
      ['multiply', '乘法'],
      ['check', '确认'],
      ['x', '取消'],
      ['help', '帮助'],
      ['question-mark', '问号'],
      ['code', '代码'],
      ['terminal', '终端'],
      ['brackets-curly', '花括号'],
      ['brand-html5', 'HTML5'],
      ['brand-css3', 'CSS3'],
      ['brand-javascript', 'JavaScript'],
    ]
  },
  status: {
    label: '状态',
    icons: [
      ['info-circle', '信息'],
      ['alert-triangle', '警告'],
      ['alert-circle', '错误'],
      ['circle-check', '成功'],
      ['loader-2', '加载'],
      ['ban', '禁止'],
      ['clock-off', '时间结束'],
      ['mood-smile', '微笑'],
      ['mood-sad', '难过'],
      ['mood-confused', '困惑'],
      ['mood-angry', '愤怒'],
      ['thumbs-up', '点赞'],
      ['thumbs-down', '踩'],
      ['hand-rock', '摇滚手势'],
      ['hand-paper', '纸牌手势'],
      ['hand-scissors', '剪刀手势'],
    ]
  },
  business: {
    label: '业务',
    icons: [
      ['briefcase', '公文包'],
      ['briefcase-2', '公文包2'],
      ['building', '建筑'],
      ['building-factory', '工厂'],
      ['building-bank', '银行'],
      ['handshake', '握手'],
      ['target', '目标'],
      ['trophy', '奖杯'],
      ['medal', '奖章'],
      ['wallet', '钱包'],
      ['credit-card', '信用卡'],
      ['coins', '硬币'],
      ['money', '钱'],
      ['receipt', '收据'],
      ['receipt-2', '收据2'],
      ['discount', '折扣'],
      ['percent', '百分比'],
      ['barcode', '条形码'],
      ['qrcode', '二维码'],
    ]
  },
  chart: {
    label: '图表',
    icons: [
      ['chart-bar', '柱形图'],
      ['chart-bar-stacked', '堆积柱形图'],
      ['chart-line', '折线图'],
      ['chart-pie', '饼图'],
      ['chart-dots', '散点图'],
      ['chart-area', '面积图'],
      ['chart-area-line', '面积线图'],
      ['chart-candle', '蜡烛图'],
      ['chart-bubble', '气泡图'],
      ['chart-radar', '雷达图'],
      ['trending-up', '上升趋势'],
      ['trending-down', '下降趋势'],
      ['arrow-up-right', '右上箭头'],
      ['arrow-down-right', '右下箭头'],
      ['line-chart', '线性图表'],
      ['axis-x', 'X轴'],
      ['axis-y', 'Y轴'],
    ]
  },
  communication: {
    label: '通讯',
    icons: [
      ['mail', '邮件'],
      ['mail-opened', '打开邮件'],
      ['mail-fast', '快速邮件'],
      ['mail-plus', '添加邮件'],
      ['phone', '电话'],
      ['phone-call', '通话'],
      ['phone-plus', '添加电话'],
      ['phone-missed', '未接电话'],
      ['phone-outgoing', '外呼'],
      ['phone-incoming', '来电'],
      ['at', '@标签'],
      ['hash', '#标签'],
      ['comments', '评论'],
      ['comment', '评论'],
      ['comment-dots', '评论详情'],
      ['comment-plus', '添加评论'],
      ['send', '发送'],
      ['share', '分享'],
      ['share-2', '分享2'],
      ['share-3', '分享3'],
      ['brand-telegram', '电报'],
      ['inbox', '收件箱'],
      ['inbox-2', '收件箱2'],
    ]
  },
  time: {
    label: '时间',
    icons: [
      ['clock', '时钟'],
      ['clock-2', '时钟2'],
      ['clock-off', '关闭时钟'],
      ['hourglass', '沙漏'],
      ['hourglass-low', '沙漏(低)'],
      ['hourglass-high', '沙漏(高)'],
      ['hourglass-empty', '空沙漏'],
      ['calendar', '日历'],
      ['calendar-event', '日程'],
      ['calendar-today', '今天'],
      ['calendar-off', '关闭日历'],
      ['calendar-plus', '添加日期'],
      ['calendar-minus', '删除日期'],
      ['date', '日期'],
      ['history', '历史'],
      ['history-toggle-10', '历史切换'],
      ['alarm', '闹钟'],
      ['alarm-plus', '添加闹钟'],
      ['alarm-minus', '删除闹钟'],
      ['stopwatch', '秒表'],
      ['timer', '计时器'],
      ['timer-off', '关闭计时器'],
    ]
  },
  game: {
    label: '游戏',
    icons: [
      ['dice', '骰子'],
      ['dice-1', '骰子1'],
      ['dice-2', '骰子2'],
      ['dice-3', '骰子3'],
      ['dice-4', '骰子4'],
      ['dice-5', '骰子5'],
      ['dice-6', '骰子6'],
      ['gamepad', '手柄'],
      ['gamepad-2', '手柄2'],
      ['cards', '卡片'],
      ['cards-diamond', '方块卡'],
      ['cards-heart', '红心卡'],
      ['cards-club', '梅花卡'],
      ['cards-spade', '黑桃卡'],
      ['puzzle', '拼图'],
      ['ball', '球'],
      ['ball-american-football', '美式足球'],
      ['ball-basketball', '篮球'],
      ['ball-bowling', '保龄球'],
      ['tennis', '网球'],
      ['chess', '国际象棋'],
      ['chess-board', '棋盘'],
      ['go', '围棋'],
      ['player-play', '播放'],
      ['player-pause', '暂停'],
      ['player-stop', '停止'],
    ]
  },
  security: {
    label: '安全',
    icons: [
      ['lock', '锁定'],
      ['lock-open', '打开'],
      ['lock-off', '关闭'],
      ['lock-check', '锁定确认'],
      ['lock-exclamation', '锁定警告'],
      ['unlock', '解锁'],
      ['shield', '盾牌'],
      ['shield-check', '盾牌确认'],
      ['shield-warning', '盾牌警告'],
      ['shield-off', '盾牌关闭'],
      ['shield-exclamation', '盾牌感叹号'],
      ['key', '钥匙'],
      ['key-off', '关闭钥匙'],
      ['keys', '多把钥匙'],
      ['fingerprint', '指纹'],
      ['eye', '查看'],
      ['eye-off', '隐藏'],
      ['eye-check', '查看确认'],
      ['password', '密码'],
      ['auth', '认证'],
      ['encrypted', '加密'],
    ]
  },
  network: {
    label: '网络',
    icons: [
      ['wifi', '无线网'],
      ['wifi-off', '关闭无线网'],
      ['wifi-0', '无信号'],
      ['wifi-1', '弱信号'],
      ['wifi-2', '中信号'],
      ['wifi-3', '强信号'],
      ['router', '路由器'],
      ['database', '数据库'],
      ['database-import', '数据库导入'],
      ['database-export', '数据库导出'],
      ['database-plus', '添加数据库'],
      ['database-minus', '删除数据库'],
      ['server', '服务器'],
      ['server-2', '服务器2'],
      ['cloud', '云'],
      ['cloud-upload', '云上传'],
      ['cloud-download', '云下载'],
      ['cloud-off', '云关闭'],
      ['globe', '全球'],
      ['globe-2', '地球2'],
      ['world', '世界'],
      ['world-latitude', '纬度'],
      ['world-longitude', '经度'],
      ['link', '链接'],
      ['link-off', '取消链接'],
      ['unlink', '取消链接'],
      ['network', '网络'],
      ['brand-internet-explorer', 'IE'],
      ['brand-chrome', 'Chrome'],
      ['brand-firefox', 'Firefox'],
      ['brand-safari', 'Safari'],
      ['rss', 'RSS订阅'],
      ['broadcast', '广播'],
    ]
  },
  editor: {
    label: '编辑',
    icons: [
      ['pencil', '铅笔'],
      ['pencil-2', '铅笔2'],
      ['pen', '笔'],
      ['pen-2', '笔2'],
      ['palette', '调色板'],
      ['highlight', '高亮'],
      ['eraser', '橡皮擦'],
      ['paint', '油漆桶'],
      ['paintbrush', '画笔'],
      ['crop', '裁剪'],
      ['crop-2', '裁剪2'],
      ['maximize', '放大'],
      ['minimize', '缩小'],
      ['rotate', '旋转'],
      ['rotate-clockwise', '顺时针旋转'],
      ['rotate-2', '旋转2'],
      ['flip-horizontal', '水平翻转'],
      ['flip-vertical', '垂直翻转'],
      ['zoom-in', '放大'],
      ['zoom-out', '缩小'],
      ['zoom-check', '缩放确认'],
      ['focus', '焦点'],
      ['focus-2', '焦点2'],
      ['aperture', '光圈'],
      ['contrast', '对比度'],
      ['brightness-half', '亮度'],
      ['brightness-up', '增加亮度'],
      ['brightness-down', '降低亮度'],
    ]
  }
}

// 图标分类标签
const iconCategories = Object.entries(iconConfig).reduce((acc, [key, value]) => {
  acc[key] = value.label
  return acc
}, { all: '全部图标' } as Record<string, string>)

// 动态生成icons数组
const icons: IconItem[] = (() => {
  const allIcons: IconItem[] = []
  Object.entries(iconConfig).forEach(([category, { icons: categoryIcons }]) => {
    categoryIcons.forEach(([iconName, label]) => {
      allIcons.push({
        id: iconName,
        name: label,
        class: `i-tabler-${iconName}`,
        category
      })
    })
  })
  return allIcons
})()

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
  let result = icons
  
  // 按搜索词过滤
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(icon => 
      icon.name.toLowerCase().includes(query) || 
      icon.id.toLowerCase().includes(query)
    )
  }
  
  // 按分类过滤
  if (activeCategory.value !== 'all') {
    result = result.filter(icon => icon.category === activeCategory.value)
  }
  
  return result
})

// 处理图标选择
const handleIconSelect = (iconClass: string) => {
  selectedIcon.value = iconClass
}

// 处理颜色选择
const handleColorSelect = (color: string) => {
  selectedColor.value = color
}

// 确认选择
const handleConfirm = () => {
  if (selectedIcon.value) {
    emit('update:selectedIcon', selectedIcon.value)
    emit('update:selectedColor', selectedColor.value)
    emit('select', selectedIcon.value, selectedColor.value)
    visible.value = false
  }
}

// 取消
const handleCancel = () => {
  visible.value = false
}

// 初始化选中状态
onMounted(() => {
  selectedIcon.value = props.selectedIcon
  selectedColor.value = props.selectedColor
})

// 监听props变化
watch(() => props.selectedIcon, (val) => {
  selectedIcon.value = val
})

watch(() => props.selectedColor, (val) => {
  selectedColor.value = val
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
          placeholder="输入图标名称搜索..."
          class="w-full"
          :clearable="true"
        >
          <template #prefix>
            <span class="i-tabler-search" />
          </template>
        </WkInput>
      </div>

      <!-- 分类筛选 -->
      <div v-if="showCategories" class="flex flex-col gap-2">
        <label class="text-sm font-medium text-[var(--text-secondary)]">图标分类</label>
        <div class="flex flex-wrap gap-2">
          <WkButton
            v-for="(categoryName, categoryKey) in iconCategories"
            :key="categoryKey"
            :variant="activeCategory === categoryKey ? 'primary' : 'secondary'"
            size="sm"
            @click="activeCategory = categoryKey"
          >
            {{ categoryName }}
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
            图标库 ({{ filteredIcons.length }})
          </label>
          <div v-if="selectedIcon" class="flex items-center gap-2 text-sm">
            <span>已选择:</span>
            <div class="flex items-center gap-1">
              <span :class="selectedIcon" :style="{ color: selectedColor }" class="text-xl" />
              <span class="text-[var(--text-secondary)]">{{ selectedIcon }}</span>
            </div>
          </div>
        </div>
        
        <!-- 图标网格 -->
        <div class="grid grid-cols-4 sm:grid-cols-6 md:grid-cols-8 gap-3 max-h-[300px] overflow-y-auto p-1">
          <div
            v-for="icon in filteredIcons"
            :key="icon.id"
            class="flex flex-col items-center justify-center p-3 rounded-lg cursor-pointer transition-all hover:bg-[var(--bg-hover)] border"
            :class="{
              'border-[var(--primary)] bg-[var(--primary-light)]': selectedIcon === icon.class,
              'border-transparent': selectedIcon !== icon.class
            }"
            :title="icon.name"
            @click="handleIconSelect(icon.class)"
          >
            <span 
              :class="icon.class" 
              class="text-2xl mb-1"
              :style="{ color: selectedIcon === icon.class ? selectedColor : 'var(--text-default)' }"
            />
            <span class="text-xs text-[var(--text-secondary)] truncate w-full text-center">
              {{ icon.name }}
            </span>
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
  max-height: 80vh;
}

/* 确保图标选择器显示在父级对话框之上 */
.wk-icon-picker-dialog :deep(.wk-dialog-overlay) {
  z-index: 10000 !important;
}
</style>
