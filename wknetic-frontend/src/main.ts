import { createApp } from 'vue'
import App from './App.vue'
import * as Vue from 'vue'

// ---------- SDK初始化 ---------------
import { WknieticSDK } from './sdk';

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

// ---------- 代码高亮 ---------------
import hljs from 'highlight.js/lib/core'
import hljsVuePlugin from '@highlightjs/vue-plugin'

// 注册常用语言
import javascript from 'highlight.js/lib/languages/javascript'
import typescript from 'highlight.js/lib/languages/typescript'
import python from 'highlight.js/lib/languages/python'
import java from 'highlight.js/lib/languages/java'
import bash from 'highlight.js/lib/languages/bash'
import json from 'highlight.js/lib/languages/json'
import xml from 'highlight.js/lib/languages/xml'
import css from 'highlight.js/lib/languages/css'
import sql from 'highlight.js/lib/languages/sql'
import markdown from 'highlight.js/lib/languages/markdown'
import plaintext from 'highlight.js/lib/languages/plaintext'

hljs.registerLanguage('plaintext', plaintext)
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('python', python)
hljs.registerLanguage('java', java)
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('sh', bash) // sh 也使用 bash 高亮
hljs.registerLanguage('json', json)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('html', xml) // html 使用 xml 高亮
hljs.registerLanguage('css', css)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('markdown', markdown)
// -------------------------------------

const app = createApp(App)

WknieticSDK.vue = Vue;

app.use(router)         // 注册路由
app.use(createPinia())  // 注册 Pinia 状态管理
app.use(i18n)           // 使用 i18n
app.use(hljsVuePlugin) // 注册代码高亮插件

// 挂载应用
app.mount('#app')