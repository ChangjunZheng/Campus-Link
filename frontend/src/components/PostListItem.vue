<script setup lang="ts">
/**
 * 帖子列表项统一组件（CR-052，闭环 ui-guideline §5.1 的 P0 挂账）
 * 首页 / 版块 / 搜索 / 收藏四处列表共用，消除重复结构。
 * - 标题是真实 RouterLink（保留 href：可中键新开 / 右键复制 / 键盘聚焦），行 hover 通栏底色；
 * - 内部版块链接独立可点（@click.stop 阻止冒泡到标题的场景不存在，二者为并列元素）；
 * - 分隔线与内边距在此统一，父卡片 body padding 置 0。
 */
import type { PostSummaryVo } from '../api/forum'
import { boardNameOf } from '../constants/boards'
import { formatRelativeTime } from '../utils/time'
import SvgIcon from './SvgIcon.vue'
import UserAvatar from './UserAvatar.vue'

withDefaults(
  defineProps<{
    post: PostSummaryVo
    /** 是否显示版块入口（版块页不显示自己；首页 / 搜索 / 收藏显示） */
    showBoard?: boolean
    /** 是否显示摘要（默认显示，两行截断） */
    showSummary?: boolean
  }>(),
  { showBoard: false, showSummary: true },
)
</script>

<template>
  <li
    class="border-b-[0.5px] border-divider px-5 py-3 transition-colors duration-fast ease-standard last:border-b-0 hover:bg-bg-hover"
  >
    <div class="flex gap-3">
      <div class="min-w-0 flex-1">
        <div class="flex items-center gap-2">
          <el-tag v-if="post.accepted" type="success" effect="light" size="small" class="flex-none">
            已采纳
          </el-tag>
          <RouterLink
            :to="`/post/${post.id}`"
            class="min-w-0 flex-1 truncate text-title-sm font-medium text-ink hover:text-link"
          >
            {{ post.title }}
          </RouterLink>
          <span
            class="flex flex-none items-center gap-0.5 rounded-sm bg-code px-1.5 py-px text-caption text-ink-meta"
            :title="`${post.replyCount} 条回复`"
          >
            <SvgIcon name="message-2" :size="12" />
            {{ post.replyCount }}
          </span>
        </div>

        <p
          v-if="showSummary && post.summary"
          class="mt-1 line-clamp-2 text-note leading-body text-ink-regular"
        >
          {{ post.summary }}
        </p>

        <div class="mt-1.5 flex flex-wrap items-center gap-1.5 text-caption text-ink-meta">
          <template v-if="showBoard">
            <RouterLink :to="`/board/${post.boardCode}`" class="text-link hover:underline">
              {{ post.boardName || boardNameOf(post.boardCode) }}
            </RouterLink>
            <span aria-hidden="true">·</span>
          </template>
          <UserAvatar :name="post.authorNickname" :size="20" />
          <span>{{ post.authorNickname }}</span>
          <span aria-hidden="true">·</span>
          <span>{{ formatRelativeTime(post.createdAt) }}</span>
          <span aria-hidden="true">·</span>
          <span title="阅读数">阅读 {{ post.viewCount }}</span>
        </div>
      </div>
      <!-- 封面缩略图（CR-074）：无图帖不占位；仅 https 外链（服务端提取时已限定） -->
      <img
        v-if="post.coverUrl"
        :src="post.coverUrl"
        alt=""
        loading="lazy"
        class="h-16 w-24 flex-none rounded-md border-[0.5px] border-line object-cover max-md:hidden"
      />
    </div>
  </li>
</template>
