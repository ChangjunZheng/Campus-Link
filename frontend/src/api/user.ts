import { get } from './client'
import type { PageVo, PostSummaryVo } from './forum'

/**
 * 他人主页的公开资料（F-ACC-002 本体最小版），对应后端 `account.web.UserProfileVo`。
 *
 * 与 `types.ts` 的 `UserInfo`（`GET /users/me`，本人视角）**刻意不同形**：那份带
 * `role` / `status` / `verified` 与 email 之外的账号列，这份一律没有——公开资料只有展示四列 + 三个计数。
 * 也**没有 `following`**：那一位需要 viewer，而本端点是匿名的；登录态下另调 `api/follow.ts` 的
 * `getFollowState` 取（与帖子详情页同一做法）。
 */
export interface UserProfileVo {
  id: number
  nickname: string
  /** 专业（学籍名册导入 / 本人可改）；未填为 null */
  major: string | null
  /** 个性签名；未填为 null */
  bio: string | null
  /** 加入时间，ISO-8601 UTC */
  createdAt: string
  postCount: number
  followerCount: number
  followingCount: number
}

/** 用户公开资料（公开端点，匿名可读）；用户不存在或已注销 → 404 / 2007 */
export const getUserProfile = (id: number | string) => get<UserProfileVo>(`/users/${id}`)

/**
 * 某用户的公开帖子（公开端点，匿名可读）：时间倒序分页，出参与全站列表**同形**
 * （`PostSummaryVo`），故主页时间线直接复用 `PostListItem` 组件。
 *
 * 可见性口径同全站（`PUBLISHED` 且未删除）：已下架与已删除的帖一律不出现，与「我的帖子」相反。
 */
export const getUserPosts = (id: number | string, page = 1, size = 20) =>
  get<PageVo<PostSummaryVo>>(`/users/${id}/posts?page=${page}&size=${size}`)
