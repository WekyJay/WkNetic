import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import UnoCss from 'unocss/vite'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// 引入 path 模块
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    UnoCss(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  // 2. 添加 resolve.alias 配置
  resolve: {
    alias: {
      // 这里的 './src' 意思是把 '@' 映射到项目根目录下的 src 文件夹
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
