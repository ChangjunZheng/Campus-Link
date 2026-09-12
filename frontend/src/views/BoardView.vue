<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { listBoards, listPosts, type BoardVo, type PostSummaryVo } from '../api/forum'
import { formatTime } from '../utils/time'

const route = useRoute()
const code = computed(() => String(route.params.code || ''))

const board = ref<BoardVo | undefined>()
const posts = ref<PostSummaryVo[]>([])
const total = ref(0)
const page = ref(1)
const size = 20
const loading = ref(false)
const loadError = ref('')

async function loadBoards() {
  try {
    const list = await listBoards()
    board.value = list.find((b) => b.code === code.value)
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '版块信息加载失败')
  }
}

async function loadPosts() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await listPosts(page.value, size, code.value)
    posts.value = res.list
    total.value = res.total
  } catch (e) {
    posts.value = []
    total.value = 0
    loadError.value = e instanceof Error ? e.message : '帖子加载失败'
  } finally {
    loading.value = false
  }
}

function onPageChange(next: number) {
  page.value = next
  loadPosts()
}

watch(
  code,
  () => {
    page.value = 1
    loadBoards()
    loadPosts()
  },
  { immediate: true },
)
</script>

<template>
  <el-card shadow="never" class="board-head">
    <div class="head-row">
      <div>
        <h2>{{ board?.name || code }}</h2>
        <p>{{ board?.description || '' }}</p>
      </div>
      <RouterLink :to="`/publish?board=${code}`">
        <el-button type="primary">发帖</el-button>
      </RouterLink>
    </div>
  </el-card>

  <el-card v-loading="loading" shadow="never">
    <el-alert v-if="loadError" :title="loadError" type="error" :closable="false" show-icon />
    <el-empty v-else-if="!posts.length && !loading" description="本版块还没有帖子，来发第一帖吧" />
    <template v-else>
      <ul class="post-list">
        <li v-for="p in posts" :key="p.id">
          <RouterLink :to="`/post/${p.id}`" class="post-title">{{ p.title }}</RouterLink>
          <p class="post-summary">{{ p.summary }}</p>
          <div class="post-meta">
            <span>{{ p.authorNickname }}</span>
            <span>{{ p.replyCount }} 回复</span>
            <span>{{ formatTime(p.createdAt) }}</span>
          </div>
        </li>
      </ul>
      <div class="pager">
        <el-pagination
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="onPageChange"
        />
      </div>
    </template>
  </el-card>
</template>

<style scoped>
.board-head {
  margin-bottom: 16px;
}
.head-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.head-row h2 {
  margin: 0 0 6px;
  font-size: 20px;
}
.head-row p {
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
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
}
.post-list li:last-child {
  border-bottom: none;
}
.post-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  text-decoration: none;
}
.post-title:hover {
  color: #409eff;
}
.post-summary {
  margin: 6px 0;
  color: #606266;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.post-meta {
  display: flex;
  gap: 16px;
  color: #909399;
  font-size: 12px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
