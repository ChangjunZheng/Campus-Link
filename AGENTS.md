---
name: Campus-Link
description: 重庆工程学院计算机专业学生交流论坛（Spring Boot 4.1 + Java 21 + Vue 3 单仓库，企业流程 BRD→PRD→设计→开发，单人 + AI 协作）
---

# AGENTS.md — Campus-Link 项目协作指引

编码代理在本工作区工作的上下文入口。**沟通用中文；代码、命令、变量名、文件路径保持英文。**

## 首要阅读（新会话启动顺序）

1. **本文件** —— 工作约定与红线；
2. **[`docs/进度驾驶舱.html`](docs/进度驾驶舱.html)** —— 项目进度 / 问题 / 待办的聚合视图（浏览器打开，快照日期见页头「最后更新」）；
3. **[`docs/下一步行动.md`](docs/下一步行动.md)** —— "现在该干什么"的单一入口（阻塞项 / 并行项 / 三件事）；
4. 本任务涉及的专项文档，按下方「必读文档」表**按需加载**，不要一次读完全部文档。

在核对实际本地配置之前，不要假设通用约定——端口、命令、开关以本文件「常用命令」为准。

## 项目定位

- **产品**：面向**重庆工程学院计算机专业学生**的垂直交流论坛（"问有所答、学有同伴、求职有路"）；
- **核心功能**：固定 6 版块（技术问答 / 学习资源 / 面经求职 / 竞赛交流 / 课程交流 / 闲聊灌水）、Markdown 帖子与代码高亮、问答最佳答案采纳、站内通知与搜索、**学籍核验注册（限本校，学号 + 姓名比对）**；
- **技术栈**：后端 Spring Boot 4.1 + Java 21 + MyBatis-Plus 3.5.17（Boot4 starter）+ MySQL 8 + Redis 7；前端 Vue 3 + Vite + TS + Pinia + Element Plus（按需自动引入）；
- **架构**：模块化单体 + DDD 四层（ADR-012），限界上下文 `module/{account,forum,...}`；
- **流程**：企业七阶段流程（BRD → PRD → 设计 → 开发 → …），基线级变更走 CR 台账；单人项目 + AI 协作，流程偏离已由发起人签署（W-01 ~ W-07）。

## 项目状态速览（快照 2026-09-12；每次收尾更新，细节看驾驶舱）

- **阶段四开发中**：Sprint 1 ✅（账号链路，T1~T9）；Sprint 2 ✅（论坛最小 MVP，M1~M6、A1~A5 真机验收通过）；Sprint 3 未开工、计划未创建；
- **B4 / B5（= A3-9 / A3-10）已于 2026-09-12 由 [CR-028](docs/变更日志/变更台账.md) 完成**：ArchUnit `ArchitectureGuardTest`（10 用例）把 ADR-012 四层规则**首次变为机器强制**（规则吸收复评 R-1 / R-2 / R-3；R-1 裁决：禁止 web 注入 domain 端口，`UserController` 已改经 application）；**N-4 闭环**（每个映射方法必须显式 `@PublicEndpoint` 或 `@SecurityRequirement`，受保护者必须调用 `CurrentUser` 统一入口）；F-1 / F-3 / F-4 与 N-5 / N-6 同批处置；单测 87 → **107** 全绿。**残留**：`SecurityConfig` 仍 `permitAll()`（路径级拦截未做）；**W-02 / W-07 的关闭前置已成立，状态为"可关闭、待发起人签署"（任何文档不代签）**；
- **流程已降密度（[CR-029](docs/变更日志/变更台账.md)）**：CR 自 CR-030 起改一行式登记；**项目状态只维护 change-log §1 + 进度驾驶舱两处**；取消凭证回填仪式（凭证 = commit message 写 CR 号）；AI 遵守下方「汇报约定」；
- **唯一外部依赖**：B1 / B2 学籍名册（未到位前注册链路只能走 `app.roster.bypass`）；
- **提测准入门 7 项中 4 项未满足**（覆盖率 / 静态扫描 / Code Review / UI 走查）；让步放行 W-01 ~ W-07 中**仅 W-06 已关闭**，**W-02 / W-07 关闭前置已成立（[CR-028](docs/变更日志/变更台账.md)）但待发起人签署、不得代签**；
- ⚠️ 以上任何一项变化后，**须更新驾驶舱**（见「工作收尾约定」）。

