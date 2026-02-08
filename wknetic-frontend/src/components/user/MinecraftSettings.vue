<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/user'
import { validateUuid, formatUuid, getMinecraftAvatarUrl, getUuidErrorMessage } from '@/utils/minecraft'
import type { MinecraftProfile, MicrosoftOAuthConfig, MicrosoftBindResult } from '@/api/user'
import WkButton from '@/components/common/WkButton.vue'
import WkInput from '@/components/common/WkInput.vue'
import WkCard from '@/components/common/WkCard.vue'
import WkAlert from '@/components/common/WkAlert.vue'
import WkLoading from '@/components/common/WkLoading.vue'

const { t } = useI18n()
const authStore = useAuthStore()

// 状态管理
const loading = ref(false)
const saving = ref(false)
const verifying = ref(false)
const message = ref('')
const messageType = ref<'success' | 'error' | 'warning' | 'info'>('info')

// Microsoft OAuth状态
const microsoftConfig = ref<MicrosoftOAuthConfig | null>(null)
const microsoftLoading = ref(false)
const microsoftAuthorizing = ref(false)
const oauthWindow = ref<Window | null>(null)

// 表单数据
const uuidInput = ref('')
const verificationResult = ref<MinecraftProfile | null>(null)

// 当前绑定的Minecraft账号
const currentMinecraft = computed(() => {
  if (!authStore.user) return null
  return {
    uuid: authStore.user.minecraftUuid,
    username: authStore.user.minecraftUsername
  }
})

// 验证UUID格式
const uuidError = computed(() => {
  if (!uuidInput.value) return ''
  return getUuidErrorMessage(uuidInput.value)
})

// 格式化显示的UUID
const formattedUuid = computed(() => {
  if (!uuidInput.value) return ''
  return formatUuid(uuidInput.value)
})

// 检查是否已配置Microsoft OAuth
const isMicrosoftConfigured = computed(() => {
  return microsoftConfig.value?.enabled && 
         microsoftConfig.value?.clientIdConfigured && 
         microsoftConfig.value?.clientSecretConfigured
})

// 验证Minecraft UUID
const verifyUuid = async () => {
  if (uuidError.value) return
  
  verifying.value = true
  message.value = ''
  
  try {
    const result = await userApi.validateMinecraftUuid(uuidInput.value)
    verificationResult.value = result.data
    
    if (result.data.valid) {
      messageType.value = 'success'
      message.value = `验证成功！玩家名称：${result.data.name}`
    } else {
      messageType.value = 'error'
      message.value = result.data.error || 'UUID验证失败'
    }
  } catch (error: any) {
    messageType.value = 'error'
    message.value = error.response?.data?.message || '验证失败，请重试'
    verificationResult.value = null
  } finally {
    verifying.value = false
  }
}

// 绑定Minecraft账号
const bindMinecraft = async () => {
  if (!verificationResult.value?.valid) {
    messageType.value = 'error'
    message.value = '请先验证有效的Minecraft UUID'
    return
  }
  
  saving.value = true
  
  try {
    // 调用新的绑定API（不需要userId参数）
    await userApi.bindMinecraftAccount(
      verificationResult.value.id,
      verificationResult.value.name
    )
    
    messageType.value = 'success'
    message.value = 'Minecraft账号绑定成功！'
    
    // 更新本地用户信息
    if (authStore.user) {
      authStore.user.minecraftUuid = verificationResult.value.id
      authStore.user.minecraftUsername = verificationResult.value.name
    }
    
    // 重置表单
    uuidInput.value = ''
    verificationResult.value = null
  } catch (error: any) {
    messageType.value = 'error'
    message.value = error.response?.data?.message || '绑定失败，请重试'
  } finally {
    saving.value = false
  }
}

// 解绑Minecraft账号
const unbindMinecraft = async () => {
  if (!confirm('确定要解绑Minecraft账号吗？')) return
  
  saving.value = true
  
  try {
    // 调用新的解绑API（不需要userId参数）
    await userApi.unbindMinecraftAccount()
    
    messageType.value = 'success'
    message.value = 'Minecraft账号已解绑'
    
    // 更新本地用户信息
    if (authStore.user) {
      authStore.user.minecraftUuid = undefined
      authStore.user.minecraftUsername = undefined
    }
  } catch (error: any) {
    messageType.value = 'error'
    message.value = error.response?.data?.message || '解绑失败，请重试'
  } finally {
    saving.value = false
  }
}

