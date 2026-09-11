# Campus-Link Sprint 1 计划（地基与账号链路）

| 文档信息 | 内容 |
|---------|------|
| 版本 | v1.4 |
| 状态 | 已执行（出口自查部分未闭环） |
| 维护人 | 技术负责人（发起人兼任） |
| 关联阶段 | 开发（阶段四） |
| 上游依据 | [next-steps.md](../next-steps.md) §3 · [技术方案](../design/tech-design.md) · [PRD](../requirements/prd.md) · [变更台账](../change-log.md) |
| 最后更新 | 2026-09-11 |

> 版本号以 [docs/README.md](../README.md) 第 2 节为单一登记处，本文交叉引用不写版本号（手册 4.5）。
>
> 本文件是 Sprint 1 的执行依据（next-steps §3 为整体节奏参考：8 周 / 4 Sprint，见 [CR-006](../change-log.md)）。周期：2 周。
>
> **程序说明**：Sprint 1 在阻塞项 B1 / B2 未清零、且设计门行动项未闭环的情况下启动并执行完毕，该偏离见 [W-02](../tailoring-waivers.md) / [W-04](../tailoring-waivers.md)，待发起人签署。

## 目标与出口标准

**目标**：地基与账号链路——能用学号 + 姓名 + 邮箱验证码注册成功，能登录，能看到个人主页；发一段含 `<script>` 的 Markdown 不产生 XSS。

**出口自查**
- [ ] 注册链路联调通过（学籍核验 → 验证码 → 注册 → 登录 → `GET /users/me`）——待本机执行 `docker compose -f docker-compose.dev.yml up -d` 后按 backend/README 步骤联调
- [x] XSS 用例回归全绿（`MarkdownRendererTest` 6 用例）
- [x] `mvn verify` 与 `npm run build` 本机通过（2026-08-30：10/10 单测全绿，前端 1622 模块构建成功；2026-09-07 DDD 重构后 14/14 全绿）
- [ ] **单测覆盖率达标（手册 3.4：≥60%，核心模块 ≥80%）——当前不可测量**：`backend/pom.xml` 未接入 jacoco 或任何覆盖率插件，见 [W-05](../tailoring-waivers.md) 补偿措施 ②
- [ ] CI 全绿（待 B3 远程仓库接入后生效，工作流已就位于根目录 `.github/workflows/`）——**根仓库（monorepo）至今零提交且无远程，CI 从未运行过一次**，见 [W-05](../tailoring-waivers.md) / [W-06](../tailoring-waivers.md) / [CR-010](../change-log.md)
- [ ] Code Review 完成（发起人 / 第二角色）——手册要求至少 1 名同行批准，单人项目须以确定形式闭环，见 [W-05](../tailoring-waivers.md) 补偿措施 ④
- [ ] 静态扫描无新增阻断级问题（手册 3.4 出口标准）——**未接入**，见 [W-05](../tailoring-waivers.md)

> **提测准入门现状：上述 6 项中 4 项未满足，其中 3 项属"客观不可执行"**（覆盖率无插件、CI 无远程、静态扫描未接入）。按手册 3.4 出口标准，Sprint 1 目前**不具备提测条件**。

## 任务清单

| # | 任务 | 产出 | 状态 |
|---|------|------|------|
| T1 | 仓库脚手架 | backend：Spring Boot（**初版 3.2，v1.2 已升级至 4.1.1 + JDK 21，见 [CR-007](../change-log.md)**）+ Maven + application.yml + docker-compose.dev.yml（MySQL 8 + Redis 7）；frontend：Vue 3 + Vite + TS + Pinia + Element Plus | ✅ 2026-08-30 |
| T2 | 数据库 | `backend/sql/01_schema.sql`（11 张表 + 索引 + 标题 ngram 全文索引）+ `02_seed_boards.sql`（6 版块种子） | ✅ 2026-08-30 |
| T3 | 公共层 | 统一响应 `ApiResponse`（含 traceId）、`ResultCode` 分段错误码、全局异常处理、参数校验 | ✅ 2026-08-30 |
| T4 | **安全地基：Markdown 渲染** | `common/markdown/MarkdownRenderer`（flexmark + jsoup 白名单，服务端单点净化，ADR-005）+ XSS 回归用例 | ✅ 2026-08-30 |
| T5 | 鉴权骨架 | Spring Security + JWT 过滤器（jjwt）+ 角色解析 + CORS；FilterChain 收紧列入 Sprint 2 | ✅ 2026-08-30 |
| T6 | **学籍核验 + 注册登录** | F-ACC-004（verify-student / 一次性票据 / 学号占用 / IP 小时限流 / 姓名归一化）+ F-ACC-001（captcha / register / login，Redis 限流）+ 名册 CSV 导入（写审计日志） | ✅（bypass + 内置测试名册；B1/B2 清零后导入真实名册、关闭 bypass 回归） |
| T7 | 个人主页 | `GET /api/v1/users/me` 最小版（F-ACC-002；他人主页与资料编辑在 Sprint 2） | ✅ 2026-08-30 |
| T8 | 前端骨架 | 顶栏导航（6 版块入口 / 搜索占位 / 用户菜单）、注册登录页（学籍核验两步流 + 60s 验证码倒计时）、首页与版块占位页 | ✅ 2026-08-30 |
| T9 | 构建验证 | `mvn verify`（编译 + 单测 10/10 全绿）与 `npm run build`（1622 模块）本机通过 | ✅ 2026-08-30 |

