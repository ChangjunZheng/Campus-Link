import { onBeforeUnmount, onMounted, ref } from 'vue'

/** 与 Tailwind md 断点、ui-guideline §7 同口径：<768px 视为窄屏 */
const QUERY = '(max-width: 767px)'

/**
 * 窄屏判定。只给 CSS 表达不了的属性用（如 textarea 的行数）；
 * 能用工具类 / 媒体查询的一律不用它。
 */
export function useNarrowScreen() {
  const mql = window.matchMedia(QUERY)
  const narrow = ref(mql.matches)
  const onChange = (e: MediaQueryListEvent) => {
    narrow.value = e.matches
  }
  onMounted(() => mql.addEventListener('change', onChange))
  onBeforeUnmount(() => mql.removeEventListener('change', onChange))
  return narrow
}
