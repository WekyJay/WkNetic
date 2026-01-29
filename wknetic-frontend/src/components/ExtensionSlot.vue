<template>
  <div class="extension-slot">
    <component
      v-for="(comp, index) in components"
      :key="comp.__pluginId || index"
      :is="comp"
      :context="context"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { WknieticSDK } from '@/sdk';

const props = defineProps<{
  name: string;
  context?: any; // 把宿主的数据（如用户信息）传给插件
}>();

// 获取的是组件对象数组，而不是 config 数组
const components = computed(() => WknieticSDK.getComponents(props.name));

</script>

<style scoped>
.extension-slot {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>