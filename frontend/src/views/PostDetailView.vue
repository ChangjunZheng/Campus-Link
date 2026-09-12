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
    if (!missing.value) {
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
      <el-card shadow="never" class="post-card">
        <div class="post-head">
          <RouterLink :to="`/board/${post.boardCode}`" class="board-link">{{ post.boardName }}</RouterLink>
          <el-tag v-if="post.accepted" type="success" size="small">已采纳</el-tag>
        </div>
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-meta">
          <span>{{ post.authorNickname }}</span>
          <span>发布于 {{ formatTime(post.createdAt) }}</span>
          <span>{{ post.replyCount }} 回复</span>
        </div>
        <el-divider />
        <div class="markdown-body" v-html="post.contentHtml" />
      </el-card>

      <el-card shadow="never" class="reply-card">
        <template #header>
          <span>全部回复（{{ total }}）</span>
        </template>
        <el-empty v-if="!replies.length" description="还没有回复，来占一楼" />
        <ul v-else class="reply-list">
          <li v-for="r in replies" :key="r.id">
            <div class="reply-head">
              <span class="floor">#{{ r.floorNo }} 楼</span>
              <span class="reply-author">{{ r.authorNickname }}</span>
              <span class="reply-time">{{ formatTime(r.createdAt) }}</span>
            </div>
            <div class="markdown-body" v-html="r.contentHtml" />
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
      </el-card>

      <el-card shadow="never" class="reply-card">
        <template #header>
          <span>写回复</span>
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
          <el-button type="primary" class="submit" :loading="submitting" @click="submitReply">发表回复</el-button>
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

<style scoped>
.post-card,
.reply-card {
  margin-bottom: 16px;
}
.post-head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.board-link {
  color: #409eff;
  font-size: 13px;
  text-decoration: none;
}
.post-title {
  margin: 8px 0;
  font-size: 22px;
}
.post-meta {
  display: flex;
  gap: 16px;
  color: #909399;
  font-size: 12px;
}
.reply-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.reply-list li {
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
}
.reply-list li:last-child {
  border-bottom: none;
}
.reply-head {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 6px;
  font-size: 12px;
  color: #909399;
}
.floor {
  color: #409eff;
  font-weight: 600;
}
.reply-author {
  color: #303133;
}
.submit {
  margin-top: 12px;
}
.pager {
  margin-top: 12px;
  display: flex;
  justify-content: center;
}
.markdown-body {
  font-size: 14px;
  line-height: 1.7;
  color: #303133;
  overflow-wrap: anywhere;
}
.markdown-body :deep(pre) {
  background: #f6f8fa;
  padding: 10px 12px;
  border-radius: 6px;
  overflow-x: auto;
}
.markdown-body :deep(code) {
  font-family: Consolas, Monaco, monospace;
}
.markdown-body :deep(img) {
  max-width: 100%;
}
.markdown-body :deep(blockquote) {
  margin: 0;
  padding-left: 12px;
  border-left: 3px solid #dcdfe6;
  color: #606266;
}
</style>
