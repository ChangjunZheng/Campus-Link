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
  <el-container class="layout">
    <el-header class="header">
      <div class="header-inner">
        <RouterLink to="/" class="logo">Campus-Link</RouterLink>
        <nav class="nav">
          <RouterLink v-for="b in BOARDS" :key="b.code" :to="`/board/${b.code}`" class="nav-item">
            {{ b.name }}
          </RouterLink>
        </nav>
        <div class="right">
          <el-input placeholder="搜索帖子（Sprint 3 上线）" size="small" class="search" disabled />
          <el-badge :value="0" :hidden="true">
            <el-icon :size="18"><Bell /></el-icon>
          </el-badge>
          <template v-if="auth.isLoggedIn">
            <el-dropdown>
              <span class="user">{{ nickname }}</span>
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
    <el-main class="main">
      <RouterView />
    </el-main>
    <el-footer class="footer">
      Campus-Link · 重庆工程学院计算机专业学生社区
    </el-footer>
  </el-container>
</template>

<style>
body {
  margin: 0;
  background: #f5f6f8;
}
.layout {
  min-height: 100vh;
}
.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}
.header-inner {
  max-width: 1080px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 16px;
  height: 60px;
}
.logo {
  font-weight: 700;
  font-size: 20px;
  color: #409eff;
  text-decoration: none;
  white-space: nowrap;
}
.nav {
  display: flex;
  gap: 4px;
  flex: 1;
  overflow-x: auto;
}
.nav-item {
  padding: 6px 10px;
  border-radius: 6px;
  color: #303133;
  text-decoration: none;
  font-size: 14px;
  white-space: nowrap;
}
.nav-item:hover,
.nav-item.router-link-active {
  background: #ecf5ff;
  color: #409eff;
}
.right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.search {
  width: 200px;
}
.user {
  cursor: pointer;
  color: #303133;
  font-size: 14px;
}
.main {
  max-width: 1080px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}
.footer {
  text-align: center;
  color: #909399;
  font-size: 12px;
}
</style>
