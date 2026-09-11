# AGENTS.md — Campus-Link 项目协作指引

> 编码代理在本工作区工作的上下文入口。沟通用中文；代码、命令、变量名、文件路径保持英文。

## 项目概述

Campus-Link：面向**重庆工程学院计算机专业学生**的垂直交流论坛（"问有所答、学有同伴、求职有路"）。
核心功能：固定 6 版块（技术问答/学习资源/面经求职/竞赛交流/课程交流/闲聊灌水）、Markdown 帖子与代码高亮、问答最佳答案采纳、站内通知与搜索、**学籍核验注册（限本校，学号 + 姓名比对）**。
当前阶段：**阶段四开发 · Sprint 1 完成 + DDD 二次开发重构**（账号上下文 `module/account` 四层化，ADR-012），执行依据见 `docs/development/sprint-1.md`。

## 仓库结构（单仓库 monorepo）

- 根目录即**唯一** git 仓库（默认分支 `main`）；
- `docs/` —— 流程手册与全部项目文档；
- `backend/` —— Spring Boot 4.1 + Java 21 + MyBatis-Plus 3.5.17（Boot4 starter）+ MySQL 8 + Redis 7；
- `frontend/` —— Vue 3 + Vite + TS + Pinia + Element Plus；
- 项目已统一为根目录单一仓库（monorepo），`git` 操作在根目录进行即可，无需区分子仓库。

## 必读文档（按需查阅）

| 文档 | 内容 |
|------|------|
| `docs/README.md` | **文档版本号唯一登记处**（第 2 节）+ 阶段门状态表（第 1 节）；交叉引用其他文档时**不要写版本号**，只写链接 |
| `docs/process-handbook.md` | 7 阶段 6 评审门流程、缺陷等级 P0–P3、环境与分支策略、4.5 节文档归档规则 |
| `docs/next-steps.md` | 阻塞项（B1/B2 名册、B3 根仓库远程托管）与并行项，"现在该干什么"入口 |
| `docs/change-log.md` | **变更台账（CR-xxx）**。任何影响范围 / 排期 / 技术选型 / 架构 / 工程结构的改动，实施前先在此登记并做影响评估（范围 / 排期 / 质量），CR 编号写入 commit message |
| `docs/tailoring-waivers.md` | 流程偏离与让步放行记录（W-xxx / T-xxx）；新增偏离须登记于此，不得只在对话里说明 |
| `docs/reviews/gate-*.md` | 三道已过评审门的纪要（立项 / 需求 / 设计），含未闭环行动项——设计门 **A3-1（Sprint 2 开工阻断项）、A3-3、A3-4 已闭环**（[CR-017](docs/change-log.md) / [CR-019](docs/change-log.md)）；仍未闭环的是 A3-2（v0.3/v0.4 追认，**材料已备齐待签署**）、A3-5~A3-8 |
| `docs/reviews/retro-review-design-v03-v04.md` | 技术方案 v0.3/v0.4 的**追认评审材料**（A3-2）：已核实的证据 + 架构评审意见 **F-1~F-7**（其中 **F-2「后端无架构守护测试」**与写代码直接相关）+ 待签署的结论页 |
| `docs/design/tech-design.md` | 技术方案与 ADR-001~012，技术选型以它为准。§2.4 为 **ADR-012 四层架构**与依赖方向说明（原 D-1~D-7 七项文档缺陷已于 2026-09-11 修正，见 [CR-017](docs/change-log.md)） |
| `docs/design/api/README.md` | **API 契约快照**（`openapi.json` + 主链路请求/响应示例 + 与技术方案 §5 的逐条差异）。springdoc 从运行中的后端导出的**只读副本**：接口契约变更后须按该文档 §2 的命令**再生成**；快照当前**不声明错误响应与鉴权**（缺口 N-1 / N-2），前端错误处理与权限判断须以 `GlobalExceptionHandler`、`ResultCode` 与技术方案 §5 为准，**不能只读快照**（见该文档 §6） |
| `docs/requirements/prd.md` | 需求基线（含学籍核验 F-ACC-004 升 P0） |
| `docs/development/sprint-1.md` | Sprint 1 任务状态与出口自查 |

## 常用命令

> **开发期依赖栈用本机原生服务，不用 Docker**（[CR-011](docs/change-log.md)）；`docker-compose.dev.yml` 保留，发布阶段（阶段六）再启用。
> Redis：`D:\Workspace\TechResources\Redis\Redis-8.6.2-Windows-x64-msys2-with-Service\redis-server.exe`（在自身目录下以 `redis.conf` 启动，监听 127.0.0.1:6379，无密码）。
> MySQL：本机 `127.0.0.1:3306`，库 `campuslink`。**表结构不用手工导入**：Flyway 在后端启动时自动执行 `src/main/resources/db/migration/` 下未应用的迁移（[CR-014](docs/change-log.md)）。

