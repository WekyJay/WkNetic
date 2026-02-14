<template>
  <div class="flex flex-col p-16">
    <!-- 页头 -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
      <div>
        <div class="flex items-center gap-3">
          <h1 class="text-2xl font-bold">{{ $t('gameChat.title') }}</h1>
          <!-- WebSocket连接状态指示器 -->
          <div v-if="isWebSocketConnected" class="flex items-center gap-1 text-xs px-2 py-1 rounded-full bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200">
            <div class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
            <span>{{ $t('gameChat.websocket.connected') }}</span>
          </div>
          <div v-else class="flex items-center gap-1 text-xs px-2 py-1 rounded-full bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200">
            <div class="w-2 h-2 rounded-full bg-red-500"></div>
            <span>{{ $t('gameChat.websocket.disconnected') }}</span>
            <el-button v-if="!loadingMessages" link size="small" @click="reconnectWebSocket">
              {{ $t('gameChat.websocket.reconnect') }}
            </el-button>
          </div>
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
          {{ $t('gameChat.subtitle') }}
        </p>
      </div>
      
      <div class="flex items-center gap-3">
        <!-- 服务器选择 -->
        <div class="flex items-center gap-2">
          <span class="text-sm text-gray-600 dark:text-gray-400">{{ $t('gameChat.server') }}:</span>
        <el-select
          v-model="selectedServerId"
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

    <div class="flex flex-col flex-1" style="height: calc(30vh);">
      <!-- 聊天区域 -->
      <div class="flex-1 flex flex-col border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden">
        <!-- 聊天消息列表 -->
        <div
          ref="messagesContainer"
          class="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50 dark:bg-gray-900 max-h-[600px]"
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
              class="flex min-h-20 gap-3 p-3 pb-0 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
            >
              <!-- 玩家头像 -->
              <div class="flex-shrink-0">
                <UserAvatar
                  :src="message.player.avatar"
                  :username="message.player.username"
                  size="md"
                />
              </div>
              
              <!-- 消息内容 -->
              <div class="min-w-0">
                <div class="flex items-baseline gap-2">
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
                <div class="flex h-6 gap-2 opacity-0 hover:opacity-100 transition-opacity">
                  <el-text
                    v-if="canReply"
                    style="cursor: pointer;"
                    type="info"
                    size="small"
                    @click="replyTo(message.player.username)"
                  >
                    {{ $t('gameChat.reply') }}
                  </el-text>
                  <el-text
                    v-if="canReport"
                    type="info"
                    style="cursor: pointer;"
                    size="small"
                    @click="reportMessage(message)"
                  >
                    {{ $t('gameChat.report') }}
                  </el-text>
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
              <el-button link size="small" @click="cancelReply">
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
              </div>
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
            :key="player.uuid"
            class="flex items-center gap-2 p-2 rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer"
            @click="whisperTo(player.username)"
          >
            <UserAvatar
              :avatar="getMinecraftAvatarUrl(player.uuid)"
              :username="player.username"
              size="sm"
            />
            <span class="text-sm truncate">{{ player.username }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useGameChatStore } from '@/stores/gameChat'
import { ElMessage, ElMessageBox } from 'element-plus'
import UserAvatar from '@/components/user/UserAvatar.vue'
import { useI18n } from 'vue-i18n'
import { getMinecraftAvatarUrl } from '@/utils/minecraft'
import { gameChatApi, type ServerInfo, type ChannelInfo, type WorldInfo } from '@/api/gameChat'

// 国际化
const { t } = useI18n()
const router = useRouter()

// Store
const authStore = useAuthStore()
const gameChatStore = useGameChatStore()

// 状态
const messagesContainer = ref<HTMLElement>()
const messageInput = ref('')
const replyingTo = ref('')
const selectedServerId = ref('')
const selectedChannel = ref<'global' | 'world' | 'party' | 'whisper' | 'staff'>('global')
const selectedWorld = ref('all')
const showWorldSelector = computed(() => selectedChannel.value === 'world')

// 从store获取状态
const loadingMessages = computed(() => gameChatStore.isLoading)
const sending = computed(() => gameChatStore.isSending)
const messages = computed(() => gameChatStore.filteredMessages)

// 服务器、频道、世界列表（从API获取）
const servers = ref<ServerInfo[]>([])
const channels = ref<ChannelInfo[]>([])
const worlds = ref<WorldInfo[]>([])

// 加载状态
const loadingServers = ref(false)
const loadingChannels = ref(false)
const loadingWorlds = ref(false)
const loadingError = ref<string | null>(null)

// 从store获取在线玩家
const onlinePlayers = computed(() => gameChatStore.onlinePlayers)

// 计算属性
const isAuthenticated = computed(() => authStore.isAuthenticated)
const hasMinecraftAccount = computed(() => authStore.user?.minecraftUsername != null)
const canReply = computed(() => gameChatStore.hasPermissionToSpeak)
const canReport = computed(() => gameChatStore.hasPermissionToView)
const isWebSocketConnected = computed(() => gameChatStore.isWebSocketConnected)

// 获取选中的服务器名称
const selectedServerName = computed(() => {
  const server = servers.value.find(s => s.id === selectedServerId.value)
  return server?.name || ''
})

const inputPlaceholder = computed(() => {
  if (!isAuthenticated.value) return t('gameChat.placeholders.loginRequired')
  if (!hasMinecraftAccount.value) return t('gameChat.placeholders.bindAccountRequired')
  if (replyingTo.value) return t('gameChat.placeholders.replying', { username: replyingTo.value })
  return t('gameChat.placeholders.default')
})