## 阻塞与依赖（对应 next-steps.md，均未清零）

- **B1 / B2 学籍名册**：当前以 `app.roster.bypass=true` + 内置测试名册（学号 2023001/2023002/2023003/2024001 ↔ 姓名 张三/李四/王五/赵六）开发联调；真实名册到位后由 SUPERADMIN 走 `POST /api/v1/admin/roster/import` 导入，然后**关闭 bypass 并回归注册链路**（名册表结构已按"学号,姓名[,年级[,专业]]"预留，B2 字段决议如有出入需调整导入列映射）。
- **B3 仓库远程**：项目已合并为根目录单一仓库（monorepo，[CR-010](../change-log.md)）；GitHub Actions 工作流位于根 `.github/workflows/`（`backend-ci.yml` 跑 `mvn verify`、`frontend-ci.yml` 跑 `npm ci && npm run build`，各自以 `working-directory` 指向子目录并按 `paths` 过滤触发），**配置远程后自动生效**。
- **P1 UI 稿**：按发起人决议直接用 Element Plus 拼页面（行动清单 §4.3），视觉细节后续迭代。

## 测试名册（开发联调用，仅 bypass 模式）

| 学号 | 姓名 |
|------|------|
| 2023001 | 张三 |
| 2023002 | 李四 |
| 2023003 | 王五 |
| 2024001 | 赵六 |

## 变更记录

- v1.4（2026-09-11）——**编辑性修订，任务范围与完成情况零改动**（[CR-009](../change-log.md)）：① 头部"版本"字段长期停留在 v1.0、与变更记录（已到 v1.3）矛盾，本次校正；② 补手册 4.5 要求的"状态"字段；③ 上游依据去掉硬编码版本号（原写"技术方案 v0.2 / PRD v1.1"，技术方案实际已 v0.4）；④ T1 行标注 Spring Boot 3.2 → 4.1.1 升级事实；⑤ **出口自查补齐手册 3.4 要求但原先缺失的两项**——单测覆盖率门槛与静态扫描，均如实标注"当前不可执行"（`backend/pom.xml` 无 jacoco、CI 无远程），并标注提测准入门现状为 6 项中 4 项未满足，见 [W-05](../tailoring-waivers.md)；⑥ 头部补程序说明，指向 W-02 / W-04。
- v1.3（2026-09-07）——**DDD 二次开发重构**（ADR-012）：auth / roster / user 合并为 `module/account` 限界上下文四层（domain 含聚合 Account + 值对象 EmailAddress/StudentId + 9 个端口 + 领域事件；application 用例编排 + Command；infrastructure 适配器含 RosterGateway bypass/DB 双策略与注册事件 AFTER_COMMIT 审计监听；web 契约不变）。旧包文件已备份至 `backend/.refactor-backup-auth-roster-user.zip` 后删除（**该 zip 是缺少版本控制的替代产物，远程与首次提交完成后应删除，见 [W-06](../tailoring-waivers.md)**）。`mvn verify` 14/14 全绿（+4 领域单测）。前端优化：Element Plus 按需自动引入（主包 1056KB→271KB，gzip 347→100KB）、`src/constants/boards.ts` 去重、`useCountdown` 组合式函数、`ApiError` 统一错误模型、requiresAuth 路由守卫、VITE_API_TARGET 代理端口可覆盖。
- v1.2（2026-08-31）——技术栈升级（发起人决议）：JDK 17 → **21**，Spring Boot 3.2 → **4.1.1**（starter `web`→`webmvc`；MyBatis-Plus 改用官方 `mybatis-plus-spring-boot4-starter` 3.5.17，分页拦截器配套 `mybatis-plus-jsqlparser`；springdoc 3.1.0、jjwt 0.13.0、jsoup 1.23.2）。`mvn verify` 10/10 全绿，业务代码零改动兼容。
- v1.1（2026-08-30）——T9 构建验证完成：`mvn verify` 10/10 全绿、`npm run build` 通过。过程中修复四个问题并沉淀经验：① flexmark 0.64.x 表格扩展更名为 `flexmark-ext-tables`（原 ext-gfm-tables 停更于 0.50.x），ADR-005 相关依赖已修正；② 扩展须同时注册到 Parser 与 HtmlRenderer，否则节点解析成功但渲染为空；③ AES 密钥必须定长，现对密钥材料做 SHA-256 派生（`CryptoService`）；④ 数据库 DDL 中 `posts.title` 全文索引为 MySQL ngram（ADR-009），与 MySQL 8 环境配套。
- v1.0（2026-08-30）——创建；T1~T8 完成。
