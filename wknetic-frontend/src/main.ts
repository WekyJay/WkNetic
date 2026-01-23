import { createApp } from 'vue'
import App from './App.vue'

// ---------- Pinia ---------------
import { createPinia } from 'pinia'

// ---------- UnoCSS ---------------
// 1. 引入 UnoCSS 重置样式
import '@unocss/reset/tailwind.css'
// 2. 引入 UnoCSS 核心样式
import 'virtual:uno.css'
// 3. 引入我们自定义的 CSS 变量
import './styles/theme.css'

// ---------- Router ---------------
import router from './router' // 引入路由实例

// ---------- i18n ---------------
import i18n from './i18n' // 导入 i18n

const app = createApp(App)

app.use(router)         // 注册路由
app.use(createPinia())  // 注册 Pinia 状态管理
app.use(i18n)           // 使用 i18n
app.mount('#app')