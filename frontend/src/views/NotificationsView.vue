<script setup lang="ts">
import { ref } from 'vue'
import { listNotifications, markAllRead, type NotificationVo } from '../api/notifications'
import { refreshUnreadCount } from '../composables/useUnreadNotifications'
import { formatRelativeTime } from '../utils/time'

const items = ref<NotificationVo[]>([])
const total = ref(0)
const page = ref(1)
const size = 20
const loading = ref(false)
const loadError = ref('')
/** null = 全部；true = 只看未读（后端 unread 参数三态） */
const onlyUnread = ref(false)
const marking = ref(false)

async function loadNotifications() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await listNotifications({
      unread: onlyUnread.value ? true : undefined,
      page: page.value,
      size,
    })
    items.value = res.list
    total.value = res.total
  } catch (e) {
    items.value = []
    total.value = 0
    loadError.value = e instanceof Error ? e.message : '通知加载失败'
  } finally {
    loading.value = false
  }
}

function onFilterChange() {
  page.value = 1
  loadNotifications()
}

function onPageChange(next: number) {
  page.value = next
  loadNotifications()
}

async function readAll() {
  marking.value = true
  try {
    await markAllRead()
    // 角标与本页状态都要跟上：unread-count 是另一条查询，不靠 30s 轮询自然收敛
    await Promise.all([refreshUnreadCount(), loadNotifications()])
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    marking.value = false
  }
}

/** 动作短语：type 决定动词，targetType 决定对象是帖子还是楼层 */
function actionOf(n: NotificationVo): string {
  if (n.type === 'reply') return '回复了你的帖子'
  if (n.type === 'accept') return '采纳了你的回复'
  if (n.type === 'favorite') return '收藏了你的帖子'
  return n.targetType === 'POST' ? '点赞了你的帖子' : '点赞了你的回复'
}

/** 楼层类通知带 floor=<replyId> 让详情页定位楼层；原帖已删（服务端不下发 postId）时不给跳转 */
function targetOf(n: NotificationVo) {
  if (!n.postId) return null
  return n.targetType === 'REPLY'
    ? { path: `/post/${n.postId}`, query: { floor: String(n.targetId) } }
    : { path: `/post/${n.postId}` }
}

loadNotifications()
</script>

<template>
  <el-card shadow="never" class="mb-4">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h2 class="text-title-lg font-medium text-ink">通知中心</h2>
        <p class="mt-1.5 text-note text-ink-meta">回复、采纳、点赞与收藏的站内提醒，仅本人可见</p>
      </div>
      <div class="flex items-center gap-3">
        <el-radio-group v-model="onlyUnread" @change="onFilterChange">
          <el-radio-button :value="false">全部</el-radio-button>
          <el-radio-button :value="true">未读</el-radio-button>
        </el-radio-group>
        <el-button text type="primary" :loading="marking" @click="readAll">全部已读</el-button>
      </div>
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
          <el-skeleton-item variant="text" class="mt-2" style="width: 72%" />
        </div>
      </template>
    </el-skeleton>

    <el-alert v-else-if="loadError" type="error" :closable="false" show-icon :title="loadError">
      <el-button size="small" @click="loadNotifications">重新加载</el-button>
    </el-alert>

    <div v-else-if="!items.length" class="py-10 text-center">
      <p class="text-body text-ink-regular">{{ onlyUnread ? '没有未读通知' : '还没有收到通知' }}</p>
      <RouterLink to="/">
        <el-button type="primary" class="mt-4">去逛逛版块</el-button>
      </RouterLink>
    </div>

    <template v-else>
      <ul>
        <li
          v-for="n in items"
          :key="n.id"
          class="flex items-start gap-2.5 border-b-[0.5px] border-divider py-3 first:pt-0 last:border-b-0 last:pb-0"
        >
          <!-- 未读用小圆点占位，保证已读 / 未读行首对齐（不为对齐再用 invisible 字符） -->
          <span
            class="mt-2 h-1.5 w-1.5 shrink-0 rounded-full"
            :class="n.read ? 'bg-transparent' : 'bg-primary'"
          />
          <div class="min-w-0 flex-1">
            <p class="text-body text-ink-regular">
              <span class="font-medium text-ink">{{ n.actorNickname }}</span>
              {{ actionOf(n) }}
              <template v-if="n.floorNo">#{{ n.floorNo }} 楼</template>
            </p>
            <RouterLink
              v-if="targetOf(n)"
              :to="targetOf(n)!"
              class="mt-1 block truncate text-title-sm text-link hover:underline"
            >
              {{ n.postTitle }}
            </RouterLink>
            <p v-else class="mt-1 truncate text-title-sm text-ink-meta">{{ n.postTitle }}</p>
            <p class="mt-1 text-caption text-ink-meta">{{ formatRelativeTime(n.createdAt) }}</p>
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
