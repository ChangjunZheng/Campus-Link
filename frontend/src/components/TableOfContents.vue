<script setup lang="ts">
/**
 * 帖子目录（Task #7）
 *
 * 输入：v-html 已经渲染出去的 contentHtml 字符串。
 * 输出：一份可点击、能跟随滚动高亮的目录。
 *
 * 关键约束：
 *   ① id 生成走 utils/heading.ts 的 parseHeadingsFromHtml，与 PostDetailView 里
 *      ensureHeadingIds 用的是同一份 slugify —— 两边不对齐就跳不动。
 *   ② 只解析、不改写外部 DOM；实际的 h1~h3 由 PostDetailView 补 id。
 *   ③ IntersectionObserver 在卸载与条目变化时必须 disconnect，否则路由切换后
 *      仍会往已销毁的组件写状态。
 */
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { parseHeadingsFromHtml, type HeadingEntry } from '../utils/heading'

const props = defineProps<{ html: string }>()

interface TocItem extends HeadingEntry {
  number: string
  depth: number
}

/** 相对深度：把 h1~h3 归一到 0~2，兼容文档从 h2 起手的情况 */
const items = computed<TocItem[]>(() => {
  const raw = parseHeadingsFromHtml(props.html)
  if (!raw.length) return []
  const minLevel = Math.min(...raw.map((r) => r.level))
  const counters = [0, 0, 0]
  return raw.map((r) => {
    const depth = Math.min(2, Math.max(0, r.level - minLevel))
    counters[depth] += 1
    for (let i = depth + 1; i < counters.length; i += 1) counters[i] = 0
    return {
      ...r,
      depth,
      number: counters.slice(0, depth + 1).join('.'),
    }
  })
})

const activeId = ref<string>('')
let observer: IntersectionObserver | null = null

function disconnect() {
  observer?.disconnect()
  observer = null
}

/**
 * 顶部让出 96px 给可能的粘性头部，底部只留 30% —— 让"当前章节"的判定
 * 贴合读者视线所在的正文上三分之一，而不是一进入视口就抢焦。
 */
function observe() {
  disconnect()
  if (!items.value.length) return
  const targets = items.value
    .map((it) => document.getElementById(it.id))
    .filter((el): el is HTMLElement => el !== null)
  if (!targets.length) return

  observer = new IntersectionObserver(
    (entries) => {
      const visible = entries
        .filter((e) => e.isIntersecting)
        .sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top)
      if (visible.length) {
        activeId.value = visible[0].target.id
      }
    },
    { rootMargin: '-96px 0px -70% 0px', threshold: 0 },
  )
  targets.forEach((el) => observer!.observe(el))
}

function onJump(item: TocItem) {
  const el = document.getElementById(item.id)
  if (!el) return
  activeId.value = item.id
  el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  // hash 只作为可分享锚点，用 replaceState 避免在浏览器历史里堆一堆跳转记录。
  // state 必须传现有值而不是 null：vue-router 把自己的路由簿记（back/current/forward/
  // position/scroll）存在 history.state 里，传 null 会整体清空，导致 dev 告警且
  // 滚动位置恢复退化。
  if (history.replaceState) {
    history.replaceState(history.state, '', `#${item.id}`)
  }
}

watch(
  items,
  async () => {
    // 等 PostDetailView 的 ensureHeadingIds 与 v-html 都落定
    await nextTick()
    observe()
    // 初始高亮：优先命中 URL hash，否则给首项
    const hash = decodeURIComponent(location.hash.slice(1))
    if (hash && items.value.some((i) => i.id === hash)) {
      activeId.value = hash
    } else if (items.value.length && !activeId.value) {
      activeId.value = items.value[0].id
    }
  },
  { immediate: true },
)

onBeforeUnmount(disconnect)
</script>

<template>
  <nav v-if="items.length" class="cl-toc" aria-label="帖子目录">
    <header class="cl-toc__head">
      <span class="cl-toc__label">目录</span>
      <span class="cl-toc__rule" aria-hidden="true"></span>
      <span class="cl-toc__count">{{ items.length }}</span>
    </header>

    <ol class="cl-toc__list">
      <li
        v-for="item in items"
        :key="item.id"
        class="cl-toc__item"
        :class="[`cl-toc__item--d${item.depth}`, { 'is-active': activeId === item.id }]"
      >
        <a
          class="cl-toc__link"
          :href="`#${item.id}`"
          @click.prevent="onJump(item)"
        >
          <span class="cl-toc__num" aria-hidden="true">{{ item.number }}</span>
          <span class="cl-toc__text">{{ item.text || '（无标题）' }}</span>
        </a>
      </li>
    </ol>
  </nav>
