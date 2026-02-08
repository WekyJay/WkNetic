<template>
  <AppLayout>
    <template #header>
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold">{{ $t('gameChat.title') }}</h1>
          <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
            {{ $t('gameChat.subtitle') }}
          </p>
        </div>
        
        <div class="flex items-center gap-3">
          <!-- 服务器选择 -->
          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-600 dark:text-gray-400">{{ $t('gameChat.server') }}:</span>
            <el-select
              v-model="selectedServer"
              :placeholder="$t('gameChat.selectServer')"
              size="small"
              style="width: 150px"
              @change="onServerChange"
            >
              <el-option
                v-for="server in servers"
                :key="server.id"
                :label="server.name"
                :value="server.id"
              />
            </el-select>
          </div>
          
          <!-- 频道选择 -->
          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-600 dark:text-gray-400">{{ $t('gameChat.channel') }}:</span>
            <el-select
              v-model="selectedChannel"
              :placeholder="$t('gameChat.selectChannel')"
              size="small"
              style="width: 150px"
              @change="onChannelChange"
            >
              <el-option
                v-for="channel in channels"
                :key="channel.id"
                :label="channel.name"
                :value="channel.id"
              />
            </el-select>
          </div>
          
          <!-- 世界选择（仅在特定频道显示） -->
          <div v-if="showWorldSelector" class="flex items-center gap-2">
            <span class="text-sm text-gray-600 dark:text-gray-400">{{ $t('gameChat.world') }}:</span>
            <el-select
              v-model="selectedWorld"
              :placeholder="$t('gameChat.selectWorld')"
              size="small"
              style="width: 150px"
              @change="onWorldChange"
            >
              <el-option
                v-for="world in worlds"
                :key="world.id"
                :label="world.name"
                :value="world.id"
              />
            </el-select>
          </div>
        </div>
      </div>
    </template>

    <div class="flex flex-col" style="height: calc(100vh - 128px);">
      <!-- 聊天区域 -->
      <div class="flex-1 flex flex-col border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden">
        <!-- 聊天消息列表 -->
        <div
          ref="messagesContainer"
          class="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50 dark:bg-gray-900"
          @scroll="handleScroll"
        >
          <div v-if="loadingMessages" class="flex justify-center py-8">
            <div class="is-loading text-xl"><i class="i-ep-loading animate-spin"></i></div>
          </div>
          
          <div v-else-if="messages.length === 0" class="flex flex-col items-center justify-center py-12 text-gray-500">
            <i class="i-ep-chat-dot-round text-4xl mb-4"></i>
            <p>{{ $t('gameChat.noMessages') }}</p>
            <p class="text-sm mt-2">{{ $t('gameChat.startChatting') }}</p>
          </div>
          
          <div v-else>
            <div
              v-for="message in messages"
              :key="message.id"
              class="flex gap-3 p-3 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
            >
              <!-- 玩家头像 -->
              <div class="flex-shrink-0">
                <UserAvatar
                  :avatar="message.player.avatar"
                  :username="message.player.username"
                  size="md"
                />
              </div>
              
              <!-- 消息内容 -->
              <div class="flex-1 min-w-0">
                <div class="flex items-baseline gap-2 mb-1">
                  <span class="font-semibold text-gray-900 dark:text-gray-100">
                    {{ message.player.username }}
                  </span>
                  <span class="text-xs text-gray-500">
                    {{ formatTime(message.timestamp) }}
                  </span>
                  <span
                    v-if="message.channel"
                    class="text-xs px-2 py-0.5 rounded-full bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200"
                  >
                    {{ getChannelName(message.channel) }}
                  </span>
                </div>
                
                <div class="text-gray-800 dark:text-gray-200 whitespace-pre-wrap">
                  {{ message.content }}
                </div>
                
                <!-- 消息操作 -->
                <div class="flex gap-2 mt-2 opacity-0 hover:opacity-100 transition-opacity">
                  <el-button
                    v-if="canReply"
                    type="text"
                    size="small"
                    @click="replyTo(message.player.username)"
                  >
                    {{ $t('gameChat.reply') }}
                  </el-button>
                  <el-button
                    v-if="canReport"
                    type="text"
                    size="small"
                    @click="reportMessage(message)"
                  >
                    {{ $t('gameChat.report') }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 分隔线 -->
        <div class="border-t border-gray-200 dark:border-gray-700"></div>
        
        <!-- 输入区域 -->
        <div class="p-4 bg-white dark:bg-gray-800">
          <!-- 未登录提示 -->
          <div v-if="!isAuthenticated" class="text-center py-4">
            <p class="text-gray-600 dark:text-gray-400 mb-2">
              {{ $t('gameChat.loginRequired') }}
            </p>
            <el-button type="primary" @click="goToLogin">
              {{ $t('gameChat.login') }}
            </el-button>
          </div>
          
          <!-- 未绑定游戏账号提示 -->
          <div v-else-if="!hasMinecraftAccount" class="text-center py-4">
            <p class="text-gray-600 dark:text-gray-400 mb-2">
              {{ $t('gameChat.minecraftAccountRequired') }}
            </p>
            <el-button type="primary" @click="goToSettings">
              {{ $t('gameChat.bindAccount') }}
            </el-button>
          </div>
          
          <!-- 正常输入框 -->
          <div v-else class="space-y-3">
            <!-- 回复提示 -->
            <div v-if="replyingTo" class="flex items-center justify-between bg-blue-50 dark:bg-blue-900/20 p-2 rounded">
                <div class="flex items-center gap-2">
                <i class="i-ep-chat-line-round"></i>
                <span class="text-sm">
                  {{ $t('gameChat.replyingTo') }} @{{ replyingTo }}
                </span>
              </div>
              <el-button type="text" size="small" @click="cancelReply">
                {{ $t('common.cancel') }}
              </el-button>
            </div>
            
            <!-- 输入框 -->
            <div class="flex gap-3">
              <div class="flex-1">
                <el-input
                  v-model="messageInput"
                  type="textarea"
                  :rows="3"
                  :placeholder="inputPlaceholder"
                  :maxlength="500"
                  show-word-limit
                  resize="none"
                  @keydown.enter.exact.prevent="sendMessage"
                />
              </div>
              
              <div class="flex flex-col gap-2">
                <el-button
                  type="primary"
                  :loading="sending"
                  :disabled="!messageInput.trim()"
                  @click="sendMessage"
                >
                  {{ $t('gameChat.send') }}
                </el-button>
                
                <el-button
                  v-if="showQuickActions"
                  type="text"
                  size="small"
                  @click="toggleQuickActions"
                >
                  {{ $t('gameChat.quickActions') }}
                </el-button>
              </div>
            </div>
            
            <!-- 快速操作 -->
            <div v-if="showQuickActionsPanel" class="grid grid-cols-2 md:grid-cols-4 gap-2">
              <el-button
                v-for="action in quickActions"
                :key="action.text"
                size="small"
                @click="insertQuickAction(action.text)"
              >
                {{ action.text }}
              </el-button>
            </div>
            
            <!-- 发送提示 -->
            <div class="text-xs text-gray-500">
              {{ $t('gameChat.sendHint') }}
            </div>
          </div>
        </div>
      </div>
      
      <!-- 在线用户列表 -->
      <div class="mt-6">
        <h3 class="text-lg font-semibold mb-3">{{ $t('gameChat.onlinePlayers') }}</h3>
        <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-3">
          <div
            v-for="player in onlinePlayers"
            :key="player.id"
            class="flex items-center gap-2 p-2 rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer"
            @click="whisperTo(player.username)"
          >
            <UserAvatar
              :avatar="player.avatar"
              :username="player.username"
              size="sm"
            />
            <span class="text-sm truncate">{{ player.username }}</span>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import { useGameChatStore } from '@/stores/gameChat'
import { ElMessage, ElMessageBox } from 'element-plus'
// Using UnoCSS icons (preset-icons). Removed Element Plus icon imports.
import AppLayout from '@/components/layout/AppLayout.vue'
import UserAvatar from '@/components/user/UserAvatar.vue'
import { useI18n } from 'vue-i18n'

// 国际化
const { t } = useI18n()
const router = useRouter()

// Store
const authStore = useAuthStore()
const userStore = useUserStore()

// 状态
const messagesContainer = ref<HTMLElement>()
const loadingMessages = ref(false)
const sending = ref(false)
const messageInput = ref('')
const replyingTo = ref('')
const showQuickActionsPanel = ref(false)
const selectedServer = ref('')
const selectedChannel = ref('world')
const selectedWorld = ref('all')
const showWorldSelector = computed(() => selectedChannel.value === 'world')

// 模拟数据
const servers = ref([
  { id: 'server1', name: t('gameChat.servers.main'), players: 42 },
  { id: 'server2', name: t('gameChat.servers.test'), players: 12 },
  { id: 'server3', name: t('gameChat.servers.dev'), players: 5 }
])

const channels = ref([
  { id: 'global', name: t('gameChat.channels.global') },
  { id: 'world', name: t('gameChat.channels.world') },
  { id: 'party', name: t('gameChat.channels.party') },
  { id: 'whisper', name: t('gameChat.channels.whisper') },
  { id: 'staff', name: t('gameChat.channels.staff') }
])

const worlds = ref([
  { id: 'all', name: t('gameChat.worlds.all') },
  { id: 'world', name: t('gameChat.worlds.world') },
  { id: 'apocalypse', name: t('gameChat.worlds.apocalypse') },
  { id: 'resource', name: t('gameChat.worlds.resource') }
])

const messages = ref([
  {
    id: '1',
    player: {
      id: 'player1',
      username: 'WekyJay',
      avatar: 'https://example.com/avatar1.jpg'
    },
    content: t('gameChat.sampleMessages.1'),
    timestamp: Date.now() - 300000, // 5分钟前
    channel: 'world'
  },
  {
    id: '2',
    player: {
      id: 'player2',
      username: 'MinecraftFan',
      avatar: 'https://example.com/avatar2.jpg'
    },
    content: t('gameChat.sampleMessages.2'),
    timestamp: Date.now() - 180000, // 3分钟前
    channel: 'world'
  },
  {
    id: '3',
    player: {
      id: 'player3',
      username: 'BuilderPro',
      avatar: 'https://example.com/avatar3.jpg'
    },
    content: t('gameChat.sampleMessages.3'),
    timestamp: Date.now() - 60000, // 1分钟前
    channel: 'global'
  }
])

const onlinePlayers = ref([
  { id: 'player1', username: 'WekyJay', avatar: 'https://example.com/avatar1.jpg' },
  { id: 'player2', username: 'MinecraftFan', avatar: 'https://example.com/avatar2.jpg' },
  { id: 'player3', username: 'BuilderPro', avatar: 'https://example.com/avatar3.jpg' },
  { id: 'player4', username: 'RedstoneWizard', avatar: 'https://example.com/avatar4.jpg' },
  { id: 'player5', username: 'AdventureTime', avatar: 'https://example.com/avatar5.jpg' },
  { id: 'player6', username: 'FarmExpert', avatar: 'https://example.com/avatar6.jpg' }
])

const quickActions = ref([
  { text: '/help' },
  { text: '/list' },
  { text: '/spawn' },
  { text: '/home' },
  { text: '/tpa' },
  { text: '/msg' },
  { text: t('gameChat.quickActions.hello') },
  { text: t('gameChat.quickActions.thanks') }
])

// 计算属性
const isAuthenticated = computed(() => authStore.isAuthenticated)
const hasMinecraftAccount = computed(() => userStore.user?.minecraftAccount != null)
const canReply = computed(() => isAuthenticated.value && hasMinecraftAccount.value)
const canReport = computed(() => isAuthenticated.value)

const inputPlaceholder = computed(() => {
  if (!isAuthenticated.value) return t('gameChat.placeholders.loginRequired')
  if (!hasMinecraftAccount.value) return t('gameChat.placeholders.bindAccountRequired')
  if (replyingTo.value) return t('gameChat.placeholders.replying', { username: replyingTo.value })
  return t('gameChat.placeholders.default')
})

const showQuickActions = computed(() => isAuthenticated.value && hasMinecraftAccount.value)

// 方法
const formatTime = (timestamp: number) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const getChannelName = (channelId: string) => {
  const channel = channels.value.find(c => c.id === channelId)
  return channel?.name || channelId
}

const replyTo = (username: string) => {
  replyingTo.value = username
  messageInput.value = `@${username} `
  nextTick(() => {
    const textarea = document.querySelector('textarea') as HTMLTextAreaElement
    textarea?.focus()
  })
}

const cancelReply = () => {
  replyingTo.value = ''
}

const whisperTo = (username: string) => {
  selectedChannel.value = 'whisper'
  replyTo(username)
}

const toggleQuickActions = () => {
  showQuickActionsPanel.value = !showQuickActionsPanel.value
}

const insertQuickAction = (text: string) => {
  messageInput.value += text + ' '
}

const onServerChange = () => {
  // 切换服务器时重新加载消息
  loadMessages()
}

const onChannelChange = () => {
  // 切换频道时重新加载消息
  loadMessages()
}

const onWorldChange = () => {
  // 切换世界时重新加载消息
  loadMessages()
}

const loadMessages = async () => {
  loadingMessages.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    // 实际实现中这里会调用API获取消息
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error(t('gameChat.errors.loadFailed'))
  } finally {
    loadingMessages.value = false
  }
}

