<script setup lang="ts">
import { ref } from 'vue'
import { listMyFavorites, type PostSummaryVo } from '../api/forum'
import { formatRelativeTime } from '../utils/time'

const posts = ref<PostSummaryVo[]>([])
const total = ref(0)
const page = ref(1)
const size = 20
const loading = ref(false)
const loadError = ref('')

async function loadFavorites() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await listMyFavorites(page.value, size)
    posts.value = res.list
    total.value = res.total
  } catch (e) {
    posts.value = []
    total.value = 0
    loadError.value = e instanceof Error ? e.message : '收藏加载失败'
  } finally {
    loading.value = false
  }
}

function onPageChange(next: number) {
  page.value = next
  loadFavorites()
}

loadFavorites()
</script>

<template>
  <el-card shadow="never" class="mb-4">
    <h2 class="text-title-lg font-medium text-ink">我的收藏</h2>
    <p class="mt-1.5 text-note text-ink-meta">按收藏时间倒序，仅本人可见</p>
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
      <el-button size="small" @click="loadFavorites">重新加载</el-button>
    </el-alert>

    <div v-else-if="!posts.length" class="py-10 text-center">
      <p class="text-body text-ink-regular">还没有收藏的帖子</p>
      <RouterLink to="/">
        <el-button type="primary" class="mt-4">去逛逛版块</el-button>
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