// 方法
const formatTime = (timestamp: number) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const getChannelName = (channelId: string) => {
  const channel = channels.value.find(c => c.id === channelId)
  return channel?.name || channelId
}

// API调用函数
const fetchServers = async () => {
  loadingServers.value = true
  try {
    const res = await gameChatApi.getServers()
    if (res.status === 200 && res.data) {
      servers.value = res.data
    } else {
      loadingError.value = '获取服务器列表失败'
      ElMessage.error(loadingError.value)
    }
  } catch (error: any) {
    console.error('Failed to fetch servers:', error)
    loadingError.value = '获取服务器列表失败：' + (error.message || '网络错误')
    ElMessage.error(loadingError.value)
  } finally {
    loadingServers.value = false
  }
}

const fetchChannels = async () => {
  loadingChannels.value = true
  try {
    const res = await gameChatApi.getChannels()
    if (res.status === 200 && res.data) {
      channels.value = res.data
    } else {
      loadingError.value = '获取频道列表失败'
      ElMessage.error(loadingError.value)
    }
  } catch (error: any) {
    console.error('Failed to fetch channels:', error)
    loadingError.value = '获取频道列表失败：' + (error.message || '网络错误')
    ElMessage.error(loadingError.value)
  } finally {
    loadingChannels.value = false
  }
}

const fetchWorlds = async () => {
  loadingWorlds.value = true
  try {
    const res = await gameChatApi.getWorlds()
    if (res.status === 200 && res.data) {
      worlds.value = res.data
    } else {
      loadingError.value = '获取世界列表失败'
      ElMessage.error(loadingError.value)
    }
  } catch (error: any) {
    console.error('Failed to fetch worlds:', error)
    loadingError.value = '获取世界列表失败：' + (error.message || '网络错误')
    ElMessage.error(loadingError.value)
  } finally {
    loadingWorlds.value = false
  }
}

// 初始化数据
const initializeData = async () => {
  loadingError.value = null
  
  // 并行加载所有数据
  await Promise.all([
    fetchServers(),
    fetchChannels(),
    fetchWorlds()
  ])
  
  // 初始化选择第一个服务器
  if (servers.value.length > 0) {
    selectedServerId.value = servers.value[0].id
    const sessionId = servers.value[0].id
    await gameChatStore.switchServer(servers.value[0].name, sessionId)
    console.info(`默认选择服务器: ${servers.value[0].name} (sessionId: ${sessionId})`)
  } else {
    // 如果没有服务器，显示警告
    ElMessage.warning('未找到可用服务器')
  }
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

const onServerChange = async () => {
  if (!selectedServerId.value) return
  
  // 获取选中的服务器信息
  const selectedServer = servers.value.find(s => s.id === selectedServerId.value)
  if (!selectedServer) return
  
  // 获取服务器会话ID，优先使用sessionId字段，否则使用id作为备用
  const sessionId = selectedServer.id
  console.info(`切换服务器: ${selectedServer.name} (sessionId: ${sessionId})`)
  
  // 传递服务器名称和sessionId
  await gameChatStore.switchServer(selectedServer.name, sessionId)
  nextTick(() => {
    scrollToBottom()
  })
}

const onChannelChange = async () => {
  await gameChatStore.switchChannel(selectedChannel.value)
  nextTick(() => {
    scrollToBottom()
  })
}

const onWorldChange = async () => {
  await gameChatStore.switchWorld(selectedWorld.value)
  nextTick(() => {
    scrollToBottom()
  })
}

const sendMessage = async () => {
  if (!messageInput.value.trim()) {
    return
  }

  const success = await gameChatStore.sendMessage(messageInput.value.trim())
  
  if (success) {
    // 清空输入
    messageInput.value = ''
    cancelReply()
    
    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
  }
}

const reconnectWebSocket = () => {
  gameChatStore.reconnectWebSocket()
  ElMessage.info('正在重新连接WebSocket...')
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

// 新消息到达后自动滚动到底部
watch(
  () => messages.value.length,
  () => {
    nextTick(() => {
      scrollToBottom()
    })
  }
)

// 生命周期
onMounted(async () => {
  // 检查登录状态
  if (!authStore.isAuthenticated) {
    ElMessage.warning(t('gameChat.loginRequired'))
    router.push('/login')
    return
  }

  // 初始化数据（服务器、频道、世界列表）
  await initializeData()

  // 检查是否有可用的服务器数据
  if (servers.value.length > 0) {
    selectedServerId.value = servers.value[0].id
    
    // 加载消息
    await gameChatStore.loadChatHistory(selectedServerName.value, selectedChannel.value, selectedWorld.value)
    
    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
  } else if (!loadingServers.value && !loadingChannels.value && !loadingWorlds.value) {
    // 如果数据加载完成但没有服务器，显示提示
    ElMessage.warning('没有可用的服务器数据，请稍后再试')
  }
})

onUnmounted(() => {
  // 断开连接并清理
  gameChatStore.disconnect()
})

// 监听用户状态变化
watch(() => authStore.isAuthenticated, (isAuth) => {
  if (!isAuth) {
    // 用户登出时断开连接
    gameChatStore.disconnect()
    router.push('/login')
  }
})
</script>

<style scoped>


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