**backend/**（构建需 **JDK 21**，本机先执行 `set "JAVA_HOME=D:\develop\Java\jdk-21"`）：

```bash
# 仅首次需要：库必须先存在，Flyway 才能连上（建库属基础设施引导，不归 Flyway 管）
mysql -h127.0.0.1 -uroot -p -e "CREATE DATABASE IF NOT EXISTS campuslink DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"

mvn verify            # 编译 + 单测（合码前必须全绿）
mvn spring-boot:run   # 启动：8088（SERVER_PORT 可覆盖）；启动时 Flyway 自动迁移建表
                      # 健康检查 /actuator/health，OpenAPI /api/docs
```

> 新增表 / 改列 / 加索引：在 `src/main/resources/db/migration/` 新增 `V<n>__<描述>.sql`（**不要改已执行的迁移**），规范见该目录 `README.md`。
> 纯数据增删：写 `backend/scripts/data/D<序号>__<描述>.py`（PyMySQL，默认 dry-run），规范见 `backend/scripts/README.md`。

**frontend/**：

```bash
npm install
npm run dev           # 5173，/api 代理到 8088
npm run build
```

**联调测试（Sprint 1 账号链路冒烟）**：浏览器打开 `http://localhost:5173/login`，注册 Tab：学籍核验 → 邮箱验证码 → 注册 → 登录 → 顶栏出现昵称。后端为 8088（见"常用命令"）。
**验证码**：默认已固定为 `123456`（`campuslink.captcha.fixed-code`，启动时会打 WARN）；把该配置留空则恢复随机 6 位，随机码在 `CODE_SENDER_MODE=log` 下只打在 backend 日志（行格式 `[DEV] captcha for <邮箱> => <6位码>`）。
**测试名册**（仅 `app.roster.bypass=true` 生效，学号须为 **9 位数字**）：`249971346/张三`、`249971347/李四`、`249971348/王五`、`249971349/赵六`。
**管理员账号**（dev-only 种子，与 bypass 同门控，bypass=false 时不存在）：`admin@campuslink.local`，登录用任意邮箱方式 + 验证码（`123456`），角色 `SUPERADMIN`，可调用 `/api/v1/admin/roster/import`；该账号**无学号、不走学籍核验**。
**入参格式**：学号 `^\d{9}$`；姓名限中文名（2~16 汉字，可含 `·`）或外文名（字母起头，可含空格 / `-` / `'` / `.`）。`nickname` 仍为 2~32 自由文本。

## 架构与编码约定

- 后端为**模块化单体 + DDD 分层（ADR-012）**：限界上下文 `module/{account,board,...}`，上下文内四层 `domain`（聚合根/值对象/领域服务/端口 gateway/领域事件）→ `application`（用例编排 + Command）→ `infrastructure`（MyBatis-Plus 仓储、Redis、通知等适配器）→ `web`（Controller + VO）；依赖方向：web/infrastructure → application → domain，**端口定义在 domain、实现在 infrastructure（DIP）**；跨上下文只允许调用对方 application 服务；MyBatis-Plus Mapper 统一放 `*.mapper` 包（`@MapperScan("com.campuslink.**.mapper")`；**实际值还含 `com.campuslink.common.audit`**，因 `AuditMapper` 不在 `.mapper` 包下——追认评审 **F-3**）；**⚠️ 四层与跨上下文规则当前无任何机器强制**（无 ArchUnit 等架构守护测试，`@MapperScan` 为宽松通配，写错包也能扫到），新增上下文时**必须人工自查依赖方向**；该敞口记为追认评审 **F-2**，处置待发起人签署 [追认纪要](docs/reviews/retro-review-design-v03-v04.md) §4 后确定；
- 统一响应 `ApiResponse{code,message,data,traceId}`，错误码分段（1xxx 通用 / 2xxx 账号 / 21xx 学籍 / 3xxx 帖子 / 4xxx 权限 / 5xxx 安全机审），见技术方案第 5 节；**数据库结构变更一律新增 Flyway 迁移** `src/main/resources/db/migration/V<n>__<描述>.sql`（**已执行的迁移不可修改**，回滚靠新增前向迁移，规范见该目录 README），**纯数据增删走** `backend/scripts/data/D<序号>__<描述>.py`（PyMySQL，默认 dry-run）；禁止 Hibernate 自动建表；
- **敏感信息**（邮箱/手机号/学号）：明文一律 AES-GCM 加密存 `*_enc`，等值查询用 HMAC 哈希 `*_hash`；任何接口不得返回 `*_enc` / `*_hash`；密钥只从环境变量读取（`APP_HASH_KEY` / `APP_CRYPT_KEY`），**源码、示例、测试不得写入可用凭据字面量**；
- **学籍核验**：学号须 9 位数字、姓名须为中文名或外文名格式（`VerifyStudentCommand` 校验 + `StudentId` 值对象不变量）；三种失败（学号不存在/姓名不匹配/已注册）统一提示，防名册枚举；`app.roster.bypass` 仅限开发联调，**生产必须为 false**（上线检查清单项）；
- **Markdown 渲染唯一出口** `common/markdown/MarkdownRenderer`（flexmark + jsoup 白名单）；flexmark 扩展须**同时注册到 Parser 与 HtmlRenderer**，否则节点解析成功但渲染为空；代码高亮由前端 highlight.js 完成；任何渲染改动必须保持 `MarkdownRendererTest` 全绿；
- **Redis 键命名**：所有键必须带应用命名空间前缀（`common/redis/RedisKeys.of(...)`，前缀常量 `campuslink:`）——本机 / 共享实例上常有多个应用共用同一 Redis，裸键名会冲突且无法按应用清理；**前缀只在 infrastructure 适配器补**，应用层只传逻辑键（如 `verify:ip:<ip>`），不要把 Redis 命名知识带进应用层；新增适配器须走 `RedisKeys`（`RedisKeyNamespaceTest` 现有 3 个适配器的断言，无编译期强制）；
- 前端页面按 PRD 5.1 清单实现；**Element Plus 按需自动引入**（unplugin-auto-import / unplugin-vue-components，勿回退全量引入）；API 统一走 `src/api/client.ts`（`ApiError` + JWT 注入 + 后端错误消息直接透出给 UI）；可复用逻辑放 `src/composables/`，版块等共享常量放 `src/constants/`；

## 测试约定

- **单测**：`mvn verify` 必须全绿；`MarkdownRendererTest` 的 6 个 XSS 回归用例是论坛安全生命线，渲染/白名单相关改动必须先补用例再改实现；
- **联调冒烟**：新链路合入前必须真实起栈（本机原生 MySQL / Redis + 后端 + 前端，见"常用命令"）并**用浏览器打开 `http://localhost:5173` 实测**，不能只依赖单测；
- **数据库结构变更必须随附新的 Flyway 迁移** `db/migration/V<n>__<描述>.sql`，不得手工改库、不得依赖 Hibernate 自动建表；`mvn verify` 不校验迁移可执行性（无集成测试），故结构变更后须真实启动一次确认迁移成功。
- **接口契约变更必须重生成 OpenAPI 快照** `docs/design/api/openapi.json`：凡改了 Controller 的路径 / HTTP 方法 / 入参出参类型 / 校验注解，或改了 `ApiResponse` 外壳、`OpenApiConfig`，都须起后端后按 [api/README.md](docs/design/api/README.md) §2 的命令重抓并格式化，然后看 `git diff`——**非空即契约已变**，须同步技术方案 §5 接口表与该 README 的端点清单/示例。快照是静态副本、**无任何机器校验**（CI 起不了栈，无法加"重新生成并 diff"步骤），漂移只能靠这条约定拦住；过期快照比没有快照更危险，因为前端会把它当契约真相。

## 文档与流程约定

- 阶段门未通过不得进入下一阶段；评审结论与状态由项目经理同步到 `docs/README.md` 阶段门状态表；
- **版本号只在 `docs/README.md` 第 2 节登记**；其他文档（含本文件）交叉引用时只写链接、不写版本号，避免升级后引用大面积失效；
- **基线级变更先登记后实施**：范围 / 排期 / 技术选型 / 架构 / 工程结构的改动，实施前在 `docs/change-log.md` 登记 CR 并写影响评估（范围 / 排期 / 质量），同步受影响文档；
- **流程偏离登记在 `docs/tailoring-waivers.md`**（W-xxx 让步放行 / T-xxx 已授权裁剪），评审门纪要归档在 `docs/reviews/gate-<n>-<name>.md`（手册 4.5 节）；
- 新文档放入 `docs/` 对应阶段目录并在 `docs/README.md` 登记；文件名英文小写中划线（如 `sprint-2.md`），文档内容用中文；
- 文档头部必须含：版本、状态、维护人、最后更新。

## 红线（与用户全局规则一致）

- **不自动 git commit / push**；提交前先展示变更摘要；commit message 用简洁英文；项目为单仓库（monorepo），统一在根目录操作；
- 删除文件/目录、修改 `.env`/密钥/证书、`git push`/`rebase`/`reset --hard`、公开发布：必须先征得用户同意；
- 生产环境红线：`app.roster.bypass=false`、**`campuslink.captcha.fixed-code` 必须留空**（固定验证码等同于取消验证码防线）、JWT 与加密密钥全部覆盖默认值、名册导入与内容处置必须写审计日志、机审降级开关（fail-closed）不得改为跳过审核。
