<script setup lang="ts">
import { BOARDS } from '../constants/boards'
</script>

<template>
  <div>
    <el-card shadow="never" class="hero">
      <h1>问有所答 · 学有同伴 · 求职有路</h1>
      <p>重庆工程学院计算机专业学生的垂直交流论坛。Sprint 1：学籍核验注册与登录链路已就绪，内容功能建设中。</p>
      <RouterLink to="/login">
        <el-button type="primary">学籍核验注册</el-button>
      </RouterLink>
    </el-card>
    <el-row :gutter="16">
      <el-col v-for="b in BOARDS" :key="b.code" :xs="24" :sm="12" :md="8">
        <RouterLink :to="`/board/${b.code}`" class="board-link">
          <el-card shadow="hover" class="board-card">
            <h3>{{ b.name }}</h3>
            <p>{{ b.desc }}</p>
          </el-card>
        </RouterLink>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.hero {
  margin-bottom: 16px;
  text-align: center;
}
.hero h1 {
  margin: 8px 0;
  font-size: 24px;
}
.hero p {
  color: #606266;
  font-size: 14px;
}
.board-link {
  text-decoration: none;
}
.board-card {
  margin-bottom: 16px;
}
.board-card h3 {
  margin: 0 0 8px;
  font-size: 16px;
}
.board-card p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}
</style>
