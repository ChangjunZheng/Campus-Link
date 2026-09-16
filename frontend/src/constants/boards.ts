import type { IconName } from '../assets/icons'

/** 版块编码（后端 board.code 同值，改值需与后端 / 数据迁移联动） */
export type BoardCode =
  | 'qna'
  | 'resources'
  | 'interview'
  | 'contest'
  | 'course'
  | 'chat'

export interface BoardDef {
  code: BoardCode
  name: string
  description: string
  /** 版块线性图标（CR-055：Tabler 图标名，经 components/SvgIcon.vue 渲染） */
  icon: IconName
}

/** 固定 6 版块（PRD 5.2，运营不增删） */
export const BOARDS: BoardDef[] = [
  {
    code: 'qna',
    name: '技术问答',
    description: '课程、编程、环境配置的问题与解答',
    icon: 'message-circle',
  },
  {
    code: 'resources',
    name: '学习资源',
    description: '课件、笔记、教程与工具分享',
    icon: 'book-2',
  },
  {
    code: 'interview',
    name: '面经求职',
    description: '实习校招面经、简历与 offer 交流',
    icon: 'briefcase',
  },
  {
    code: 'contest',
    name: '竞赛交流',
    description: '程序设计、软件杯等赛事组队与经验',
    icon: 'trophy',
  },
  {
    code: 'course',
    name: '课程交流',
    description: '各门课程的学习讨论与资料互助',
    icon: 'school',
  },
  {
    code: 'chat',
    name: '闲聊灌水',
    description: '校园日常与其他话题',
    icon: 'messages',
  },
]

export const BOARD_MAP: Record<BoardCode, BoardDef> = Object.fromEntries(
  BOARDS.map((b) => [b.code, b]),
) as Record<BoardCode, BoardDef>

export function boardNameOf(code: string): string {
  return BOARD_MAP[code as BoardCode]?.name ?? code
}
