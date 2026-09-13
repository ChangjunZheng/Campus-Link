import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: () => import('../views/HomeView.vue'), meta: { title: '首页' } },
    { path: '/board/:code', name: 'board', component: () => import('../views/BoardView.vue'), meta: { title: '版块' } },
    { path: '/login', name: 'login', component: () => import('../views/LoginView.vue'), meta: { title: '登录 / 注册', bare: true } },
    { path: '/post/:id', name: 'post', component: () => import('../views/PostDetailView.vue'), meta: { title: '帖子详情' } },
    { path: '/publish', name: 'publish', component: () => import('../views/PublishView.vue'), meta: { title: '发布', requiresAuth: true } },
    { path: '/search', name: 'search', component: () => import('../components/Placeholder.vue'), props: { title: '搜索' }, meta: { title: '搜索' } },
    { path: '/u/:id', name: 'profile', component: () => import('../components/Placeholder.vue'), props: { title: '个人主页' }, meta: { title: '个人主页' } },
    { path: '/notifications', name: 'notifications', component: () => import('../components/Placeholder.vue'), props: { title: '通知中心' }, meta: { title: '通知', requiresAuth: true } },
    { path: '/favorites', name: 'favorites', component: () => import('../views/MyFavoritesView.vue'), meta: { title: '我的收藏', requiresAuth: true } },
    { path: '/admin', name: 'admin', component: () => import('../components/Placeholder.vue'), props: { title: '管理后台' }, meta: { title: '后台' } },
  ],
})

// 登录守卫：requiresAuth 路由未登录时跳转登录页并携带回跳地址
router.beforeEach((to) => {
  if (!to.meta.requiresAuth) {
    return true
  }
  const auth = useAuthStore()
  if (auth.isLoggedIn) {
    return true
  }
  return { path: '/login', query: { redirect: String(to.fullPath) } }
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${String(to.meta.title)} · Campus-Link` : 'Campus-Link'
})

export default router
