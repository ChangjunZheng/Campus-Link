# Campus-Link 文档中心

本目录是项目全部文档的唯一归档地。流程规范见[研发流程手册](process-handbook.md)，文档命名与归档规则见手册 4.5 节。

> **版本号单一登记处**：本项目全部文档的当前版本号**只在本文件第 2 节登记**。其他文档交叉引用时只写链接、不写版本号（手册 4.5），避免版本升级后引用大面积失效。

## 1. 阶段门状态

| # | 阶段 | 状态 | 关键交付物 | 评审门纪要 |
|---|------|------|-----------|-----------|
| 1 | 立项启动 | 🟡 **有条件通过**（发起人单人简化评审，2026-08-30；出口标准 4 项中 1 项未满足，行动项 A1-1~A1-6 未闭环） | [BRD](initiation/brd.md) · [可行性分析](initiation/feasibility-study.md) · [项目章程](initiation/project-charter.md) | [gate-1-initiation.md](reviews/gate-1-initiation.md) |
| 2 | 需求 | 🟡 **有条件通过**（同上；PRD 四方检查清单 0/9 勾选，行动项 A2-1~A2-8 未闭环） | [PRD](requirements/prd.md)（基线，含学籍核验 F-ACC-004 升 P0） | [gate-2-requirements.md](reviews/gate-2-requirements.md) |
| 3 | 设计 | 🟡 **有条件通过**（仅 v0.1 经评审；**v0.3 / v0.4 未评审**；UI 稿遗留行动项未闭环；出口标准 4 项中 3 项未满足，行动项 A3-1~A3-8，其中 **A3-1 为 Sprint 2 开工阻断项**） | [技术方案](design/tech-design.md) | [gate-3-design.md](reviews/gate-3-design.md) |
| 4 | 开发 | 🔄 进行中（Sprint 1：T1~T9 已完成）· **提测准入门 3 项硬指标当前不可执行**（覆盖率无插件 / CI 无远程 / Code Review 未完成，见 W-05）· 外部依赖 B1/B2/B3 仍开放 | [Sprint 1 计划](development/sprint-1.md) | 提测准入门（阶段启动时创建） |
| 5 | 测试 | ⬜ 未开始 | 测试报告、UAT 结论 | 验收评审门 |
| 6 | 发布 | ⬜ 未开始（**部署形态 ADR-011 决策截止点在本阶段启动前**） | 发布单、回滚预案 | 上线检查门 |
| 7 | 运维与收尾 | ⬜ 未开始 | 监控大盘、复盘报告 | 迭代闭环 |

> **状态更新规则**：评审门结论变化后由项目经理更新本表，并同步更新对应文档头部的"评审状态"（草案 → 已评审）。
>
> **阶段一 / 二状态由 ✅ 改为 🟡 的说明（2026-09-11）**：补录评审纪要时按手册 4.1 三选一术语逐项核对出口标准，两道门均存在未满足项，原"✅ 已通过"标注与文档内检查清单矛盾。按手册 4.1"有条件通过的行动项未闭环前不得视为该门通过"，如实改标。阶段四开发在程序上不应启动但已启动，该偏离待发起人在 [W-02](tailoring-waivers.md) 签署让步放行。

## 2. 文档版本登记（唯一登记处）

| 文档 | 当前版本 | 状态 | 最后更新 |
|------|:-------:|------|:-------:|
| [研发流程手册](process-handbook.md) | v1.1 | 草案（随项目演进修订） | 2026-09-11 |
| [变更台账](change-log.md) | v1.1 | 已生效（累积登记，CR-001~CR-010） | 2026-09-11 |
| [裁剪与让步放行记录](tailoring-waivers.md) | v1.0 | **待签署**（W-01~W-06） | 2026-09-11 |
| [下一步行动清单](next-steps.md) | v1.1 | 已生效 | 2026-09-11 |
| [BRD](initiation/brd.md) | v0.3 | 已评审（发起人确认，2026-08-30） | 2026-08-30 |
| [可行性分析](initiation/feasibility-study.md) | v0.3 | 已评审（同上） | 2026-09-11 |
| [项目章程](initiation/project-charter.md) | v0.4 | 已生效（正式签署待补） | 2026-09-11 |
| [PRD](requirements/prd.md) | v1.1 | 基线（有条件通过，见 gate-2） | 2026-09-11 |
| [技术方案](design/tech-design.md) | v0.4 | 草案（v0.1 有条件通过；**v0.3 / v0.4 未评审**） | 2026-09-07 |
| [Sprint 1 计划](development/sprint-1.md) | v1.4 | 已执行（出口自查部分未闭环） | 2026-09-11 |
| [立项门评审纪要](reviews/gate-1-initiation.md) | v1.0 | 待签署 | 2026-09-11 |
| [需求门评审纪要](reviews/gate-2-requirements.md) | v1.0 | 待签署 | 2026-09-11 |
| [设计门评审纪要](reviews/gate-3-design.md) | v1.0 | 待签署 | 2026-09-11 |

