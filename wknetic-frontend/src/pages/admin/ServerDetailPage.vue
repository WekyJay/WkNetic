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
            <div class="text-2xl font-semibold text-text">{{ server.onlinePlayers }} / {{ server.maxPlayers }}</div>
            <div class="text-sm text-text-secondary mt-1">在线玩家</div>
          </div>
        </div>

        <div class="card flex items-center gap-4">
          <i class="i-carbon-dashboard text-4xl text-green-500 dark:text-green-400" />
          <div>
            <div class="text-2xl font-semibold text-text">{{ server.tps?.toFixed(2) || 'N/A' }}</div>
            <div class="text-sm text-text-secondary mt-1">TPS</div>
          </div>
        </div>

        <div class="card flex items-center gap-4">
          <i class="i-carbon-chip text-4xl text-purple-500 dark:text-purple-400" />
          <div>
            <div class="text-2xl font-semibold text-text">
              {{ formatMemory(server.ramUsage) }} / {{ formatMemory(server.maxRam) }}
            </div>
            <div class="text-sm text-text-secondary mt-1">内存使用</div>
          </div>
        </div>

        <div class="card flex items-center gap-4">
          <i class="i-carbon-plugin text-4xl text-orange-500 dark:text-orange-400" />
          <div>
            <div class="text-2xl font-semibold text-text">{{ server.pluginList?.length || 0 }}</div>
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
          <p class="text-text-secondary">暂无在线玩家</p>
        </div>
        <div v-else class="overflow-x-auto">
          <table class="w-full border-collapse">
            <thead>
              <tr class="bg-bg-raised">
                <th class="px-3 py-3 text-left text-sm font-semibold text-text border-b border-border">玩家名称</th>
                <th class="px-3 py-3 text-left text-sm font-semibold text-text border-b border-border">UUID</th>
                <th class="px-3 py-3 text-left text-sm font-semibold text-text border-b border-border">延迟</th>
                <th class="px-3 py-3 text-left text-sm font-semibold text-text border-b border-border">世界</th>
                <th class="px-3 py-3 text-left text-sm font-semibold text-text border-b border-border">游戏模式</th>
                <th class="px-3 py-3 text-left text-sm font-semibold text-text border-b border-border">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="player in server.playerList" :key="player.uuid" class="hover:bg-bg-hover transition-colors">
                <td class="px-3 py-3 text-sm text-text border-b border-border">{{ player.name }}</td>
                <td class="px-3 py-3 text-sm text-text-secondary border-b border-border">
                  <code class="px-2 py-1 bg-bg-raised rounded text-xs font-mono">{{ player.uuid.substring(0, 8) }}...</code>
                </td>
                <td class="px-3 py-3 text-sm text-text border-b border-border">{{ player.ping }}ms</td>
                <td class="px-3 py-3 text-sm text-text border-b border-border">{{ player.world }}</td>
                <td class="px-3 py-3 text-sm text-text border-b border-border">{{ player.gameMode }}</td>
                <td class="px-3 py-3 text-sm border-b border-border">
                  <button 
                    class="px-3 py-1 mr-2 border border-blue-500 dark:border-blue-400 bg-transparent text-blue-500 dark:text-blue-400 rounded cursor-pointer text-xs hover:(bg-blue-500 dark:bg-blue-400 text-white) transition-colors"
                    @click="kickPlayer(player.name)"
                  >
                    踢出
                  </button>
                  <button 
                    class="px-3 py-1 border border-red-500 dark:border-red-400 bg-transparent text-red-500 dark:text-red-400 rounded cursor-pointer text-xs hover:(bg-red-500 dark:bg-red-400 text-white) transition-colors"
                    @click="banPlayer(player.name)"
                  >
                    封禁
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 插件列表 -->
      <div v-show="activeTab === 'plugins'" class="card">
        <div v-if="!server.pluginList || server.pluginList.length === 0" class="flex-col-center py-15">
          <p class="text-text-secondary">暂无插件</p>
        </div>
        <div v-else class="overflow-x-auto">
          <table class="w-full border-collapse">
            <thead>
              <tr class="bg-bg-raised">
                <th class="px-3 py-3 text-left text-sm font-semibold text-text border-b border-border">插件名称</th>
                <th class="px-3 py-3 text-left text-sm font-semibold text-text border-b border-border">版本</th>
                <th class="px-3 py-3 text-left text-sm font-semibold text-text border-b border-border">状态</th>
                <th class="px-3 py-3 text-left text-sm font-semibold text-text border-b border-border">作者</th>
                <th class="px-3 py-3 text-left text-sm font-semibold text-text border-b border-border">描述</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="plugin in server.pluginList" :key="plugin.name" class="hover:bg-bg-hover transition-colors">
                <td class="px-3 py-3 text-sm font-semibold text-text border-b border-border">{{ plugin.name }}</td>
                <td class="px-3 py-3 text-sm text-text border-b border-border">{{ plugin.version }}</td>
                <td class="px-3 py-3 text-sm border-b border-border">
                  <span 
                    :class="[
                      'badge',
                      plugin.enabled 
                        ? 'bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-400' 
                        : 'bg-red-50 dark:bg-red-900/30 text-red-700 dark:text-red-400'
                    ]"
                  >
                    {{ plugin.enabled ? '启用' : '禁用' }}
                  </span>
                </td>
                <td class="px-3 py-3 text-sm text-text-secondary border-b border-border">{{ plugin.author || '-' }}</td>
                <td class="px-3 py-3 text-sm text-text-secondary border-b border-border">{{ plugin.description || '-' }}</td>
              </tr>
            </tbody>
          </table>
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

const route = useRoute()
const router = useRouter()
const { getServerByToken } = useServerMonitor()

const token = route.params.token as string
const server = computed(() => getServerByToken(token))

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

const kickPlayer = async (playerName: string) => {
  if (!confirm(`确定要踢出玩家 ${playerName} 吗？`)) return

  try {
    await sendCommand({
      token,
      commandType: 'KICK',
      targetPlayer: playerName,
      reason: '管理员踢出'
    })
    alert('命令已发送')
  } catch (error) {
    console.error('发送命令失败:', error)
    alert('操作失败')
  }
}

const banPlayer = async (playerName: string) => {
  if (!confirm(`确定要封禁玩家 ${playerName} 吗？`)) return

  const reason = prompt('封禁原因:')
  if (!reason) return

  try {
    await sendCommand({
      token,
      commandType: 'BAN',
      targetPlayer: playerName,
      reason
    })
    alert('命令已发送')
  } catch (error) {
    console.error('发送命令失败:', error)
    alert('操作失败')
  }
}

const executeCommand = async () => {
  if (!commandInput.value.trim()) return

  try {
    await sendCommand({
      token,
      commandType: 'COMMAND',
      command: commandInput.value
    })
    alert('命令已发送')
    commandInput.value = ''
  } catch (error) {
    console.error('发送命令失败:', error)
    alert('操作失败')
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
