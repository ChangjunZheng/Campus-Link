export interface UserInfo {
  id: number
  nickname: string
  avatarUrl?: string
  school?: string
  major?: string
  grade?: string
  bio?: string
  role: string
  /** 账号状态（F-ACC-007d）：ACTIVE / BANNED / DEACTIVATED；只回本人，设置页据此回显与驱动注销入口 */
  status?: string
  verified: boolean
  createdAt?: string
}
