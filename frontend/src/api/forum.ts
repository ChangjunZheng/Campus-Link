import { get, post } from './client'

/** 与后端 web/vo 一一对应（设计 §3），字段名即接口契约，勿改名 */
export interface BoardVo {
  code: string
  name: string
  description: string
  type: string
  sort: number
}

export interface PageVo<T> {
  list: T[]
  total: number
  page: number
  size: number
}

export interface PostSummaryVo {
  id: number
  boardCode: string
  boardName: string
  title: string
  authorNickname: string
  replyCount: number
  likeCount: number
  /** 服务端已去 Markdown 并截断为 120 字（设计 §3.2），前端不再兜底截断 */
  summary: string
  createdAt: string
  /** 已有最佳答案（F-QA-001），列表页 [已采纳] 徽标 */
  accepted: boolean
}

export interface PostDetailVo {
  id: number
  boardCode: string
  boardName: string
  /** QUESTION = 技术问答帖（提问），其余为讨论帖 */
  boardType: string
  title: string
  /** 服务端渲染并净化后的 HTML，前端直接 v-html（XSS 防线在 MarkdownRenderer） */
  contentHtml: string
  authorId: number
  authorNickname: string
  replyCount: number
  likeCount: number
  accepted: boolean
  createdAt: string
}

export interface ReplyVo {
  id: number
  /** 楼层 = 回复序号（第一条回复为 1 楼，帖子本体不占楼层号，设计 §4.1） */
  floorNo: number
  contentHtml: string
  authorId: number
  authorNickname: string
  /** 最佳答案（F-QA-001）；楼层列表按 is_accepted DESC 排序，最佳答案自然置顶 */
  accepted: boolean
  createdAt: string
}

export const listBoards = () => get<BoardVo[]>('/boards')

/** boardCode 缺省即全站最新；分页 page 从 1 起、size 上限 100（设计 §3.2） */
export const listPosts = (page = 1, size = 20, boardCode?: string) => {
  const query = new URLSearchParams({ page: String(page), size: String(size) })
  if (boardCode) {
    query.set('boardCode', boardCode)
  }
  return get<PageVo<PostSummaryVo>>(`/posts?${query.toString()}`)
}

export const getPost = (id: number | string) => get<PostDetailVo>(`/posts/${id}`)

export const listReplies = (postId: number | string, page = 1, size = 20) =>
  get<PageVo<ReplyVo>>(`/posts/${postId}/replies?page=${page}&size=${size}`)

export const publishPost = (payload: { boardCode: string; title: string; contentMd: string }) =>
  post<{ id: number }>('/posts', payload)

export const publishReply = (postId: number | string, contentMd: string) =>
  post<{ id: number; floorNo: number }>(`/posts/${postId}/replies`, { contentMd })

/** 采纳最佳答案（仅提问者）：可更换（后一次覆盖前一次），不能采纳自己的回复（3003） */
export const acceptReply = (postId: number | string, replyId: number) =>
  post<void>(`/posts/${postId}/accept`, { replyId })