</template>

<style scoped>
.cl-toc {
  font-family: var(--cl-font-family);
  color: var(--cl-text-regular);
  user-select: none;
}

.cl-toc__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  padding-left: 10px;
}

.cl-toc__label {
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.16em;
  color: var(--cl-text-meta);
  text-transform: uppercase;
}

.cl-toc__rule {
  flex: 1;
  height: 1px;
  background: linear-gradient(
    to right,
    var(--cl-border-base),
    transparent
  );
}

.cl-toc__count {
  font-family: ui-monospace, SFMono-Regular, 'JetBrains Mono', Menlo, Consolas, monospace;
  font-size: 11px;
  font-variant-numeric: tabular-nums;
  color: var(--cl-text-meta);
  padding: 1px 6px;
  border: 1px solid var(--cl-border-base);
  border-radius: 999px;
  line-height: 1.4;
}

.cl-toc__list {
  list-style: none;
  margin: 0;
  padding: 0;
  border-left: 1px solid var(--cl-border-base);
  position: relative;
}

.cl-toc__item {
  position: relative;
  margin: 0;
  padding: 0;
  transition: background-color var(--cl-duration-fast) var(--cl-ease-standard);
}

/* 左侧高亮竖条：用 ::before 而非 border-l，避免撑动布局 */
.cl-toc__item::before {
  content: '';
  position: absolute;
  left: -1px;
  top: 4px;
  bottom: 4px;
  width: 2px;
  background: var(--cl-color-primary);
  transform: scaleY(0);
  transform-origin: center;
  transition: transform var(--cl-duration-base) var(--cl-ease-standard);
  border-radius: 1px;
}

.cl-toc__item.is-active::before {
  transform: scaleY(1);
}

.cl-toc__item.is-active {
  background: linear-gradient(
    to right,
    color-mix(in srgb, var(--cl-color-primary) 6%, transparent),
    transparent 70%
  );
}

.cl-toc__link {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 5px 8px 5px 12px;
  text-decoration: none;
  color: inherit;
  font-size: 12.5px;
  line-height: 1.55;
  transition:
    color var(--cl-duration-fast) var(--cl-ease-standard),
    transform var(--cl-duration-fast) var(--cl-ease-standard);
}

.cl-toc__link:hover {
  color: var(--cl-color-primary);
  transform: translateX(2px);
}

.cl-toc__item.is-active .cl-toc__link {
  color: var(--cl-color-primary);
  font-weight: 500;
}

.cl-toc__num {
  flex: 0 0 auto;
  font-family: ui-monospace, SFMono-Regular, 'JetBrains Mono', Menlo, Consolas, monospace;
  font-size: 11px;
  font-variant-numeric: tabular-nums;
  color: var(--cl-text-meta);
  min-width: 26px;
  transition: color var(--cl-duration-fast) var(--cl-ease-standard);
}

.cl-toc__item.is-active .cl-toc__num,
.cl-toc__link:hover .cl-toc__num {
  color: var(--cl-color-primary);
}

.cl-toc__text {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  /* 长中文标题两行截断，避免侧栏被单条撑破 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
}

/* 层级缩进：每深一级 +10px */
.cl-toc__item--d1 .cl-toc__link {
  padding-left: 22px;
  font-size: 12px;
}
.cl-toc__item--d2 .cl-toc__link {
  padding-left: 32px;
  font-size: 11.5px;
  color: var(--cl-text-meta);
}
.cl-toc__item--d2.is-active .cl-toc__link,
.cl-toc__item--d2 .cl-toc__link:hover {
  color: var(--cl-color-primary);
}

/* 键盘可达性：聚焦时给出与激活态一致的可视化 */
.cl-toc__link:focus-visible {
  outline: none;
  box-shadow: var(--cl-focus-ring);
  border-radius: 2px;
  color: var(--cl-color-primary);
}
</style>