## 仓库结构（单仓库 monorepo）

- 根目录即**唯一** git 仓库（默认分支 `main`），git 操作在根目录进行；
- `docs/` —— 流程手册与全部项目文档（含 [`docs/进度驾驶舱.html`](docs/进度驾驶舱.html) 进度驾驶舱）；
- `backend/` —— Spring Boot 4.1 + Java 21 + MyBatis-Plus；
- `frontend/` —— Vue 3 + Vite + TS。

## 必读文档（按需加载）

| 文档 | 什么时候读 |
|------|-----------|
| [`docs/进度驾驶舱.html`](docs/进度驾驶舱.html) | 想一眼看清进度 / 问题 / 待办全貌（聚合视图；**数据可能滞后，以事实源文档为准**） |
| [`docs/README.md`](docs/README.md) | **文档版本号唯一登记处**（§2）+ 阶段门状态表（§1）；新增文档必须在此登记 |
| [`docs/下一步行动.md`](docs/下一步行动.md) | 动工前：阻塞项（B1~B5）、并行项、当前该做的三件事 |
| [`docs/变更日志/变更台账.md`](docs/变更日志/变更台账.md) | 任何基线级变更**登记前**；想知道某次变更的来龙去脉（§1 汇总表先看） |
| [`docs/流程偏离记录.md`](docs/流程偏离记录.md) | 涉及流程裁剪 / 偏离时（W-xxx / T-xxx 单一登记处） |
| [`docs/流程手册.md`](docs/流程手册.md) | 流程规则本身：4.2 变更登记、4.5 文档归档、5.1 裁剪指南 |
| [`docs/设计/技术方案.md`](docs/设计/技术方案.md) | 技术选型与 **ADR-001~012**；接口清单与错误码分段见 §5 |
| [`docs/设计/接口契约/README.md`](docs/设计/接口契约/README.md) | 改接口 / 错误码 / 鉴权**之后**重生成快照（§2 命令）；N-1 ~ N-6 六项缺口的处置与残留见 §6.2（**N-4 已由 [CR-028](docs/变更日志/变更台账.md) 闭环**） |
| [`docs/设计/UI规范/界面规范.md`](docs/设计/UI规范/界面规范.md) | 新增 / 修改前端页面前：令牌表、组件与交互约定（W-03 补偿①落点） |
| [`docs/开发/Sprint2计划.md`](docs/开发/Sprint2计划.md) · [`docs/开发/Sprint2增量设计.md`](docs/开发/Sprint2增量设计.md) | 做论坛相关任务时：IN/OUT 范围、六端点契约、三条人工自查（§4）、落码偏差（§8 P-1~P-8） |
| [`docs/评审/gate-*.md`](docs/评审/立项门纪要.md) · [`docs/评审/技术方案追认纪要.md`](docs/评审/技术方案追认纪要.md) | 追溯评审结论与行动项（A1-*/A2-*/A3-* 状态、F-1~F-7、R-1~R-3） |
| [`docs/需求/产品需求文档.md`](docs/需求/产品需求文档.md) | 需求基线（学籍核验 F-ACC-004 为 P0） |

## 常用命令

> **开发期依赖栈用本机原生服务，不用 Docker**（[CR-011](docs/变更日志/变更台账.md)）；`docker-compose.dev.yml` 保留，发布阶段（阶段六）再启用。
> Redis：`D:\Workspace\TechResources\Redis\Redis-8.6.2-Windows-x64-msys2-with-Service\redis-server.exe`（在自身目录下以 `redis.conf` 启动，监听 127.0.0.1:6379，无密码）。
> MySQL：本机 `127.0.0.1:3306`，库 `campuslink`。**表结构不用手工导入**：Flyway 在后端启动时自动执行迁移（[CR-014](docs/变更日志/变更台账.md)）。

