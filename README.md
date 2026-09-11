# Campus-Link

面向**重庆工程学院计算机专业学生**的垂直交流论坛：**问有所答、学有同伴、求职有路**。

核心方向：固定版块体系（技术问答 / 学习资源 / 面经求职 / 竞赛交流 / 课程交流 / 闲聊灌水）、Markdown 帖子与代码高亮、问答最佳答案采纳、站内通知与搜索、**学籍核验注册（限本校）**。

当前处于**阶段四开发 · Sprint 1 已完成（T1~T9）**；三道已过的评审门均为**有条件通过**且行动项未闭环，外部依赖 B1/B2/B3 仍开放。详见[文档中心](docs/README.md)第 1 节与[裁剪与让步放行记录](docs/tailoring-waivers.md)。

## 文档

全部项目文档位于 [docs/](docs/README.md)。**文档版本号以 [docs/README.md 第 2 节](docs/README.md) 为唯一登记处**，本页与其他文档交叉引用时不写版本号（手册 4.5）。

**先看这三个**

- [**下一步行动清单**](docs/next-steps.md) — 阻塞项 / 并行项 / Sprint 拆解，"现在该干什么"
- [**变更台账**](docs/change-log.md) — 全部基线级变更与影响评估（CR-001~CR-010）
- [**裁剪与让步放行记录**](docs/tailoring-waivers.md) — 6 项流程偏离，**待发起人签署**

**规范与索引**

- [研发流程手册](docs/process-handbook.md) — 7 阶段 6 评审门的标准研发流程
- [文档中心](docs/README.md) — 全部文档索引、版本登记与阶段门状态

**阶段交付物**

- 立项：[BRD](docs/initiation/brd.md) · [可行性分析](docs/initiation/feasibility-study.md) · [项目章程](docs/initiation/project-charter.md)
- 需求：[PRD](docs/requirements/prd.md)（基线）
- 设计：[技术方案](docs/design/tech-design.md)（ADR-001~012）
- 开发：[Sprint 1 计划](docs/development/sprint-1.md)

**评审记录**

- [立项门](docs/reviews/gate-1-initiation.md) · [需求门](docs/reviews/gate-2-requirements.md) · [设计门](docs/reviews/gate-3-design.md)（三份均为 2026-09-11 事后补录，批准行待签署）

## 技术栈

| 层 | 选型 |
|---|------|
| 前端 | Vue 3 + Vite + Pinia + TypeScript + Element Plus（按需自动引入） |
| 后端 | **Spring Boot 4.1 + Java 21**（2026-08-30 由 NestJS 反转，2026-08-31 升级 4.1 + JDK 21；见 CR-004 / CR-007） |
| ORM | MyBatis-Plus 3.5.17（spring-boot4-starter + jsqlparser 模块） |
| 数据库 | **MySQL 8.0**（ngram 全文索引支持中文检索） |
| 缓存 | Redis 7（缓存 / 热榜 / 验证码限流 / 分布式锁） |
| 渲染 | flexmark-java + jsoup 白名单（服务端单点渲染防 XSS）+ 前端 highlight.js |
| 架构 | 模块化单体 + DDD 四层（`module/account` 已落地，ADR-012） |
| 部署 | Docker Compose；**形态待定**（公网 + ICP 备案 或 校园内网，阶段六决策，ADR-011） |

## 仓库结构

本项目为根目录**单一 git 仓库（monorepo）**，默认分支 `main`：

- `docs/` — 流程手册与全部项目文档
- `backend/` — 服务端代码（DDL、公共层、Markdown 渲染、账号上下文 `module/account` DDD 四层，见[技术方案 2.4](docs/design/tech-design.md)）
- `frontend/` — Web 前端代码（Vue 3 + Vite + Element Plus，注册 / 登录页可用）

> CI 工作流位于根目录 `.github/workflows/`（`backend-ci.yml` / `frontend-ci.yml`）。⚠️ 仓库目前**尚无提交、未配置远程托管**（见 [W-06](docs/tailoring-waivers.md)）。当前项目资产**无任何副本**，且变更台账与评审纪要的日期暂无 git 凭证可交叉验证。这是当前最高优先级风险。

## 当前状态

- [x] 阶段一 · 立项启动 — 🟡 **有条件通过**（2026-08-30 发起人单人简化评审）；定位为计算机专业学生交流论坛，后收敛为单校范围
- [x] 阶段二 · 需求 — 🟡 **有条件通过**；PRD 基线（单校范围 + 学籍核验升 P0），**四方检查清单 0/9 勾选**
- [x] 阶段三 · 设计 — 🟡 **有条件通过**；技术方案 v0.1 经评审，**v0.3 / v0.4（含已落地的 DDD 重构）未评审**；UI 稿未产出
- [ ] 阶段四 · 开发 — **Sprint 1 完成（T1~T9）**；提测准入门 3 项硬指标当前不可执行（覆盖率 / CI / Code Review，见 [W-05](docs/tailoring-waivers.md)）
- [ ] 阶段五 · 测试
- [ ] 阶段六 · 发布 — 部署形态决策点（ADR-011）在本阶段启动前
- [ ] 阶段七 · 运维与收尾

> 阶段一 / 二 / 三标注为"有条件通过"而非"通过"的依据：手册 4.1 节"'有条件通过'的行动项未闭环前，不得视为该门通过"。逐项核对见各门评审纪要。

## 本轮决议（2026-08-30，发起人）

1. 服务范围锁定**重庆工程学院单校**，本期不外扩（CR-002）；
2. 后端技术栈定稿 **Spring Boot**（原 NestJS 反转）——决议时为 Spring Boot 3 + Java 17，**2026-08-31 已升级至 Spring Boot 4.1 + JDK 21，见 [CR-007](docs/change-log.md)**，当前技术栈以上方"技术栈"表为准（CR-004 / CR-007）；
3. 学籍核验方式 = **学号 + 姓名比对**（名册预置），F-ACC-004 升为 P0（CR-003）；
4. 部署形态**挂起**，阶段六启动前决策（CR-005）。

四项决议的完整影响评估见[变更台账](docs/change-log.md)。后续另有 CR-006（开发节奏 6 → 8 周）、CR-007（JDK 21 + Boot 4.1）、CR-008（账号上下文 DDD 重构）、CR-009（治理记录补录）、CR-010（三仓库 → 根目录 monorepo）。
