<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { IconName } from '../assets/icons'
import { listPosts, type PostSummaryVo } from '../api/forum'
import { BOARDS } from '../constants/boards'
import { useAuthStore } from '../stores/auth'
import PostListItem from '../components/PostListItem.vue'
import SvgIcon from '../components/SvgIcon.vue'

const auth = useAuthStore()

/** hero 右侧「快速开始」入口（CR-052 品牌化）；icon 显式标注类型，否则推成 string 传不进 SvgIcon */
const quickEntries: { label: string; to: string; icon: IconName }[] = [
  { label: '我要提问 / 发帖', to: '/publish', icon: 'pencil' },
  { label: '逛技术问答版块', to: '/board/qna', icon: 'message-circle' },
  { label: '搜索全站内容', to: '/search', icon: 'search' },
]

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
    <!-- hero 品牌横幅（CR-052：左主张 + 右快速开始，全站唯一渐变底） -->
    <section class="cl-hero-bg mb-4 overflow-hidden rounded-lg border-[0.5px] border-line">
      <div class="flex flex-col gap-6 p-6 md:flex-row md:items-center md:justify-between md:p-8">
        <div class="max-w-[620px]">
          <h1 class="text-balance text-hero font-medium text-ink">问有所答 · 学有同伴 · 求职有路</h1>
          <p class="mt-2.5 text-body leading-body text-ink-regular">
            重庆工程学院计算机专业学生的垂直交流论坛。技术问答、学习资源、面经求职、竞赛与课程，都可以在这里讨论。
          </p>
          <div class="mt-5 flex flex-wrap gap-3">
            <RouterLink to="/publish">
              <el-button type="primary" size="large">发布帖子</el-button>
            </RouterLink>
            <RouterLink v-if="!auth.isLoggedIn" to="/login">
              <el-button size="large">学籍核验注册</el-button>
            </RouterLink>
          </div>
        </div>

        <div class="flex w-full flex-none flex-col gap-2 md:w-[264px]">
          <p class="px-1 text-caption text-ink-meta">快速开始</p>
          <RouterLink
            v-for="quick in quickEntries"
            :key="quick.to"
            :to="quick.to"
            class="group flex items-center gap-2.5 rounded-md border-[0.5px] border-line bg-card px-3 py-2.5 transition-colors duration-fast ease-standard hover:border-primary"
          >
            <span class="inline-flex h-7 w-7 flex-none items-center justify-center rounded-sm bg-primary-soft text-primary">
              <SvgIcon :name="quick.icon" :size="15" />
            </span>
            <span class="flex-1 text-note text-ink-regular">{{ quick.label }}</span>
            <SvgIcon
              name="chevron-right"
              :size="14"
              class="text-ink-meta transition-transform duration-fast ease-standard group-hover:translate-x-0.5"
            />
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- 6 版块矩阵：线性图标 + 识别色淡底，hover 微抬、图标实色化 -->
    <el-row :gutter="16">
      <el-col v-for="b in BOARDS" :key="b.code" :xs="24" :sm="12" :md="8">
        <RouterLink :to="`/board/${b.code}`" class="block">
          <el-card shadow="never" class="cl-board-card mb-4" :body-style="{ padding: 'var(--cl-space-4)' }">
            <div class="flex items-start gap-3">
              <span :class="['cl-board-icon', `cl-board-icon--${b.code}`]">
                <SvgIcon :name="b.icon" :size="20" />
              </span>
              <div class="min-w-0">
                <h3 class="text-title-sm font-medium text-ink">{{ b.name }}</h3>
                <p class="mt-0.5 text-caption leading-body text-ink-meta">{{ b.description }}</p>
              </div>
            </div>
          </el-card>
        </RouterLink>
      </el-col>
    </el-row>

    <el-card shadow="never" :body-style="{ padding: 0 }">
      <template #header>
        <span class="font-medium">全站最新</span>
      </template>

      <el-skeleton v-if="loading" animated class="px-5 pt-4">
        <template #template>
          <div
            v-for="i in 5"
            :key="i"
            class="border-b-[0.5px] border-divider py-3 last:border-b-0"
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

      <div v-else-if="loadError" class="p-5">
        <el-alert type="error" :closable="false" show-icon :title="loadError">
          <el-button size="small" @click="loadLatest">重新加载</el-button>
        </el-alert>
      </div>

      <div v-else-if="!latest.length" class="py-10 text-center">
        <p class="text-body text-ink-regular">还没有帖子，来发第一帖吧</p>
        <RouterLink to="/publish">
          <el-button type="primary" class="mt-4">去发第一帖</el-button>
        </RouterLink>
      </div>

      <template v-else>
        <ul>
          <PostListItem v-for="p in latest" :key="p.id" :post="p" show-board />
        </ul>
        <div class="flex justify-center px-5 py-4">
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