**backend/**（构建需 **JDK 21**，本机先执行 `set "JAVA_HOME=D:\develop\Java\jdk-21"`）：

```bash
# 仅首次需要：库必须先存在，Flyway 才能连上（建库属基础设施引导，不归 Flyway 管）
mysql -h127.0.0.1 -uroot -p -e "CREATE DATABASE IF NOT EXISTS campuslink DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"

mvn verify            # 编译 + 单测（合码前必须全绿，当前基线 107 个；含架构守护测试）
mvn spring-boot:run   # 启动：8088（SERVER_PORT 可覆盖）；健康检查 /actuator/health，OpenAPI /api/docs
```

**frontend/**：

```bash
npm install
npm run dev           # 5173，/api 代理到 8088（可用 VITE_API_TARGET 覆盖）
npm run build
```

**联调冒烟（账号链路）**：浏览器打开 `http://localhost:5173/login`，注册 Tab：学籍核验 → 邮箱验证码 → 注册 → 登录 → 顶栏出现昵称。
**联调冒烟（论坛主链路）**：登录后走 首页版块列表 → 进版块 → 发帖（Markdown + 代码块）→ 帖子详情 → 楼层回帖，刷新后数据仍在；**匿名访问 `POST /api/v1/posts` 必须 401**。
**验证码**：默认固定 `123456`（`campuslink.captcha.fixed-code`，启动打 WARN）；留空恢复随机 6 位（`CODE_SENDER_MODE=log` 下只打日志，行格式 `[DEV] captcha for <邮箱> => <6位码>`）。
**测试名册**（仅 `app.roster.bypass=true` 生效，学号须 **9 位数字**）：`249971346/张三`、`249971347/李四`、`249971348/王五`、`249971349/赵六`。
**管理员账号**（dev-only 种子，与 bypass 同门控）：`admin@campuslink.local`，任意邮箱方式 + 验证码 `123456`，角色 `SUPERADMIN`，可调用 `/api/v1/admin/roster/import`；**无学号、不走学籍核验**。
**入参格式**：学号 `^\d{9}$`；姓名限中文名（2~16 汉字，可含 `·`）或外文名（字母起头，可含空格 / `-` / `'` / `.`）；`nickname` 2~32 自由文本。

## 架构与编码约定

### 后端分层（ADR-012，强制）

- 限界上下文 `module/{account,forum,...}`，上下文内四层：`domain`（聚合根 / 值对象 / 领域服务 / **端口 gateway** / 领域事件）→ `application`（用例编排 + Command）→ `infrastructure`（MyBatis-Plus 仓储、Redis、通知等适配器）→ `web`（Controller + VO）；
- 依赖方向：**web / infrastructure → application → domain**；端口定义在 domain、实现在 infrastructure（DIP）；**跨上下文只允许调用对方 application 服务**；
- MyBatis-Plus Mapper 统一放 `*.mapper` 包；`@MapperScan` 的值**恰为单值** `{"com.campuslink.**.mapper"}`（2026-09-12 [CR-028](docs/变更日志/变更台账.md) 将 `AuditMapper` 移入 `common/audit/mapper/` 后收窄，**并由守护测试断言"恰为单值"——再加特例即测试失败**）；
- ✅ **四层与跨上下文规则自 [CR-028](docs/变更日志/变更台账.md)（A3-9）起由 `ArchitectureGuardTest` 机器强制**（ArchUnit，10 用例）：分层依赖方向 / domain 不依赖框架（lombok 放行、`domain → common` 放行）/ **web 不得注入 `domain.gateway` 端口、不得调 `domain.service`（R-1），允许对 `domain.model` 与不可变载体做只读类型引用（R-2）** / 跨上下文只调对方 `application` / 端口实现必须在同上下文 `infrastructure` / Mapper 实现必须在 `..mapper..` 包 / N-4 声明一致性。**改依赖结构前先读 [技术方案 §2.4](docs/设计/技术方案.md)（规则已与该节双向绑定，任一侧改动须同步）**；新增上下文最有效做法仍为**照抄 `module/{account,forum}` 的结构**（范式可复制性已由 A3-11 复评确认，结论见追认纪要 §8）。

### 错误响应与契约声明（[CR-021](docs/变更日志/变更台账.md) 起）

- 错误外壳 `common/result/ApiError{code,message,traceId}`（**无 `data`**，与成功外壳 `ApiResponse` 分开）；**所有 ≥400 的响应只能由 `GlobalExceptionHandler` 产出**，Controller 只 `throw new ApiException(ResultCode.X)`，不自己拼错误体；
- **HTTP 状态码不另行约定**——每个 `ResultCode` 自带 `httpStatus`，改状态码只改枚举一处；
- 契约里的错误响应由 `OpenApiErrorResponseCustomizer` 从 `ResultCode` **单点派生**：端点用项目自有注解 `@ErrorCodes({ResultCode.X, ...})` 声明业务错误码（通用 400/500 自动补，不必声明）；**鉴权声明（N-4 闭环，[CR-028](docs/变更日志/变更台账.md) 起机器强制）**：`module/*/web` 下**每个 HTTP 映射方法必须显式二选一**——公开端点标 `@PublicEndpoint`、受保护端点标 `@SecurityRequirement(name = ApiDocs.BEARER_AUTH)` **且方法体真的调用 `common/web/CurrentUser`**（`requireId` / `requireRole`，统一抛 `NOT_LOGGED_IN` / `FORBIDDEN`）——漏写即守护测试失败；**不要加全局 security**（会把公开端点错标为需鉴权）；**禁止用 swagger 的 `@ApiResponses` 手写错误响应**（事实存两份，改错误码必然漂移）；
- **日志分档**：业务错误不记日志、框架级客户端错误记 1 行 WARN 不打全栈、只有兜底 `Exception` 记 ERROR + 全栈（客户端错误进 ERROR 会污染 5xx 监控——N-3 缺陷成因）。

### 数据与迁移

- 统一响应 `ApiResponse{code,message,data,traceId}`；错误码分段（1xxx 通用 / 2xxx 账号 / 21xx 学籍 / 3xxx 帖子 / 4xxx 权限 / 5xxx 安全机审），见技术方案 §5；分页口径 **`page` / `size`**（短名，从 1 起）；
- **结构变更一律新增 Flyway 迁移** `src/main/resources/db/migration/V<n>__<描述>.sql`（**已执行的迁移不可修改**，回滚靠新增前向迁移）；**纯数据增删走** `backend/scripts/data/D<序号>__<描述>.py`（PyMySQL，默认 dry-run）；**禁止 Hibernate 自动建表**。

### 安全

- **敏感信息**（邮箱 / 手机号 / 学号）：明文一律 AES-GCM 加密存 `*_enc`，等值查询用 HMAC 哈希 `*_hash`；任何接口不得返回 `*_enc` / `*_hash`；密钥只从环境变量读取（`APP_HASH_KEY` / `APP_CRYPT_KEY`），**源码、示例、测试不得写入可用凭据字面量**；
- **学籍核验**：三种失败（学号不存在 / 姓名不匹配 / 已注册）统一提示，防名册枚举；`app.roster.bypass` 仅限开发联调，**生产必须为 false**（上线检查清单项）；
- **Markdown 渲染唯一出口** `common/markdown/MarkdownRenderer`（flexmark + jsoup 白名单）；flexmark 扩展须**同时注册到 Parser 与 HtmlRenderer**，否则解析成功渲染为空；代码高亮由前端 highlight.js 完成（**尚未引入，属 ADR-005 欠账**）；任何渲染改动必须保持 `MarkdownRendererTest` 全绿；
- **Redis 键命名**：所有键必须带 `campuslink:` 前缀（走 `common/redis/RedisKeys.of(...)`）；**前缀只在 infrastructure 适配器补**，应用层只传逻辑键；新增适配器须走 `RedisKeys`（`RedisKeyNamespaceTest` 有 3 个适配器断言，无编译期强制）。

### 前端

- 页面按 PRD 5.1 清单实现；**Element Plus 按需自动引入**（unplugin-auto-import / unplugin-vue-components，勿回退全量引入）；
- **样式体系（[CR-023](docs/变更日志/变更台账.md) 建立、[CR-024](docs/变更日志/变更台账.md) 调色）**：`src/styles/tokens.css` 是**设计令牌唯一来源**——主色 `#409eff`、**正文链接色 `#1a6fc4` 独立于主色**（`#409eff` 白底 2.78:1 不适合小号文字）、字号阶梯、间距为 4 的倍数、圆角三档、0.5px 细边框、去默认阴影 / 渐变；以 `:root:root` 覆写 EP 的 `--el-*`；**改主色只改 `--cl-color-primary` 一个值，但 `--el-color-primary-rgb` 须手工同步**（链接色不派生、不随动）；`src/styles/tailwind.css` 提供工具类（`@theme inline` 映射令牌），两者在 `main.ts` 一次性引入；
- **层叠边界（强制约定，无 lint 强制）**：Tailwind v4 输出在 `@layer` 内、EP 样式不带 layer，**无层恒胜有层——EP 组件一律通过 CSS 变量调样式，自有元素才用工具类**；新增 / 修改页面前先查 [ui-guideline](docs/设计/UI规范/界面规范.md)；
- API 统一走 `src/api/client.ts`（`ApiError` + JWT 注入 + 后端错误消息直接透出 UI）；可复用逻辑放 `src/composables/`，共享常量放 `src/constants/`。