// 加载用户数据
const loadUserData = () => {
  // 数据已通过authStore获取
}

// 加载Microsoft OAuth配置
const loadMicrosoftConfig = async () => {
  microsoftLoading.value = true
  try {
    const result = await userApi.getMicrosoftConfig()
    microsoftConfig.value = result.data
  } catch (error: any) {
    console.error('Failed to load Microsoft OAuth config:', error)
    messageType.value = 'error'
    message.value = '无法加载Microsoft OAuth配置'
  } finally {
    microsoftLoading.value = false
  }
}

// 启动Microsoft OAuth授权流程
const startMicrosoftOAuth = async () => {
  if (!isMicrosoftConfigured.value) {
    messageType.value = 'error'
    message.value = 'Microsoft OAuth功能未配置，请联系管理员'
    return
  }
  
  microsoftAuthorizing.value = true
  try {
    // 获取授权URL
    const result = await userApi.getMicrosoftAuthorizationUrl()
    const authorizationUrl = result.data
    
    if (!authorizationUrl) {
      throw new Error('无法获取授权URL')
    }
    
    // 打开新窗口进行授权
    oauthWindow.value = window.open(
      authorizationUrl,
      'Microsoft OAuth',
      'width=600,height=700,menubar=no,toolbar=no,location=yes,resizable=yes,scrollbars=yes,status=yes'
    )
    
    if (!oauthWindow.value) {
      throw new Error('请允许弹出窗口进行授权')
    }
    
    // 监听窗口关闭
    const checkWindowClosed = setInterval(() => {
      if (oauthWindow.value?.closed) {
        clearInterval(checkWindowClosed)
        microsoftAuthorizing.value = false
        messageType.value = 'info'
        message.value = '授权窗口已关闭，如果授权成功，请等待绑定完成'
      }
    }, 1000)
    
  } catch (error: any) {
    microsoftAuthorizing.value = false
    messageType.value = 'error'
    message.value = error.response?.data?.message || error.message || '启动Microsoft授权失败'
  }
}

// 绑定Microsoft账户
const bindMicrosoftAccount = async () => {
  if (!isMicrosoftConfigured.value) {
    messageType.value = 'error'
    message.value = 'Microsoft OAuth功能未配置'
    return
  }
  
  microsoftLoading.value = true
  try {
    const result = await userApi.bindMicrosoftAccount()
    const bindResult = result.data
    
    if (bindResult.success) {
      messageType.value = 'success'
      message.value = bindResult.message
      
      // 如果绑定成功并且返回了Minecraft信息，更新本地用户信息
      if (bindResult.minecraftUuid && bindResult.minecraftUsername && authStore.user) {
        authStore.user.minecraftUuid = bindResult.minecraftUuid
        authStore.user.minecraftUsername = bindResult.minecraftUsername
      }
    } else {
      messageType.value = 'warning'
      message.value = bindResult.message || 'Microsoft账户绑定失败'
    }
  } catch (error: any) {
    messageType.value = 'error'
    message.value = error.response?.data?.message || '绑定Microsoft账户失败'
  } finally {
    microsoftLoading.value = false
  }
}

// 处理来自OAuth窗口的消息
const handleOAuthMessage = (event: MessageEvent) => {
  // 确保消息来自可信源
  if (event.origin !== window.location.origin) return
  
  const data = event.data
  
  if (data.type === 'microsoft_oauth_callback') {
    const { code, state, error, errorDescription } = data
    
    // 调用回调API
    userApi.microsoftOAuthCallback(code, state, error, errorDescription)
      .then(response => {
        messageType.value = 'success'
        message.value = response.data.message
        
        // 自动尝试绑定
        setTimeout(() => {
          bindMicrosoftAccount()
        }, 1000)
      })
      .catch(error => {
        messageType.value = 'error'
        message.value = error.response?.data?.message || 'OAuth回调处理失败'
      })
      .finally(() => {
        if (oauthWindow.value && !oauthWindow.value.closed) {
          oauthWindow.value.close()
        }
        microsoftAuthorizing.value = false
      })
  }
}

