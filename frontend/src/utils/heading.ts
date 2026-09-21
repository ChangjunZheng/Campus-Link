/**
 * 标题锚点工具（Task #7 · 帖子目录 / 代码块复制按钮）
 *
 * 后端 MarkdownRenderer 只负责净化与渲染，不给 h1~h3 落 id —— 锚点是纯前端关注点，
 * 由 PostDetailView 在挂载后遍历补写、TableOfContents 用**同一份**函数解析 html
 * 生成条目，两侧 id 一致才能对得上。
 *
 * 边界：只在 v-html 渲染出的**已净化**片段上工作，不承担再次净化的职责。
 */

export interface HeadingEntry {
  id: string
  text: string
  level: number
}

/** 保留中英数字，其余压成 `-`；空串走 `heading-{index}` 兜底 */
function slugify(text: string, index: number): string {
  const slug = text
    .trim()
    .toLowerCase()
    .replace(/[^\w\u4e00-\u9fa5]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 48)
  return slug ? `h-${slug}` : `heading-${index}`
}

/**
 * 遍历 h1~h3，按顺序生成稳定 id（重复文本自动追加 `-2` / `-3`）。
 * 与 DOM 是否已有 id **无关** —— 调用方决定要不要覆写。
 *
 * 去重看的是**最终产出的 id**而不是原始 slug：否则像 `A` / `A` / `A-2` 这样的标题序列
 * 会先给第二个 `A` 生成 `h-a-2`，再给第三个标题（slug 恰好也是 `h-a-2`）生成同名 id，
 * 两个不同的标题撞到同一个 DOM id 上，目录跳转会错位。
 */
export function collectHeadings(root: ParentNode): HeadingEntry[] {
  const nodes = Array.from(root.querySelectorAll<HTMLElement>('h1, h2, h3'))
  const seen = new Set<string>()
  return nodes.map((el, index) => {
    const text = (el.textContent || '').replace(/\s+/g, ' ').trim()
    const base = slugify(text, index)
    let id = base
    let counter = 2
    while (seen.has(id)) {
      id = `${base}-${counter}`
      counter += 1
    }
    seen.add(id)
    return { id, text, level: Number(el.tagName.slice(1)) || 1 }
  })
}

/**
 * 给 DOM 中的 h1~h3 补 id（**只补缺失、不覆盖已有**，尊重后端未来若下发 id 的可能）。
 * 返回实际补写后的条目列表，供目录组件对齐使用。
 */
export function ensureHeadingIds(container: HTMLElement): HeadingEntry[] {
  const entries = collectHeadings(container)
  const nodes = Array.from(container.querySelectorAll<HTMLElement>('h1, h2, h3'))
  nodes.forEach((el, i) => {
    const entry = entries[i]
    if (!entry) return
    if (!el.id) el.id = entry.id
    // 若后端已给 id 且与我们生成的不一致，以后端为准，回写到条目上
    else if (el.id !== entry.id) entry.id = el.id
  })
  return entries
}

/**
 * 从 html 字符串解析目录条目（TableOfContents 用）：
 * 走 DOMParser 而非挂载到文档，避免副作用；id 生成逻辑与 ensureHeadingIds 同源。
 */
export function parseHeadingsFromHtml(html: string): HeadingEntry[] {
  if (!html) return []
  const doc = new DOMParser().parseFromString(`<div id="__toc_root">${html}</div>`, 'text/html')
  const root = doc.getElementById('__toc_root')
  return root ? collectHeadings(root) : []
}
