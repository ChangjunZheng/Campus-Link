# Campus-Link 文档中心

本目录是项目全部文档的唯一归档地。流程规范见[研发流程手册](process-handbook.md)，文档命名与归档规则见手册 4.5 节。

> **版本号单一登记处**：本项目全部文档的当前版本号**只在本文件第 2 节登记**。其他文档交叉引用时只写链接、不写版本号（手册 4.5），避免版本升级后引用大面积失效。

## 1. 阶段门状态

| # | 阶段 | 状态 | 关键交付物 | 评审门纪要 |
|---|------|------|-----------|-----------|
| 1 | 立项启动 | 🟡 **有条件通过**（发起人单人简化评审，2026-08-30；出口标准 4 项中 1 项未满足，行动项 A1-1~A1-6 未闭环） | [BRD](initiation/brd.md) · [可行性分析](initiation/feasibility-study.md) · [项目章程](initiation/project-charter.md) | [gate-1-initiation.md](reviews/gate-1-initiation.md) |
| 2 | 需求 | 🟡 **有条件通过**（同上；PRD 四方检查清单 0/9 勾选，行动项 A2-1~A2-8 未闭环） | [PRD](requirements/prd.md)（基线，含学籍核验 F-ACC-004 升 P0） | [gate-2-requirements.md](reviews/gate-2-requirements.md) |
| 3 | 设计 | 🟡 **有条件通过**（仅 v0.1 经评审；**v0.3 / v0.4 未评审**，追认材料已备齐待签署；UI 稿遗留行动项未闭环；出口标准 4 项中 2 项未满足、2 项部分满足。**A3-1（D-1）、A3-3（D-2~D-7）与 A3-4（OpenAPI 快照）已于 2026-09-11 闭环**；仍未闭环：**A3-2** v0.3/v0.4 追认（**材料已归档，待发起人签署**）、A3-5 ER 图、A3-6 UI 稿、A3-7 检查清单勾选、A3-8 部署与机审选型。**注**：A3-4 闭环 ≠ 出口标准第 2 项满足——快照只覆盖 Sprint 1 已实现的 6 个端点，且归档时实测出三项契约缺口 **N-1 / N-2 / N-3**（见 [api/README.md](design/api/README.md) §6，其中 N-3 为 P2 缺陷）待另开 CR 修复 | [技术方案](design/tech-design.md) · [API 契约快照](design/api/README.md) · [v0.3/v0.4 追认纪要](reviews/retro-review-design-v03-v04.md) | [gate-3-design.md](reviews/gate-3-design.md) |
| 4 | 开发 | 🔄 进行中（Sprint 1：T1~T9 已完成，**账号链路联调冒烟 2026-09-11 首次真机跑通**）· 提测准入门 **7 项出口标准中 4 项未满足**（覆盖率无插件 / 静态扫描未接入 / Code Review 未闭环 / CI 缺扫描环节）· 外部依赖 **B1 / B2 仍开放**（**B3 已于 2026-09-11 闭环**）· **Sprint 2 开工前仍需：签署 A3-2 追认纪要与 W-01~W-06、修复契约缺口 [N-1 / N-2 / N-3](design/api/README.md)（含 P2 缺陷 N-3：畸形请求体返回 500）** | [Sprint 1 计划](development/sprint-1.md) | 提测准入门（阶段启动时创建） |
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
| [变更台账](change-log.md) | v1.10 | 已生效（累积登记，CR-001~CR-019） | 2026-09-11 |
| [裁剪与让步放行记录](tailoring-waivers.md) | v1.2 | **待签署**（W-01~W-06；其中 W-06 关闭条件已具备，W-02 补偿①材料已备齐） | 2026-09-11 |
| [下一步行动清单](next-steps.md) | v1.2 | 已生效（B3 已闭环） | 2026-09-11 |
| [BRD](initiation/brd.md) | v0.3 | 已评审（发起人确认，2026-08-30） | 2026-08-30 |
| [可行性分析](initiation/feasibility-study.md) | v0.3 | 已评审（同上） | 2026-09-11 |
| [项目章程](initiation/project-charter.md) | v0.4 | 已生效（正式签署待补） | 2026-09-11 |
| [PRD](requirements/prd.md) | v1.2 | 基线（有条件通过，见 gate-2） | 2026-09-11 |
| [技术方案](design/tech-design.md) | v0.5 | 草案（v0.1 有条件通过；**v0.3 / v0.4 未评审**；D-1~D-7 已修正；§5 已指向 API 快照并补入 `users/me`） | 2026-09-11 |
| [API 契约快照说明](design/api/README.md) | v1.0 | **已归档快照**（`openapi.json` 取自后端提交 `68d40ae`，6 端点 / 13 schema；接口契约变更后须再生成；闭环设计门 A3-4；含缺口 N-1/N-2/N-3） | 2026-09-11 |
| [Sprint 1 计划](development/sprint-1.md) | v1.10 | 已执行（出口自查部分未闭环；联调冒烟已通过） | 2026-09-11 |
| [立项门评审纪要](reviews/gate-1-initiation.md) | v1.0 | 待签署 | 2026-09-11 |
| [需求门评审纪要](reviews/gate-2-requirements.md) | v1.0 | 待签署 | 2026-09-11 |
| [设计门评审纪要](reviews/gate-3-design.md) | v1.2 | 待签署（A3-1 / A3-3 / A3-4 已闭环；A3-2 材料已备齐待签署） | 2026-09-11 |
| [v0.3 / v0.4 追认纪要](reviews/retro-review-design-v03-v04.md) | v1.1 | **待签署**（设计门 A3-2 材料；含架构评审意见 F-1~F-7 与三选一结论建议；v1.1 仅回填 A3-4 已闭环这一事实） | 2026-09-11 |

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
| 阶段三 → 四（设计） | [gate-3-design.md](reviews/gate-3-design.md) | 有条件通过，行动项 A3-1~A3-8（**A3-1 / A3-3 / A3-4 已闭环**） |
| 补充评审（非评审门）：技术方案 v0.3 / v0.4 追认 | [retro-review-design-v03-v04.md](reviews/retro-review-design-v03-v04.md) | **待签署**——对应行动项 A3-2 与 W-02 补偿①；含架构评审意见（认可 5 项、问题 F-1~F-7）与三选一结论建议 |

