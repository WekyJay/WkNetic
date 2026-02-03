<script setup lang="ts">
import { ref } from 'vue'

const visible = ref(false)
</script>

<template>
  <div class="p-6">
    <h1>Test Dialog Page</h1>
    <p>visible = {{ visible }}</p>
    
    <button 
      @click="visible = !visible"
      style="padding: 10px; background: blue; color: white; cursor: pointer;"
    >
      Toggle (directly)
    </button>
    
    <button 
      @click="() => visible = true"
      style="padding: 10px; background: green; color: white; cursor: pointer; margin-left: 10px;"
    >
      Set to True
    </button>
    
    <el-dialog v-model="visible" title="Test Dialog">
      <div style="background: yellow; padding: 20px;">
        This is a test dialog. visible = {{ visible }}
      </div>
    </el-dialog>
  </div>
</template>
