// filepath: src/i18n/index.ts
import { createI18n } from 'vue-i18n'
import en from './en.json'
import zh from './zh.json'

const messages = {
  en,
  zh
}

const i18n = createI18n({
  legacy: false, // 使用 Composition API
  locale: 'zh', // 默认语言
  fallbackLocale: 'en',
  messages
})

export default i18n