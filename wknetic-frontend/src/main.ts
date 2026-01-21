import { createApp } from 'vue'
import App from './App.vue'

// ---------- UnoCSS ---------------
// 1. 引入 UnoCSS 重置样式
import '@unocss/reset/tailwind.css'
// 2. 引入 UnoCSS 核心样式
import 'virtual:uno.css'
// 3. 引入我们自定义的 CSS 变量
import './styles/theme.css'
// ---------- Router ---------------
import router from './router' // 引入路由实例

const app = createApp(App)

app.use(router) // 注册路由
app.mount('#app')