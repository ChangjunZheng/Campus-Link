<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPost, listReplies, publishReply, type PostDetailVo, type ReplyVo } from '../api/forum'
import { ApiError } from '../api/client'
import { useAuthStore } from '../stores/auth'
import { formatTime } from '../utils/time'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const postId = computed(() => String(route.params.id || ''))
const post = ref<PostDetailVo | null>(null)
const replies = ref<ReplyVo[]>([])
const total = ref(0)
const page = ref(1)
const size = 20
const loading = ref(false)
const missing = ref(false)
const replyMd = ref('')
const submitting = ref(false)

async function loadPost() {
  loading.value = true
  missing.value = false
  post.value = null
  try {
    post.value = await getPost(postId.value)
  } catch (e) {
    // 3001 = 帖子不存在 / 已删除 / 未发布（设计 §3.4），单独给「不存在」态而非通用报错
    missing.value = e instanceof ApiError && e.code === 3001
    if (!missing.value) {
      ElMessage.error(e instanceof Error ? e.message : '帖子加载失败')
    }
  } finally {
    loading.value = false
  }
}

async function loadReplies() {
  try {
    const res = await listReplies(postId.value, page.value, size)
    replies.value = res.list
    total.value = res.total
  } catch (e) {
    replies.value = []
    total.value = 0
    // 帖子本身不存在（3001）时由 loadPost 渲染整页错误态；两个请求并发，此处可能先于 loadPost 失败，
    // 只按错误码判断才能保证不重复弹提示
    if (!(e instanceof ApiError && e.code === 3001)) {
      ElMessage.error(e instanceof Error ? e.message : '楼层加载失败')
    }
  }
}

function onPageChange(next: number) {
  page.value = next
  loadReplies()
}

async function submitReply() {
  if (!auth.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  const contentMd = replyMd.value.trim()
  if (!contentMd) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  submitting.value = true
  try {
    const res = await publishReply(postId.value, contentMd)
    ElMessage.success(`回复成功，你在 ${res.floorNo} 楼`)
    replyMd.value = ''
    page.value = Math.max(1, Math.ceil(res.floorNo / size))
    await Promise.all([loadReplies(), loadPost()])
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '回复失败')
  } finally {
    submitting.value = false
  }
}

watch(
  postId,
  () => {
    page.value = 1
    replyMd.value = ''
    loadPost()
    loadReplies()
  },
  { immediate: true },
)
</script>

<template>
  <div v-loading="loading">
    <el-result v-if="missing" icon="warning" title="帖子不存在或已删除" sub-title="它可能已被作者删除，或链接有误">
      <template #extra>
        <RouterLink to="/">
          <el-button type="primary">回到首页</el-button>
        </RouterLink>
      </template>
    </el-result>

    <template v-else-if="post">
      <el-card shadow="never" class="mb-4">
        <!-- 标题与正文同处 760px 阅读列（ui-guideline §6），否则标题贴卡片边、正文内缩，左缘不齐 -->
        <div class="mx-auto max-w-reading">
          <div class="flex items-center gap-2">
            <RouterLink :to="`/board/${post.boardCode}`" class="text-note text-primary hover:text-primary-hover">
              {{ post.boardName }}
            </RouterLink>
            <el-tag v-if="post.accepted" type="success" size="small">已采纳</el-tag>
          </div>
          <h1 class="my-2 text-h1 font-medium text-ink">{{ post.title }}</h1>
          <div class="flex flex-wrap gap-4 text-caption text-ink-meta">
            <span>{{ post.authorNickname }}</span>
            <span>发布于 {{ formatTime(post.createdAt) }}</span>
            <span>{{ post.replyCount }} 回复</span>
          </div>
          <el-divider />
          <div class="markdown-body" v-html="post.contentHtml" />
        </div>
      </el-card>

      <el-card shadow="never" class="mb-4">
        <template #header>
          <span class="font-medium">全部回复（{{ total }}）</span>
        </template>
        <el-empty v-if="!replies.length" description="还没有回复，来占一楼" />
        <ul v-else>
          <li
            v-for="r in replies"
            :key="r.id"
            class="border-b-[0.5px] border-divider py-3 last:border-b-0"
          >
            <div class="mx-auto max-w-reading">
              <div class="mb-1.5 flex flex-wrap items-center gap-3 text-caption text-ink-meta">
                <span class="font-medium text-primary">#{{ r.floorNo }} 楼</span>
                <span class="text-ink">{{ r.authorNickname }}</span>
                <span>{{ formatTime(r.createdAt) }}</span>
              </div>
              <div class="markdown-body" v-html="r.contentHtml" />
            </div>
          </li>
        </ul>
        <div class="mt-3 flex justify-center">
          <el-pagination
            v-if="total > size"
            layout="prev, pager, next"
            :total="total"
            :page-size="size"
            :current-page="page"
            @current-change="onPageChange"
          />
        </div>
      </el-card>

      <el-card shadow="never" class="mb-4">
        <template #header>
          <span class="font-medium">写回复</span>
        </template>
        <template v-if="auth.isLoggedIn">
          <el-input
            v-model="replyMd"
            type="textarea"
            :rows="6"
            maxlength="50000"
            show-word-limit
            placeholder="支持 Markdown 语法（标题 / 列表 / 代码块 / 链接）"
          />
          <el-button type="primary" class="mt-3" :loading="submitting" @click="submitReply">发表回复</el-button>
        </template>
        <el-alert v-else type="info" :closable="false" show-icon>
          <template #title>
            登录后才能回帖 ·
            <RouterLink :to="{ path: '/login', query: { redirect: route.fullPath } }">去登录</RouterLink>
          </template>
        </el-alert>
      </el-card>
    </template>

    <el-skeleton v-else :rows="8" animated />
  </div>
</template>
