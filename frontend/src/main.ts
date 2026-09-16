import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
// 只含 @font-face（101 个 unicode-range 分片、相对 url）：Vite 会把 woff2 一并产出到 dist，
// 即"自托管"；不含任何元素样式，故与下面的令牌层无层叠冲突。许可见 public/THIRD-PARTY-LICENSES.txt
import '@fontsource-variable/noto-sans-sc'
import './styles/tailwind.css'
import './styles/tokens.css'
import './styles/hljs.css'

// Element Plus 组件与 API 由 unplugin 按需自动引入（见 vite.config.ts）
createApp(App).use(createPinia()).use(router).mount('#app')
