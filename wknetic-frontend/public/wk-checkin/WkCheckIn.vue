<template>
  <div class="wk-plugin-checkin">
    <button 
      v-if="!isSigned" 
      class="btn-checkin" 
      @click="handleCheckIn" 
      :disabled="loading"
    >
      <span v-if="loading" class="spinner">↻</span>
      <span v-else>📅</span>
      
      <span v-if="loading">签到中...</span>
      <span v-else>每日签到</span>
    </button>

    <div v-else class="status-signed">
      <span class="icon">✅</span>
      <span class="text">
        已领 {{ rewardAmount }} 币
        <small v-if="context?.user">({{ context.user.name }})</small>
      </span>
    </div>
  </div>
</template>

<script setup>
// sfc-loader 会自动处理这里的 vue 导入，使用宿主的 Vue 实例
import { ref, onMounted } from 'vue';

// 接收宿主传来的上下文 (包含用户信息 user, 配置 config 等)
const props = defineProps(['context']);

// 响应式状态
const loading = ref(false);
const isSigned = ref(false);
const rewardAmount = ref(0);

// 获取宿主 SDK (为了安全，建议这样获取，或者直接用 window.WknieticSDK)
const SDK = window.WknieticSDK;

// 模拟检查签到状态 (初始化)
onMounted(async () => {
  // 真实场景：这里调用 SDK.http.get('/plugin/wk-checkin/status')
  // 这里做个假的随机状态，方便你看效果
  console.log('签到插件加载，当前用户:', props.context?.user);
});

// 处理点击事件
const handleCheckIn = async () => {
  if (loading.value) return;
  loading.value = true;

  try {
    // 1. 调用宿主 API (模拟网络请求)
    // 真实场景: await SDK.http.post('/plugin/wk-checkin/sign', { ... })
    await new Promise(resolve => setTimeout(resolve, 800));

    // 2. 变更状态
    isSigned.value = true;
    rewardAmount.value = 10;
    
    // 3. 可以在这里触发其他逻辑，比如弹窗
    // alert('签到成功'); 

  } catch (error) {
    console.error('签到失败', error);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
/* 注意：这里的样式是 Scoped 的，不会影响宿主页面 
  sfc-loader 会自动处理 CSS 隔离
*/

.wk-plugin-checkin {
  display: inline-flex;
  align-items: center;
}

/* 按钮样式 - 仿照游戏风格的渐变按钮 */
.btn-checkin {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 6px rgba(118, 75, 162, 0.3);
}

.btn-checkin:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(118, 75, 162, 0.4);
  filter: brightness(1.1);
}

.btn-checkin:active:not(:disabled) {
  transform: translateY(1px);
}

.btn-checkin:disabled {
  background: #a0aec0;
  cursor: not-allowed;
  box-shadow: none;
}

/* 旋转动画 */
.spinner {
  display: inline-block;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 已签到状态样式 */
.status-signed {
  background-color: #f0fdf4;
  border: 1px solid #86efac;
  color: #15803d;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 5px;
  animation: fadeIn 0.5s ease;
}

.status-signed small {
  font-weight: normal;
  color: #166534;
  opacity: 0.8;
  margin-left: 4px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}
</style>