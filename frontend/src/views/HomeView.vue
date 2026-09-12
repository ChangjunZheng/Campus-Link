<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listPosts, type PostSummaryVo } from '../api/forum'
import { BOARDS } from '../constants/boards'
import { useAuthStore } from '../stores/auth'
import { formatRelativeTime } from '../utils/time'

const auth = useAuthStore()

const latest = ref<PostSummaryVo[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)
const loadError = ref('')

async function loadLatest() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await listPosts(page.value, size)
    latest.value = res.list
    total.value = res.total
  } catch (e) {
    latest.value = []
    total.value = 0
    loadError.value = e instanceof Error ? e.message : '最新帖子加载失败'
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
    <el-card shadow="never" class="mb-4 text-center" :body-style="{ padding: 'var(--cl-space-5)' }">
      <h1 class="my-2 text-balance text-hero font-medium text-ink">问有所答 · 学有同伴 · 求职有路</h1>
      <p class="text-body text-ink-regular">
        重庆工程学院计算机专业学生的垂直交流论坛。技术问答、学习资源、面经求职、竞赛与课程，都可以在这里讨论。
      </p>
      <div class="flex justify-center gap-3">
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
        <RouterLink :to="`/board/${b.code}`" class="block">
          <el-card shadow="hover" class="mb-4">
            <h3 class="mb-2 text-title font-medium text-ink">{{ b.name }}</h3>
            <p class="text-note text-ink-meta">{{ b.desc }}</p>
          </el-card>
        </RouterLink>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <span class="font-medium">全站最新</span>
      </template>

      <el-skeleton v-if="loading" animated>
        <template #template>
          <div
            v-for="i in 5"
            :key="i"
            class="border-b-[0.5px] border-divider py-3 first:pt-0 last:border-b-0 last:pb-0"
          >
            <el-skeleton-item variant="text" style="width: 56%" />
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
        <el-button size="small" @click="loadLatest">重新加载</el-button>
      </el-alert>

      <div v-else-if="!latest.length" class="py-10 text-center">
        <p class="text-body text-ink-regular">还没有帖子，来发第一帖吧</p>
        <RouterLink to="/publish">
          <el-button type="primary" class="mt-4">去发第一帖</el-button>
        </RouterLink>
      </div>

      <template v-else>
        <ul>
          <li
            v-for="p in latest"
            :key="p.id"
            class="border-b-[0.5px] border-divider py-3 first:pt-0 last:border-b-0 last:pb-0"
          >
            <RouterLink :to="`/post/${p.id}`" class="text-title-sm text-ink hover:text-link">
              {{ p.title }}
            </RouterLink>
            <div class="mt-1.5 flex flex-wrap items-center gap-1.5 text-caption text-ink-meta">
              <RouterLink :to="`/board/${p.boardCode}`" class="text-link hover:underline">
                {{ p.boardName }}
              </RouterLink>
              <span>·</span>
              <span>{{ p.authorNickname }}</span>
              <span>·</span>
              <span>{{ p.replyCount }} 回复</span>
              <span>·</span>
              <span>{{ formatRelativeTime(p.createdAt) }}</span>
            </div>
          </li>
        </ul>
        <div class="mt-4 flex justify-center">
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
