# OpenAPI 快照说明（`./`）

| 文档信息 | 内容 |
|---------|------|
| 版本 | v1.9.2 |
| 状态 | 已归档（**快照**，接口契约变更后须再生成，见 §2） |
| 维护人 | 技术负责人（发起人兼任） |
| 最后更新 | 2026-09-15 |
| 对应行动项 | [设计门纪要](../../评审/设计门纪要.md) **A3-4**：补归档 OpenAPI 快照，或补 2~3 个主链路接口请求/响应结构（**两个备选项本文都做**） |
| 变更登记 | [CR-019](../../变更日志/变更台账.md#cr-019)（首次归档）· [CR-021](../../变更日志/变更台账.md#cr-021)（修复 N-1 / N-2 / N-3 后**再生成**：契约新增错误响应与 bearerAuth 声明）· [CR-022](../../变更日志/变更台账.md#cr-022)（Sprint 2 MVP 新增 6 个论坛端点后**再生成**：6 → **12** 端点）· [CR-028](../../变更日志/变更台账.md#cr-028)（**N-4 闭环后**再生成：`bearerAuth` 描述据实改写，**端点 / schema 数一字未变**）· [CR-031](../../变更日志/变更台账.md#cr-031)（**路径级鉴权落地后再生成**：`bearerAuth` 描述随框架拦截据实改写，**端点 / schema / `security` 声明仍未变**）· [CR-043](../../变更日志/变更台账.md)（**采纳最佳答案落地后再生成**：新增 `POST /posts/{id}/accept` 与 `AcceptReplyCommand` schema，12 → **13** 端点）· [CR-048](../../变更日志/变更台账.md)（**点赞收藏落地后再生成**：新增 4 端点与 `InteractionResult` schema，13 → **17** 端点）· [CR-049](../../变更日志/变更台账.md)（**站内搜索落地后再生成**：新增公开端点 `GET /posts/search`，17 → **18** 端点，**schema 数未变**）· [CR-050](../../变更日志/变更台账.md)（**通知中心落地后再生成**：新建 `module/notification` 上下文，新增 3 个受保护端点与 7 个 schema，18 → **21** 操作 / 33 → **40** schema；⚠️ 本次 diff **非纯新增**，另有 1 处 springdoc 的 `operationId` 重编号，见 §2）· [CR-051](../../变更日志/变更台账.md)（**时间戳时区口径修复——非再生成**：只改数据源时区参数、不动接口形状，重抓核对 `git diff` **为空**，快照文件零改动；本文新增 §1「时间字段口径」条与 §2 核对行，见 §7 v1.9.2） |
| 关联记录 | [技术方案](../技术方案.md) §5 接口契约 · [PRD](../../需求/产品需求文档.md) F-ACC-004 · [Sprint 1 计划](../../开发/Sprint1计划.md) · [Sprint 2 计划](../../开发/Sprint2计划.md) · [Sprint 2 增量设计](../../开发/Sprint2增量设计.md) |

## 1. 这份文件是什么、不是什么

- **是**：`openapi.json` —— 由 springdoc 从**运行中的后端**导出的一次**冻结快照**，用于设计门归档与"文档 vs 实现"的差异比对；
- **不是**手写的接口规范：契约的权威来源永远是运行中的服务（`GET /api/docs`，Swagger UI 在 `/api/docs/swagger-ui.html`）。本文与快照一旦落后于代码，以代码为准并**立即再生成**；
- **覆盖范围是**已实现的 **21 个端点**（19 条路径，本文口径为"**一个 operation 计一个端点**"，故路径数与端点数本就不同：`/api/v1/posts` 与 `/api/v1/posts/{postId}/replies` 各含 GET + POST），账号 6 + 论坛 12 + **通知 3**，不得读成"全部接口契约已归档"——[技术方案](../技术方案.md) §5 接口表中仍有多行属后续 Sprint 计划项（逐条差异见 §5）。因此技术方案文末检查清单第 3 项"接口契约**完整**"**仍不勾选**；
- **自 [CR-021](../../变更日志/变更台账.md#cr-021) 起，快照也表达错误响应与鉴权**：每个操作都声明了 4xx / 5xx 及错误体 schema `ApiError`，`components.securitySchemes.bearerAuth` 已定义，**12 个**受保护操作带 `security`（`users/me`、`admin/roster/import`、`POST /posts`、`POST /posts/{postId}/replies`、`POST /posts/{id}/accept`、点赞 ×2、收藏、`favorites/mine`、**通知 ×3**（列表 / 未读数 / 全部已读，[CR-050](../../变更日志/变更台账.md) —— 前三个不属于账号或论坛上下文的受保护端点）。**N-4 的"漏写鉴权静默变公开"缺口已由 [CR-028](../../变更日志/变更台账.md#cr-028) 闭环**（统一入口 `CurrentUser` + `ArchitectureGuardTest` 机器校验）；**路径级拦截已由 [CR-031](../../变更日志/变更台账.md#cr-031) 落地**（`SecurityConfig` 去 `permitAll()`，按端点注解裁决，见 §6.2），另有 4 类事实快照表达不了（§6）——读快照前必须先看 §6；
- **时间字段口径（[CR-051](../../变更日志/变更台账.md) 起，2026-09-15）**：快照里所有 `format: date-time` 的字段（`createdAt` / `updatedAt` 等）**值即为真 UTC**，`Z` 后缀可信，前端可直接 `new Date(...)` 后按本地时区展示。**实现口径**：字段类型为 `java.time.Instant`，序列化恒 UTC（JSR-310，与 `spring.jackson.time-zone` 无关）；**存储侧**由数据源 URL 的 `connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true` **成对**把 MySQL 会话时区强制为 `+00:00`（否则 V1 各表的 `DEFAULT CURRENT_TIMESTAMP` 落的是本机本地墙钟，见 [技术方案](../技术方案.md) §4.1），由 `config/DataSourceTimezoneTest` 守护防回退。⚠️ **不受本条影响的字段**：`expiresAtEpochSecond`（整数 epoch 秒，由 `Instant.now().getEpochSecond()` 产出、不经数据库，L-6 期间也一直正确）。⚠️ **本条之前的历史实测值带 +8 小时偏移**（Sprint 2 起至 2026-09-14，台账外问题 **L-6**）——读 §4 各主链路实测响应时注意这一点，**存量开发数据已一次性 −8h 校正**，但**采集于修复前的文字与取值不回填改写**（保留"当时实测"的证据价值）；
- **不留多份历史副本**：本目录只有一个当前快照，历史版本由 git 承担（[CR-008](../../变更日志/变更台账.md#cr-008) 的教训——不该用 zip / 副本替代 git 历史）。

## 2. 快照来源与再生成

| 项 | 值 |
|---|---|
| 首次归档 | 2026-09-11（[CR-019](../../变更日志/变更台账.md#cr-019)，源提交 `68d40ae`，6 端点 / 13 schema） |
| **本次生成时间** | **2026-09-14 22:46**（[CR-050](../../变更日志/变更台账.md) 通知中心落地后**再生成**：新增 3 个受保护端点，18 → **21** 操作；**schema 33 → 40**（新增 7 个：`NotificationVo` / `PageVoNotificationVo` / `UnreadCountResult` / `MarkAllReadResult` + 对应 `ApiResponse*` 外壳 3 个）；**tag 7 → 8**（新增 `notification`）；受保护操作 **9 → 12**） |
| **代码来源** | ✅ **提交 `70c6ca9`**（CR-050）。**"提交后核对"已于 2026-09-15 完成**——自 `http://localhost:8088/api/docs` 重抓、按 §2 同一方法格式化后与仓库快照**逐字节一致**（59977 字节），快照自此对应提交 `70c6ca9`（与 v1.5.1 / v1.6.1 / v1.7.1 / v1.8.1 同例）。方法学备注：本次快照直接自本机 **8088** 端口实例抓取（`APP_ROSTER_BYPASS=false` 的重启实例，注册链路走真实名册表），`servers` 天然即 `http://localhost:8088`，**无需回写**；⚠️ 抓取须用 **`http://localhost:8088/api/docs`** 而非 `127.0.0.1`——springdoc 会把 `servers` 回显成请求 Host，那是**抓取方式**差异、不是契约差异（v1.8.1 已记过一次） |
| 后端地址 | `http://localhost:8088`（开发端口，[CR-012](../../变更日志/变更台账.md#cr-012)）——快照 `servers` 字段即此值，**部署形态（ADR-011）确定后须重生成** |
| 生成方 | springdoc-openapi 3.1.0（`springdoc-openapi-starter-webmvc-ui`） |
| 规范版本 | OpenAPI **3.1.0**；`info.title` = `Campus-Link API`，`info.version` = `0.1.0`（未随 Sprint 递增，见 §7 遗留） |
| 规模 | **8 个 tag** / **21 个端点**（19 条路径）/ **40 个 schema**（v1.8 为 7 tag / 18 端点 / 33 schema）；文件 **52003 → 59977 字节**。tag 清单：`admin-roster` / `auth` / `board` / `favorite` / **`notification`**（本次新增）/ `post` / `reply` / `user` |
| 落盘处理 | 原始响应单行压缩 → 按 2 空格缩进格式化。**键序保持 springdoc 原序，未排序、未删改任何字段**；本次 `servers` **无需回写**（直接抓 8088） |
| 可复现性 | 格式化方法与历次一致（`json.dumps(ensure_ascii=False, indent=2)` + 尾部换行），`git diff` 仍可作为契约变更的可靠信号 |
| 本次 diff 性质 | ⚠️ **非纯新增**：`git diff --numstat` **322 行新增 / 1 行删除**。删除行不像 v1.4 / v1.5 那样是一次文案改写（那两次也是 1 增 1 删），而是**历次再生成里第一次动到某个既有端点自身的内容**——`GET /api/v1/boards` 的 `operationId` 由 **`list_2` → `list_3`**。根因：springdoc 默认生成器按"**方法名 + 同名出现次序**"编号，`list` 这个方法名现在被 4 个 Controller 共用（`PostController` / `ReplyController` / **`NotificationController`（本次新增）** / `BoardController`），新操作插在中间把 boards 的序号顶后一位（`GET /notifications` 取到 `list_2`）。**判定：生成器编号漂移，不是契约变化**——该端点的路径 / 方法 / 入参 / 出参 / `security` 全部未变，且前端 `src/api/*` 为**手写**、项目无 openapi 代码生成环节，故**无破坏性**。**但如实登记这条纪律**：`git diff` 出现删除行即须逐行看清并给出判定，**不能默认"多了几个端点而已"**；其余 322 行新增恰为 3 个通知端点与 7 个 schema，无其他既有字段被改动 |
| **CR-051 重抓核对（2026-09-15）** | [CR-051](../../变更日志/变更台账.md) 只改数据源 URL 的时区参数、不动 Controller / VO / `ResultCode` / 鉴权声明，故按本节命令起后端重抓后 **`git diff --numstat openapi.json` 为空**（59977 字节，逐字节一致）——**该方案的验收点之一即"快照 diff 必须为空"**，非空就说明意外改到了接口形状。⚠️ 注意快照的**值**里没有时间戳，diff 为空也**不代表时间口径未变**：时间口径的变更只体现在 §1 的口径条与响应体的实际取值上，`Z` 由"假 UTC"变成真 UTC 是**数据层**变化、不是**契约层**变化，这正是本行要写清的原因。**快照代码来源仍为提交 `70c6ca9`**（CR-051 尚未提交，但其生产代码改动不影响快照内容） |

**再生成命令**（在仓库根目录执行；先按 `AGENTS.md`"常用命令"起栈：本机 MySQL / Redis + `mvn spring-boot:run`）：

```bash
curl -s http://localhost:8088/api/docs -o /tmp/openapi.json
python -c "import json;d=json.load(open('/tmp/openapi.json',encoding='utf-8'));open('openapi.json','w',encoding='utf-8',newline='\n').write(json.dumps(d,ensure_ascii=False,indent=2)+'\n')"
git diff --stat openapi.json   # 非空即契约有变更，须同步技术方案 §5
```

**再生成触发点**（已写入 `AGENTS.md`）：① 新增 / 修改端点或请求响应模型后；② 每个 Sprint 收尾；③ 部署形态确定后（`servers` 会变）；④ ~~N-1 / N-2 修复后~~ **已完成**（[CR-021](../../变更日志/变更台账.md#cr-021)）；⑤ 改了 `common/result/ResultCode` 的错误码、提示语或 `getHttpStatus()` 映射后——契约里的状态码与 description 全部由它派生，改它即改契约；⑥ ~~`SecurityConfig` 从 `permitAll()` 改为真正按路径授权后~~ **已完成**（[CR-031](../../变更日志/变更台账.md#cr-031)，2026-09-12 再生成：`security` 声明未变，`bearerAuth` 描述已写明"框架按端点注解裁决、未登录由过滤器链产出 401 / 4001"）；⑦ ~~Sprint 2 MVP 的 6 个论坛端点合入前~~ **已完成**（[CR-022](../../变更日志/变更台账.md#cr-022)，2026-09-12 再生成）；⑧ 增删端点的 `@PublicEndpoint` / `@SecurityRequirement` 声明、或改动 `common/web/CurrentUser` 鉴权入口语义后（[CR-028](../../变更日志/变更台账.md#cr-028) 起该纪律由 `ArchitectureGuardTest` 机器校验，但**快照的 `security` 声明不会自动跟随**，仍须重抓）；⑨ ~~N-4 闭环后复核 `bearerAuth` 描述~~ **已完成**（[CR-028](../../变更日志/变更台账.md#cr-028)，2026-09-12 再生成，diff 仅 1 行文案）。**新增**：⑩ 改动 `security/EndpointAuthorizationManager` 的裁决口径（如 fail-closed 范围、非 module 处理器放行规则）后——**运行时行为变了而契约描述可能落后**，须重抓并核对 `bearerAuth` 描述。**其中 ① 是高频触发点**：Sprint 2 起每次加端点都要重抓一次，`git diff` 非空即须同步本文 §3 / §4 与技术方案 §5。

## 3. 端点清单（快照实际覆盖的 21 个）

### 3.1 账号上下文（`module/account`，6 个，Sprint 1）

| 方法 | 路径 | tag | 摘要 | 请求体 | `data` 类型 | **快照声明的鉴权** | **运行时实际强制方式**（见 N-4） |
|------|------|-----|------|--------|------------|------------------|--------------------------------|
| POST | `/api/v1/auth/verify-student` | auth | 学籍核验（F-ACC-004），通过返回一次性票据 | `VerifyStudentCommand` | `VerifyStudentResult` | 无（公开） | 公开（另有 IP 限流） |
| POST | `/api/v1/auth/captcha` | auth | 发送邮箱验证码（60s 重发间隔，单账号日上限 10 条） | `CaptchaCommand` | 无（`data` 省略） | 无（公开） | 公开（另有 IP / 账号限流） |
| POST | `/api/v1/auth/register` | auth | 注册，需先通过学籍核验并携带票据 | `RegisterCommand` | `AuthResponse` | 无（公开） | 公开 |
| POST | `/api/v1/auth/login` | auth | 登录：邮箱 + 验证码 | `LoginCommand` | `AuthResponse` | 无（公开） | 公开 |
| GET | `/api/v1/users/me` | user | 我的主页（F-ACC-002 最小版） | — | `UserVo` | **`security: bearerAuth`** | **框架层（[CR-031](../../变更日志/变更台账.md#cr-031)）按 `@SecurityRequirement` 前置拦截 → 匿名 `401 / 4001`（进入业务代码前）**；业务层 `CurrentUser.requireId(authentication)` 同码兜底 |
| POST | `/api/v1/admin/roster/import?batch=` | admin-roster | 学籍名册 CSV 导入，body 为 CSV 文本 | `string`（CSV） | `RosterImportResult` | **`security: bearerAuth`** | 框架层前置拦截匿名 → `401 / 4001`；业务层 `CurrentUser.requireRole(authentication, ROLE_SUPERADMIN)` → 角色不足 `403 / 4002`（**403 支由单测 `AdminRosterControllerAuthTest` 覆盖，未在真机复现**，见 §6.2） |

### 3.2 论坛上下文（`module/forum`，12 个：Sprint 2 MVP 6 个 + [CR-043](../../变更日志/变更台账.md) 采纳 1 个 + [CR-048](../../变更日志/变更台账.md) 点赞收藏 4 个 + [CR-049](../../变更日志/变更台账.md) 站内搜索 1 个）

| 方法 | 路径 | tag | 摘要 | 请求体 | `data` 类型 | **快照声明的鉴权** | **运行时实际强制方式**（见 N-4） |
|------|------|-----|------|--------|------------|------------------|--------------------------------|
| GET | `/api/v1/boards` | board | 版块列表（6 个启用版块，按 `sort` 升序，**不分页**） | — | `List<BoardVo>` | 无（公开） | 公开 |
| GET | `/api/v1/posts?boardCode=&page=&size=` | post | 帖子列表：按 `created_at DESC`；带 `boardCode` 则版块内，不带则全站最新 | — | `PageVo<PostSummaryVo>` | 无（公开） | 公开 |
| GET | `/api/v1/posts/search?keyword=&boardCode=&days=&page=&size=` | post | **站内搜索（F-FORUM-008，[CR-049](../../变更日志/变更台账.md)）**：`keyword` **必填、2~50 字**，匹配标题（MySQL FULLTEXT + **ngram** 分词）与 `tags`（`LIKE` 兜底）；`boardCode` 可选筛版块（不存在 → `404 / 3001`）；`days` 可选且**只受理 7 / 30 / 90**；排序 **`MATCH` 相关度 DESC → `created_at DESC` → `id DESC`**；只搜 `PUBLISHED` 且未删除 | — | `PageVo<PostSummaryVo>` | 无（公开） | 公开（匿名可搜，[CR-049](../../变更日志/变更台账.md) 真机实测 200）。⚠️ **`keyword` 下限 2 字不是随意取值**：MySQL `ngram_token_size` 默认 **2**，单字关键词分不出词、全文索引恒不命中，故应用层直接拒为 `400 / 1001`（前端同口径先拦一次）；**改 `ngram_token_size` 须同步改这条下限** |
| POST | `/api/v1/posts` | post | 发帖（MVP 范围：`boardCode` + `title` + `contentMd`，不含标签） | `PublishPostCommand` | `PublishedPost` | **`security: bearerAuth`** | **框架层（[CR-031](../../变更日志/变更台账.md#cr-031)）前置拦截 → 匿名 `401 / 4001`（§4.5 ⑫ 实测）**；业务层 `CurrentUser.requireId(authentication)` 同码兜底 |
| GET | `/api/v1/posts/{id}` | post | 帖子详情，返回服务端渲染好的 `contentHtml` | — | `PostDetailVo` | 无（公开） | 公开 |
| GET | `/api/v1/posts/{postId}/replies?page=&size=` | reply | 楼层列表，**最佳答案置顶**（`is_accepted DESC, floor_no ASC`，[CR-043](../../变更日志/变更台账.md) 调整） | — | `PageVo<ReplyVo>` | 无（公开） | 公开 |
| POST | `/api/v1/posts/{postId}/replies` | reply | 回帖（MVP 范围：平铺楼层，不含引用回复） | `PublishReplyCommand` | `PublishedReply` | **`security: bearerAuth`** | **框架层（[CR-031](../../变更日志/变更台账.md#cr-031)）前置拦截 → 匿名 `401 / 4001`（§4.5 ⑫ 实测）**；业务层 `CurrentUser.requireId(authentication)` 同码兜底 |
| POST | `/api/v1/posts/{id}/accept` | post | 采纳最佳答案（仅提问者 + 问答帖 + 非自答；可更换，后写覆盖前写） | `AcceptReplyCommand` | 无（`data` 省略） | **`security: bearerAuth`** | **框架层（[CR-031](../../变更日志/变更台账.md#cr-031)）前置拦截 → 匿名 `401 / 4001`**；业务层 `CurrentUser.requireId(authentication)`；**提问者 / 问答帖 / 非自答校验在业务层**（403 `4002` / 400 `3002` / 400 `3003` / 404 `3001` 防探测，[CR-043](../../变更日志/变更台账.md) 真机实测） |
| POST | `/api/v1/posts/{id}/like` | post | 点赞 / 取消点赞帖子（toggle，再次 POST 即取消） | — | `InteractionResult`（`{active, count}`） | **`security: bearerAuth`** | **框架层前置拦截 → 匿名 `401 / 4001`**（[CR-048](../../变更日志/变更台账.md) 真机实测）；业务层 `CurrentUser.requireId(authentication)`；目标不存在 `404 / 3001` |
| POST | `/api/v1/posts/{id}/favorite` | post | 收藏 / 取消收藏帖子（toggle，同上） | — | `InteractionResult` | **`security: bearerAuth`** | 同上（[CR-048](../../变更日志/变更台账.md)） |
| POST | `/api/v1/posts/{postId}/replies/{replyId}/like` | reply | 点赞 / 取消点赞楼层（toggle，同上） | — | `InteractionResult` | **`security: bearerAuth`** | 同上（[CR-048](../../变更日志/变更台账.md)；楼层不存在同 `404 / 3001` 防探测） |
| GET | `/api/v1/favorites/mine?page=&size=` | favorite | 我的收藏（按收藏时间倒序分页，出参同帖子摘要） | — | `PageVo<PostSummaryVo>` | **`security: bearerAuth`** | **框架层前置拦截 → 匿名 `401 / 4001`**；业务层 `CurrentUser.requireId(authentication)`（[CR-048](../../变更日志/变更台账.md)） |

> ⚠️ **路径参数名不一致（真实存在）**：详情是 `/posts/{id}`（`PostController`），回复列表与回帖是 `/posts/{postId}/replies`（`ReplyController` 类级 `@RequestMapping`）。两者指同一个帖子 id，仅是**两个 Controller 各自的参数命名不同**，快照如实呈现。前端调用不受影响（URL 模板里只是占位符），但**读快照时不要以为是两个不同的资源**。属文档-代码细节差异，登记在 [增量设计](../../开发/Sprint2增量设计.md) §8（P-2）。
>
> ⚠️ **`accepted` 而非 `isAccepted`**：[增量设计](../../开发/Sprint2增量设计.md) §3.4 写的字段名是 `isAccepted`，**落码实际为 `accepted`**（Java record 组件名 `accepted` → Jackson 序列化即 `accepted`，无 `is` 前缀）。**以快照为准**，前端已按 `accepted` 对接；设计文档已同步修正（[增量设计](../../开发/Sprint2增量设计.md) §8 的 P-1）。

### 3.3 通知上下文（`module/notification`，3 个，[CR-050](../../变更日志/变更台账.md) / F-SOC-001）

| 方法 | 路径 | tag | 摘要 | 请求体 | `data` 类型 | **快照声明的鉴权** | **运行时实际强制方式**（见 N-4） |
|------|------|-----|------|--------|------------|------------------|--------------------------------|
| GET | `/api/v1/notifications?unread=&page=&size=` | notification | 我的通知列表，按 `created_at DESC` 分页；`unread` **三态**：缺省=不限、`true`=只未读、`false`=只已读（`page` 默认 1、`size` 默认 20） | — | `PageVo<NotificationVo>` | **`security: bearerAuth`** | **框架层前置拦截 → 匿名 `401 / 4001`**（§4.7 实测）；业务层 `CurrentUser.requireId(authentication)` 同码兜底。⚠️ **`unread` 非数字（如 `maybe`）在匿名时仍是 `401`**（框架先于 Bean Validation），登录后才 `400 / 1001` |
| GET | `/api/v1/notifications/unread-count` | notification | 未读通知数（顶栏铃铛角标；前端 30s 轮询 + 路由切换补读，**无实时推送**） | — | `UnreadCountResult{unreadCount}` | **`security: bearerAuth`** | 同上（框架层 + `CurrentUser.requireId`） |
| PUT | `/api/v1/notifications/read-all` | notification | 全部标记已读；`updated` 为**本次新标记**的条数，无未读时为 `0`（**幂等**，§4.7 实测连打两次为 `1` → `0`） | — | `MarkAllReadResult{updated}` | **`security: bearerAuth`** | 同上。**只有"全部已读"，没有"单条已读"**——点进某条通知不会把它自己标为已读（[技术方案](../技术方案.md) §5 已如实登记） |

> ⚠️ **`NotificationVo` 的三个可空字段在前端是"可选属性"，不是"值为 null 的属性"**：`postId` / `postTitle` / `floorNo` 由服务端**读时组装**（跨上下文调 `module/account` 取昵称、调 `module/forum` 取标题与楼层号），目标帖已删时 `postId` / `postTitle` 为 `null`、`floorNo` 仅楼层类通知有值。而后端全局 `spring.jackson.default-property-inclusion: non_null`（`application.yml`）会把 `null` 字段**从 JSON 里整个删掉**，所以前端拿到的是 `undefined`。⚠️ **由此产生过一个真实缺陷**：`NotificationsView.vue` 原判空写作 `n.postId === null`、`n.floorNo !== null`，两条都恒不成立，导致帖级通知渲染出「#undefined 楼」、点进去跳 `/post/undefined`——CR-050 真机走查发现并改为真值判断（`if (!n.postId)` / `v-if="n.floorNo"`），类型声明同步改为可选（`postId?: number`）。**这条口径对所有接口通用**：判空一律用真值判断，`=== null` 在带 `non_null` 的响应上不可靠；快照里 `NotificationVo` 仍把这三个字段列在 `properties` 下且**不带 `required`**，与"可能不出现"一致。
>
> **`type` 取值**：`reply` / `like` / `favorite` / `accept` 四类**已实现并会真的产出**；V1 迁移还留有一个 `quote`（引用回复）取值，但**引用回复从未建模，通知侧也不产出**（与 [技术方案](../技术方案.md) §5 的 `quote` 未产出说明一致）。`targetType` 为 `POST` / `REPLY`，`targetId` 指向楼层或帖子本身——**跳转用 `postId`，定位用 `?floor=<targetId>`（仅 `targetType=REPLY`）**。

**快照中各端点声明的非 200 响应**（description 为 `提示语（错误码）`，由 `ResultCode` 派生；下表为本次抓取的实际内容）：

| 端点 | 声明的状态码 | description（错误码） |
|------|------------|---------------------|
| `POST /auth/verify-student` | 400 / 429 / 500 | 400：`1001` + `2101`；429：`2103` |
| `POST /auth/captcha` | 400 / 429 / 500 | 400：`1001`；429：`2002` + `2003` |
| `POST /auth/register` | 400 / 409 / 500 | 400：`1001` + `2001` + `2102` + `2101`；409：`2004` |
| `POST /auth/login` | 400 / 403 / 500 | 400：`1001` + `2005`；403：`2006` |
| `GET /users/me` | 401 / 404 / 500 | 401：`4001`；404：`2007`（业务码，非路由级 `1004`） |
| `POST /admin/roster/import` | 400 / 401 / 403 / 500 | 400：`1001`；401：`4001`；403：`4002` |
| `GET /boards` | 500 | —（无业务错误码：无入参、无鉴权，只可能落 `9999`） |
| `GET /posts` | 400 / 404 / 500 | 400：`1001`；404：`3001`（`boardCode` 不存在） |
| `GET /posts/search` | 400 / 404 / 500 | 400：`1001`（`keyword` 缺失 / 不足 2 字 / 超 50 字、`days` 不在 {7,30,90}、`page`·`size` 非数字）；404：`3001`（`boardCode` 不存在）。⚠️ **`@ErrorCodes` 只声明了 `NOT_FOUND`**：`1001` / `9999` 由 `OpenApiErrorResponseCustomizer` 自动补，**再显式写一遍 `INVALID_PARAM` 会让 description 变成"参数错误（1001）；参数错误（1001）"**（本次实施中真实踩到并已修正，见 §7 v1.8） |
| `POST /posts` | 400 / 401 / 404 / 500 | 400：`1001`；401：`4001`；404：`3001`（`boardCode` 不存在） |
| `GET /posts/{id}` | 400 / 404 / 500 | 400：`1001`；404：`3001` |
| `GET /posts/{postId}/replies` | 400 / 404 / 500 | 400：`1001`；404：`3001`（帖子不存在） |
| `POST /posts/{postId}/replies` | 400 / 401 / 404 / 500 | 400：`1001`；401：`4001`；404：`3001`（帖子不存在） |
| `POST /posts/{id}/accept` | 400 / 401 / 403 / 404 / 500 | 400：`1001` + `3002`（非问答帖）+ `3003`（采纳自己的回复）；401：`4001`；403：`4002`（非提问者）；404：`3001`（帖子或回复不存在，防探测统一提示） |
| `POST /posts/{id}/like` | 400 / 401 / 404 / 500 | 400：`1001`；401：`4001`；404：`3001`（帖子不存在） |
| `POST /posts/{id}/favorite` | 400 / 401 / 404 / 500 | 400：`1001`；401：`4001`；404：`3001` |
| `POST /posts/{postId}/replies/{replyId}/like` | 400 / 401 / 404 / 500 | 400：`1001`；401：`4001`；404：`3001`（帖子或楼层不存在，防探测统一提示） |
| `GET /favorites/mine` | 400 / 401 / 500 | 400：`1001`；401：`4001` |
| `GET /notifications` | 400 / 401 / 500 | 400：`1001`（`unread` 非布尔、`page`·`size` 非数字）；401：`4001`。⚠️ **越界不报错、口径与 `GET /posts/search` 一致**（§4.7 实测）：`page=99` → 200 且 `list` 空、`total` 照常、`page` 原样回显；`page=0` 归一为 1；`size=999` 归一为 100 |
| `GET /notifications/unread-count` | 401 / 500 | 401：`4001`（无入参，`1001` 无用武之地；`9999` 自动补） |
| `PUT /notifications/read-all` | 401 / 500 | 401：`4001`（同上）。**没有"资源不存在"支**：全部已读对零条通知也是 200 / `updated: 0` |

> ✅ **N-4 的"漏写鉴权静默变公开"缺口已由 [CR-028](../../变更日志/变更台账.md#cr-028) 闭环，路径级拦截已由 [CR-031](../../变更日志/变更台账.md#cr-031) 补齐**。机制是**三层**：① **统一入口** `common/web/CurrentUser`（`requireId` / `requireRole`，未登录 `401 / 4001`、权限不足 `403 / 4002`）——上表第 8 列的强制逻辑全部收敛到它；② **显式声明锚点** `common/web/PublicEndpoint`（公开）与 `@SecurityRequirement`（受保护）二选一，由 `ArchitectureGuardTest` **机器校验**两条纪律：**每个** HTTP 映射方法必须且只能声明其一（漏写即测试失败）· **凡**声明 `@SecurityRequirement` 的方法必须真的调用 `CurrentUser`（只声明不调用同样失败）——因此"新增端点忘了加鉴权"不再是静默的；③ **框架层（[CR-031](../../变更日志/变更台账.md#cr-031) 起）**：`config/SecurityConfig` 已去掉 `anyRequest().permitAll()`，改由 `security/EndpointAuthorizationManager` **按同一批注解裁决**（`@PublicEndpoint` 放行、`@SecurityRequirement` 要求已登录、`com.campuslink.module.*` 端点两者都没标即 fail-closed），匿名请求在**进入业务代码前**即被过滤器链内的 `security/ApiErrorSecurityHandler` 拒为 `401 / 4001`。
>
> ⚠️ **两层边界须知情**：**其一**，框架**只区分"登录 / 未登录"**——角色与资源级授权（如"仅作者可删"、管理端的 `role in (ops, superadmin)`）仍归业务代码的 `CurrentUser.requireRole`；`403 / 4002` 出口（`AccessDeniedHandler`）为防御性接线，当前裁决口径下实际走入口点分支。**其二**，[CR-031](../../变更日志/变更台账.md#cr-031) 带来一处**行为变化**：受保护端点的"匿名 + 非法请求体"由 `400 / 1001`（[CR-028](../../变更日志/变更台账.md#cr-028) 前 `@Valid` 先于方法体执行）**前置为 `401 / 4001`**（框架先于 Bean Validation）——公开端点不受影响，仍走 `400 / 1001`。`bearerAuth` 的 description 已就地写明这些事实（§4.5 ⑦⑧ 与 ⑫ 实测）。
>
> **历史沿革（Sprint 2 的人工防线，已被机器校验 + 框架拦截取代）**：Sprint 2 交付时两个论坛受保护端点是**手工**写判空（`PostController` / `ReplyController`）+ 各 3 例「匿名 → 401」单测 + `ForumPublicReadTest` 3 例确认匿名可读——当时这三道防线是"人工加的、非流程强制"，A3-9 落地前下一个上下文的受保护端点仍会静默敞开。这一状态在 [CR-028](../../变更日志/变更台账.md#cr-028) 后被上述机器校验取代，路径级强制在 [CR-031](../../变更日志/变更台账.md#cr-031) 后到位。
>
> 另需注意：**路径级错误无法按端点声明**。`405`（`1002` 方法不允许）、`404`（`1004` 无此路由）、`415`（`1003` 媒体类型不支持）由 `GlobalExceptionHandler` 统一处理，springdoc 的 `OperationCustomizer` 只能改已注册的操作，看不到"不存在的路径"，故这三类响应在快照里**完全不出现**（详见 §6）。

## 4. 主链路请求 / 响应结构（A3-4 备选项②）

以下响应体的来源分五档，逐条标注：**【实测】**= 2026-09-11 对 `68d40ae` 运行实例抓取（**均为成功响应**）；**【CR-016 实测】**= 取自 [CR-016](../../变更日志/变更台账.md#cr-016) 当时记录的实测结果，本次未重跑；**【CR-021 实测】**= 2026-09-12 对**修复后**的运行实例抓取，集中在 §4.4 的错误响应；**【Sprint 2 实测】**= 2026-09-12 对含 `module/forum` 的工作区运行实例抓取，集中在 §4.5；**【推演】**= 按快照 schema 与 `common/result/ResultCode` 的 httpStatus 映射写出，未实测。一次性凭据（票据 / JWT）已脱敏。dev 固定验证码与测试名册见 `AGENTS.md`，本文不重复登记凭据。

### 4.1 学籍核验 → 验证码 → 注册（F-ACC-004 主链路）

**① `POST /api/v1/auth/verify-student`**

```jsonc
// 请求
{ "studentId": "249971346", "name": "张三" }

// 成功 HTTP 200 【实测】（dev bypass 名册）
{ "code": 0, "message": "成功", "data": { "ticket": "<一次性核验票据，32 位十六进制，已脱敏>" }, "traceId": "b208e820c94e" }

// 核验不通过 HTTP 400 —— 学号不存在 / 姓名不匹配 / 学号已注册**三者同码同提示**
// 本行为"学号不存在"（240000001）【实测】；"学号已注册"见 ③【CR-016 实测】；"姓名不匹配"未实测【推演】
{ "code": 2101, "message": "学籍信息校验未通过", "traceId": "1a2399953d26" }

// 格式违规 HTTP 400 【实测】—— 与上面区别开：格式错误不泄露名册信息，故明确指出字段
{ "code": 1001, "message": "studentId 学号必须为 9 位数字", "traceId": "451c16d92118" }
```

**② `POST /api/v1/auth/captcha`**

```jsonc
// 请求
{ "target": "someone@example.com" }

// 成功 HTTP 200 【实测】—— 注意 data 字段被省略（jackson default-property-inclusion: non_null），
// 而快照的 ApiResponseVoid 仍声明了 "data": {}，这是 schema 与实际响应的一处不一致
{ "code": 0, "message": "成功", "traceId": "d7dd5bf899fa" }
```

**③ `POST /api/v1/auth/register`**（票据一次性，用后即失效）

```jsonc
// 请求
{ "ticket": "<①返回的票据>", "email": "someone@example.com", "code": "<6 位验证码>", "nickname": "小明" }

// 成功 HTTP 200 【推演】（按 ApiResponseAuthResponse schema；该链路已在 Sprint 1 联调冒烟中用浏览器跑通，本次未抓取响应体）
{ "code": 0, "message": "成功", "data": { "token": "<JWT，已脱敏>", "expiresAtEpochSecond": 1799999999, "user": { "...": "UserVo，见 ④" } }, "traceId": "..." }

// 邮箱已注册 HTTP 409 【推演】（按 ResultCode.EMAIL_EXISTS = 2004 / httpStatus 409，唯一键冲突翻译见 CR-016）
{ "code": 2004, "message": "该邮箱已注册", "traceId": "..." }

// 学号已被占用 HTTP 400 【CR-016 实测】—— 与"学号不存在""姓名不匹配"同码同提示，防名册枚举
{ "code": 2101, "message": "学籍信息校验未通过", "traceId": "..." }
```

### 4.2 登录 → 我的主页

**④ `POST /api/v1/auth/login`**

```jsonc
// 请求
{ "email": "someone@example.com", "code": "<6 位验证码>" }

// 成功 HTTP 200 【推演】（按 ApiResponseAuthResponse / UserVo schema；登录链路已在 Sprint 1 联调冒烟中跑通，本次未抓取响应体）
{ "code": 0, "message": "成功", "data": { "token": "<JWT，7 天滑动续期，已脱敏>", "expiresAtEpochSecond": 1799999999,
  "user": { "id": 1, "nickname": "小明", "avatarUrl": null, "school": "重庆工程学院", "major": "计算机科学与技术",
            "grade": "2024", "bio": null, "role": "USER", "verified": true, "createdAt": "2026-09-11T12:00:00Z" } }, "traceId": "..." }

// 未登录访问受保护端点 GET /api/v1/users/me（无 Authorization 头）HTTP 401 【实测】
{ "code": 4001, "message": "未登录", "traceId": "870d69df657b" }
```

### 4.3 快照中含有的字段级约束（可直接用于前端校验与测试用例）

| 字段 | 快照中的约束 | 来源 |
|------|-------------|------|
| `studentId` | `pattern: \d{9}`、`minLength: 1`、必填 | Jakarta Validation（[CR-013](../../变更日志/变更台账.md#cr-013)） |
| `name` | `pattern: ^(?:[\p{IsHan}]{2,16}(?:[·・][\p{IsHan}]{1,16})?\|[A-Za-z][A-Za-z .'\-]{1,63})$`、必填 | 同上（中文名 2~16 汉字可含 `·`，或外文名字母起头） |
| `email` / `target` | `format: email`、`minLength: 1`、必填 | 同上 |
| `code` | `pattern: \d{6}`、必填 | 同上 |
| `nickname` | `minLength: 2`、`maxLength: 32`、必填 | 同上 |
| `ticket` | `minLength: 1`、必填 | 同上 |

### 4.4 错误响应实例（CR-021 修复后实测）

错误体统一为 `common/result/ApiError`，形状固定为 **`{code, message, traceId}`**——**没有 `data` 字段**（与成功响应的 `ApiResponse` 外壳不同，这是有意的：错误不携带业务数据）。以下 7 条均为 **2026-09-12【CR-021 实测】**，`traceId` 为真实抓取值（非凭据，无脱敏必要）。

```jsonc
// ① 请求体不是合法 JSON：POST /auth/verify-student，body = {"studentId":}
//    HTTP 400 —— 改前为 HTTP 500 / 9999「系统繁忙」并打全栈 error 日志（N-3）
{ "code": 1001, "message": "请求体缺失或不是合法 JSON", "traceId": "047d204b6b28" }

// ② 完全不带请求体：同一端点，无 body
//    HTTP 400 —— 与 ① 同码同提示（改前也是 500 / 9999）
{ "code": 1001, "message": "请求体缺失或不是合法 JSON", "traceId": "e6bd4152ac7e" }

// ③ Content-Type: text/plain（body 内容合法）
//    HTTP 415 —— 改前为 500 / 9999
{ "code": 1003, "message": "不支持的请求内容类型", "traceId": "207fd3bd371f" }

// ④ 用 GET 打 POST-only 端点
//    HTTP 405，且响应头含 Allow: POST（RFC 9110 要求；改前为 500 / 9999）
{ "code": 1002, "message": "请求方法不支持", "traceId": "a2b802ee2d31" }

// ⑤ 请求不存在的路径 /api/v1/nonexistent
//    HTTP 404 —— 改前为 500 / 9999
{ "code": 1004, "message": "接口不存在", "traceId": "3ec34cc27532" }

// ⑥ 匿名访问受保护端点 GET /users/me（无 Authorization 头）
//    HTTP 401 —— 改前改后一致
{ "code": 4001, "message": "未登录", "traceId": "522f54766a72" }

// ⑦ 匿名调 POST /admin/roster/import
//    HTTP 401 —— 改前为 403 / 4002，本次拆开「未登录」与「无权限」两态（对齐技术方案 §5）
{ "code": 4001, "message": "未登录", "traceId": "134701226102" }
```

**与快照的对应关系**：只有 **①②⑥⑦** 对应的状态码在快照里有声明（①② → `400`，⑥⑦ → `401`）；**③④⑤ 在快照里一律查不到**——`415` 是因为 springdoc 按 Controller 的 `consumes` 生成请求体媒体类型、不生成 415 分支；`405` / `404` 是因为 OpenAPI 无法为"不允许的方法"或"不存在的路径"挂响应（见 §6 末两行）。这三类前端若要处理，须读本文与 `GlobalExceptionHandler`，**不能只读快照**。

**日志档位**（`GlobalExceptionHandler` 的设计，非快照内容）：①~⑤ 各记 **1 行 WARN**，不打栈；⑥⑦ 这类业务鉴权失败**不记日志**；只有落到兜底 `Exception` 分支的未知异常才 `log.error` 打全栈。改前 ①~⑤ 全部走兜底分支，每个请求一条带全栈的 ERROR——**这就是 N-3 污染 5xx 监控的机制**。

### 4.5 论坛主链路（Sprint 2 MVP 的 6 个新端点，2026-09-12【Sprint 2 实测】）

链路：**版块列表 → 帖子列表 → 发帖 → 详情 → 楼层列表 → 回帖**。以下 `traceId` 均为真实抓取值（非凭据）。发帖 / 回帖的成功响应由 `admin@campuslink.local` 的 JWT 调用取得（[CR-013](../../变更日志/变更台账.md#cr-013) 的 dev-only 管理员账号）。

**① `GET /api/v1/boards`（公开）**

```jsonc
// 成功 HTTP 200 —— 6 个版块已由 V2__seed_boards.sql 种入，按 sort 升序；此处略去中间 4 个
{ "code": 0, "message": "成功", "data": [
  { "code": "qna", "name": "技术问答", "description": "提问、报错排查、环境配置、技术选型", "type": "QUESTION", "sort": 1 },
  { "code": "resources", "name": "学习资源", "description": "教程、笔记、工具、资源分享", "type": "DISCUSSION", "sort": 2 }
  /* …interview / contest / course / chat，sort 3~6… */ ],
  "traceId": "3ed853d4185d" }
```

**② `GET /api/v1/posts?page=1&size=2`（公开，全站最新）**

```jsonc
// 成功 HTTP 200 —— 分页外壳为 PageVo{list,total,page,size}，注意不是 pageNum/pageSize
{ "code": 0, "message": "成功", "data": { "list": [
    { "id": 3, "boardCode": "chat", "boardName": "闲聊灌水", "title": "M6 契约取证：发帖成功响应体",
      "authorNickname": "管理员", "replyCount": 0, "likeCount": 0, "summary": "取证用正文",
      "createdAt": "2026-09-12T15:10:21Z" },
    { "id": 2, "boardCode": "resources", "boardName": "学习资源", "title": "M5 前端联调：浏览器实测发帖",
      "authorNickname": "管理员", "replyCount": 2, "likeCount": 0, "summary": "…（content_md 去 Markdown 记号后前 120 字）",
      "createdAt": "2026-09-12T15:03:23Z" } ],
  "total": 3, "page": 1, "size": 2 }, "traceId": "…" }
```

**③ `POST /api/v1/posts`（需登录）**

```jsonc
// 请求（body 含中文，故用 --data-binary 从 UTF-8 文件发送）
{ "boardCode": "chat", "title": "M6 契约取证：发帖成功响应体", "contentMd": "取证用正文" }

// 成功 HTTP 200 —— 只回 id，前端据 ②跳 /post/{id}
{ "code": 0, "message": "成功", "data": { "id": 3 }, "traceId": "…" }
```

**④ `GET /api/v1/posts/2`（公开）→ ⑤ `GET /api/v1/posts/2/replies?page=1&size=2`（公开）→ ⑥ `POST /api/v1/posts/2/replies`（需登录）**

```jsonc
// ④ 详情：contentHtml 是发布时服务端渲染好的 HTML（ADR-005），此处只截前 60 字
{ "code": 0, "message": "成功", "data": { "id": 2, "boardCode": "resources", "boardName": "学习资源",
    "title": "M5 前端联调：浏览器实测发帖", "contentHtml": "<h2>二级标题</h2>\n<p>这是<strong>浏览器实测</strong>发帖…",
    "authorNickname": "管理员", "replyCount": 2, "likeCount": 0, "accepted": false,
    "createdAt": "2026-09-12T15:03:23Z" }, "traceId": "a73815618bef" }

// ⑤ 楼层：floor_no 升序；内容同样已渲染为 HTML
{ "code": 0, "message": "成功", "data": { "list": [
    { "id": 3, "floorNo": 1, "contentHtml": "<p>浏览器实测回帖：<strong>楼层</strong>应为 1。</p>",
      "authorNickname": "管理员", "createdAt": "2026-09-12T15:03:54Z" },
    { "id": 4, "floorNo": 2, "contentHtml": "<p>M6 契约取证回帖</p>",
      "authorNickname": "管理员", "createdAt": "2026-09-12T15:10:21Z" } ],
  "total": 2, "page": 1, "size": 2 }, "traceId": "9376e254630d" }

// ⑥ 回帖请求 → 成功响应：id + floorNo（floor_no 由 posts.reply_count 行锁派生，见增量设计 §4.1）
{ "contentMd": "M6 契约取证回帖" }
{ "code": 0, "message": "成功", "data": { "id": 4, "floorNo": 2 }, "traceId": "…" }
```

**⑦⑧ 两个受保护端点的匿名调用（**body 合法**、无 Authorization 头；此组为 Sprint 2 实测，[CR-031](../../变更日志/变更台账.md#cr-031) 后响应不变但拦截点上移——由框架层在进入业务代码前产出，见 ⑫）**

```jsonc
// ⑦ 匿名 POST /posts（**body 合法**、无 Authorization 头）→ HTTP 401
{ "code": 4001, "message": "未登录", "traceId": "a4d1c6ea395e" }

// ⑧ 匿名 POST /posts/2/replies（**body 合法**、无 Authorization 头）→ HTTP 401
{ "code": 4001, "message": "未登录", "traceId": "3b437a230c05" }
```

**⑨⑩⑪ 论坛侧的参数与资源错误**

```jsonc
// ⑨ 发帖 title 101 字（DDL 上限 100）→ HTTP 400，字段级提示（格式错误不属名册信息，可明确指出字段）
{ "code": 1001, "message": "title 标题最长 100 字", "traceId": "757a20cb9caa" }

// ⑩ boardCode 不存在 → HTTP 404 / 3001
{ "code": 3001, "message": "资源不存在", "traceId": "4b05ac33521e" }

// ⑪ 帖子不存在（或已删、非 PUBLISHED）→ HTTP 404 / 3001 —— 与 ⑩ 同码同提示，不区分
{ "code": 3001, "message": "资源不存在", "traceId": "429f282dc14f" }
```

**⑫ [CR-031](../../变更日志/变更台账.md#cr-031) 路径级鉴权落地后的复测（2026-09-12，框架拦截生效）**

```jsonc
// 匿名 POST /posts（**body 非法**，`{}`）→ HTTP 401 / 4001 —— 框架先于 Bean Validation
// 对照：CR-031 前该组合返回 400 / 1001「boardCode 版块不能为空」（即下方 🔍 记录的行为顺序已改变）
{ "code": 4001, "message": "未登录", "traceId": "4d9c7822b3bc" }

// 匿名 POST /posts（**body 合法**）→ HTTP 401 / 4001（与上同码同体，60 字节）
{ "code": 4001, "message": "未登录", "traceId": "a80d69fe861b" }

// 伪造 token（Authorization: Bearer garbage）访问 GET /users/me → HTTP 401 / 4001
{ "code": 4001, "message": "未登录", "traceId": "67b4a8f88f2b" }

// 公开端点（GET /boards、GET /posts、GET /posts/{id}、GET /posts/{postId}/replies）→ 200 不受影响
// 匿名 DELETE /api/v1/posts → HTTP 405 / 1002（带 Allow 头；方法不支持先于鉴权，交回 MVC）
// 请求不存在的路径 → HTTP 404 / 1004；actuator/health 与 /api/docs → 200
```

> 🔍 **一个已经改变的行为顺序（[CR-031](../../变更日志/变更台账.md#cr-031) 前后对比）**：**CR-031 前，受保护端点是"参数校验先于登录态校验"**——鉴权写在方法体内部，`@Valid` 在校验阶段先执行，匿名 + 非法 body 返回 `400 / 1001`、匿名 + 合法 body 才返回 `401 / 4001`（Sprint 2 实测，即原 ⑦⑧ 与本节旧注记）。**CR-031 起，框架层在进入业务代码前按端点注解裁决**，受保护端点的**任意**匿名请求（无论 body 是否合法）一律 `401 / 4001`（⑫ 实测）；**公开端点不受影响**（仍走 `400 / 1001`）。前端判断登录态仍须看 `401` 或本地 token，不得把"400"当作已登录的证据。**注**：`GET /users/me` 无入参，两阶段行为一致。

### 4.6 站内搜索（[CR-049](../../变更日志/变更台账.md) / F-FORUM-008，2026-09-13 真机实测）

```jsonc
// ① PRD 验收主用例：**匿名** GET /posts/search?keyword=Redis → HTTP 200
{ "code": 0, "message": "成功", "data": { "list": [
    { "id": 13, "boardCode": "qna", "boardName": "技术问答",
      "title": "Redis 缓存穿透怎么防？CR-049 冒烟帖", "authorNickname": "管理员",
      "replyCount": 0, "likeCount": 0, "summary": "布隆过滤器 + 空值缓存，SETNX 限流。",
      "createdAt": "2026-09-13T22:40:53Z", "accepted": false } ],
  "total": 1, "page": 1, "size": 20 }, "traceId": "8c0fe8ec0d17" }

// ② 中文 2 字关键词（ngram 分词生效）keyword=冒烟 → 200，total=6，ids=[14,13,11,10,9,1]
// ③ keyword=R（单字）/ 全空白 / 完全缺失 / 51 字 → 一律 HTTP 400 / 1001「参数错误」
// ④ keyword=50 字（上限）→ 200（total=0，受理不报错）
// ⑤ days=15、days=0（不在 {7,30,90}）→ 400 / 1001；days=7|30|90 → 200
// ⑥ boardCode=nope（不存在）→ HTTP 404 / 3001「资源不存在」；qna → total=4；chat → total=2
// ⑦ page=abc（类型不匹配）→ 400 / 1001；page=0 → 归一为 1；size=999 → 归一为 100
// ⑧ page=99（越界）→ 200 且 list 为空、total 仍为 6（静默归一，与 ③⑤ 的"校验失败即拒"刻意不同口径）
// ⑨ 无命中关键词 → 200 / total=0（**不是** 404：无结果是正常业务态）
```

> 🔍 **相关度优先于时间的取证**（PRD 要求"按相关度 + 时间排序"，只看 API 输出无法证明两级排序真的分级）：关键词 `CR-043` 返回 `ids=[11,10,9,14,13,8]`，而 DB 侧 `MATCH(title) AGAINST('CR-043')` 的得分为 **11 / 10 / 9 = 0.6905**、**14 / 13 = 0.2850**、**8 = 0.1128**。其中 **14 / 13 的 `created_at`（09-13 22:40）明显晚于 11 / 10 / 9（09-13 13:30）却排在其后**——这就证明 `relevance DESC` 确实在 `created_at DESC` 之前生效，而非退化为纯时间序。同关键词下得分相同的 6 条（如 `冒烟` 全部 0.1128）则由 `created_at DESC, id DESC` 决定次序。
>
> ⚠️ **两处取证用了临时置数，且都已还原**（本节如实记录，避免读者误以为库里本来就有这些数据）：
>
> | 取证目的 | 临时置数 | 实测结果 | 还原确认 |
> |---------|---------|---------|---------|
> | **时间窗口过滤真的会收窄**（库内全部帖子都落在 7 天内，`days` 三个取值结果恒同，无法证明过滤生效） | `UPDATE posts SET created_at = DATE_SUB(NOW(), INTERVAL 40 DAY) WHERE id = 1` | `keyword=冒烟`：无 `days` → total=6；`days=7` → **total=5（排除 1）**；`days=30` → **total=5**；`days=90` → **total=6（含 1）** | 已还原为 `2026-09-12 14:53:29`（原值逐字回写），复核 `MIN/MAX(created_at)` = `2026-09-12 14:53:29` / `2026-09-13 22:40:53`，且 `days=7` 重新返回 total=6 |
> | **`tags LIKE` 兜底通道可用** | `UPDATE posts SET tags = '面试,内推' WHERE id = 12`（标题为「你好」，不含该词） | `keyword=面试` → 200 / total=1 / 命中 12，且该行 `MATCH` 得分为 **0**（纯 LIKE 通道命中，排在有得分者之后）；叠加 `boardCode=qna` 仍命中、`boardCode=chat` → total=0 | 已还原为 `NULL`，复核 `SELECT COUNT(*) FROM posts WHERE tags IS NOT NULL` = **0** |
>
> ⚠️ **`tags` 兜底通道当前没有真实数据来源**（这是本次实施如实点明的取舍，不是遗漏）：发帖链路**从未建模标签**——`PublishPostCommand` 无 `tags` 字段、全库 `posts.tags` 恒为 `NULL`、`post_tags` 关联表未启用。因此"按标签搜索"目前**只有人工置数才能验**，用户从 UI 无法产生任何标签数据。方案已把它列入"不做清单"，标签建模（发帖标签输入 + 关联表）留待后续 CR。
>
> ✅ **回归面**（同批实测，均无变化）：`GET /posts` → total=14 / `created_at DESC`；`GET /boards` → 6 项；`GET /posts/13` → 详情 14 字段齐（含 [CR-048](../../变更日志/变更台账.md) 的 `likedByMe` / `favoritedByMe`）；`GET /posts/13/replies` → 200；`GET /favorites/mine` 匿名 → `401 / 4001`。前端 `/search` 已浏览器实测（宽屏顶栏搜索框 `readOnly` 已解除、`maxlength=50`、回车跳转 `/search?keyword=`；窄屏图标跳 `/search`；版块 / 时间下拉变更即重搜并把条件写回 URL；无结果态出「去提问」按钮；1 字关键词在前端先拦为「关键词至少 2 个字」不发请求）。⚠️ **目视走查未完成**：本轮内嵌浏览器视口为 `0×0`，截图与指针事件通道均不可用，故**宽屏 / 窄屏的视觉呈现只做了 DOM 结构校验、未做像素级目视**，该残留归入 [W-03](../../流程偏离记录.md) 补偿③ 的逐页走查。

### 4.7 通知中心（[CR-050](../../变更日志/变更台账.md) / F-SOC-001，2026-09-14 真机实测）

链路：**A 触发 → B 收通知 → B 查未读数 → B 列表分页与 `unread` 筛选 → B 全部已读**。取证环境为 `APP_ROSTER_BYPASS=false` 的真实名册链路：以 SUPERADMIN 导入名册批次 `cr050-smoke` 后注册 **小A**（`userId=25`，触发者）与 **小B**（`userId=24`，接收者），小B 是目标帖 `id=15` 的作者。以下响应体与 `traceId` 均为真实抓取值。

```jsonc
// ① 触发：A 在 B 的帖子下回帖 → HTTP 200（论坛侧顺带把通知写进 notifications，同事务）
{ "code": 0, "message": "成功", "data": { "id": 19, "floorNo": 4 }, "traceId": "ce43a647d18c" }

// ② 触发：A 收藏 B 的帖子 —— 但 A 此前已收藏，本次 POST 是 **toggle 取消**（active:false）
//    因此**不产生** favorite 通知：这正是"取消交互不发通知"的复测证据
{ "code": 0, "message": "成功", "data": { "active": false, "count": 0 }, "traceId": "a8a5256db45d" }

// ③ B GET /notifications/unread-count → 只有 ① 这一条未读
{ "code": 0, "message": "成功", "data": { "unreadCount": 1 }, "traceId": "174724328b74" }

// ④ B GET /notifications?page=1&size=2 → 读时组装出昵称 / 标题 / 楼层号
{ "code": 0, "message": "成功", "data": { "list": [
    { "id": 9, "type": "reply", "actorId": 25, "actorNickname": "小A", "targetType": "REPLY", "targetId": 19,
      "postId": 15, "postTitle": "CR050 通知冒烟帖", "floorNo": 4, "read": false,
      "createdAt": "2026-09-14T23:06:34Z" },            // ← ⚠️ 时区缺陷见下方注记
    { "id": 8, "type": "like", "actorId": 25, "actorNickname": "小A", "targetType": "POST", "targetId": 15,
      "postId": 15, "postTitle": "CR050 通知冒烟帖", "read": true,
      "createdAt": "2026-09-14T22:47:34Z" } ],
  "total": 8, "page": 1, "size": 2 }, "traceId": "14c9ec9413a8" }
// 注意第 2 条是**帖子级**点赞通知，JSON 里**根本没有 floorNo 这个键**（不是 "floorNo": null）——见下方 non_null 注记

// ⑤ B GET /notifications?unread=true&page=1&size=2 → 只回未读
{ "code": 0, "message": "成功", "data": { "list": [ { "id": 9, "type": "reply", "read": false, "…": "…" } ],
  "total": 1, "page": 1, "size": 2 }, "traceId": "d51d1f91eeb1" }

// ⑥ B GET /notifications?unread=false&page=1&size=1 → 只回已读，与 ⑤ 互补（1 + 7 = 8 = total）
{ "code": 0, "message": "成功", "data": { "list": [ { "id": 8, "type": "like", "read": true, "…": "…" } ],
  "total": 7, "page": 1, "size": 1 }, "traceId": "8ddbd46ee96e" }

// ⑦ B PUT /notifications/read-all → updated 是本次新标记条数
{ "code": 0, "message": "成功", "data": { "updated": 1 }, "traceId": "173bf2efd1a0" }
// ⑧ 再打一次（幂等，不报错、不重复计数）
{ "code": 0, "message": "成功", "data": { "updated": 0 }, "traceId": "68a8ffe50978" }
// ⑨ 未读数归零
{ "code": 0, "message": "成功", "data": { "unreadCount": 0 }, "traceId": "f74e5990399f" }

// ⑩⑪⑫ 三个端点匿名访问 → 一律 HTTP 401 / 4001（框架层在进入业务代码前拦截）
{ "code": 4001, "message": "未登录", "traceId": "888c2123f3b6" }   // GET /notifications
{ "code": 4001, "message": "未登录", "traceId": "fb3d27388803" }   // GET /notifications/unread-count
{ "code": 4001, "message": "未登录", "traceId": "d6c3a4083a31" }   // PUT /notifications/read-all

// ⑬ 受保护端点 + 非法入参 + 匿名：unread=maybe → 仍是 401（框架先于参数绑定，CR-031 的既有口径）
{ "code": 4001, "message": "未登录", "traceId": "ced22da1b269" }
// ⑭ 同一请求带合法 token → 才落 400 / 1001
{ "code": 1001, "message": "参数错误", "traceId": "d92d2f9586b0" }
```

> 🔍 **两处"只有真机才暴露"的缺陷，本次已修**（单测全绿期间它们一直存在）：
>
> | 缺陷 | 表现 | 根因 | 处置 |
> |------|------|------|------|
> | **`unread` 筛选语义反了** | `unread=true` 返回**已读**、`unread=false` 返回**未读**（实测：3 条未读时 `unread=true` 的 `total=0`；全部已读后反而 `total=3`） | 入参叫 `unread`（"只看未读"）、列叫 `is_read`，**语义相反**，适配器却直接 `.eq(is_read, unread)` | `NotificationRepositoryImpl.findPage` 改为 `!unread`（`unread == null` 时不加条件）；**新增适配层测试** `NotificationRepositoryImplFilterTest` 3 例——从捕获的 `QueryWrapper` 里正则取出 `is_read = #{ew.paramNameValuePairs.MPGENVALx}` 的绑定值断言（`true→false` / `false→true` / `null→该列根本不出现在 SQL 段`）。⚠️ **为何必须新增**：应用层是 Mockito 打桩的 service 测试，**结构上看不见适配器的列语义**，这类缺陷只能靠"捕 SQL 参数"的适配层测试或真机兜住 |
> | **前端把可空字段当 `null` 判** | 帖级通知渲染出「**#undefined 楼**」、点进去跳 `/post/undefined` | 全局 `default-property-inclusion: non_null` 把 `null` 字段**从 JSON 删掉**，前端拿到 `undefined`，而代码写的是 `n.postId === null` / `n.floorNo !== null` | 类型改为可选属性（`postId?: number` / `floorNo?: number`），判空改真值判断（`if (!n.postId)` / `v-if="n.floorNo"`）。**这条对所有接口通用**，已在 §3.3 写成口径 |
>
> ⚠️ **一处本次发现、未修的缺陷（属既有链路，非 CR-050 引入）**：上面 ④ 的 `createdAt` 是 **`2026-09-14T23:06:34Z`，而真实 UTC 时刻约 `15:06Z`**——写侧由 MySQL 填（V1 迁移各表均 `created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP`，Java 侧不传值），存进去的是**服务器本地墙钟 +08:00**；读侧 JDBC URL 带 **`serverTimezone=UTC`**（`application.yml` 第 17 行），驱动把同一串数字**当 UTC** 读成 `Instant`，于是序列化出的 `Z` 值整体**偏 +8 小时**。表现：帖子详情显示"**发布于 2026-09-15 06:38**"（未来时间）、35 分钟前的内容显示"刚刚"、约 24 小时前的显示"16 小时前"。**Sprint 2 起影响全部时间戳**，通知中心只是把它照出来了。本次**不改**（根因在数据源与时区口径、跨所有读写链路，改法要先定"库里到底存 UTC 还是存本地"这一决策并可能重写历史行），已登记为问题台账条目与 [CR-051](../../变更日志/变更台账.md) 候选。
>
> ✅ **2026-09-15 加注：该缺陷已由 [CR-051](../../变更日志/变更台账.md) 闭环（台账外问题 L-6 置为已处置）**——决策为**库里存 UTC**：数据源 URL 的 `serverTimezone=UTC` 换成 `connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true`（两参数必须成对，只改前者不触会话时区），新增 `config/DataSourceTimezoneTest` 守护；开发库存量 11 表 / 19 列先 `mysqldump` 备份再一次性 `−8 HOUR` 校正；**未新增 `V*.sql` 迁移、未改 `DATETIME` 列类型、未在前端做 −8h 补偿**。实测：新写入行与 `UTC_TIMESTAMP()` 差 0~1 秒，`formatRelativeTime` 由恒"刚刚"恢复正常递进（"13 分钟前"），**契约快照重抓 `git diff` 为空**（本 CR 不动接口形状）。⚠️ **本节 ④ 的 `2026-09-14T23:06:34Z` 是修复前的真实取证值，保留不改**（它是那条缺陷的证据），当前口径见 §1「时间字段口径」。
>
> ✅ **回归面**（同批实测，均无变化）：`GET /posts`、`GET /boards`、`GET /posts/15`、`GET /posts/15/replies` 形状不变（`GET /boards` 仅 `operationId` 由 `list_2` → `list_3`，见 §2）；点赞 / 收藏 / 采纳三条触发链在 CR-050 挂点后仍各自 200；**自触发不发**（自己赞自己的帖，`notifications` 行数不变）、**toggle 取消不发**（② 即为证据）。前端 `/notifications` 已浏览器实测（顶栏铃铛角标读数 → 列表 → 「全部 / 未读」切换 → 点楼层类通知跳 `/post/15?floor=<replyId>` 并高亮 → 「全部已读」后角标消失），⚠️ **目视走查仍受限于 `0×0` 视口**：本轮用 DOM 结构 + `getComputedStyle` + `scrollY` 取证（高亮类 `bg-primary-soft` 命中、滚动到 `scrollY=834`），**像素级视觉未验**，归 [W-03](../../流程偏离记录.md) 补偿③。

## 5. 与技术方案 §5 接口表的逐条差异

| 类别 | 端点 | 处置 |
|------|------|------|
| **§5 已列且已实现（账号 5 行）** | `POST /auth/verify-student`、`POST /auth/captcha`、`POST /auth/register`、`POST /auth/login`、`POST /admin/roster/import` | 无需动作 |
| **§5 未列、已补入并实现** | `GET /api/v1/users/me`（[CR-019](../../变更日志/变更台账.md#cr-019) 补入）、`GET /api/v1/boards`、`GET /api/v1/posts/{postId}/replies`（[CR-022](../../变更日志/变更台账.md#cr-022) 的 MVP 范围新增，**§5 原表没有这两行**） | ✅ 技术方案 §5 已同步补入这两行 |
| **本次由"计划项"转为"已实现"（MVP 子集）** | `GET /posts`（**仅全站最新 / 版块内时间序，无 `sort=hot`**）、`POST /posts`（**无标签**）、`GET /posts/{id}`（**无 DELETE**）、`POST /posts/{id}/replies`（**无 `quotedReplyId`**） | 技术方案 §5 已标注"已实现（MVP 子集）"与未实现部分 |
| **§5 已列但仍未实现**（后续 Sprint） | `GET /boards/{code}/posts`（**已由 `GET /posts?boardCode=` 替代**，不是缺口）、`sort=hot` 与 `hot_score`（ADR-006 定时任务）、`DELETE /posts/{id}`、`PUT /posts/{id}/accepted-reply`、**toggle 版点赞 / 收藏已由 [CR-048](../../变更日志/变更台账.md) 实现并取代**（原列的 `POST\|DELETE` 双方法语义收敛为单一 POST toggle）、`GET\|PUT /notifications` **通知已由 [CR-050](../../变更日志/变更台账.md) 实现，落点为 `GET /notifications?unread=&page=&size=` + `GET /notifications/unread-count` + `PUT /notifications/read-all`**（原计划的 `PUT /notifications` 由 `read-all` **取代**、不留作缺口；**单条已读未实现**，`quote` 类通知不产出）、~~`GET /search`~~ **站内搜索已由 [CR-049](../../变更日志/变更台账.md) 实现，落点为 `GET /posts/search`**（**仅标题 + 标签**；正文全文检索 = F-FORUM-009，仍属 P1 未实现）、`POST /reports`、`/admin/...` 处置台与工单 | 契约尚未设计；各 Sprint 开工前补 §5 并在实现后重生成快照（对应 [sprint-2.md](../../开发/Sprint2计划.md) §1 的 OUT 清单） |
| **§5 有约定但快照无对应表达** | 时间 ISO 8601 UTC、错误码**分段规则**（分页口径已随本次同步为 `page/size`，不再是差异） | **错误码自 [CR-021](../../变更日志/变更台账.md#cr-021) 起部分可见**：具体码与提示语出现在各响应的 description 里（§3 的非 200 表），但"1xxx 通用 / 2xxx 账号 / 21xx 学籍 …"这一**分段规则**OpenAPI 表达不了，仍须读技术方案 §5。⚠️ **时间这一项现在不只是"快照表达不了"，而是"实现与约定相反"**：字段类型确为 `string` + `format: date-time`、后缀 `Z`，**但值带 +8 小时偏移**（JDBC `serverTimezone=UTC` vs MySQL 写本地墙钟），故**读快照不得把 `Z` 当作真 UTC**，实测取证见 §4.7 的时区注记。**✅ 2026-09-15 加注：该"实现与约定相反"的差异已由 [CR-051](../../变更日志/变更台账.md) 消除**——会话时区被数据源强制为 `+00:00` 后，`Z` 即为真 UTC，本行"时间"一项退回纯"OpenAPI 表达不了分段规则"式的表达极限问题，现行口径改见 §1「时间字段口径」；上文对 L-6 时期的叙述保留为历史记录，不改写。 |

## 6. 快照的已知缺口与表达极限（读快照前必须知道）

### 6.1 已由 [CR-021](../../变更日志/变更台账.md#cr-021) 处置的三项缺口（2026-09-12）

| # | 原缺口（[CR-019](../../变更日志/变更台账.md#cr-019) 归档时发现） | 修复方式 | 复测结论 |
|---|--------------------------------------------|---------|---------|
| **N-1** | **错误响应完全缺失**：6 个端点只声明 `200`。springdoc 仅按 Controller 返回类型生成，而实际错误经 `GlobalExceptionHandler` 按 `ResultCode.getHttpStatus()` 返回真实状态码，契约里完全看不到 | 新增项目自有注解 `common/result/ErrorCodes`，端点用它声明会抛的业务码；新增 `config/OpenApiErrorResponseCustomizer`（同时实现 `OperationCustomizer` 与 `OpenApiCustomizer`）统一生成 4xx / 5xx 响应，并把错误体 schema `ApiError` 注册进 `components`。**状态码与提示语一律取自 `ResultCode`，定制器不手写任何字面量** | ✅ 快照现有 `ApiError` schema，6 端点全部声明非 200 响应（逐条见 §3 第二张表）。**未采用**原计划的 swagger `@ApiResponses`——那要在每个端点重复书写 `ResultCode` 已有的状态码与提示语，同一事实存两份，改错误码必漂移 |
| **N-2** | **未声明鉴权**：`OpenApiConfig` 只设 `info`，无 `securitySchemes` → 快照无 bearerAuth、Swagger UI 无 Authorize 按钮、需登录的 `GET /users/me` 看起来是公开接口 | `OpenApiConfig` 补 `components.securitySchemes.bearerAuth`（HTTP bearer / JWT）；受保护端点加 `@SecurityRequirement`，常量集中在 `common/web/ApiDocs` | ✅ 快照有 `securitySchemes: [bearerAuth]`，`users/me` 与 `admin/roster/import` 两个操作带 `security`，Swagger UI 出现 Authorize 按钮。**但见 N-4：声明 ≠ 强制** |
| **N-3** | **不可解析的请求体返回 500 / 9999（缺陷，[手册](../../流程手册.md) 4.3 定级 P2）**：`HttpMessageNotReadableException` 无专用处理器，落到兜底 `@ExceptionHandler(Exception.class)`，既报成服务端错误、又 `log.error` 打全栈 | 重写 `GlobalExceptionHandler`：新增 **6 个**框架异常处理器（覆盖 **8 类**异常：校验失败、不可解析请求体、缺参与类型不匹配共用一个、方法不支持、媒体类型不支持、无路由含 `NoResourceFound` 与 `NoHandlerFound` 两类），返回类型改 `ApiError`，并按"客户端错误 WARN 不打栈、业务错误不记日志、未知异常才 ERROR 打全栈"分档 | ✅ **范围比原记录更大**：改前不止畸形 JSON，共 **5 类**客户端错误都落兜底（畸形/缺失请求体 → `400/1001`、`text/plain` → `415/1003`、错方法 → `405/1002` 且带 `Allow`、无路由 → `404/1004`）。10 条探针全部符合预期，整轮复测后端日志 **ERROR 行数 = 0**、`unhandled exception` 出现 **0 次**（逐条实测响应见 §4.4） |

### 6.2 原开放问题 N-4 / N-5 / N-6 —— 已由 [CR-028](../../变更日志/变更台账.md#cr-028) 处置（2026-09-12；N-4 的框架级强制随后由 [CR-031](../../变更日志/变更台账.md#cr-031) 补齐）

| # | 原问题 | 处置 | 复测结论 |
|---|------|------|---------|
| **N-4** | 🔴 **契约声明了鉴权，运行时并未按该声明强制** | 新增统一入口 `common/web/CurrentUser`（`requireId` / `requireRole`）+ 公开声明锚点 `common/web/PublicEndpoint`，并由 `ArchitectureGuardTest` **机器校验**两条纪律：每个 HTTP 映射方法必须且只能声明 `@PublicEndpoint` 或 `@SecurityRequirement`；凡声明 `@SecurityRequirement` 的方法必须真的调用 `CurrentUser`。4 个受保护端点全部改走该入口（逐个见 §3 第 8 列） | ✅ **"漏写鉴权静默变公开"缺口闭环**：负向对照实验（临时注入 3 个违规探针文件 → 守护测试 **7 例失败**、探针删除后全绿）证明规则**真的会拦**；真机复测 4 个受保护端点匿名 / 非法 token → 全部 `401`。⚠️ **当时残留的"路径级拦截未做"已由 [CR-031](../../变更日志/变更台账.md#cr-031)（2026-09-12）闭环**：`SecurityConfig` 去 `permitAll()`，按同一批端点注解在过滤器链内裁决（`module` 未声明 fail-closed），匿名在进入业务代码前即被拒（§4.5 ⑫ 实测）；**残留边界变为**：框架只区分"登录 / 未登录"，角色与资源级授权仍归业务代码（详见 §3 的 N-4 说明段） |
| **N-5** | 技术方案 §5 的鉴权表述与实现机制不符（写"未登录可读、写操作 401"，而实现是各 Controller 自判） | 修正 §5 的鉴权表述，使其与 **`CurrentUser` + `ArchitectureGuardTest` 的机器校验机制**一致 | ✅ 技术方案 §5 已同步（[CR-028](../../变更日志/变更台账.md#cr-028)） |
| **N-6** | **名册 CSV 导入的表头识别只认中文（缺陷，P3）** | `RosterImportApplicationService` 的表头识别改为**中英双认**（`studentId` / `student_id` / `Student ID` / `STUDENT-ID` 等别名归一后比对），并**在导入侧补 `^\d{9}$` 格式校验** | ✅ 新增 10 个回归用例（`RosterImportApplicationServiceTest`，含表头跳过、非法学号不入库、失败也写审计等）；真机复测英文表头 CSV → `inserted:0 skipped:0 failed:2` 且**不再污染名册**，`audit_logs` 如实写入 |

> **本次的净效果**：N-1 / N-2 / N-3（[CR-021](../../变更日志/变更台账.md#cr-021)）+ N-4 / N-5 / N-6（[CR-028](../../变更日志/变更台账.md#cr-028)）共 **6 项**契约缺口全部处置；N-4 中"框架级强制"这一层随后由 [CR-031](../../变更日志/变更台账.md#cr-031) 补齐（**声明、机器校验、运行时拦截三层齐备**，边界与行为变化见 §3 与 §4.5 ⑫）。**但快照本身仍不是"运行时真相"**，读它之前请先看 §6.3 的表达极限。

### 6.3 OpenAPI 的固有表达极限（非缺陷，快照里**永远**看不到）

| 内容 | 为什么表达不了 | 读者该看哪里 |
|------|--------------|------------|
| 错误码语义 | `2101` 三态同码同提示的**防名册枚举**约定（PRD F-ACC-004）在 OpenAPI 里没有对应结构，只体现在 §4.1 的实测响应里 | [技术方案](../技术方案.md) §5 与 [PRD](../../需求/产品需求文档.md) |
| 统一响应外壳是逐端点展开的 | 快照里是 `ApiResponseAuthResponse`、`ApiResponseUserVo` 等 **15 个具体类型**（⚠️ 本次更正：[CR-048](../../变更日志/变更台账.md) 新增 `ApiResponseInteractionResult` 后即为 12，v1.7 的本文仍写 11；[CR-049](../../变更日志/变更台账.md) 的搜索端点**未新增**——复用 `ApiResponsePageVoPostSummaryVo`；[CR-050](../../变更日志/变更台账.md) 通知中心 +3 → 现为 **15**：`ApiResponsePageVoNotificationVo` / `ApiResponseUnreadCountResult` / `ApiResponseMarkAllReadResult`），没有 `ApiResponse<T>` 泛型表达；改外壳字段会同时改动这 15 个 schema | 读 diff 时注意"一处改动、多处变化" |
| **`405`（`1002` 方法不允许）与 `404`（`1004` 无此路由）** | `OperationCustomizer` 只能修改**已注册的 operation**，而"不存在的路径"根本没有 operation，"不允许的方法"也无从挂在某个 operation 上 | §4.4 的 ④⑤ 实测响应 + `GlobalExceptionHandler` |
| **`415`（`1003` 媒体类型不支持）** | springdoc 按 Controller 的 `consumes` 生成请求体媒体类型，不生成 415 响应分支 | §4.4 的 ③ 实测响应 |

> **当前状态**：N-1 / N-2 / N-3 / N-4 / N-5 / N-6 **六项全部处置**（[CR-021](../../变更日志/变更台账.md#cr-021) 三项 + [CR-028](../../变更日志/变更台账.md#cr-028) 三项），其中 **N-4 的运行时强制已由 [CR-031](../../变更日志/变更台账.md#cr-031) 补齐第二层**——**声明（契约）、机器校验（守护测试）、运行时拦截（框架）三层齐备**；框架只区分"登录 / 未登录"，角色与资源级授权仍归业务代码。**上表四类是表达极限而非缺陷**：它们不是"忘了写"，是 OpenAPI 格式本身装不下，故**再生成多少次也不会出现**，只能读文字约定（§4.4 / 技术方案 §5 / `GlobalExceptionHandler`）。

## 7. 变更记录

- **v1.9.2（2026-09-15）——[CR-051](../../变更日志/变更台账.md) 时间戳时区口径修复：`openapi.json` 快照文件零改动，只改本文口径**：① **§1 新增「时间字段口径」条**——写明 `format: date-time` 的 `Z` 自本日起即为真 UTC、`Instant` 序列化与 `spring.jackson.time-zone` 无关、存储侧靠 `connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true` 成对强制会话时区、由 `DataSourceTimezoneTest` 守护，并单列 `expiresAtEpochSecond`（epoch 秒、不经数据库，L-6 期间也一直正确）；② **§2 新增「CR-051 重抓核对」行**——改配置后按本节命令重抓，**`git diff` 为空**，故快照代码来源仍为 `70c6ca9`；③ **§4.7 与 §5 各加一条"已闭环"注记**（原叙述与修复前实测值 `2026-09-14T23:06:34Z` **保留不改写**，它们是 L-6 的证据）。⚠️ **一处必读的判定纪律**：快照 diff 为空 **≠** 时间口径没变——快照里不含取值，时间口径属**数据层**变化、不属**契约层**变化；因此本 CR 不触发 §5 技术方案接口表同步，只动本文口径条与历史记录处的加注。
- **v1.9.1（2026-09-15）——补记提交凭证与"提交后核对"结果，快照文件零改动**：CR-050 已提交为 `70c6ca9`；自**运行中后端**（`http://localhost:8088/api/docs`，`APP_ROSTER_BYPASS=false` 实例）重抓并按 §2 命令格式化，与仓库快照**逐字节一致**（59977 字节，并记录 `servers` 即为既定值 `http://localhost:8088`，无需回写）——v1.9 遗留的"提交后核对"就此完成，快照来源由"未提交的工作区"更正为**提交 `70c6ca9`**。本次未复现 v1.8.1 记过的 `servers` 回显差异（抓取即用 `localhost`）。
- **v1.9（2026-09-14）——[CR-050](../../变更日志/变更台账.md) 通知中心落地后再生成**：新建限界上下文 `module/notification`（第三个上下文），新增 **3 个受保护端点**（`GET /notifications?unread=&page=&size=` / `GET /notifications/unread-count` / `PUT /notifications/read-all`），18 → **21** 操作（19 条路径）/ 33 → **40** schema（+7：`NotificationVo`、`PageVoNotificationVo`、`UnreadCountResult`、`MarkAllReadResult` 与 3 个 `ApiResponse*` 外壳）/ tag 7 → **8**（新增 `notification`）/ 受保护操作 9 → **12**。⚠️ **本次 diff 非纯新增**：`git diff --numstat` **322 增 / 1 删**，删除行是 `GET /api/v1/boards` 的 `operationId` **`list_2` → `list_3`**——springdoc 按"方法名 + 同名次序"编号，`NotificationController.list` 加入后把 boards 的序号顶后一位；**判定为生成器编号漂移、非契约变化、无破坏性**（该端点路径 / 方法 / 入参 / 出参 / `security` 全未变，前端 `src/api/*` 手写、无代码生成环节），但这是历次再生成**第一次动到既有端点自身内容**，纪律随之收紧为"见删除行必逐行判定"。同步范围：**§1** 覆盖 18 → 21、受保护 9 → 12；**§2** 本次来源与规模、diff 性质；**§3** 新增 **3.3 通知上下文**表（含 `unread` 三态、读时组装、**`non_null` 使可空字段在前端是"可选"而非"null"**这条通用口径）与非 200 表 3 行（分页越界口径实测）；**§4.7** 新增通知中心真机实测（触发链 + 三端点 + 幂等 + 3 条匿名 401 + 非法入参的 401 / 400 分档，全部真实 `traceId`）；**§5** 通知行由"仍未实现"改为"已实现并取代原计划 `PUT /notifications`"，并对"时间 ISO 8601 UTC"补一条**实现与约定相反**的警示；**§6.3** `ApiResponse*` 具体类型 12 → 15。**真机才暴露的两处缺陷已修**：① `GET /notifications` 的 **`unread` 筛选反义**（入参 `unread` 与列 `is_read` 语义相反，适配器直接 `.eq(is_read, unread)`），改 `!unread` 并**新增适配层测试** `NotificationRepositoryImplFilterTest` 3 例（应用层 Mockito 结构上看不见列语义）；② 前端 `n.postId === null` / `n.floorNo !== null` 在 `non_null` 下恒不成立，渲染出「#undefined 楼」并跳 `/post/undefined`，改真值判断 + 类型改可选。**发现 1 处未修缺陷（不属本次范围、Sprint 2 起即存在）**：时间戳整体偏 **+8 小时**（V1 各表 `DEFAULT CURRENT_TIMESTAMP` 写本地墙钟 + JDBC `serverTimezone=UTC` 读成 UTC），已登记为问题台账条目与 **CR-051 候选**。单测 145 → **162** 全绿。快照自本机 8088 实例（`APP_ROSTER_BYPASS=false` 重启实例）抓取，`servers` 无需回写。**遗留**：① 快照的"提交后核对"须在 CR-050 代码提交后按 §2 方法补做（与 v1.3 / v1.5 / v1.7 同例）；② `info.version` 仍为 `0.1.0`；③ **`/notifications` 页像素级目视走查未完成**（内嵌浏览器视口 `0×0`，只做了 DOM 结构 / `getComputedStyle` / `scrollY` 取证），归 [W-03](../../流程偏离记录.md) 补偿③；④ 通知**无实时推送**（30s 轮询 + 切页补读），单条已读与 `quote` 类通知未实现。
- **v1.8.1（2026-09-13）——补记提交凭证与"提交后核对"结果，快照文件零改动**：CR-049 已提交为 `5cdcf93`；自**运行中后端**（8088 实例）重抓 `/api/docs` 并规范化后与仓库快照**逐字节一致**——v1.8 遗留的"提交后核对"就此完成，快照来源由"未提交的工作区"更正为**提交 `5cdcf93`**。**过程如实记**：首次以 `http://127.0.0.1:8088` 为 Host 抓取时，springdoc 会把 `servers` 回显为请求 Host（`http://127.0.0.1:8088`），与快照仅此一处不同；改以 `http://localhost:8088` 重抓即完全一致——这是 springdoc 回显请求 Host 的行为，**不是契约变化**，快照文件从未因此变动。
- **v1.8（2026-09-13）——[CR-049](../../变更日志/变更台账.md) 站内搜索落地后再生成，契约纯新增**：新增**公开**端点 `GET /api/v1/posts/search`（5 个 query 参数：`keyword` 必填 2~50 字、`boardCode`、`days` 仅 7/30/90、`page`、`size`；出参复用 `PageVo<PostSummaryVo>`），17 → **18** 端点；**schema 数未变**（33）、**tag 数未变**、**`security` 声明未变**（受保护操作仍 9 个）；`git diff` **98 行新增 / 0 行删除**。本次直接自 8088 实例抓取，`servers` **无需回写**（不同于 v1.6 / v1.7 的 8093 / 8094 变通）。**再生成过程中发现并修正一处自己引入的契约瑕疵**：`@ErrorCodes` 里显式写了 `INVALID_PARAM`，而 `OpenApiErrorResponseCustomizer` 本就会自动补通用 400，导致 400 的 description 变成 `参数错误（1001）；参数错误（1001）`——已删去显式声明（只留 `NOT_FOUND`），重编译重启后重抓确认归一为单条 `参数错误（1001）`；旧快照零重复，属本次新增端点独有。并同步本文：**§1** 覆盖范围 17 → 18（论坛 11 → 12）；**§2** 换成本次来源与规模；**§3** 标题与 §3.2 表新增搜索行（含 `ngram_token_size` 决定 2 字下限的约束说明）、非 200 表新增一行；**§4.6** 新增站内搜索实测（PRD 验收主用例 + 相关度优先于时间的取证 + 校验矩阵 + 两处临时置数的还原确认 + 回归面）；**§5** `GET /search` 由"仍未实现"划掉并标注落点。**顺带更正两处陈旧计数**（均只改本文、快照文件从未因此变动）：tag 数自 CR-048 起实际为 **7**（本文自 v1.3 起一直写 6）、`ApiResponse*` 具体类型自 CR-048 起实际为 **12**（v1.7 仍写 11）。**遗留**：① 快照的"提交后核对"须在 CR-049 代码提交后按 §2 方法补做（与 v1.3 / v1.5 / v1.7 同例）；② `info.version` 仍为 `0.1.0`；③ **`tags` 侧只有 `LIKE` 兜底、无真实数据来源**（发帖链路从未建模标签，详见 §4.6 的如实说明）；④ 前端**宽屏 / 窄屏目视走查未完成**（内嵌浏览器视口 `0×0`，只做了 DOM 结构校验），归 [W-03](../../流程偏离记录.md) 补偿③。
- **v1.7.1（2026-09-13）——补记提交凭证与"提交后核对"结果，快照文件零改动**：CR-048 已提交为 `c510493`（同批含 CR-046 / CR-047 文档）；自**运行中后端**（8094 临时端口实例，Redis 已另行启动）重抓 `/api/docs` 并按 §2 命令格式化、`servers` 归一回 `http://localhost:8088`，与仓库快照**逐字节一致**——v1.7 遗留的"提交后核对"就此完成，快照来源由"未提交的工作区"更正为**提交 `c510493`**。
- **v1.7（2026-09-13）——[CR-048](../../变更日志/变更台账.md) 点赞收藏落地后再生成，契约纯新增 + 出参扩展**：新增 4 个受保护端点——`POST /posts/{id}/like`、`POST /posts/{id}/favorite`、`POST /posts/{postId}/replies/{replyId}/like`（三者均为 **toggle**，响应 `InteractionResult{active, count}`）与 `GET /favorites/mine`（`PageVo<PostSummaryVo>`），13 → **17** 端点 / 31 → **33** schema（新增 `InteractionResult`、`ApiResponseInteractionResult`）；两个既有 VO 扩展字段：`PostDetailVo` +`likedByMe` +`favoritedByMe`、`ReplyVo` +`likeCount` +`likedByMe`（匿名访问详情 / 楼层列表仍 200，登录态回显字段恒 false）。快照自 8094 临时端口实例抓取（8088 被旧栈占用），`servers` 回写既定值；与 v1.6 比对核对差异恰为上述内容，无意外漂移。§1 / §2 / §3.2 / 非 200 表 / §5 已同步。**遗留**：提交后核对待 CR-048 提交后补做（→ v1.7.1 已完成）；`info.version` 仍为 `0.1.0`。
- **v1.6.1（2026-09-13）——补记提交凭证与"提交后核对"结果，快照文件零改动**：CR-043 已提交为 `493ef87`；自**运行中后端**（8094 临时端口实例，8088 仍被旧版进程占用）重抓 `/api/docs` 并按 §2 命令格式化、`servers` 归一回 `http://localhost:8088`，与仓库快照**逐字节一致**（唯一原始差异即 `servers` 抓取端口，归一后零差异）——v1.6 遗留的"提交后核对"就此完成，快照来源由"未提交的工作区"更正为**提交 `493ef87`**。
- **v1.6（2026-09-13）——[CR-043](../../变更日志/变更台账.md) 采纳最佳答案落地后再生成，契约纯新增**：新增 `POST /api/v1/posts/{id}/accept`（受保护，`AcceptReplyCommand` 请求体）与 schema `AcceptReplyCommand`，12 → **13** 端点 / 30 → **31** schema；三个既有 VO 扩展字段：`PostSummaryVo` +`accepted`、`PostDetailVo` +`boardType` +`authorId`、`ReplyVo` +`authorId` +`accepted`；楼层列表排序改为最佳答案置顶。快照由 8093 端口实例抓取（8088 被旧版进程占用），`servers` 回写既定值，见 §2 方法学备注。§3.2 / 非 200 表已同步，§5 中 `PUT /{id}/accepted-reply` 计划项由本实现取代。
- **v1.5.1（2026-09-13）——补记提交凭证与"提交后核对"结果，快照文件零改动**：CR-031 已提交为 `7720adc`；自**运行中后端**（`http://localhost:8088/api/docs`）重抓并按 §2 命令格式化，与仓库快照**逐字节一致**（`git diff --no-index` 零差异）——v1.5 遗留的"提交后核对"就此完成，快照来源由"未提交的工作区"更正为**提交 `7720adc`**（`info.version` 仍为 `0.1.0`）。一处方法学备注：核对须抓**配置的契约入口 `/api/docs`**——springdoc 默认的 `/v3/api-docs` 在本项目返回的是另一份更小的文档，不是快照的事实来源。
- **v1.5（2026-09-12）**——配合 [CR-031](../../变更日志/变更台账.md#cr-031)（**路径级鉴权落地**：`SecurityConfig` 去 `permitAll()`，`EndpointAuthorizationManager` 按端点注解在过滤器链内裁决）**再生成快照**：**端点 / schema / tag / `security` 声明全部未变**（仍 12 端点 / 30 schema / 6 tag / 4 个受保护操作），`git diff` **仅 1 行**——`components.securitySchemes.bearerAuth.description` 据实改写（改为"框架按端点注解裁决、`module` 未声明 fail-closed、未登录由过滤器链产出 401 / 4001"，并注明角色与资源级授权仍归 `CurrentUser`）。并同步本文：**§1** 改述为"**路径级拦截已由 CR-031 落地**"；**§2** 换成本次来源（**仍是未提交的工作区**，`HEAD` = `c806153`，时刻核验：落盘 22:29:12 晚于 `backend/src/main` 最后的 mtime 22:26:32）与规模（18481 → 36529 字节），触发点⑥改为**已完成**、新增 ⑩（改 `EndpointAuthorizationManager` 裁决口径后须重抓）；**§3** 两张表第 8 列对 4 个受保护端点补"**框架层前置拦截 + 业务层同码兜底**"两层描述、`admin/roster/import` 行如实标注 403 支**未在真机复现**（单测覆盖），N-4 说明段重写为"**三层机制 + 两条边界**"；**§4.5** 新增 **⑫ CR-031 复测段**（匿名非法 body / 匿名合法 body / 伪造 token 三条真实 `traceId`；405 / 404 / 公开端点对照）并把原 🔍 注记改写为**行为变化说明**（受保护端点"匿名 + 非法 body"由 `400 / 1001` 前置为 `401 / 4001`，公开端点不受影响）；**§6.2** N-4 行补"路径级强制已由 CR-031 闭环"，尾注与 §6.3 当前状态改为"声明 / 机器校验 / 运行时拦截三层齐备"。**遗留**：快照的"提交后核对"须在 CR-031 代码提交后按 §2 方法补做（与 v1.3 / v1.4 同）；`info.version` 仍为 `0.1.0`。【本快照的端点契约与 v1.4 **完全一致**，若前端只关心接口形状，v1.4 → v1.5 **无破坏性变更**】
- **v1.4（2026-09-12）**——配合 [CR-028](../../变更日志/变更台账.md#cr-028)（B4 / B5：A3-9 架构守护测试 + A3-10 文档-代码一致性）**再生成快照**：**端点 / schema / tag / `security` 声明全部未变**（仍 12 端点 / 30 schema / 6 tag / 4 个受保护操作），`git diff` **仅 1 行**——`components.securitySchemes.bearerAuth.description` 据实改写（原描述说"实际强制点是 Controller 内校验"，现改为指向 `CurrentUser` 统一入口 + `ArchitectureGuardTest` 机器校验，并写明 `SecurityConfig` 仍为 permitAll）。并同步本文：**§1** N-4 改述为"**漏写缺口已闭环**、路径级拦截仍未做"；**§2** 换成本次来源（**仍是未提交的工作区**，`HEAD` = `e11a2a2`）与规模（18290 → 36338 字节），diff 性质改为"单行文案改动"，触发点⑥改为"迄今未发生"、新增 ⑧（增删端点声明 / 改 `CurrentUser` 语义）与 ⑨（本次已完成）；**§3** 两张表的第 8 列由"Controller 内手工判空 / `AdminRosterController.requireSuperadmin`"改为 **`CurrentUser.requireId` / `requireRole`** 统一入口，N-4 说明段重写为"已闭环 + 两条残留事实 + 历史沿革"；**§4.5** 🔍 行为顺序注补"位置不变、仍为方法体内"，并补 `GET /users/me` 无此先后问题的说明；**§6.2** 由"仍开放"改写为"**已处置**"（N-4 / N-5 / N-6 逐条给处置与复测结论，N-4 记录**负向对照实验**证据：注入 3 个违规探针 → 守护测试 7 例失败、删除后全绿）；**§6.3** 尾注由"N-4 未闭环更危险"改写为"六项全部处置，N-4 闭环的是漏写而非框架拦截"。**遗留**：快照的"提交后核对"须在 CR-028 代码提交后按 §2 方法补做（与 v1.3 同）；`SecurityConfig` 的路径级拦截仍未做（**不属 N-4 范围**，[CR-028](../../变更日志/变更台账.md#cr-028) 未承诺）；`info.version` 仍为 `0.1.0`。【本快照的端点契约与 v1.3 **完全一致**，若前端只关心接口形状，v1.3 → v1.4 **无破坏性变更**】
- **v1.3（2026-09-12）**——配合 [CR-022](../../变更日志/变更台账.md#cr-022) 的 Sprint 2 MVP（6 个论坛端点）**再生成快照**（**12 端点 / 30 schema**；18185 → 36233 字节），并同步本文：**§1** 覆盖范围由"6 个"改为"**12 个**（账号 6 + 论坛 6）"，受保护操作由 2 个改为 **4 个**；**§2** 换成本次的来源与规模，**如实标注代码来源仍是未提交的工作区**（与 v1.1 的差别：v1.1 事后补齐了"对应 `63ed21c`"，本次**无法**声称对应任何提交，只有"快照落盘时刻晚于 `backend/src/main` 全部 mtime"的时刻核验），新增触发点⑦与"① 是高频触发点"的提示；**§3** 拆为 3.1 账号（原表原样保留）/ **3.2 论坛**两张表，非 200 响应表新增 6 行，N-4 说明段补"Sprint 2 的三个新防线"与两条**文档-代码差异**（`{postId}` vs `{id}` 路径参数名、`accepted` vs `isAccepted`）；**§4** 证据分档增【Sprint 2 实测】一档，新增 **§4.5 论坛主链路**（11 条实测响应：6 条成功 + 5 条错误，含两个受保护端点的 401）+ 一条实测发现的**行为顺序**（参数校验先于登录态校验，故"匿名 + 非法 body"返回 400 而非 401）；**§5** 差异表重写为五类（已实现 / 已补入 / 计划项转已实现 / 仍未实现 / 无对应表达）；**§6.2** N-4 补"人工处置但机制未闭环"、N-5 补"差距又扩大一次"；**§6.3** `ApiResponse*` 具体类型 5 → **11**。**遗留**：N-4（→ A3-9，**A3-9 已被推迟至 MVP 验收后**）、N-5（→ A3-10）、N-6（P3，未修）；快照的"提交后核对"须在本轮代码提交后按 §2 方法补做；`.openapi` 的 `info.version` 仍为 `0.1.0`、**未随 Sprint 递增**，本轮未改（是否引入版本策略归 A3-8 / 部署形态讨论）。
- **v1.2（2026-09-12）**——**补记提交凭证，无内容变更**：把"代码来源为未提交的工作区"改为**来源即提交 `63ed21c`**（CR-020 与 CR-021 合并为一次提交）。依据为**文件时间戳核验**：本快照导出（01:24:21）之后 `backend/src/main` **零改动**，唯一变动是 01:29 新增的测试类 `AdminRosterControllerAuthTest`，而快照只由生产代码与注解生成、测试类不影响其内容，故 §2 原要求的"提交后重抓一次比对"**已由该证据解除、无需执行**。快照文件本身与 §1 / §3 / §4 / §6 的实测内容**一字未改**。
- **v1.1（2026-09-12）**——配合 [CR-021](../../变更日志/变更台账.md#cr-021) 修复 N-1 / N-2 / N-3 后**再生成快照**（6 端点 / **14** schema，新增 `ApiError`；9108 → 17545 字节），并同步本文：**§1** 增"快照现在也表达错误响应与鉴权，但声明 ≠ 强制"一条；**§2** 换成本次的来源与规模，并**如实标注代码来源是未提交的工作区**（v1.0 能声称对应提交 `68d40ae`，本次不能），补 diff 纯新增性的脚本比对结论与两条新触发点（改 `ResultCode`、`SecurityConfig` 收紧后）；**§3** 鉴权列拆为"快照声明"与"运行时强制方式"两列，新增各端点非 200 响应清单表，并把原"鉴权为什么不在快照里"整段（已过期）改写为 **N-4**；**§4** 证据分档增【CR-021 实测】一档，新增 **§4.4 错误响应实例**（7 条实测响应体 + 与快照的对应关系 + 日志档位）；**§6** 重构为三小节：6.1 已修的三项（含修复方式与复测结论）、6.2 仍开放的 **N-4 / N-5 / N-6**、6.3 OpenAPI 固有表达极限（新增 `405`/`404`/`415` 三行），删掉"N-1/N-2/N-3 均未处置"的过期结论。**遗留**：N-4（→ A3-9）、N-5（→ A3-10）、N-6（P3，未修）；本 CR 代码已于 2026-09-12 提交为 `63ed21c`，且经文件时间戳核验快照与该提交一致（`backend/src/main` 在导出后零改动），**原要求的"提交后重抓一次"已解除**；本次**未在浏览器目视 UI 回归**（改经 Vite 代理实测 + 审阅 `client.ts`，详见 [CR-021](../../变更日志/变更台账.md#cr-021) 复测结果⑦）。
- v1.0（2026-09-11）——创建。归档 `openapi.json` 快照（提交 `68d40ae`，6 端点 / 13 schema），补 2 条主链路的实测请求响应结构、与技术方案 §5 的逐条差异、快照未表达的 5 项内容（含新发现缺陷 N-3）。闭环设计门 **A3-4**，登记为 [CR-019](../../变更日志/变更台账.md#cr-019)。**设计门出口标准第 2 项仍为 ❌**（A3-5 ER 图与数据量级预估未做）。