> 三份纪要均为 **2026-09-11 事后补录**，文首有补录声明与证据效力说明；批准记录行待签署。第四行不是评审门纪要，而是为设计门行动项 A3-2 准备的**补充评审 / 书面确认材料**，其 §0 明确写出效力边界（仍是单人评审、属事后追认），签署前不产生流程效力。提测准入门 / 验收评审门 / 上线检查门的纪要在对应阶段启动时按同一格式创建。

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
- **设计**：[design/tech-design.md](design/tech-design.md) — 技术方案（Spring Boot 4.1 + JDK 21 + MySQL 8；学籍核验设计；ADR-001~012）；[design/api/README.md](design/api/README.md) — **API 契约快照**（`openapi.json` + 2 条主链路请求响应示例 + 与技术方案 §5 的逐条差异 + 快照未表达的 5 项内容）。**原矛盾 D-1~D-7 已于 2026-09-11 全部修正**（[CR-017](change-log.md)），**A3-4 已于同日闭环**（[CR-019](change-log.md)）；仍开放：A3-2 追认（[材料已备齐待签署](reviews/retro-review-design-v03-v04.md)）、A3-5 ER 图与量级预估、A3-7 检查清单勾选、A3-8 部署与机审选型，以及归档快照时实测出的契约缺口 **N-1 / N-2 / N-3**（[api/README.md](design/api/README.md) §6，须另开 CR 修复）
- **开发**：[development/sprint-1.md](development/sprint-1.md) — Sprint 1 计划与任务状态（T1~T9 完成）
- **测试**：`docs/testing/`（阶段启动时创建，在此登记）
- **发布**：`docs/release/`（每次发布一份检查清单，在此登记）
- **收尾**：`docs/retrospectives/`（复盘记录，在此登记）

## 4. 当前最需要处理的三件事

按风险收益比排序，详情见[行动清单](next-steps.md)：

1. **签署：W-01 ~ W-06、三份纪要的批准行、[v0.3/v0.4 追认纪要](reviews/retro-review-design-v03-v04.md)** — 未签署前，六道流程偏离在程序上均属"未获批准"。**A3-2 的材料已于 2026-09-11 备齐归档**（[CR-018](change-log.md)），剩下的只是签字动作：追认纪要 §4 给了三选一（无条件追认 / **有条件追认，技术负责人建议** / 拒绝追认），§6 是逐角色签署页。建议选**有条件追认**——其中条件 1 对应 **F-2「后端无任何架构守护测试」**，这是 ADR-012"范式若有误将放大至整个后端"这一风险**至今唯一未被补偿**的敞口（四层依赖方向、跨上下文规则、Mapper 位置全靠人工自觉，而该范式将被后续 8 个上下文复制）。另 **W-05 偏离项②与 W-06 的关闭条件已具备**（[CR-011](change-log.md)：远程 + 首次提交 + CI 首跑成功），签署后可直接标记关闭；
2. **B1 / B2 学籍名册仍开放** — 唯一无法靠工程手段消除的外部依赖。未清零则注册链路只能靠 `app.roster.bypass` 验证，真实名册导入后须做一次关闭 bypass 的全链路回归；
3. **提测准入门 4 项未满足 + 契约缺口 N-1 / N-2 / N-3 + 设计门 A3-5 / A3-7** — 覆盖率不可测（无 jacoco）、静态扫描未接入、Code Review 未闭环、CI 缺扫描环节；**A3-4（归档 OpenAPI 快照）已于 2026-09-11 闭环**（[CR-019](change-log.md)），但归档时实测出三项契约缺口：**N-1** 快照只声明 `200`、错误响应（400/401/409/500）在契约中不可见；**N-2** 未声明 `bearerAuth`，需登录端点看起来无需鉴权；**N-3** 畸形请求体实测返回 **HTTP 500 / `9999`**（应为 400 / `1001`），按手册 4.3 定级 **P2 缺陷**。三项均须改代码，**建议在 Sprint 2 开工前另开一个 CR 一并处理**（详见 [api/README.md](design/api/README.md) §6）；A3-5（ER 图与数据量级预估）与 A3-7（技术方案文末检查清单逐项勾选）仍开放，属 Sprint 2 开工前的收尾项。

> 原"最高优先级风险"（monorepo 零提交、无远程、无备份）已于 2026-09-11 闭环，见 [CR-011](change-log.md)；原第二项**技术方案 §2.4 与 ADR-012 的冲突（缺陷 D-1，Sprint 2 阻断项）已于同日随 [CR-017](change-log.md) 闭环**（连同 D-2~D-7）。
