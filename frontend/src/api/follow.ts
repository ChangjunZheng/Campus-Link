import { del, get, post } from './client'

/** 关注状态（CR-074）：following 为登录者视角；fanCount / followCount 为目标的粉丝数与关注数 */
export interface FollowStateVo {
  following: boolean
  fanCount: number
  followCount: number
}

/** 关注用户（不能关注自己 → 400 / 2009；重复关注幂等） */
export const followUser = (targetId: number) => post<void>(`/users/${targetId}/follow`)

/** 取消关注（未关注时幂等） */
export const unfollowUser = (targetId: number) => del<void>(`/users/${targetId}/follow`)

/** 我与目标的关注状态 + 目标的粉丝 / 关注计数（需登录） */
export const getFollowState = (targetId: number) => get<FollowStateVo>(`/users/${targetId}/follow-state`)
