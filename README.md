# Campus-Link

面向**重庆工程学院计算机专业学生**的垂直交流论坛：**问有所答、学有同伴、求职有路**。

核心方向：固定版块体系（技术问答 / 学习资源 / 面经求职 / 竞赛交流 / 课程交流 / 闲聊灌水）、Markdown 帖子与代码高亮、问答最佳答案采纳、站内通知与搜索、**学籍核验注册（限本校）**。

当前处于**阶段四开发 · Sprint 2 已交付（MVP · 论坛最小闭环，2026-09-12）**（Sprint 1 已完成 T1~T9；Sprint 2 完成 `module/forum` 6 个端点 + 前端论坛三页，**M1~M6 完成、A1~A5 五条验收标准真机实测通过**，见 [Sprint 2 计划（MVP）](docs/development/sprint-2.md) §5）。**执行依据是 [Sprint 2 计划（MVP）](docs/development/sprint-2.md) 与[增量设计](docs/development/sprint-2-design.md)**；Sprint 3 尚未开工。**已交付 ≠ 欠账消失**：**A3-9（架构守护测试）/ A3-10（消除 F-1/F-3/F-4）的推迟截止点"MVP 验收后"已经到达而两项仍未开工**（偏离登记为 [W-07](docs/tailoring-waivers.md)）——**推迟 ≠ 免除**，[W-02](docs/tailoring-waivers.md) 的关闭前置仍是 A3-9；**A3-11（新上下文落地后复评四层范式）触发条件已达成、不得再延期**。当前该做的是[下一步行动清单](docs/next-steps.md) §4 第 1 条：**先做 A3-11 复评（不改码），再开 CR 做掉 B4 / B5**。A3-9 缺位期**四层依赖方向无机器强制**（`module/forum` 经人工逐条核对、未发现违规，**但只有人工核对作为证据**）、**N-4 仍敞口**（Sprint 2 新增的 2 个受保护端点靠手写鉴权 + 401 单测顶住，漏写会静默成为公开接口）。

三道已过的评审门均为**有条件通过**且行动项未闭环（A1-1~A1-6、A2-1~A2-8、A3-5~A3-11），外部依赖 **B1 / B2 学籍名册仍开放**（B3 仓库远程已于 2026-09-11 闭环）。**治理待签项已于 2026-09-11 全部签署**（[CR-020](docs/change-log.md)）：W-01~W-05 接受让步放行、W-06 接受并关闭、三份门纪要批准行逐角色签署、技术方案 v0.3/v0.4 签署「②有条件追认」使设计门 A3-2 闭环——**但签署只批准结论，未闭环任何行动项**。**契约缺口 N-1 / N-2 / N-3 已于 2026-09-12 修复**（[CR-021](docs/change-log.md)：错误响应与 `bearerAuth` 写入 OpenAPI 契约、畸形请求体由 `500 / 9999` 改回 `400 / 1001` 并降日志档位，真机复测通过、快照已再生成），Sprint 2 交付时**二度再生成至 12 端点 / 30 schema**（⚠️ 本次来源为**未提交的工作区**，提交后须按 [api/README](docs/design/api/README.md) §2 复核），同时**新登记 N-4**——契约声明了鉴权但 `SecurityConfig` 仍 `permitAll()`，**声明 ≠ 强制**，归口 A3-9。详见[文档中心](docs/README.md)第 1 节与[裁剪与让步放行记录](docs/tailoring-waivers.md)。

## 文档

全部项目文档位于 [docs/](docs/README.md)。**文档版本号以 [docs/README.md 第 2 节](docs/README.md) 为唯一登记处**，本页与其他文档交叉引用时不写版本号（手册 4.5）。

**先看这三个**

- [**下一步行动清单**](docs/next-steps.md) — 阻塞项（B1/B2 名册 + **B4/B5 的推迟截止点已到达、恢复为当前该做**）/ 并行项 / Sprint 拆解，"现在该干什么"
- [**Sprint 2 计划（MVP）**](docs/development/sprint-2.md) — **已于 2026-09-12 交付**：发帖 / 列表 / 详情 / 楼层回复四项 + 13 项不做 + 5 条验收标准 + **§5 逐条真机验收记录**
- [**变更台账**](docs/change-log.md) — 全部基线级变更与影响评估（CR-001~**CR-022**，含 CR-022 的后续实施记录）
- [**裁剪与让步放行记录**](docs/tailoring-waivers.md) — **7 项流程偏离**（W-07 为 2026-09-12 新增：最小 MVP 优先，推迟 A3-9 / A3-10），**已于 2026-09-11 / 09-12 由发起人签署**（W-06 关闭；**W-02 / W-05 签署但不得关闭**；**推迟的截止点已到达而两项仍未开工**）

**规范与索引**

