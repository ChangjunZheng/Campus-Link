<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { useAuthStore } from './stores/auth'
import { BOARDS } from './constants/boards'

const router = useRouter()
const auth = useAuthStore()

const nickname = computed(() => auth.user?.nickname || '')

function logout() {
  auth.logout()
  router.push('/')
}
</script>

<template>
  <el-container class="min-h-screen">
    <el-header class="border-b-[0.5px] border-line bg-card">
      <div class="mx-auto flex h-full max-w-page items-center gap-4">
        <RouterLink to="/" class="flex shrink-0 items-center gap-2 text-title-lg font-medium text-primary">
          <img src="/logo.svg" alt="" class="h-[22px] w-[22px]" width="22" height="22" />
          Campus-Link
        </RouterLink>
        <nav class="flex flex-1 gap-1 overflow-x-auto">
          <RouterLink
            v-for="b in BOARDS"
            :key="b.code"
            :to="`/board/${b.code}`"
            class="whitespace-nowrap rounded-sm px-2.5 py-1.5 text-body text-ink transition-colors hover:bg-primary-soft hover:text-primary [&.router-link-active]:bg-primary-soft [&.router-link-active]:text-primary"
          >
            {{ b.name }}
          </RouterLink>
        </nav>
        <div class="flex items-center gap-3">
          <div class="hidden w-[200px] md:block">
            <el-input placeholder="搜索即将开放" size="small" disabled />
          </div>
          <el-badge :value="0" :hidden="true">
            <el-icon :size="18"><Bell /></el-icon>
          </el-badge>
          <template v-if="auth.isLoggedIn">
            <el-dropdown>
              <span class="cursor-pointer text-body text-ink">{{ nickname }}</span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/notifications')">通知中心</el-dropdown-item>
                  <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <RouterLink v-else to="/login">
            <el-button type="primary" size="small">登录 / 注册</el-button>
          </RouterLink>
        </div>
      </div>
    </el-header>
    <el-main class="mx-auto w-full max-w-page">
      <RouterView />
    </el-main>
    <el-footer class="text-center text-caption text-ink-meta">
      Campus-Link · 重庆工程学院计算机专业学生社区
    </el-footer>
  </el-container>
</template>
