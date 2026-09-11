import { ref } from 'vue'

/**
 * 组合式函数：验证码发送 60s 倒计时（登录 / 注册两处复用）。
 * 使用方式：const cd = useCountdown(); cd.run(() => sendCaptcha(email))
 */
export function useCountdown(seconds = 60) {
  const sending = ref(false)
  const remaining = ref(0)
  let timer: number | undefined

  async function run(action: () => Promise<void>) {
    if (remaining.value > 0 || sending.value) return
    sending.value = true
    try {
      await action()
      remaining.value = seconds
      timer = window.setInterval(() => {
        remaining.value--
        if (remaining.value <= 0 && timer) window.clearInterval(timer)
      }, 1000)
    } finally {
      sending.value = false
    }
  }

  return { sending, remaining, run }
}
