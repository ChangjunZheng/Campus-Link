<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { listBoards, listPosts, type BoardVo, type PostSummaryVo } from '../api/forum'
import { formatRelativeTime } from '../utils/time'

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
  <nav class="mb-3 flex items-center gap-1.5 text-note" aria-label="面包屑">
    <RouterLink to="/">首页</RouterLink>
    <span class="text-ink-meta">/</span>
    <span class="text-ink-regular">{{ board?.name || code }}</span>
  </nav>

  <el-card shadow="never" class="mb-4">
    <div class="flex items-center justify-between gap-3">
      <div>
        <h2 class="mb-1.5 text-title-lg font-medium text-ink">{{ board?.name || code }}</h2>
        <p class="text-note text-ink-meta">{{ board?.description || '' }}</p>
      </div>
      <RouterLink :to="`/publish?board=${code}`">
        <el-button type="primary">发帖</el-button>
      </RouterLink>
    </div>
  </el-card>

  <el-card shadow="never">
    <el-skeleton v-if="loading" animated>
      <template #template>
        <div
          v-for="i in 4"
          :key="i"
          class="border-b-[0.5px] border-divider py-3 first:pt-0 last:border-b-0 last:pb-0"
        >
          <el-skeleton-item variant="text" style="width: 46%" />
          <el-skeleton-item variant="text" class="mt-2" style="width: 100%" />
          <el-skeleton-item variant="text" class="mt-1" style="width: 72%" />
          <div class="mt-2 flex items-center gap-2">
            <el-skeleton-item variant="text" style="width: 64px" />
            <el-skeleton-item variant="text" style="width: 64px" />
            <el-skeleton-item variant="text" style="width: 88px" />
          </div>
        </div>
      </template>
    </el-skeleton>

    <el-alert
      v-else-if="loadError"
      type="error"
      :closable="false"
      show-icon
      :title="loadError"
    >
      <el-button size="small" @click="loadPosts">重新加载</el-button>
    </el-alert>

    <div v-else-if="!posts.length" class="py-10 text-center">
      <p class="text-body text-ink-regular">本版块还没有帖子，来发第一帖吧</p>
      <RouterLink :to="`/publish?board=${code}`">
        <el-button type="primary" class="mt-4">去发第一帖</el-button>
      </RouterLink>
    </div>

    <template v-else>
      <ul>
        <li
          v-for="p in posts"
          :key="p.id"
          class="border-b-[0.5px] border-divider py-3 first:pt-0 last:border-b-0 last:pb-0"
        >
          <RouterLink :to="`/post/${p.id}`" class="text-title-sm font-medium text-ink hover:text-link">
            {{ p.title }}
          </RouterLink>
          <p class="my-1.5 line-clamp-2 text-note text-ink-regular">{{ p.summary }}</p>
          <div class="flex flex-wrap items-center gap-1.5 text-caption text-ink-meta">
            <el-tag v-if="p.accepted" type="success" effect="light" size="small">已采纳</el-tag>
            <span>{{ p.authorNickname }}</span>
            <span>·</span>
            <span>{{ p.replyCount }} 回复</span>
            <span>·</span>
            <span>{{ formatRelativeTime(p.createdAt) }}</span>
          </div>
        </li>
      </ul>
      <div v-if="total > size" class="mt-4 flex justify-center">
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