> ADR 当前范围：**ADR-001 ~ ADR-012**（全部在技术方案第 6 节）。

## 3. 文档索引

### 流程与治理

- [研发流程手册](process-handbook.md) — 7 阶段 6 评审门、角色职责、缺陷等级、环境与分支策略、裁剪指南
- [**变更台账**](change-log.md) — 基线级变更的登记与影响评估（手册 4.2 要求的载体），CR-001~CR-008 为事后补录
- [**裁剪与让步放行记录**](tailoring-waivers.md) — 流程偏离的单一登记处，**待发起人签署**
- [**下一步行动清单**](next-steps.md) — 阻塞项 / 并行项 / Sprint 拆解，"现在该干什么"的单一入口

### 评审记录（reviews/）

| 评审门 | 纪要 | 结论 |
|-------|------|------|
| 阶段一 → 二（立项） | [gate-1-initiation.md](reviews/gate-1-initiation.md) | 有条件通过，行动项 A1-1~A1-6 |
| 阶段二 → 三（需求） | [gate-2-requirements.md](reviews/gate-2-requirements.md) | 有条件通过，行动项 A2-1~A2-8 |
| 阶段三 → 四（设计） | [gate-3-design.md](reviews/gate-3-design.md) | 有条件通过，行动项 A3-1~A3-8 |

> 三份纪要均为 **2026-09-11 事后补录**，文首有补录声明与证据效力说明；批准记录行待签署。提测准入门 / 验收评审门 / 上线检查门的纪要在对应阶段启动时按同一格式创建。

### 模板（templates/）

| 模板 | 用于阶段 |
|------|---------|
| [BRD 模板](templates/brd-template.md) | 立项启动 |
| [可行性分析模板](templates/feasibility-template.md) | 立项启动 |
| [项目章程模板](templates/charter-template.md) | 立项启动 |
| [PRD 模板](templates/prd-template.md) | 需求 |
| [技术方案模板](templates/tech-design-template.md) | 设计 |
| [测试计划模板](templates/test-plan-template.md) | 测试 |
| [发布检查清单模板](templates/release-checklist-template.md) | 发布 |
| [项目复盘模板](templates/retrospective-template.md) | 收尾 |

### 项目文档（按阶段归档）

- **立项启动**：[initiation/](initiation/brd.md) — brd.md · feasibility-study.md · project-charter.md
- **需求**：[requirements/prd.md](requirements/prd.md) — PRD 基线（单校范围 + 学籍核验升 P0；RTM 见 PRD 第 9 节附录，**追溯不完整，见 gate-2 行动项 A2-8**）
- **设计**：[design/tech-design.md](design/tech-design.md) — 技术方案（Spring Boot 4.1 + JDK 21 + MySQL 8；学籍核验设计；ADR-001~012）。**已知矛盾 D-1~D-7 待修，见 gate-3 第 5 节；其中 D-1（§2.4 分包约定与 ADR-012 冲突）为 Sprint 2 开工阻断项**
- **开发**：[development/sprint-1.md](development/sprint-1.md) — Sprint 1 计划与任务状态（T1~T9 完成）
- **测试**：`docs/testing/`（阶段启动时创建，在此登记）
- **发布**：`docs/release/`（每次发布一份检查清单，在此登记）
- **收尾**：`docs/retrospectives/`（复盘记录，在此登记）

## 4. 当前最需要处理的三件事

按风险收益比排序，详情见[行动清单](next-steps.md)：

1. **单仓库（monorepo）零提交、无远程**（[W-06](tailoring-waivers.md)）— 无任何副本，本机故障即项目归零；且变更台账与评审纪要的日期均无 git 凭证可验证；
2. **技术方案 §2.4 分包约定与 ADR-012 冲突**（gate-3 行动项 A3-1）— 误导性内容，Sprint 2 开工前必须修正，否则后续上下文按错误结构开发；
3. **签署 W-01 ~ W-06 与三份纪要的批准记录** — 未签署前，六道流程偏离在程序上均属"未获批准"。
