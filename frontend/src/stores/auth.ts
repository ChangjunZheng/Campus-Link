import { defineStore } from 'pinia'
import { fetchMe, login as loginApi, register as registerApi } from '../api/auth'
import type { UserInfo } from '../types'

const TOKEN_KEY = 'cl_token'
const USER_KEY = 'cl_user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null') as UserInfo | null,
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  actions: {
    async login(email: string, code: string) {
      const res = await loginApi(email, code)
      this.setSession(res.token, res.user)
    },
    async register(payload: { ticket: string; email: string; code: string; nickname: string }) {
      const res = await registerApi(payload)
      this.setSession(res.token, res.user)
    },
    async fetchMe() {
      this.setUser(await fetchMe())
    },
    /** 资料编辑成功后由 PUT 的全量回显直接落库（F-ACC-007a：不必二次拉取 `GET /users/me`） */
    setUser(user: UserInfo) {
      this.user = user
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    },
    setSession(token: string, user: UserInfo) {
      this.token = token
      this.user = user
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    },
  },
})
