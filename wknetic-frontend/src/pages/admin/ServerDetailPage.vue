<template>
  <div class="p-5">
    <div class="flex items-center gap-4 mb-8">
      <button class="btn-secondary gap-2" @click="goBack">
        <i class="i-carbon-arrow-left" /> 返回
      </button>
      <h2 class="text-2xl font-semibold m-0 text-text">{{ server?.serverName || '服务器详情' }}</h2>
    </div>

    <div v-if="!server" class="flex-col-center min-h-100">
      <p class="text-text-secondary">服务器离线或不存在</p>
    </div>

    <template v-else>
      <!-- 服务器基本信息 -->
      <div class="grid grid-cols-[repeat(auto-fit,minmax(200px,1fr))] gap-5 mb-8">
        <div class="card flex items-center gap-4">
          <i class="i-carbon-user text-4xl text-blue-500 dark:text-blue-400" />
          <div>
            <div class="text-xl font-semibold text-text">{{ server.onlinePlayers }} / {{ server.maxPlayers }}</div>
            <div class="text-sm text-text-secondary mt-1">在线玩家</div>
          </div>
        </div>

        <div class="card flex items-center gap-4">
          <i class="i-carbon-dashboard text-4xl text-green-500 dark:text-green-400" />
          <div>
            <div class="text-xl font-semibold text-text">{{ server.tps?.toFixed(2) || 'N/A' }}</div>
            <div class="text-sm text-text-secondary mt-1">TPS</div>
          </div>
        </div>

        <div class="card flex items-center gap-4">
          <i class="i-carbon-chip text-4xl text-purple-500 dark:text-purple-400" />
          <div>
            <div class="text-xl font-semibold text-text">
              {{ formatMemory(server.ramUsage) }} / {{ formatMemory(server.maxRam) }}
            </div>
            <div class="text-sm text-text-secondary mt-1">内存使用</div>
          </div>
        </div>

        <div class="card flex items-center gap-4">
          <i class="i-carbon-plug text-4xl text-orange-500 dark:text-orange-400" />
          <div>
            <div class="text-xl font-semibold text-text">{{ server.pluginList?.length || 0 }}</div>
            <div class="text-sm text-text-secondary mt-1">插件数量</div>
          </div>
        </div>
      </div>

      <!-- 标签页 -->
      <div class="flex gap-1 border-b-2 border-border mb-5">
        <button
          :class="[
            'px-6 py-3 bg-transparent border-none cursor-pointer text-sm font-medium transition-colors',
            'border-b-2 -mb-0.5',
            activeTab === 'players' 
              ? 'text-brand border-brand' 
              : 'text-text-secondary border-transparent hover:text-text'
          ]"
          @click="activeTab = 'players'"
        >
          在线玩家
        </button>
        <button
          :class="[
            'px-6 py-3 bg-transparent border-none cursor-pointer text-sm font-medium transition-colors',
            'border-b-2 -mb-0.5',
            activeTab === 'plugins' 
              ? 'text-brand border-brand' 
              : 'text-text-secondary border-transparent hover:text-text'
          ]"
          @click="activeTab = 'plugins'"
        >
          插件列表
        </button>
        <button
          :class="[
            'px-6 py-3 bg-transparent border-none cursor-pointer text-sm font-medium transition-colors',
            'border-b-2 -mb-0.5',
            activeTab === 'console' 
              ? 'text-brand border-brand' 
              : 'text-text-secondary border-transparent hover:text-text'
          ]"
          @click="activeTab = 'console'"
        >
          控制台
        </button>
      </div>

      <!-- 在线玩家 -->
      <div v-show="activeTab === 'players'" class="card">
        <div v-if="!server.playerList || server.playerList.length === 0" class="flex-col-center py-15">
          <el-empty description="暂无在线玩家">
            <template #image>
              <i class="i-carbon-user-multiple text-6xl text-text-secondary" />
            </template>
          </el-empty>
        </div>
        <div v-else>
          <el-table :data="server.playerList" stripe style="width: 100%">
            <el-table-column prop="name" label="玩家名称" fixed="left" min-width="120">
              <template #default="{ row }">
                <div class="flex items-center gap-2">
                  <i class="i-carbon-user-avatar text-lg text-blue-500" />
                  <span class="font-medium">{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="uuid" label="UUID" min-width="220">
              <template #default="{ row }">
                <el-tag size="small" type="info">{{row.uuid}}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="ping" label="延迟" width="100">
              <template #default="{ row }">
                <div class="flex items-center gap-1">
                  <i 
                    :class="[
                      'i-carbon-wifi',
                      row.ping < 50 ? 'text-green-500' : row.ping < 100 ? 'text-yellow-500' : 'text-red-500'
                    ]"
                  />
                  <span>{{ row.ping }}ms</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="world" label="世界" width="150">
              <template #default="{ row }">
                <div class="flex items-center gap-1">
                  <i class="i-carbon-earth text-green-500" />
                  <span>{{ row.world }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="gameMode" label="游戏模式" width="150">
              <template #default="{ row }">
                <el-tag 
                  size="small" 
                  :type="getGameModeType(row.gameMode)"
                >
                  {{ row.gameMode }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="160" fixed="right">
              <template #default="{ row }">
                <el-button 
                  text
                  type="primary" 
                  @click="kickPlayer(row.name)"
                >
                  踢出
                </el-button>
                <el-button 
                  text
                  type="danger" 
                  @click="banPlayer(row.name)"
                >
                  封禁
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 插件列表 -->
      <div v-show="activeTab === 'plugins'" class="card">
        <div v-if="!server.pluginList || server.pluginList.length === 0" class="flex-col-center py-15">
          <el-empty description="暂无插件">
            <template #image>
              <i class="i-carbon-plug text-6xl text-text-secondary" />
            </template>
          </el-empty>
        </div>
        <div v-else>
          <el-table :data="server.pluginList" stripe style="width: 100%">
            <el-table-column prop="name" label="插件名称" min-width="150">
              <template #default="{ row }">
                <div class="flex items-center gap-2">
                  <i class="i-carbon-cube text-lg text-purple-500" />
                  <span class="font-semibold">{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="version" label="版本" min-width="150">
              <template #default="{ row }">
                <el-text>{{ row.version || '-' }}</el-text>
              </template>
            </el-table-column>
            <el-table-column prop="enabled" label="状态" width="120">
              <template #default="{ row }">
                <el-tag 
                  size="small" 
                  :type="row.enabled ? 'success' : 'danger'"
                >
                  <i :class="row.enabled ? 'i-carbon-checkmark-filled' : 'i-carbon-close-filled'" class="mr-1" />
                  {{ row.enabled ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="author" label="作者" min-width="200">
              <template #default="{ row }">
                <div v-if="row.author" class="flex flex-wrap gap-1">
                  <el-tag 
                    v-for="author in parseAuthors(row.author)" 
                    :key="author"
                    size="small"
                    type="info"
                  >
                    <i class="i-carbon-user mr-1" />
                    {{ author }}
                  </el-tag>
                </div>
                <span v-else class="text-text-secondary">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="250">
              <template #default="{ row }">
                <el-tooltip 
                  v-if="row.description" 
                  :content="row.description" 
                  placement="top"
                  :show-after="500"
                >
                  <div class="truncate text-text-secondary">
                    <i class="i-carbon-information mr-1" />
                    {{ row.description }}
                  </div>
                </el-tooltip>
                <span v-else class="text-text-secondary">-</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 控制台 -->
      <div v-show="activeTab === 'console'" class="card max-w-800px">
        <div class="mb-5">
          <label class="block mb-2 font-medium text-text">执行命令</label>
          <div class="flex gap-3">
            <input
              v-model="commandInput"
              type="text"
              placeholder="输入命令（不含斜杠）"
              class="input-base font-mono"
              @keyup.enter="executeCommand"
            />
            <button class="btn-primary" @click="executeCommand">执行</button>
          </div>
        </div>

        <div class="flex gap-2 flex-wrap">
          <button class="btn-secondary" @click="commandInput = 'say Hello'">
            发送消息
          </button>
          <button class="btn-secondary" @click="commandInput = 'list'">查看玩家</button>
          <button class="btn-secondary" @click="commandInput = 'tps'">查看TPS</button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useServerMonitor } from '@/composables/useServerMonitor'
import { sendCommand } from '@/api/serverMonitor'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const { getServerBySessionId } = useServerMonitor()

const sessionId = route.params.sessionId as string
const server = computed(() => getServerBySessionId(sessionId))

const activeTab = ref('players')
const commandInput = ref('')

const formatMemory = (mb: number) => {
  if (mb >= 1024) {
    return `${(mb / 1024).toFixed(1)} GB`
  }
  return `${mb} MB`
}

const goBack = () => {
  router.push('/admin/server-monitor')
}

// 解析作者信息，过滤掉链接
const parseAuthors = (authorString: string) => {
  if (!authorString) return []
  
  // 按分号分割
  const authors = authorString.split(/[,]/).map(item => item.trim()).filter(Boolean)
  
  // 过滤掉链接（包含http、www等）
  return authors.filter(author => {
    const lowerAuthor = author.toLowerCase()
    return !lowerAuthor.includes('http://') && 
           !lowerAuthor.includes('https://') && 
           !lowerAuthor.includes('www.') &&
           !lowerAuthor.startsWith('//') &&
           !/^[a-z0-9-]+\.[a-z]{2,}/.test(lowerAuthor) // 过滤域名格式
  })
}

// 获取游戏模式对应的标签类型
const getGameModeType = (gameMode: string) => {
  const modeMap: Record<string, any> = {
    'SURVIVAL': 'success',
    'CREATIVE': 'warning',
    'ADVENTURE': 'primary',
    'SPECTATOR': 'info'
  }
  return modeMap[gameMode?.toUpperCase()] || ''
}

const kickPlayer = async (playerName: string) => {
  try {
    await ElMessageBox.confirm(
      `确定要踢出玩家 ${playerName} 吗？`,
      '确认踢出',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await sendCommand({
      sessionId,
      commandType: 'KICK',
      targetPlayer: playerName,
      reason: '管理员踢出'
    })
    ElMessage.success('命令已发送')
  } catch (error) {
    if (error === 'cancel') return
    console.error('发送命令失败:', error)
    ElMessage.error('操作失败')
  }
}

const banPlayer = async (playerName: string) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `确定要封禁玩家 ${playerName} 吗？`,
      '封禁玩家',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入封禁原因',
        inputValidator: (value) => {
          if (!value || !value.trim()) {
            return '封禁原因不能为空'
          }
          return true
        }
      }
    )

    await sendCommand({
      sessionId,
      commandType: 'BAN',
      targetPlayer: playerName,
      reason
    })
    ElMessage.success('命令已发送')
  } catch (error) {
    if (error === 'cancel') return
    console.error('发送命令失败:', error)
    ElMessage.error('操作失败')
  }
}

const executeCommand = async () => {
  if (!commandInput.value.trim()) {
    ElMessage.warning('请输入命令')
    return
  }

  try {
    await sendCommand({
      sessionId,
      commandType: 'COMMAND',
      command: commandInput.value
    })
    ElMessage.success('命令已发送')
    commandInput.value = ''
  } catch (error) {
    console.error('发送命令失败:', error)
    ElMessage.error('操作失败')
  }
}
</script>

<style scoped>
.server-detail-page {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 30px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.btn-back:hover {
  background: #f5f5f5;
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.info-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.info-card i {
  font-size: 32px;
  color: #1976d2;
}

.info-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.info-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.tabs {
  display: flex;
  gap: 4px;
  border-bottom: 2px solid #e0e0e0;
  margin-bottom: 20px;
}

.tab-button {
  padding: 12px 24px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
}

.tab-button.active {
  color: #1976d2;
  border-bottom-color: #1976d2;
}

.tab-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table th {
  background: #f5f5f5;
  font-weight: 600;
}

.btn-action {
  padding: 4px 12px;
  margin-right: 8px;
  border: 1px solid #1976d2;
  background: white;
  color: #1976d2;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-action:hover {
  background: #1976d2;
  color: white;
}

.btn-action.btn-danger {
  border-color: #f44336;
  color: #f44336;
}

.btn-action.btn-danger:hover {
  background: #f44336;
  color: white;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.status-badge.active {
  background: #e8f5e9;
  color: #2e7d32;
}

.status-badge.inactive {
  background: #ffebee;
  color: #c62828;
}

.console-container {
  max-width: 800px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.command-input-group {
  display: flex;
  gap: 12px;
}

.command-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-family: monospace;
}

.common-commands {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.btn-primary,
.btn-secondary {
  padding: 8px 16px;
  border: none;
  background-color: transparent!important;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary {
  background: #1976d2;
  color: white;
}

.btn-secondary {
  background: #f5f5f5;
  color: var(--text-secondary);
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}
</style>
