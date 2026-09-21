import { get, post, put } from './client'
import type { PageVo } from './forum'

/** 通知类型码，与后端 notifications.type 落库值一致（quote 尚未实现，故不在此列出） */
export type NotificationType = 'reply' | 'like' | 'favorite' | 'accept'

/**
 * 与后端 web/vo/NotificationVo 一一对应（F-SOC-001）。
 * 内容字段（昵称 / 标题 / 楼层号）由服务端读时组装：触发人已注销时昵称是「已注销用户」，
 * 原帖已删除时标题是「内容已删除」且不给 postId（此时前端不应给出跳转）。
 * ⚠️ 后端全局 `default-property-inclusion: non_null`，null 字段直接从 JSON 消失，
 * 因此可空字段声明为可选（判空一律用真值判断，不要用 `=== null`）。
 */
export interface NotificationVo {
  id: number
  type: NotificationType
  actorId: number
  actorNickname: string
  /** POST = 目标是帖子（点赞 / 收藏），REPLY = 目标是楼层（回复 / 采纳 / 楼层点赞） */
  targetType: 'POST' | 'REPLY'
  targetId: number
  /** 目标帖已删除时不下发 */
  postId?: number
  postTitle: string
  /** 仅楼层类通知有值，用于「#N 楼」提示 */
  floorNo?: number
  read: boolean
  createdAt: string
}

export interface UnreadCountVo {
  unreadCount: number
}

/** 我的通知（需登录）：unread 缺省不限、true 只未读；按时间倒序 */
export const listNotifications = (opts: { unread?: boolean; page?: number; size?: number } = {}) => {
  const query = new URLSearchParams({
    page: String(opts.page ?? 1),
    size: String(opts.size ?? 20),
  })
  if (opts.unread !== undefined) {
    query.set('unread', String(opts.unread))
  }
  return get<PageVo<NotificationVo>>(`/notifications?${query.toString()}`)
}

/** 未读数（顶栏铃铛角标） */
export const getUnreadCount = () => get<UnreadCountVo>('/notifications/unread-count')

/** 全部标记已读：updated 为本次新标记的条数 */
export const markAllRead = () => put<{ updated: number }>('/notifications/read-all')

/**
 * 单条标记已读（幂等，CR-078 功能 C）：无请求体也无出参。
 * 已读再调仍 200；不存在与非本人一律 404 / 3001（后端不区分二者，避免泄露他人通知的存在性）。
 */
export const markNotificationRead = (id: number) => post<void>(`/notifications/${id}/read`)
