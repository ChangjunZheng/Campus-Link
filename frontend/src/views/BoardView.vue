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

  <el-card v-loading="loading" shadow="never">
    <el-alert v-if="loadError" :title="loadError" type="error" :closable="false" show-icon />
    <el-empty v-else-if="!posts.length && !loading" description="本版块还没有帖子，来发第一帖吧" />
    <template v-else>
      <ul>
        <li
          v-for="p in posts"
          :key="p.id"
          class="border-b-[0.5px] border-divider py-3 last:border-b-0"
        >
          <RouterLink :to="`/post/${p.id}`" class="text-title-sm font-medium text-ink hover:text-primary">
            {{ p.title }}
          </RouterLink>
          <p class="my-1.5 line-clamp-2 text-note text-ink-regular">{{ p.summary }}</p>
          <div class="flex flex-wrap gap-4 text-caption text-ink-meta">
            <span>{{ p.authorNickname }}</span>
            <span>{{ p.replyCount }} 回复</span>
            <span>{{ formatTime(p.createdAt) }}</span>
          </div>
        </li>
      </ul>
      <div class="mt-4 flex justify-center">
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
