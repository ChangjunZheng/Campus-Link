export interface BoardMeta {
  code: string
  name: string
  desc: string
}

/** 固定 6 版块（PRD F-FORUM-001）——导航与首页共用，避免多处硬编码（DRY） */
export const BOARDS: BoardMeta[] = [
  { code: 'qna', name: '技术问答', desc: '提问、报错排查、环境配置、技术选型' },
  { code: 'resources', name: '学习资源', desc: '教程、笔记、工具、资源分享' },
  { code: 'interview', name: '面经求职', desc: '实习 / 校招面经、内推、职业规划' },
  { code: 'contest', name: '竞赛交流', desc: 'ACM / 蓝桥杯 / 数模、组队、真题' },
  { code: 'course', name: '课程交流', desc: '课程攻略、作业讨论、考试经验' },
  { code: 'chat', name: '闲聊灌水', desc: '校园生活、轻松话题' },
]

export const BOARD_NAMES: Record<string, string> = Object.fromEntries(
  BOARDS.map((b) => [b.code, b.name]),
)