onMounted(() => {
  loadUserData()
  loadMicrosoftConfig()
  
  // 监听OAuth回调消息
  window.addEventListener('message', handleOAuthMessage)
})

onUnmounted(() => {
  // 清理事件监听器
  window.removeEventListener('message', handleOAuthMessage)
  
  // 关闭可能未关闭的OAuth窗口
  if (oauthWindow.value && !oauthWindow.value.closed) {
    oauthWindow.value.close()
  }
})
</script>

<template>
  <div class="minecraft-settings space-y-6">
    <!-- 提示消息 -->
    <WkAlert
      v-if="message"
      :type="messageType"
      :message="message"
      @close="message = ''"
    />
    
    <!-- 当前绑定状态 -->
    <WkCard>
      <div class="space-y-4">
        <h3 class="text-lg font-semibold text-text">当前绑定的Minecraft账号</h3>
        
        <div v-if="currentMinecraft?.uuid" class="space-y-4">
          <div class="flex items-center gap-4 p-4 bg-bg-raised rounded-lg">
            <!-- Minecraft头像 -->
            <div class="relative">
              <img
                :src="getMinecraftAvatarUrl(currentMinecraft.uuid, 64)"
                :alt="currentMinecraft.username || 'Minecraft玩家'"
                class="w-16 h-16 rounded-lg border-2 border-border"
                @error="(e: any) => e.target.src = '/default-avatar.png'"
              />
              <div class="absolute -bottom-1 -right-1 w-6 h-6 bg-green-500 rounded-full border-2 border-bg-raised flex items-center justify-center">
                <i class="i-tabler-check text-xs text-white" />
              </div>
            </div>
            
            <!-- 账号信息 -->
            <div class="flex-1">
              <div class="flex items-center gap-2">
                <h4 class="text-base font-medium text-text">
                  {{ currentMinecraft.username || '未知玩家' }}
                </h4>
                <span class="px-2 py-0.5 text-xs bg-brand/10 text-brand rounded">
                  已绑定
                </span>
              </div>
              <p class="text-sm text-text-secondary mt-1">
                UUID: {{ formatUuid(currentMinecraft.uuid) }}
              </p>
              <p class="text-xs text-text-muted mt-1">
                绑定时间: {{ authStore.user?.updateTime ? new Date(authStore.user.updateTime).toLocaleDateString() : '未知' }}
              </p>
            </div>
            
            <!-- 解绑按钮 -->
            <WkButton
              variant="ghost"
              size="sm"
              @click="unbindMinecraft"
              :loading="saving"
            >
              <i class="i-tabler-unlink" />
              <span>解绑</span>
            </WkButton>
          </div>
          
          <div class="text-sm text-text-secondary">
            <p>✅ 此账号已成功绑定到您的WkNetic账户</p>
            <p class="mt-1">您可以在游戏中使用此账号登录WkNetic相关服务</p>
          </div>
        </div>
        
        <div v-else class="text-center py-8 text-text-secondary">
          <i class="i-tabler-brand-minecraft text-4xl mb-3" />
          <p class="text-base">尚未绑定Minecraft账号</p>
          <p class="text-sm mt-1">绑定后可在游戏中使用WkNetic功能</p>
        </div>
      </div>
    </WkCard>
    
    <!-- 绑定新账号 -->
    <WkCard>
      <div class="space-y-6">
        <div>
          <h3 class="text-lg font-semibold text-text mb-2">绑定新的Minecraft账号</h3>
          <p class="text-sm text-text-secondary">
            输入您的Minecraft UUID进行验证和绑定
          </p>
        </div>
        
        <!-- UUID输入 -->
        <div class="space-y-3">
          <label class="block text-sm font-medium text-text">
            Minecraft UUID
          </label>
          <div class="flex gap-2">
            <WkInput
              v-model="uuidInput"
              placeholder="输入Minecraft UUID（32位十六进制）"
              :error="uuidError"
              class="flex-1"
            >
              <template #prefix>
                <i class="i-tabler-id text-text-secondary" />
              </template>
            </WkInput>
            <WkButton
              @click="verifyUuid"
              :disabled="!uuidInput || !!uuidError"
              :loading="verifying"
            >
              <i class="i-tabler-search" />
              <span>验证</span>
            </WkButton>
          </div>
          
          <!-- UUID格式提示 -->
          <div v-if="uuidInput" class="text-xs text-text-muted">
            <p>格式化UUID: {{ formattedUuid }}</p>
            <p class="mt-1">
              <i class="i-tabler-info-circle inline mr-1" />
              如何获取UUID？在Minecraft游戏中按F3+H显示高级提示，然后查看物品的NBT数据
            </p>
          </div>
          
          <!-- UUID错误提示 -->
          <div v-if="uuidError" class="text-sm text-red-500">
            <i class="i-tabler-alert-circle inline mr-1" />
            {{ uuidError }}
          </div>
        </div>
        
        <!-- 验证结果 -->
        <div v-if="verificationResult" class="space-y-4">
          <div class="p-4 rounded-lg border" :class="{
            'border-green-500 bg-green-50 dark:bg-green-900/20': verificationResult.valid,
            'border-red-500 bg-red-50 dark:bg-red-900/20': !verificationResult.valid
          }">
            <div class="flex items-center gap-3">
              <i
                :class="verificationResult.valid ? 'i-tabler-check text-green-500' : 'i-tabler-x text-red-500'"
                class="text-xl"
              />
              <div class="flex-1">
                <h4 class="font-medium" :class="verificationResult.valid ? 'text-green-700 dark:text-green-300' : 'text-red-700 dark:text-red-300'">
                  {{ verificationResult.valid ? '验证成功' : '验证失败' }}
                </h4>
                <p class="text-sm mt-1" :class="verificationResult.valid ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'">
                  {{ verificationResult.valid ? `玩家: ${verificationResult.name}` : verificationResult.error }}
                </p>
              </div>
            </div>
            
            <!-- 验证成功时的详细信息 -->
            <div v-if="verificationResult.valid" class="mt-4 pt-4 border-t border-green-200 dark:border-green-800">
              <div class="flex items-center gap-4">
                <img
                  :src="getMinecraftAvatarUrl(verificationResult.id, 48)"
                  :alt="verificationResult.name"
                  class="w-12 h-12 rounded-lg border border-green-300 dark:border-green-700"
                  @error="(e: any) => e.target.src = '/default-avatar.png'"
                />
                <div>
                  <p class="text-sm font-medium text-green-700 dark:text-green-300">
                    {{ verificationResult.name }}
                  </p>
                  <p class="text-xs text-green-600 dark:text-green-400 mt-1">
                    UUID: {{ formatUuid(verificationResult.id) }}
                  </p>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 绑定按钮 -->
          <div v-if="verificationResult.valid" class="flex justify-end">
            <WkButton
              variant="primary"
              @click="bindMinecraft"
              :loading="saving"
            >
              <i class="i-tabler-link" />
              <span>绑定此账号</span>
            </WkButton>
          </div>
        </div>
        
        <!-- 使用说明 -->
        <div class="pt-4 border-t border-border">
          <h4 class="text-sm font-medium text-text mb-2">如何获取Minecraft UUID？</h4>
          <ul class="text-sm text-text-secondary space-y-1">
            <li class="flex items-start gap-2">
              <i class="i-tabler-number-1 text-brand mt-0.5" />
              <span>在Minecraft Java版中，按<code class="px-1 py-0.5 bg-bg-raised rounded text-xs">F3+H</code>开启高级提示</span>
            </li>
            <li class="flex items-start gap-2">
              <i class="i-tabler-number-2 text-brand mt-0.5" />
              <span>将鼠标悬停在任意物品上，查看NBT数据中的<code class="px-1 py-0.5 bg-bg-raised rounded text-xs">UUID</code>字段</span>
            </li>
            <li class="flex items-start gap-2">
              <i class="i-tabler-number-3 text-brand mt-0.5" />
              <span>也可以使用第三方网站如<code class="px-1 py-0.5 bg-bg-raised rounded text-xs">namemc.com</code>查询玩家UUID</span>
            </li>
          </ul>
          
          <div class="mt-4 p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg border border-blue-200 dark:border-blue-800">
            <div class="flex items-start gap-2">
              <i class="i-tabler-info-circle text-blue-500 mt-0.5" />
              <div class="text-sm text-blue-700 dark:text-blue-300">
                <p class="font-medium">注意：Microsoft账户支持</p>
                <p class="mt-1">如果您使用Microsoft账户登录Minecraft，请确保您输入的是正确的Java版UUID。Bedrock版支持即将推出。</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </WkCard>
    
    <!-- Microsoft账户绑定 -->
    <WkCard>
      <div class="space-y-6">
        <div class="flex items-center gap-3">
          <i class="i-tabler-brand-windows text-2xl text-blue-500" />
          <div>
            <h3 class="text-lg font-semibold text-text">Microsoft账户绑定</h3>
            <p class="text-sm text-text-secondary">支持Bedrock版和Microsoft账户登录</p>
          </div>
        </div>
        
        <!-- 加载状态 -->
        <WkLoading v-if="microsoftLoading" message="正在加载Microsoft OAuth配置..." />
        
        <!-- Microsoft OAuth配置状态 -->
        <div v-else-if="microsoftConfig" class="space-y-4">
          <!-- 配置状态提示 -->
          <div v-if="!isMicrosoftConfigured" class="p-4 rounded-lg border border-yellow-500 bg-yellow-50 dark:bg-yellow-900/20">
            <div class="flex items-start gap-2">
              <i class="i-tabler-alert-triangle text-yellow-500 mt-0.5" />
              <div class="flex-1">
                <h4 class="font-medium text-yellow-700 dark:text-yellow-300">Microsoft OAuth功能未配置</h4>
                <p class="text-sm text-yellow-600 dark:text-yellow-400 mt-1">
                  请联系管理员配置Microsoft Azure应用信息以启用Microsoft账户绑定功能。
                </p>
                <ul class="text-xs text-yellow-600 dark:text-yellow-400 mt-2 space-y-1">
                  <li class="flex items-center gap-1">
                    <i class="i-tabler-circle-dot text-xs" />
                    <span>启用状态: {{ microsoftConfig.enabled ? '已启用' : '已禁用' }}</span>
                  </li>
                  <li class="flex items-center gap-1">
                    <i class="i-tabler-circle-dot text-xs" />
                    <span>客户端ID: {{ microsoftConfig.clientIdConfigured ? '已配置' : '未配置' }}</span>
                  </li>
                  <li class="flex items-center gap-1">
                    <i class="i-tabler-circle-dot text-xs" />
                    <span>客户端密钥: {{ microsoftConfig.clientSecretConfigured ? '已配置' : '未配置' }}</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
          
          <!-- Microsoft账户绑定流程 -->
          <div v-else class="space-y-6">
            <!-- 当前绑定状态 -->
            <div class="space-y-3">
              <h4 class="text-sm font-medium text-text">当前绑定状态</h4>
              <div class="p-4 rounded-lg bg-bg-raised">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-3">
                    <i class="i-tabler-brand-minecraft text-xl text-green-500" />
                    <div>
                      <p class="text-sm font-medium text-text">
                        {{ currentMinecraft?.uuid ? '已绑定Minecraft账号' : '未绑定Minecraft账号' }}
                      </p>
                      <p class="text-xs text-text-secondary mt-1">
                        {{ currentMinecraft?.uuid ? 
                          `玩家: ${currentMinecraft.username}` : 
                          '通过Microsoft账户绑定Bedrock版或Java版账号' 
                        }}
                      </p>
                    </div>
                  </div>
                  <span v-if="currentMinecraft?.uuid" class="px-2 py-1 text-xs bg-green-500/10 text-green-600 dark:text-green-400 rounded">
                    已绑定
                  </span>
                  <span v-else class="px-2 py-1 text-xs bg-gray-500/10 text-gray-600 dark:text-gray-400 rounded">
                    未绑定
                  </span>
                </div>
              </div>
            </div>
            
            <!-- Microsoft绑定流程 -->
            <div class="space-y-4">
              <h4 class="text-sm font-medium text-text">绑定流程</h4>
              
              <div class="space-y-3">
                <div class="flex items-center gap-3 p-3 rounded-lg border border-border">
                  <div class="flex-shrink-0 w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
                    <span class="text-white font-medium">1</span>
                  </div>
                  <div class="flex-1">
                    <p class="text-sm font-medium text-text">Microsoft账户授权</p>
                    <p class="text-xs text-text-secondary mt-1">
                      点击下方按钮登录Microsoft账户并授权WkNetic访问您的Minecraft信息
                    </p>
                  </div>
                </div>
                
                <div class="flex items-center gap-3 p-3 rounded-lg border border-border">
                  <div class="flex-shrink-0 w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
                    <span class="text-white font-medium">2</span>
                  </div>
                  <div class="flex-1">
                    <p class="text-sm font-medium text-text">自动获取Minecraft信息</p>
                    <p class="text-xs text-text-secondary mt-1">
                      系统自动获取您的Minecraft UUID和玩家名称
                    </p>
                  </div>
                </div>
                
                <div class="flex items-center gap-3 p-3 rounded-lg border border-border">
                  <div class="flex-shrink-0 w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
                    <span class="text-white font-medium">3</span>
                  </div>
                  <div class="flex-1">
                    <p class="text-sm font-medium text-text">完成绑定</p>
                    <p class="text-xs text-text-secondary mt-1">
                      自动将Minecraft账号绑定到您的WkNetic账户
                    </p>
                  </div>
                </div>
              </div>
              
              <!-- 操作按钮 -->
              <div class="flex flex-col gap-3">
                <WkButton
                  variant="primary"
                  @click="startMicrosoftOAuth"
                  :loading="microsoftAuthorizing"
                  :disabled="!isMicrosoftConfigured"
                >
                  <i class="i-tabler-brand-windows" />
                  <span>{{ microsoftAuthorizing ? '正在授权...' : '使用Microsoft账户绑定' }}</span>
                </WkButton>
                
                <WkButton
                  variant="ghost"
                  @click="bindMicrosoftAccount"
                  :loading="microsoftLoading"
                  :disabled="!isMicrosoftConfigured"
                  class="mt-2"
                >
                  <i class="i-tabler-refresh" />
                  <span>手动完成绑定（如果已授权）</span>
                </WkButton>
              </div>
            </div>
            
            <!-- 使用说明 -->
            <div class="pt-4 border-t border-border">
              <h4 class="text-sm font-medium text-text mb-2">Microsoft账户绑定的优势</h4>
              <ul class="text-sm text-text-secondary space-y-2">
                <li class="flex items-start gap-2">
                  <i class="i-tabler-check text-green-500 mt-0.5" />
                  <span>支持Bedrock版（基岩版）和Java版账号绑定</span>
                </li>
                <li class="flex items-start gap-2">
                  <i class="i-tabler-check text-green-500 mt-0.5" />
                  <span>无需手动输入UUID，自动获取准确信息</span>
                </li>
                <li class="flex items-start gap-2">
                  <i class="i-tabler-check text-green-500 mt-0.5" />
                  <span>支持Microsoft账户登录的所有Minecraft版本</span>
                </li>
                <li class="flex items-start gap-2">
                  <i class="i-tabler-check text-green-500 mt-0.5" />
                  <span>更加安全，无需担心UUID输入错误</span>
                </li>
              </ul>
              
              <div class="mt-4 p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg border border-blue-200 dark:border-blue-800">
                <div class="flex items-start gap-2">
                  <i class="i-tabler-info-circle text-blue-500 mt-0.5" />
                  <div class="text-sm text-blue-700 dark:text-blue-300">
                    <p class="font-medium">注意：授权流程</p>
                    <p class="mt-1">
                      点击授权按钮后，将会打开Microsoft登录页面。请确保您登录的是与Minecraft关联的Microsoft账户。
                      授权完成后，页面会自动关闭并继续绑定流程。
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </WkCard>
  </div>
</template>

<style scoped>
.minecraft-settings {
  min-height: 400px;
}

code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}
</style>