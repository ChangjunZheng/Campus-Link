import { onBeforeUnmount, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getUnreadCount } from '../api/notifications'
import { useAuthStore } from '../stores/auth'

/**
 * 顶栏铃铛未读角标的共享状态（F-SOC-001）。
 *
 * <p>用模块级 ref 而不是 Pinia：全站只有顶栏一个消费者、通知中心页「全部已读」后需要顺带清零，
 * 一份计数不值得再开一个 store。刷新时机：登录后取一次 + 路由切换刷新 + 30s 轮询兜底——
 * 没有实时推送就必然有滞后。取数失败静默：角标是提示不是主链路，下一次轮询自然恢复。
 */
const POLL_INTERVAL_MS = 30_000

const unreadCount = ref(0)
let pollTimer: ReturnType<typeof setInterval> | null = null

export async function refreshUnreadCount(): Promise<void> {
  const auth = useAuthStore()
  if (!auth.isLoggedIn) {
    unreadCount.value = 0
    return
  }
  try {
    unreadCount.value = (await getUnreadCount()).unreadCount
  } catch {
    // 静默：见上文说明
  }
}

export function useUnreadNotifications() {
  const auth = useAuthStore()
  const route = useRoute()

  function armPolling() {
    if (pollTimer === null) {
      pollTimer = setInterval(refreshUnreadCount, POLL_INTERVAL_MS)
    }
  }

  function disarmPolling() {
    if (pollTimer !== null) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  watch(
    () => auth.isLoggedIn,
    (loggedIn) => {
      if (loggedIn) {
        armPolling()
      } else {
        disarmPolling()
      }
      void refreshUnreadCount()
    },
    { immediate: true },
  )

  // 站内动作（回帖 / 点赞 / 全部已读）不落服务端推送，靠切页时补一次读
  watch(() => route.fullPath, () => void refreshUnreadCount())

  onBeforeUnmount(disarmPolling)

  return { unreadCount, refreshUnreadCount }
}