- [研发流程手册](docs/process-handbook.md) — 7 阶段 6 评审门的标准研发流程
- [文档中心](docs/README.md) — 全部文档索引、版本登记与阶段门状态

**阶段交付物**

- 立项：[BRD](docs/initiation/brd.md) · [可行性分析](docs/initiation/feasibility-study.md) · [项目章程](docs/initiation/project-charter.md)
- 需求：[PRD](docs/requirements/prd.md)（基线）
- 设计：[技术方案](docs/design/tech-design.md)（ADR-001~012，§5 现为 12 个已实现端点 + 6 行计划项）· [API 契约快照](docs/design/api/README.md)（`openapi.json`：**12 端点 / 30 schema** + 主链路与错误响应实测 + 仍开放的 N-4/N-5/N-6；⚠️ 本次来源为未提交的工作区）
- 开发：[Sprint 1 计划](docs/development/sprint-1.md)（已执行）· [**Sprint 2 计划（MVP）**](docs/development/sprint-2.md)（**已于 2026-09-12 交付**：M1~M6 完成、A1~A5 全部通过，**§5 为逐条真机验收记录**）· [**Sprint 2 增量设计**](docs/development/sprint-2-design.md)（**实现依据**：四层文件清单 + `module/forum` 分层自检 + 六端点契约 + **§8 落码偏差 P-1~P-8**）

**评审记录**

- [立项门](docs/reviews/gate-1-initiation.md) · [需求门](docs/reviews/gate-2-requirements.md) · [设计门](docs/reviews/gate-3-design.md)（三份均为 2026-09-11 事后补录，**批准行已于同日由发起人逐角色签署**，[CR-020](docs/change-log.md)）
- [v0.3 / v0.4 追认纪要](docs/reviews/retro-review-design-v03-v04.md)（补充评审，非评审门）— **已签署「②有条件追认」**，闭环设计门 A3-2，三条件生效为 A3-9 / A3-10 / A3-11；含架构评审意见 F-1~F-7

## 技术栈

| 层 | 选型 |
|---|------|
| 前端 | Vue 3 + Vite + Pinia + TypeScript + Element Plus（按需自动引入） |
| 后端 | **Spring Boot 4.1 + Java 21**（2026-08-30 由 NestJS 反转，2026-08-31 升级 4.1 + JDK 21；见 CR-004 / CR-007） |
| ORM | MyBatis-Plus 3.5.17（spring-boot4-starter + jsqlparser 模块） |
| 数据库 | **MySQL 8.0**（ngram 全文索引支持中文检索） |
| 缓存 | Redis 7（缓存 / 热榜 / 验证码限流 / 分布式锁） |
| 渲染 | flexmark-java + jsoup 白名单（服务端单点渲染防 XSS）+ 前端 highlight.js |
| 架构 | 模块化单体 + DDD 四层（`module/account` / `module/forum` 已落地，ADR-012） |
| 部署 | Docker Compose；**形态待定**（公网 + ICP 备案 或 校园内网，阶段六决策，ADR-011） |

## 仓库结构

本项目为根目录**单一 git 仓库（monorepo）**，默认分支 `main`：

- `docs/` — 流程手册与全部项目文档
- `backend/` — 服务端代码（DDL、公共层、Markdown 渲染、`module/account` + `module/forum` 两个上下文 DDD 四层，共 12 个端点，见[技术方案 2.4](docs/design/tech-design.md)）
- `frontend/` — Web 前端代码（Vue 3 + Vite + Element Plus；登录 / 注册 / 个人主页 + 版块列表 / 发帖 / 帖子详情共 6 页可用）

> CI 工作流位于根目录 `.github/workflows/`（`backend-ci.yml` / `frontend-ci.yml`）。✅ **仓库已配置远程** `https://github.com/changjunzheng/Campus-Link`，`main` 跟踪 `origin/main`，首次提交 `837a22f` 已推送，两个工作流均已在 GitHub Actions **首次运行成功**（原"零提交、无远程、无备份"风险已于 2026-09-11 闭环，见 [CR-011](docs/change-log.md)）。尚未配置的是 `main` 分支保护与 PR 流程。

## 当前状态

