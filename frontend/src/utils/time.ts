/** 后端时间为 ISO-8601 UTC（如 2026-09-12T14:53:29Z），统一转为本地时区展示 */
export function formatTime(iso: string): string {
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) {
    return iso
  }
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

/** 列表元信息用相对时间（UI 设计稿口径），超过 7 天回退为绝对时间 */
export function formatRelativeTime(iso: string): string {
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) {
    return iso
  }
  const diff = Date.now() - date.getTime()
  if (diff < 60_000) {
    return '刚刚'
  }
  if (diff < 3_600_000) {
    return `${Math.floor(diff / 60_000)} 分钟前`
  }
  if (diff < 86_400_000) {
    return `${Math.floor(diff / 3_600_000)} 小时前`
  }
  const startOfToday = new Date()
  startOfToday.setHours(0, 0, 0, 0)
  const startOfDate = new Date(date)
  startOfDate.setHours(0, 0, 0, 0)
  const days = Math.round((startOfToday.getTime() - startOfDate.getTime()) / 86_400_000)
  if (days === 1) {
    return '昨天'
  }
  if (days === 2) {
    return '前天'
  }
  if (days < 7) {
    return `${days} 天前`
  }
  return formatTime(iso)
}
