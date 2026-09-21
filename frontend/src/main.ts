import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { setUnauthorizedHandler } from './api/client'
import { useAuthStore } from './stores/auth'
import './styles/tailwind.css'
import './styles/tokens.css'
import './styles/hljs.css'

// Element Plus 组件与 API 由 unplugin 按需自动引入（见 vite.config.ts）
const app = createApp(App)
app.use(createPinia())
app.use(router)

// BUG-004：401 / 4001 统一退出——清会话 + 提示 + 跳登录页（带 redirect 回跳）。
// 去重 guard 防并发请求同时 401 触发多次跳转；仅在本地仍有 token 时触发，不劫持匿名浏览的公开接口 401。
let redirectingToLogin = false
setUnauthorizedHandler(() => {
  const auth = useAuthStore()
  if (!auth.token || redirectingToLogin) {
    return
  }
  redirectingToLogin = true
  auth.logout()
  ElMessage.warning('登录已过期，请重新登录')
  const from = router.currentRoute.value.fullPath
  const query = from && from !== '/' && from !== '/login' ? { redirect: from } : {}
  router.push({ path: '/login', query }).finally(() => {
    redirectingToLogin = false
  })
})

app.mount('#app')