- [x] 阶段一 · 立项启动 — 🟡 **有条件通过**（2026-08-30 发起人单人简化评审；**批准记录已于 2026-09-11 签署**，A1-1~A1-6 仍开放）；定位为计算机专业学生交流论坛，后收敛为单校范围
- [x] 阶段二 · 需求 — 🟡 **有条件通过**（**批准记录已签署**，A2-1~A2-8 仍开放）；PRD 基线（单校范围 + 学籍核验升 P0），**四方检查清单 0/9 勾选**
- [x] 阶段三 · 设计 — 🟡 **有条件通过**；技术方案 v0.1 经评审会，**v0.2 至今无任何形式评审**，**v0.3 / v0.4（含已落地的 DDD 重构）已于 2026-09-11 签署「②有条件追认」**（发起人单人书面确认，**非评审会**，[A3-2 闭环](docs/reviews/retro-review-design-v03-v04.md)）；**UI 交互稿 / 视觉稿经 [W-03](docs/tailoring-waivers.md) 签署裁剪、不再产出**，替代的三条补偿措施（组件规范约定 / 截图归档 / 逐页走查）**均未执行**，故 A3-6 仍开放；**A3-4 已闭环**——[API 契约快照](docs/design/api/README.md) 于 2026-09-11 归档（[CR-019](docs/change-log.md)）、2026-09-12 随 [CR-021](docs/change-log.md) 修复 **N-1 / N-2 / N-3** 后再生成，但**出口标准第 2 项（数据模型与接口契约完整）仍为 ❌**（A3-5 ER 图未做、快照虽已扩至 **12 端点 / 30 schema** 但只覆盖已实现的端点、技术方案 §5 另有 6 行计划项契约未设计、**N-4「声明 bearerAuth ≠ 运行时强制」未闭环**）
- [ ] 阶段四 · 开发 — **Sprint 2 已于 2026-09-12 交付**（Sprint 1 完成 T1~T9；Sprint 2 完成 `module/forum` 6 个端点 + 前端论坛三页，**M1~M6 完成、A1~A5 五条验收标准真机实测通过**，见 [sprint-2.md](docs/development/sprint-2.md) §5）；提测准入门 **7 项出口标准中 4 项未满足**（① 覆盖率不可测，无 jacoco；② 静态扫描未接入；③ CI 因缺扫描环节不能判"全绿"；④ Code Review 未闭环——见 [W-05](docs/tailoring-waivers.md)）；**原开工阻断项 A3-9（架构守护测试）+ A3-10（消除 F-1/F-3/F-4）推迟至 MVP 验收后**（[CR-022](docs/change-log.md) / [W-07](docs/tailoring-waivers.md)，**推迟 ≠ 免除**）——**该截止点现已到达而两项仍未开工**，[W-02](docs/tailoring-waivers.md) 的关闭前置仍是 A3-9；**A3-11（复评四层范式）触发条件已达成、不得再延期**。两项合计约 **1 天**（A3-9 约 0.5 天含 N-4、A3-10 约 0.5 天含 N-5 / N-6），**当前该做**——[下一步行动清单](docs/next-steps.md) §4 第 1 条：**先做 A3-11 复评（不改码），再开 CR 做 B4 / B5**
- [ ] 阶段五 · 测试
- [ ] 阶段六 · 发布 — 部署形态决策点（ADR-011）在本阶段启动前
- [ ] 阶段七 · 运维与收尾

> 阶段一 / 二 / 三标注为"有条件通过"而非"通过"的依据：手册 4.1 节"'有条件通过'的行动项未闭环前，不得视为该门通过"。逐项核对见各门评审纪要。

## 本轮决议（2026-08-30，发起人）

1. 服务范围锁定**重庆工程学院单校**，本期不外扩（CR-002）；
2. 后端技术栈定稿 **Spring Boot**（原 NestJS 反转）——决议时为 Spring Boot 3 + Java 17，**2026-08-31 已升级至 Spring Boot 4.1 + JDK 21，见 [CR-007](docs/change-log.md)**，当前技术栈以上方"技术栈"表为准（CR-004 / CR-007）；
3. 学籍核验方式 = **学号 + 姓名比对**（名册预置），F-ACC-004 升为 P0（CR-003）；
4. 部署形态**挂起**，阶段六启动前决策（CR-005）。

四项决议的完整影响评估见[变更台账](docs/change-log.md)。后续另有 CR-006（开发节奏 6 → 8 周）、CR-007（JDK 21 + Boot 4.1）、CR-008（账号上下文 DDD 重构）、CR-009（治理记录补录）、CR-010（三仓库 → 根目录 monorepo）、CR-011（版本控制闭环）、CR-012（开发期环境改本机 MySQL/Redis、后端端口 8088）、CR-013（字段校验收紧 + dev 便利开关）、CR-014（Flyway 管理 DDL + 数据脚本规范）、CR-015（Redis 键命名空间前缀）、CR-016（唯一键冲突翻译为业务错误）、CR-017（修正技术方案 D-1~D-7）、CR-018（归档 v0.3/v0.4 追认材料）、CR-019（归档 OpenAPI 快照）、**CR-020（发起人签署全部治理待签项）**、**CR-021（修复契约缺口 N-1/N-2/N-3，再生成快照）**、**CR-022（改按最小 MVP 推进，推迟 A3-9 / A3-10；实施后补记 Sprint 2 MVP 交付与验收记录，不新增编号）**。
