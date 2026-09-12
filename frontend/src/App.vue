<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, ArrowLeft, Bell, Search } from '@element-plus/icons-vue'
import { useAuthStore } from './stores/auth'
import { BOARDS } from './constants/boards'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const nickname = computed(() => auth.user?.nickname || '')
const avatarText = computed(() => (nickname.value ? nickname.value.slice(0, 1) : ''))
const isLoggedIn = computed(() => auth.isLoggedIn)
/** 登录页用独立极简页头（设计稿 page-05），不渲染全局顶栏与页脚 */
const bare = computed(() => Boolean(route.meta.bare))

function logout() {
  auth.logout()
  router.push('/')
}

function onSearchClick() {
  ElMessage.info('搜索功能即将开放')
}
</script>

<template>
  <el-container class="min-h-screen">
    <el-header v-if="bare" class="border-b-[0.5px] border-line bg-card">
      <!-- 窄屏下 --el-header-height 为 auto（tokens.css），补上下内边距撑回约 56px -->
      <div class="mx-auto flex h-full max-w-page items-center justify-between max-md:py-4">
        <!-- 品牌字样用主色：WCAG 对 logo / 品牌名的文字不计对比度要求 -->
        <RouterLink to="/" class="flex shrink-0 items-center gap-2 text-title font-medium text-primary">
          <img src="/logo.svg" alt="" class="h-[22px] w-[22px]" width="22" height="22" />
          Campus-Link
        </RouterLink>
        <RouterLink to="/" class="flex items-center gap-1 text-body">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </RouterLink>
      </div>
    </el-header>
    <el-header v-else class="border-b-[0.5px] border-line bg-card">
      <!-- 窄屏折成两行（设计稿 page-09）：第一行 logo + 图标，第二行版块胶囊带；
           行高由 tokens.css 的窄屏媒体查询把 --el-header-height 放开为 auto -->
      <div class="mx-auto flex h-full max-w-page flex-wrap items-center gap-x-5 gap-y-1 max-md:py-2">
        <!-- 品牌字样用主色：WCAG 对 logo / 品牌名的文字不计对比度要求 -->
        <RouterLink to="/" class="order-1 flex shrink-0 items-center gap-2 text-title font-medium text-primary">
          <img src="/logo.svg" alt="" class="h-[22px] w-[22px]" width="22" height="22" />
          Campus-Link
        </RouterLink>
        <div class="order-2 ml-auto flex shrink-0 items-center gap-4 md:order-3 md:ml-0">
          <div class="hidden w-[200px] cursor-pointer md:block" @click="onSearchClick">
            <el-input placeholder="搜索帖子标题 / 标签" readonly>
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <!-- 窄屏折叠为图标（ui-guideline §7），行为与宽屏搜索框一致 -->
          <button
            type="button"
            class="cursor-pointer text-ink-regular hover:text-link md:hidden"
            aria-label="搜索"
            @click="onSearchClick"
          >
            <el-icon :size="18"><Search /></el-icon>
          </button>
          <RouterLink to="/notifications" class="text-ink-regular hover:text-link" aria-label="通知中心">
            <el-icon :size="18"><Bell /></el-icon>
          </RouterLink>
          <el-dropdown v-if="isLoggedIn">
            <span class="flex cursor-pointer items-center gap-1.5">
              <!-- el-avatar 底色/文字色写在内联样式：EP 组件样式无 @layer，工具类压不过 -->
              <el-avatar
                :size="28"
                :style="{
                  background: 'var(--cl-color-primary-soft)',
                  color: 'var(--cl-color-link)',
                  flexShrink: 0,
                  fontSize: 'var(--cl-font-body)',
                  fontWeight: 500,
                }"
              >
                {{ avatarText }}
              </el-avatar>
              <span class="hidden max-w-[100px] truncate text-body text-ink md:inline">{{ nickname }}</span>
              <el-icon :size="12" class="text-ink-meta"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/notifications')">通知中心</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <RouterLink v-else to="/login">
            <el-button type="primary" size="small">登录 / 注册</el-button>
          </RouterLink>
        </div>
        <!-- 窄屏整行铺满并出血到页头边缘：-mx-4 / +2rem 对应 tokens.css 窄屏下的 --el-header-padding: 0 16px -->
        <nav
          class="order-3 -mx-4 flex w-[calc(100%+2rem)] gap-2 overflow-x-auto bg-page px-4 py-2 md:order-2 md:mx-0 md:w-auto md:flex-1 md:gap-1 md:bg-transparent md:px-0 md:py-0"
        >
          <RouterLink
            v-for="b in BOARDS"
            :key="b.code"
            :to="`/board/${b.code}`"
            class="whitespace-nowrap rounded-full border-[0.5px] border-line bg-card px-3 py-1 text-body text-ink transition-colors hover:bg-primary-soft hover:text-link md:rounded-md md:border-0 md:bg-transparent md:px-3 md:py-1.5 [&.router-link-active]:bg-primary-soft [&.router-link-active]:font-medium [&.router-link-active]:text-link"
          >
            {{ b.name }}
          </RouterLink>
        </nav>
      </div>
    </el-header>
    <el-main class="mx-auto w-full max-w-page">
      <RouterView />
    </el-main>
    <el-footer v-if="!bare" class="text-center text-caption text-ink-meta">
      Campus-Link · 重庆工程学院计算机专业学生社区
    </el-footer>
  </el-container>
</template>