## 测试约定

- **单测**：`mvn verify` 必须全绿（**当前基线 107 个**，含 `ArchitectureGuardTest` 架构守护测试）；`MarkdownRendererTest` 的 6 个 XSS 回归用例是论坛安全生命线，渲染 / 白名单改动**先补用例再改实现**；
- **受保护端点必须真的校验鉴权（N-4 已闭环，[CR-028](docs/变更日志/变更台账.md)；机制为机器强制）**：**每个 HTTP 映射方法必须显式标 `@PublicEndpoint` 或 `@SecurityRequirement`，且标后者者必须真的调用 `common/web/CurrentUser`（`requireId` / `requireRole`）**——`ArchitectureGuardTest` 会拦（漏写即测试失败）；**新增需登录的端点仍须补一个「匿名 → 401」单测**（真机 401 也要验）。⚠️ **`SecurityConfig` 仍是 `anyRequest().permitAll()`**——闭环的是"漏写鉴权静默变公开"，**不是**框架级路径拦截，登录态仍由 Controller 主动校验；
- **联调冒烟**：新链路合入前必须真实起栈（本机 MySQL / Redis + 后端 + 前端）并**浏览器实测**，不能只依赖单测；
- **结构变更后真实启动一次**确认 Flyway 迁移成功（`mvn verify` 不校验迁移可执行性，也不校验 `@MapperScan` 通配的实际扫描结果）；
- **接口契约变更必须重生成 OpenAPI 快照** `docs/设计/接口契约/openapi.json`：凡改 Controller 路径 / 方法 / 入参出参 / 校验注解、**新增端点（公开 / 受保护声明）**、`ApiResponse` / `ApiError` 外壳、`OpenApiConfig`、**`common/web/CurrentUser` 或 `PublicEndpoint`**、`ResultCode` 的错误码 / 提示语 / `getHttpStatus()` 映射、或把 `SecurityConfig` 从 `permitAll()` 改成真正按路径授权，都须起后端后按 [api/README.md](docs/设计/接口契约/README.md) §2 命令重抓并格式化，看 `git diff`——**非空即契约已变**，同步技术方案 §5 与该 README。**快照无机器校验，漂移只能靠这条约定拦**；`1002` / `1003` / `1004` 三类响应永远进不了快照，以该 README §6.3 文字约定为准。

