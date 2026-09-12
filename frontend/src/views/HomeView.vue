<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listPosts, type PostSummaryVo } from '../api/forum'
import { BOARDS } from '../constants/boards'
import { useAuthStore } from '../stores/auth'
import { formatTime } from '../utils/time'

const auth = useAuthStore()

const latest = ref<PostSummaryVo[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)

async function loadLatest() {
  loading.value = true
  try {
    const res = await listPosts(page.value, size)
    latest.value = res.list
    total.value = res.total
  } catch (e) {
    latest.value = []
    total.value = 0
    ElMessage.error(e instanceof Error ? e.message : '最新帖子加载失败')
  } finally {
    loading.value = false
  }
}

function onPageChange(next: number) {
  page.value = next
  loadLatest()
}

onMounted(loadLatest)
</script>

<template>
  <div>
    <el-card shadow="never" class="hero">
      <h1>问有所答 · 学有同伴 · 求职有路</h1>
      <p>重庆工程学院计算机专业学生的垂直交流论坛。Sprint 2：发帖 / 列表 / 详情 / 楼层回复链路已上线。</p>
      <div class="hero-actions">
        <RouterLink to="/publish">
          <el-button type="primary">发布帖子</el-button>
        </RouterLink>
        <RouterLink v-if="!auth.isLoggedIn" to="/login">
          <el-button>学籍核验注册</el-button>
        </RouterLink>
      </div>
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

    <el-card v-loading="loading" shadow="never">
      <template #header>
        <span>全站最新</span>
      </template>
      <el-empty v-if="!latest.length && !loading" description="还没有帖子，来发第一帖吧" />
      <template v-else>
        <ul class="post-list">
          <li v-for="p in latest" :key="p.id">
            <RouterLink :to="`/post/${p.id}`" class="post-title">{{ p.title }}</RouterLink>
            <div class="post-meta">
              <RouterLink :to="`/board/${p.boardCode}`" class="post-board">{{ p.boardName }}</RouterLink>
              <span>{{ p.authorNickname }}</span>
              <span>{{ p.replyCount }} 回复</span>
              <span>{{ formatTime(p.createdAt) }}</span>
            </div>
          </li>
        </ul>
        <div class="pager">
          <el-pagination
            v-if="total > size"
            layout="prev, pager, next"
            :total="total"
            :page-size="size"
            :current-page="page"
            @current-change="onPageChange"
          />
        </div>
      </template>
    </el-card>
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
.hero-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
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
.post-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.post-list li {
  padding: 10px 0;
  border-bottom: 1px solid #f0f2f5;
}
.post-list li:last-child {
  border-bottom: none;
}
.post-title {
  font-size: 15px;
  color: #303133;
  text-decoration: none;
}
.post-title:hover {
  color: #409eff;
}
.post-meta {
  display: flex;
  gap: 16px;
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}
.post-board {
  color: #409eff;
  text-decoration: none;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
