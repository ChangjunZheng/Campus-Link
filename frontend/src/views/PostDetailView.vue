<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPost, listReplies, publishReply, type PostDetailVo, type ReplyVo } from '../api/forum'
import { ApiError } from '../api/client'
import { useAuthStore } from '../stores/auth'
import { useNarrowScreen } from '../composables/useNarrowScreen'
import { formatTime } from '../utils/time'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const narrow = useNarrowScreen()
const postId = computed(() => String(route.params.id || ''))
const post = ref<PostDetailVo | null>(null)
const missing = ref(false)
const postError = ref('')
const replies = ref<ReplyVo[]>([])
const total = ref(0)
const page = ref(1)
const size = 20
const repliesLoading = ref(false)
const repliesError = ref('')
const replyMd = ref('')
const submitting = ref(false)

async function loadPost() {
  missing.value = false
  postError.value = ''
  post.value = null
  try {
    post.value = await getPost(postId.value)
  } catch (e) {
    // 3001 = 帖子不存在 / 已删除 / 未发布（设计 §3.4），单独给「不存在」态而非通用报错
    if (e instanceof ApiError && e.code === 3001) {
      missing.value = true
    } else {
      postError.value = e instanceof Error ? e.message : '帖子加载失败'
    }
  }
}

async function loadReplies() {
  repliesLoading.value = true
  repliesError.value = ''
  try {
    const res = await listReplies(postId.value, page.value, size)
    replies.value = res.list
    total.value = res.total
  } catch (e) {
    replies.value = []
    total.value = 0
    // 帖子本身不存在（3001）时由 loadPost 渲染整页「不存在」态；两个请求并发，此处可能先于 loadPost 失败，
    // 只按错误码判断才能保证不重复报错
    if (!(e instanceof ApiError && e.code === 3001)) {
      repliesError.value = e instanceof Error ? e.message : '楼层加载失败'
    }
  } finally {
    repliesLoading.value = false
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
  <div>
    <el-result v-if="missing" icon="warning" title="帖子不存在或已删除" sub-title="它可能已被作者删除，或链接有误">
      <template #extra>
        <RouterLink to="/">
          <el-button type="primary">回到首页</el-button>
        </RouterLink>
      </template>
    </el-result>

    <el-alert
      v-else-if="postError"
      type="error"
      :closable="false"
      show-icon
      :title="postError"
    >
      <el-button size="small" @click="loadPost">重新加载</el-button>
    </el-alert>

    <template v-else-if="post">
      <el-card shadow="never" class="mb-4">
        <!-- 标题与正文同处 760px 阅读列（ui-guideline §6），否则标题贴卡片边、正文内缩，左缘不齐 -->
        <div class="mx-auto max-w-reading">
          <div class="flex items-center gap-2">
            <RouterLink :to="`/board/${post.boardCode}`">
              <el-tag type="primary" effect="light" size="small">{{ post.boardName }}</el-tag>
            </RouterLink>
            <el-tag v-if="post.accepted" type="success" effect="light" size="small">已采纳</el-tag>
          </div>
          <h1 class="my-2 text-h1 font-medium text-ink">{{ post.title }}</h1>
          <div class="flex flex-wrap items-center gap-1.5 text-caption text-ink-meta">
            <span>{{ post.authorNickname }}</span>
            <span>·</span>
            <span>发布于 {{ formatTime(post.createdAt) }}</span>
            <span>·</span>
            <span>{{ post.replyCount }} 回复</span>
          </div>
          <el-divider />
          <div class="markdown-body" v-html="post.contentHtml" />
        </div>
      </el-card>

      <el-card shadow="never" class="mb-4">
        <template #header>
          <span class="font-medium">
            全部回复<template v-if="!repliesLoading && !repliesError">（{{ total }}）</template>
          </span>
        </template>

        <el-skeleton v-if="repliesLoading" animated>
          <template #template>
            <div
              v-for="i in 3"
              :key="i"
              class="border-b-[0.5px] border-divider py-3 first:pt-0 last:border-b-0 last:pb-0"
            >
              <div class="mx-auto max-w-reading">
                <el-skeleton-item variant="text" style="width: 42%" />
                <el-skeleton-item variant="text" class="mt-2" style="width: 100%" />
                <el-skeleton-item variant="text" class="mt-1" style="width: 68%" />
              </div>
            </div>
          </template>
        </el-skeleton>

        <el-alert
          v-else-if="repliesError"
          type="error"
          :closable="false"
          show-icon
          :title="repliesError"
        >
          <el-button size="small" @click="loadReplies">重新加载</el-button>
        </el-alert>

        <template v-else>
          <p v-if="!replies.length" class="py-6 text-center text-body text-ink-regular">
            还没有回复，来占一楼
          </p>
          <ul v-else>
            <li
              v-for="r in replies"
              :key="r.id"
              class="border-b-[0.5px] border-divider py-3 first:pt-0 last:border-b-0"
            >
              <div class="mx-auto max-w-reading">
                <div class="mb-1.5 flex flex-wrap items-center gap-1.5 text-caption text-ink-meta">
                  <span class="font-medium text-link">#{{ r.floorNo }} 楼</span>
                  <span>·</span>
                  <span class="text-ink">{{ r.authorNickname }}</span>
                  <span>·</span>
                  <span>{{ formatTime(r.createdAt) }}</span>
                </div>
                <div class="markdown-body" v-html="r.contentHtml" />
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

          <div class="mx-auto mt-4 max-w-reading">
            <template v-if="auth.isLoggedIn">
              <el-input
                v-model="replyMd"
                type="textarea"
                :rows="narrow ? 4 : 6"
                maxlength="50000"
                show-word-limit
                placeholder="写下你的回复…支持 Markdown（回复发布后按楼层展示）"
              />
              <div class="mt-3 flex justify-end">
                <el-button type="primary" :loading="submitting" @click="submitReply">回复</el-button>
              </div>
            </template>
            <el-alert v-else type="info" :closable="false" show-icon>
              <template #title>
                登录后才能回帖 ·
                <RouterLink :to="{ path: '/login', query: { redirect: route.fullPath } }">去登录</RouterLink>
              </template>
            </el-alert>
          </div>
        </template>
      </el-card>
    </template>

    <el-card v-else shadow="never">
      <el-skeleton animated>
        <template #template>
          <div class="mx-auto max-w-reading">
            <el-skeleton-item variant="text" style="width: 88px" />
            <el-skeleton-item variant="h1" class="mt-3" style="width: 72%" />
            <div class="mt-3 flex items-center gap-2">
              <el-skeleton-item variant="text" style="width: 56px" />
              <el-skeleton-item variant="text" style="width: 136px" />
              <el-skeleton-item variant="text" style="width: 56px" />
            </div>
            <el-skeleton-item variant="text" class="mt-5" style="width: 100%" />
            <el-skeleton-item variant="text" class="mt-2" style="width: 100%" />
            <el-skeleton-item variant="text" class="mt-2" style="width: 64%" />
          </div>
        </template>
      </el-skeleton>
    </el-card>
  </div>
</template>