const sendMessage = async () => {
  if (!messageInput.value.trim() || !isAuthenticated.value || !hasMinecraftAccount.value) {
    return
  }

  sending.value = true
  try {
    const content = messageInput.value.trim()
      const newMessage = {
        id: Date.now().toString(),
        player: {
          id: userStore.userId.toString() || '',
          username: userStore.user?.username || 'Anonymous',
          avatar: userStore.user?.avatar || ''
        },
        content: content,
        timestamp: Date.now(),
        channel: selectedChannel.value
      }

    // 模拟WebSocket发送
    await new Promise(resolve => setTimeout(resolve, 300))
    
    // 添加到消息列表
    messages.value.push(newMessage)
    
    // 清空输入
    messageInput.value = ''
    cancelReply()
    
    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
    
    ElMessage.success(t('gameChat.sendSuccess'))
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error(t('gameChat.errors.sendFailed'))
  } finally {
    sending.value = false
  }
}

const reportMessage = async (message: any) => {
  try {
    await ElMessageBox.confirm(
      t('gameChat.reportConfirm', { username: message.player.username }),
      t('gameChat.report'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    // 模拟举报
    await new Promise(resolve => setTimeout(resolve, 300))
    ElMessage.success(t('gameChat.reportSuccess'))
  } catch {
    // 用户取消
  }
}

const goToLogin = () => {
  router.push('/login')
}

const goToSettings = () => {
  router.push('/settings')
}

const handleScroll = () => {
  // 实现滚动加载更多消息
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 生命周期
onMounted(() => {
  // 初始化选择第一个服务器
  if (servers.value.length > 0) {
    selectedServer.value = servers.value[0].id
  }
  
  // 加载消息
  loadMessages()
  
  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
  
  // 模拟WebSocket连接
  // 实际实现中这里会建立WebSocket连接
})

onUnmounted(() => {
  // 清理WebSocket连接
})

// 监听用户状态变化
watch(() => authStore.isAuthenticated, (isAuth) => {
  if (!isAuth) {
    // 用户登出时清空消息
    messages.value = []
  }
})
</script>

<style scoped>
.flex-1 {
  min-height: 400px;
}

.overflow-y-auto {
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 transparent;
}

.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background-color: #c1c1c1;
  border-radius: 3px;
}

.dark .overflow-y-auto::-webkit-scrollbar-thumb {
  background-color: #4b5563;
}
</style>
