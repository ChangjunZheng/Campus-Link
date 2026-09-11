import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Element Plus 组件与 API 由 unplugin 按需自动引入（见 vite.config.ts）
createApp(App).use(createPinia()).use(router).mount('#app')