## 汇报约定（每次 AI 会话执行）★

单人项目 + 多会话并行，用户对"AI 在干什么"必须有完整可见性：

- **开工先报计划**：任何实质工作开始前，先用 ≤3 行说明"这轮要做什么、改哪些文件"；
- **小步汇报**：长任务拆成小步，每完成一步用一两句话报告进展；**不要一口气跑完多个批次才汇报**（CR-025 六批次连跑是被记录在案的反面教训）；
- **收尾摘要 ≤10 行**：改了什么 / 为什么 / 验证结果，并指向驾驶舱与相关文档；台账登记只报"已登记 CR-xxx"，不复述台账内容；
- **一次会话只做一个逻辑任务**，除非用户明确要求连做；
- **并行会话敏感性**：改公共文档（change-log / AGENTS.md / 驾驶舱 / docs/README）前先看当前内容是否已被其他会话更新，**用追加与加注，不做整段覆写**。

## 工作收尾约定（每次任务完成后执行）★

1. **更新进度驾驶舱 [`docs/进度驾驶舱.html`](docs/进度驾驶舱.html)**——触发条件：本次工作改变了进度事实（新 CR / 欠账状态变化（A3-*、W-*、B-*、N-*）/ Sprint 里程碑 / 新问题 / 新业务需求）。更新内容：页头「最后更新」与数据快照日期；① 总览统计与阶段 / Sprint 状态；② 时间线新增 CR 行（**从 change-log §1 抄录**，含日期 / 标题 / 类型 / 状态 / 评审方式 / 提交凭证）；③ 问题台账状态；④ 待办与新需求。**边界**：驾驶舱只做聚合，**不得登记 change-log / next-steps / tailoring-waivers 等事实源文档中没有的新事实**；只改数据，不改结构与样式；更新前先核对 change-log 最新条目，防止驾驶舱静默过时；
2. **基线级变更确认已登记（[CR-029](docs/变更日志/变更台账.md) 起用轻量格式）**：范围 / 排期 / 技术选型 / 架构 / 工程结构的改动，实施前在 `docs/变更日志/变更台账.md` §1 登记——**一行一条**（改了什么 / 为什么 / 凭证），**不再写九行明细表与三维度影响评估**；CR 编号写入 commit message 即完成凭证对齐，**不做台账回填仪式**；
3. **展示变更摘要**：列出本次改动的文件与要点，**不自动 git commit / push**，等用户确认。

## 文档与流程约定

- 阶段门未通过不得进入下一阶段；评审结论与状态由项目经理同步到 `docs/README.md` 阶段门状态表；
- **版本号只在 `docs/README.md` §2 登记**；其他文档（含本文件）交叉引用只写链接、不写版本号；
- **状态两处收口（[CR-029](docs/变更日志/变更台账.md) 起）**：项目当前状态只维护 **change-log §1 + 进度驾驶舱** 两处；next-steps / gate-* / waivers 等文档中的既有状态保留为历史，**不再新增状态同步义务**——新状态变化时只改两处；
- **流程偏离登记在 `docs/流程偏离记录.md`**（不得只在对话里说明）；评审门纪要归档在 `docs/评审/gate-<n>-<name>.md`；
- **文档命名用中文（[CR-030](docs/变更日志/变更台账.md) 起，推翻原"英文小写中划线"规则）**：文档与文件夹一律用中文名（如 `docs/评审/设计门纪要.md`）；**唯一例外 `README.md`**（GitHub 目录入口约定）；目标结构：`docs/{立项,需求,设计,开发,评审,模板,变更日志}/`；
- **简洁原则**：流程文档只保留"当前结论 + 指针"，**日志式过程记录（实施补记、状态回填、凭证追踪、版本变更记录）一律写入 `docs/变更日志/`**，不在流程文档里累积；
- **批量迁移待执行**：中文重命名与日志迁移将在 **CR-028 实施会话结束后一次性批量执行**（含全量链接修复），避免与并行会话按旧路径写文件撞车；在此之前新文档暂维持现名；
- 新文档放入 `docs/` 对应位置并在 `docs/README.md` §2 + §3 **两处登记**（漏登记即产生文档不一致），内容用中文；
- 文档头部必须含：**版本、状态、维护人、最后更新**。

## 红线（与用户全局规则一致）

- **不自动 git commit / push**；提交前先展示变更摘要；commit message 用简洁英文；monorepo 统一根目录操作；
- 删除文件 / 目录、修改 `.env` / 密钥 / 证书、`git push` / `rebase` / `reset --hard`、公开发布：**必须先征得用户同意**；
- **生产环境红线**：`app.roster.bypass=false`、`campuslink.captcha.fixed-code` 必须留空（固定验证码等同取消验证码防线）、JWT 与加密密钥全部覆盖默认值、名册导入与内容处置必须写审计日志、机审降级开关（fail-closed）不得改为跳过审核。
