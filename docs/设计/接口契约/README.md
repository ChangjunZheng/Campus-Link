# OpenAPI 快照说明（`./`）

| 文档信息 | 内容 |
|---------|------|
| 版本 | v1.7 |
| 状态 | 已归档（**快照**，接口契约变更后须再生成，见 §2） |
| 维护人 | 技术负责人（发起人兼任） |
| 最后更新 | 2026-09-13 |
| 对应行动项 | [设计门纪要](../../评审/设计门纪要.md) **A3-4**：补归档 OpenAPI 快照，或补 2~3 个主链路接口请求/响应结构（**两个备选项本文都做**） |
| 变更登记 | [CR-019](../../变更日志/变更台账.md#cr-019)（首次归档）· [CR-021](../../变更日志/变更台账.md#cr-021)（修复 N-1 / N-2 / N-3 后**再生成**：契约新增错误响应与 bearerAuth 声明）· [CR-022](../../变更日志/变更台账.md#cr-022)（Sprint 2 MVP 新增 6 个论坛端点后**再生成**：6 → **12** 端点）· [CR-028](../../变更日志/变更台账.md#cr-028)（**N-4 闭环后**再生成：`bearerAuth` 描述据实改写，**端点 / schema 数一字未变**）· [CR-031](../../变更日志/变更台账.md#cr-031)（**路径级鉴权落地后再生成**：`bearerAuth` 描述随框架拦截据实改写，**端点 / schema / `security` 声明仍未变**）· [CR-043](../../变更日志/变更台账.md)（**采纳最佳答案落地后再生成**：新增 `POST /posts/{id}/accept` 与 `AcceptReplyCommand` schema，12 → **13** 端点）· [CR-048](../../变更日志/变更台账.md)（**点赞收藏落地后再生成**：新增 4 端点与 `InteractionResult` schema，13 → **17** 端点） |
| 关联记录 | [技术方案](../技术方案.md) §5 接口契约 · [PRD](../../需求/产品需求文档.md) F-ACC-004 · [Sprint 1 计划](../../开发/Sprint1计划.md) · [Sprint 2 计划](../../开发/Sprint2计划.md) · [Sprint 2 增量设计](../../开发/Sprint2增量设计.md) |

## 1. 这份文件是什么、不是什么

- **是**：`openapi.json` —— 由 springdoc 从**运行中的后端**导出的一次**冻结快照**，用于设计门归档与"文档 vs 实现"的差异比对；
- **不是**手写的接口规范：契约的权威来源永远是运行中的服务（`GET /api/docs`，Swagger UI 在 `/api/docs/swagger-ui.html`）。本文与快照一旦落后于代码，以代码为准并**立即再生成**；
- **覆盖范围是**已实现的 **17 个端点**（账号 6 + 论坛 11），不得读成"全部接口契约已归档"——[技术方案](../技术方案.md) §5 接口表中仍有多行属后续 Sprint 计划项（逐条差异见 §5）。因此技术方案文末检查清单第 3 项"接口契约**完整**"**仍不勾选**；
- **自 [CR-021](../../变更日志/变更台账.md#cr-021) 起，快照也表达错误响应与鉴权**：每个操作都声明了 4xx / 5xx 及错误体 schema `ApiError`，`components.securitySchemes.bearerAuth` 已定义，**9 个**受保护操作带 `security`（`users/me`、`admin/roster/import`、`POST /posts`、`POST /posts/{postId}/replies`、`POST /posts/{id}/accept`、点赞 ×2、收藏、`favorites/mine`）。**N-4 的"漏写鉴权静默变公开"缺口已由 [CR-028](../../变更日志/变更台账.md#cr-028) 闭环**（统一入口 `CurrentUser` + `ArchitectureGuardTest` 机器校验）；**路径级拦截已由 [CR-031](../../变更日志/变更台账.md#cr-031) 落地**（`SecurityConfig` 去 `permitAll()`，按端点注解裁决，见 §6.2），另有 4 类事实快照表达不了（§6）——读快照前必须先看 §6；
- **不留多份历史副本**：本目录只有一个当前快照，历史版本由 git 承担（[CR-008](../../变更日志/变更台账.md#cr-008) 的教训——不该用 zip / 副本替代 git 历史）。

## 2. 快照来源与再生成

| 项 | 值 |
|---|---|
| 首次归档 | 2026-09-11（[CR-019](../../变更日志/变更台账.md#cr-019)，源提交 `68d40ae`，6 端点 / 13 schema） |
| **本次生成时间** | **2026-09-13 17:00**（[CR-048](../../变更日志/变更台账.md) 点赞收藏落地后**再生成**：新增 4 端点 / `InteractionResult` + `ApiResponseInteractionResult` schema，13 → 17 端点 / 31 → 33 schema） |
| **代码来源** | ⚠️ **未提交的工作区**（CR-048 后端改动尚未提交）。方法学备注：本次快照自本机 **8094** 端口实例抓取（8088 被用户旧栈占用），`servers` 字段按既定值回写为 `http://localhost:8088`，除此之外零改动；与旧快照比对脚本核对，差异**恰为**新增 4 端点 + 2 个既有端点出参扩展 + 2 个新 schema，无意外漂移。**提交后须按 v1.5.1 / v1.6.1 同一方法补做"无改动"核对** |
| 后端地址 | `http://localhost:8088`（开发端口，[CR-012](../../变更日志/变更台账.md#cr-012)）——快照 `servers` 字段即此值，**部署形态（ADR-011）确定后须重生成** |
| 生成方 | springdoc-openapi 3.1.0（`springdoc-openapi-starter-webmvc-ui`） |
| 规范版本 | OpenAPI **3.1.0**；`info.title` = `Campus-Link API`，`info.version` = `0.1.0`（未随 Sprint 递增，见 §7 遗留） |
| 规模 | **6 个 tag** / **17 个端点** / **33 个 schema**（v1.6 为 6 tag / 13 端点 / 31 schema）。v1.7 新增 2 个 schema：`InteractionResult`、`ApiResponseInteractionResult` |
| 落盘处理 | 原始响应单行压缩 → 按 2 空格缩进格式化。**键序保持 springdoc 原序，未排序、未删改任何字段**（`servers` 回写见上） |
| 可复现性 | 格式化方法与历次一致（`json.dumps(ensure_ascii=False, indent=2)` + 尾部换行），`git diff` 仍可作为契约变更的可靠信号 |
| 本次 diff 性质 | **纯新增 + 出参扩展**：`git diff` 净增约 370 行——① 新增 4 个 path（3 个 toggle 写操作 + `GET /favorites/mine`，均带 `security`）与 2 个新 schema；② 两个既有 VO 新增字段（真实契约扩展，[CR-048](../../变更日志/变更台账.md)）：`PostDetailVo` +`likedByMe` +`favoritedByMe`、`ReplyVo` +`likeCount` +`likedByMe`；无任何既有字段 / 类型被删改 |

**再生成命令**（在仓库根目录执行；先按 `AGENTS.md`"常用命令"起栈：本机 MySQL / Redis + `mvn spring-boot:run`）：

```bash
curl -s http://localhost:8088/api/docs -o /tmp/openapi.json
python -c "import json;d=json.load(open('/tmp/openapi.json',encoding='utf-8'));open('openapi.json','w',encoding='utf-8',newline='\n').write(json.dumps(d,ensure_ascii=False,indent=2)+'\n')"
git diff --stat openapi.json   # 非空即契约有变更，须同步技术方案 §5
```

**再生成触发点**（已写入 `AGENTS.md`）：① 新增 / 修改端点或请求响应模型后；② 每个 Sprint 收尾；③ 部署形态确定后（`servers` 会变）；④ ~~N-1 / N-2 修复后~~ **已完成**（[CR-021](../../变更日志/变更台账.md#cr-021)）；⑤ 改了 `common/result/ResultCode` 的错误码、提示语或 `getHttpStatus()` 映射后——契约里的状态码与 description 全部由它派生，改它即改契约；⑥ ~~`SecurityConfig` 从 `permitAll()` 改为真正按路径授权后~~ **已完成**（[CR-031](../../变更日志/变更台账.md#cr-031)，2026-09-12 再生成：`security` 声明未变，`bearerAuth` 描述已写明"框架按端点注解裁决、未登录由过滤器链产出 401 / 4001"）；⑦ ~~Sprint 2 MVP 的 6 个论坛端点合入前~~ **已完成**（[CR-022](../../变更日志/变更台账.md#cr-022)，2026-09-12 再生成）；⑧ 增删端点的 `@PublicEndpoint` / `@SecurityRequirement` 声明、或改动 `common/web/CurrentUser` 鉴权入口语义后（[CR-028](../../变更日志/变更台账.md#cr-028) 起该纪律由 `ArchitectureGuardTest` 机器校验，但**快照的 `security` 声明不会自动跟随**，仍须重抓）；⑨ ~~N-4 闭环后复核 `bearerAuth` 描述~~ **已完成**（[CR-028](../../变更日志/变更台账.md#cr-028)，2026-09-12 再生成，diff 仅 1 行文案）。**新增**：⑩ 改动 `security/EndpointAuthorizationManager` 的裁决口径（如 fail-closed 范围、非 module 处理器放行规则）后——**运行时行为变了而契约描述可能落后**，须重抓并核对 `bearerAuth` 描述。**其中 ① 是高频触发点**：Sprint 2 起每次加端点都要重抓一次，`git diff` 非空即须同步本文 §3 / §4 与技术方案 §5。

## 3. 端点清单（快照实际覆盖的 17 个）

### 3.1 账号上下文（`module/account`，6 个，Sprint 1）

| 方法 | 路径 | tag | 摘要 | 请求体 | `data` 类型 | **快照声明的鉴权** | **运行时实际强制方式**（见 N-4） |
|------|------|-----|------|--------|------------|------------------|--------------------------------|
| POST | `/api/v1/auth/verify-student` | auth | 学籍核验（F-ACC-004），通过返回一次性票据 | `VerifyStudentCommand` | `VerifyStudentResult` | 无（公开） | 公开（另有 IP 限流） |
| POST | `/api/v1/auth/captcha` | auth | 发送邮箱验证码（60s 重发间隔，单账号日上限 10 条） | `CaptchaCommand` | 无（`data` 省略） | 无（公开） | 公开（另有 IP / 账号限流） |
| POST | `/api/v1/auth/register` | auth | 注册，需先通过学籍核验并携带票据 | `RegisterCommand` | `AuthResponse` | 无（公开） | 公开 |
| POST | `/api/v1/auth/login` | auth | 登录：邮箱 + 验证码 | `LoginCommand` | `AuthResponse` | 无（公开） | 公开 |
| GET | `/api/v1/users/me` | user | 我的主页（F-ACC-002 最小版） | — | `UserVo` | **`security: bearerAuth`** | **框架层（[CR-031](../../变更日志/变更台账.md#cr-031)）按 `@SecurityRequirement` 前置拦截 → 匿名 `401 / 4001`（进入业务代码前）**；业务层 `CurrentUser.requireId(authentication)` 同码兜底 |
| POST | `/api/v1/admin/roster/import?batch=` | admin-roster | 学籍名册 CSV 导入，body 为 CSV 文本 | `string`（CSV） | `RosterImportResult` | **`security: bearerAuth`** | 框架层前置拦截匿名 → `401 / 4001`；业务层 `CurrentUser.requireRole(authentication, ROLE_SUPERADMIN)` → 角色不足 `403 / 4002`（**403 支由单测 `AdminRosterControllerAuthTest` 覆盖，未在真机复现**，见 §6.2） |

### 3.2 论坛上下文（`module/forum`，11 个：Sprint 2 MVP 6 个 + [CR-043](../../变更日志/变更台账.md) 采纳 1 个 + [CR-048](../../变更日志/变更台账.md) 点赞收藏 4 个）

| 方法 | 路径 | tag | 摘要 | 请求体 | `data` 类型 | **快照声明的鉴权** | **运行时实际强制方式**（见 N-4） |
|------|------|-----|------|--------|------------|------------------|--------------------------------|
| GET | `/api/v1/boards` | board | 版块列表（6 个启用版块，按 `sort` 升序，**不分页**） | — | `List<BoardVo>` | 无（公开） | 公开 |
| GET | `/api/v1/posts?boardCode=&page=&size=` | post | 帖子列表：按 `created_at DESC`；带 `boardCode` 则版块内，不带则全站最新 | — | `PageVo<PostSummaryVo>` | 无（公开） | 公开 |
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
| `POST /posts` | 400 / 401 / 404 / 500 | 400：`1001`；401：`4001`；404：`3001`（`boardCode` 不存在） |
| `GET /posts/{id}` | 400 / 404 / 500 | 400：`1001`；404：`3001` |
| `GET /posts/{postId}/replies` | 400 / 404 / 500 | 400：`1001`；404：`3001`（帖子不存在） |
| `POST /posts/{postId}/replies` | 400 / 401 / 404 / 500 | 400：`1001`；401：`4001`；404：`3001`（帖子不存在） |
| `POST /posts/{id}/accept` | 400 / 401 / 403 / 404 / 500 | 400：`1001` + `3002`（非问答帖）+ `3003`（采纳自己的回复）；401：`4001`；403：`4002`（非提问者）；404：`3001`（帖子或回复不存在，防探测统一提示） |
| `POST /posts/{id}/like` | 400 / 401 / 404 / 500 | 400：`1001`；401：`4001`；404：`3001`（帖子不存在） |
| `POST /posts/{id}/favorite` | 400 / 401 / 404 / 500 | 400：`1001`；401：`4001`；404：`3001` |
| `POST /posts/{postId}/replies/{replyId}/like` | 400 / 401 / 404 / 500 | 400：`1001`；401：`4001`；404：`3001`（帖子或楼层不存在，防探测统一提示） |
| `GET /favorites/mine` | 400 / 401 / 500 | 400：`1001`；401：`4001` |

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

## 5. 与技术方案 §5 接口表的逐条差异

| 类别 | 端点 | 处置 |
|------|------|------|
| **§5 已列且已实现（账号 5 行）** | `POST /auth/verify-student`、`POST /auth/captcha`、`POST /auth/register`、`POST /auth/login`、`POST /admin/roster/import` | 无需动作 |
| **§5 未列、已补入并实现** | `GET /api/v1/users/me`（[CR-019](../../变更日志/变更台账.md#cr-019) 补入）、`GET /api/v1/boards`、`GET /api/v1/posts/{postId}/replies`（[CR-022](../../变更日志/变更台账.md#cr-022) 的 MVP 范围新增，**§5 原表没有这两行**） | ✅ 技术方案 §5 已同步补入这两行 |
| **本次由"计划项"转为"已实现"（MVP 子集）** | `GET /posts`（**仅全站最新 / 版块内时间序，无 `sort=hot`**）、`POST /posts`（**无标签**）、`GET /posts/{id}`（**无 DELETE**）、`POST /posts/{id}/replies`（**无 `quotedReplyId`**） | 技术方案 §5 已标注"已实现（MVP 子集）"与未实现部分 |
| **§5 已列但仍未实现**（后续 Sprint） | `GET /boards/{code}/posts`（**已由 `GET /posts?boardCode=` 替代**，不是缺口）、`sort=hot` 与 `hot_score`（ADR-006 定时任务）、`DELETE /posts/{id}`、`PUT /posts/{id}/accepted-reply`、**toggle 版点赞 / 收藏已由 [CR-048](../../变更日志/变更台账.md) 实现并取代**（原列的 `POST\|DELETE` 双方法语义收敛为单一 POST toggle）、`GET\|PUT /notifications`、`GET /search`、`POST /reports`、`/admin/...` 处置台与工单 | 契约尚未设计；各 Sprint 开工前补 §5 并在实现后重生成快照（对应 [sprint-2.md](../../开发/Sprint2计划.md) §1 的 OUT 清单） |
| **§5 有约定但快照无对应表达** | 时间 ISO 8601 UTC、错误码**分段规则**（分页口径已随本次同步为 `page/size`，不再是差异） | **错误码自 [CR-021](../../变更日志/变更台账.md#cr-021) 起部分可见**：具体码与提示语出现在各响应的 description 里（§3 的非 200 表），但"1xxx 通用 / 2xxx 账号 / 21xx 学籍 …"这一**分段规则**OpenAPI 表达不了，仍须读技术方案 §5 |

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
| 统一响应外壳是逐端点展开的 | 快照里是 `ApiResponseAuthResponse`、`ApiResponseUserVo` 等 **11 个具体类型**（Sprint 2 新增 6 个），没有 `ApiResponse<T>` 泛型表达；改外壳字段会同时改动这 11 个 schema | 读 diff 时注意"一处改动、多处变化" |
| **`405`（`1002` 方法不允许）与 `404`（`1004` 无此路由）** | `OperationCustomizer` 只能修改**已注册的 operation**，而"不存在的路径"根本没有 operation，"不允许的方法"也无从挂在某个 operation 上 | §4.4 的 ④⑤ 实测响应 + `GlobalExceptionHandler` |
| **`415`（`1003` 媒体类型不支持）** | springdoc 按 Controller 的 `consumes` 生成请求体媒体类型，不生成 415 响应分支 | §4.4 的 ③ 实测响应 |

> **当前状态**：N-1 / N-2 / N-3 / N-4 / N-5 / N-6 **六项全部处置**（[CR-021](../../变更日志/变更台账.md#cr-021) 三项 + [CR-028](../../变更日志/变更台账.md#cr-028) 三项），其中 **N-4 的运行时强制已由 [CR-031](../../变更日志/变更台账.md#cr-031) 补齐第二层**——**声明（契约）、机器校验（守护测试）、运行时拦截（框架）三层齐备**；框架只区分"登录 / 未登录"，角色与资源级授权仍归业务代码。**上表四类是表达极限而非缺陷**：它们不是"忘了写"，是 OpenAPI 格式本身装不下，故**再生成多少次也不会出现**，只能读文字约定（§4.4 / 技术方案 §5 / `GlobalExceptionHandler`）。

## 7. 变更记录

- **v1.7（2026-09-13）——[CR-048](../../变更日志/变更台账.md) 点赞收藏落地后再生成，契约纯新增 + 出参扩展**：新增 4 个受保护端点——`POST /posts/{id}/like`、`POST /posts/{id}/favorite`、`POST /posts/{postId}/replies/{replyId}/like`（三者均为 **toggle**，响应 `InteractionResult{active, count}`）与 `GET /favorites/mine`（`PageVo<PostSummaryVo>`），13 → **17** 端点 / 31 → **33** schema（新增 `InteractionResult`、`ApiResponseInteractionResult`）；两个既有 VO 扩展字段：`PostDetailVo` +`likedByMe` +`favoritedByMe`、`ReplyVo` +`likeCount` +`likedByMe`（匿名访问详情 / 楼层列表仍 200，登录态回显字段恒 false）。快照自 8094 临时端口实例抓取（8088 被旧栈占用），`servers` 回写既定值；与 v1.6 比对核对差异恰为上述内容，无意外漂移。§1 / §2 / §3.2 / 非 200 表 / §5 已同步。**遗留**：提交后核对待 CR-048 提交后补做；`info.version` 仍为 `0.1.0`。
- **v1.6.1（2026-09-13）——补记提交凭证与"提交后核对"结果，快照文件零改动**：CR-043 已提交为 `493ef87`；自**运行中后端**（8094 临时端口实例，8088 仍被旧版进程占用）重抓 `/api/docs` 并按 §2 命令格式化、`servers` 归一回 `http://localhost:8088`，与仓库快照**逐字节一致**（唯一原始差异即 `servers` 抓取端口，归一后零差异）——v1.6 遗留的"提交后核对"就此完成，快照来源由"未提交的工作区"更正为**提交 `493ef87`**。
- **v1.6（2026-09-13）——[CR-043](../../变更日志/变更台账.md) 采纳最佳答案落地后再生成，契约纯新增**：新增 `POST /api/v1/posts/{id}/accept`（受保护，`AcceptReplyCommand` 请求体）与 schema `AcceptReplyCommand`，12 → **13** 端点 / 30 → **31** schema；三个既有 VO 扩展字段：`PostSummaryVo` +`accepted`、`PostDetailVo` +`boardType` +`authorId`、`ReplyVo` +`authorId` +`accepted`；楼层列表排序改为最佳答案置顶。快照由 8093 端口实例抓取（8088 被旧版进程占用），`servers` 回写既定值，见 §2 方法学备注。§3.2 / 非 200 表已同步，§5 中 `PUT /{id}/accepted-reply` 计划项由本实现取代。
- **v1.5.1（2026-09-13）——补记提交凭证与"提交后核对"结果，快照文件零改动**：CR-031 已提交为 `7720adc`；自**运行中后端**（`http://localhost:8088/api/docs`）重抓并按 §2 命令格式化，与仓库快照**逐字节一致**（`git diff --no-index` 零差异）——v1.5 遗留的"提交后核对"就此完成，快照来源由"未提交的工作区"更正为**提交 `7720adc`**（`info.version` 仍为 `0.1.0`）。一处方法学备注：核对须抓**配置的契约入口 `/api/docs`**——springdoc 默认的 `/v3/api-docs` 在本项目返回的是另一份更小的文档，不是快照的事实来源。
- **v1.5（2026-09-12）**——配合 [CR-031](../../变更日志/变更台账.md#cr-031)（**路径级鉴权落地**：`SecurityConfig` 去 `permitAll()`，`EndpointAuthorizationManager` 按端点注解在过滤器链内裁决）**再生成快照**：**端点 / schema / tag / `security` 声明全部未变**（仍 12 端点 / 30 schema / 6 tag / 4 个受保护操作），`git diff` **仅 1 行**——`components.securitySchemes.bearerAuth.description` 据实改写（改为"框架按端点注解裁决、`module` 未声明 fail-closed、未登录由过滤器链产出 401 / 4001"，并注明角色与资源级授权仍归 `CurrentUser`）。并同步本文：**§1** 改述为"**路径级拦截已由 CR-031 落地**"；**§2** 换成本次来源（**仍是未提交的工作区**，`HEAD` = `c806153`，时刻核验：落盘 22:29:12 晚于 `backend/src/main` 最后的 mtime 22:26:32）与规模（18481 → 36529 字节），触发点⑥改为**已完成**、新增 ⑩（改 `EndpointAuthorizationManager` 裁决口径后须重抓）；**§3** 两张表第 8 列对 4 个受保护端点补"**框架层前置拦截 + 业务层同码兜底**"两层描述、`admin/roster/import` 行如实标注 403 支**未在真机复现**（单测覆盖），N-4 说明段重写为"**三层机制 + 两条边界**"；**§4.5** 新增 **⑫ CR-031 复测段**（匿名非法 body / 匿名合法 body / 伪造 token 三条真实 `traceId`；405 / 404 / 公开端点对照）并把原 🔍 注记改写为**行为变化说明**（受保护端点"匿名 + 非法 body"由 `400 / 1001` 前置为 `401 / 4001`，公开端点不受影响）；**§6.2** N-4 行补"路径级强制已由 CR-031 闭环"，尾注与 §6.3 当前状态改为"声明 / 机器校验 / 运行时拦截三层齐备"。**遗留**：快照的"提交后核对"须在 CR-031 代码提交后按 §2 方法补做（与 v1.3 / v1.4 同）；`info.version` 仍为 `0.1.0`。【本快照的端点契约与 v1.4 **完全一致**，若前端只关心接口形状，v1.4 → v1.5 **无破坏性变更**】
- **v1.4（2026-09-12）**——配合 [CR-028](../../变更日志/变更台账.md#cr-028)（B4 / B5：A3-9 架构守护测试 + A3-10 文档-代码一致性）**再生成快照**：**端点 / schema / tag / `security` 声明全部未变**（仍 12 端点 / 30 schema / 6 tag / 4 个受保护操作），`git diff` **仅 1 行**——`components.securitySchemes.bearerAuth.description` 据实改写（原描述说"实际强制点是 Controller 内校验"，现改为指向 `CurrentUser` 统一入口 + `ArchitectureGuardTest` 机器校验，并写明 `SecurityConfig` 仍为 permitAll）。并同步本文：**§1** N-4 改述为"**漏写缺口已闭环**、路径级拦截仍未做"；**§2** 换成本次来源（**仍是未提交的工作区**，`HEAD` = `e11a2a2`）与规模（18290 → 36338 字节），diff 性质改为"单行文案改动"，触发点⑥改为"迄今未发生"、新增 ⑧（增删端点声明 / 改 `CurrentUser` 语义）与 ⑨（本次已完成）；**§3** 两张表的第 8 列由"Controller 内手工判空 / `AdminRosterController.requireSuperadmin`"改为 **`CurrentUser.requireId` / `requireRole`** 统一入口，N-4 说明段重写为"已闭环 + 两条残留事实 + 历史沿革"；**§4.5** 🔍 行为顺序注补"位置不变、仍为方法体内"，并补 `GET /users/me` 无此先后问题的说明；**§6.2** 由"仍开放"改写为"**已处置**"（N-4 / N-5 / N-6 逐条给处置与复测结论，N-4 记录**负向对照实验**证据：注入 3 个违规探针 → 守护测试 7 例失败、删除后全绿）；**§6.3** 尾注由"N-4 未闭环更危险"改写为"六项全部处置，N-4 闭环的是漏写而非框架拦截"。**遗留**：快照的"提交后核对"须在 CR-028 代码提交后按 §2 方法补做（与 v1.3 同）；`SecurityConfig` 的路径级拦截仍未做（**不属 N-4 范围**，[CR-028](../../变更日志/变更台账.md#cr-028) 未承诺）；`info.version` 仍为 `0.1.0`。【本快照的端点契约与 v1.3 **完全一致**，若前端只关心接口形状，v1.3 → v1.4 **无破坏性变更**】
- **v1.3（2026-09-12）**——配合 [CR-022](../../变更日志/变更台账.md#cr-022) 的 Sprint 2 MVP（6 个论坛端点）**再生成快照**（**12 端点 / 30 schema**；18185 → 36233 字节），并同步本文：**§1** 覆盖范围由"6 个"改为"**12 个**（账号 6 + 论坛 6）"，受保护操作由 2 个改为 **4 个**；**§2** 换成本次的来源与规模，**如实标注代码来源仍是未提交的工作区**（与 v1.1 的差别：v1.1 事后补齐了"对应 `63ed21c`"，本次**无法**声称对应任何提交，只有"快照落盘时刻晚于 `backend/src/main` 全部 mtime"的时刻核验），新增触发点⑦与"① 是高频触发点"的提示；**§3** 拆为 3.1 账号（原表原样保留）/ **3.2 论坛**两张表，非 200 响应表新增 6 行，N-4 说明段补"Sprint 2 的三个新防线"与两条**文档-代码差异**（`{postId}` vs `{id}` 路径参数名、`accepted` vs `isAccepted`）；**§4** 证据分档增【Sprint 2 实测】一档，新增 **§4.5 论坛主链路**（11 条实测响应：6 条成功 + 5 条错误，含两个受保护端点的 401）+ 一条实测发现的**行为顺序**（参数校验先于登录态校验，故"匿名 + 非法 body"返回 400 而非 401）；**§5** 差异表重写为五类（已实现 / 已补入 / 计划项转已实现 / 仍未实现 / 无对应表达）；**§6.2** N-4 补"人工处置但机制未闭环"、N-5 补"差距又扩大一次"；**§6.3** `ApiResponse*` 具体类型 5 → **11**。**遗留**：N-4（→ A3-9，**A3-9 已被推迟至 MVP 验收后**）、N-5（→ A3-10）、N-6（P3，未修）；快照的"提交后核对"须在本轮代码提交后按 §2 方法补做；`.openapi` 的 `info.version` 仍为 `0.1.0`、**未随 Sprint 递增**，本轮未改（是否引入版本策略归 A3-8 / 部署形态讨论）。
- **v1.2（2026-09-12）**——**补记提交凭证，无内容变更**：把"代码来源为未提交的工作区"改为**来源即提交 `63ed21c`**（CR-020 与 CR-021 合并为一次提交）。依据为**文件时间戳核验**：本快照导出（01:24:21）之后 `backend/src/main` **零改动**，唯一变动是 01:29 新增的测试类 `AdminRosterControllerAuthTest`，而快照只由生产代码与注解生成、测试类不影响其内容，故 §2 原要求的"提交后重抓一次比对"**已由该证据解除、无需执行**。快照文件本身与 §1 / §3 / §4 / §6 的实测内容**一字未改**。
- **v1.1（2026-09-12）**——配合 [CR-021](../../变更日志/变更台账.md#cr-021) 修复 N-1 / N-2 / N-3 后**再生成快照**（6 端点 / **14** schema，新增 `ApiError`；9108 → 17545 字节），并同步本文：**§1** 增"快照现在也表达错误响应与鉴权，但声明 ≠ 强制"一条；**§2** 换成本次的来源与规模，并**如实标注代码来源是未提交的工作区**（v1.0 能声称对应提交 `68d40ae`，本次不能），补 diff 纯新增性的脚本比对结论与两条新触发点（改 `ResultCode`、`SecurityConfig` 收紧后）；**§3** 鉴权列拆为"快照声明"与"运行时强制方式"两列，新增各端点非 200 响应清单表，并把原"鉴权为什么不在快照里"整段（已过期）改写为 **N-4**；**§4** 证据分档增【CR-021 实测】一档，新增 **§4.4 错误响应实例**（7 条实测响应体 + 与快照的对应关系 + 日志档位）；**§6** 重构为三小节：6.1 已修的三项（含修复方式与复测结论）、6.2 仍开放的 **N-4 / N-5 / N-6**、6.3 OpenAPI 固有表达极限（新增 `405`/`404`/`415` 三行），删掉"N-1/N-2/N-3 均未处置"的过期结论。**遗留**：N-4（→ A3-9）、N-5（→ A3-10）、N-6（P3，未修）；本 CR 代码已于 2026-09-12 提交为 `63ed21c`，且经文件时间戳核验快照与该提交一致（`backend/src/main` 在导出后零改动），**原要求的"提交后重抓一次"已解除**；本次**未在浏览器目视 UI 回归**（改经 Vite 代理实测 + 审阅 `client.ts`，详见 [CR-021](../../变更日志/变更台账.md#cr-021) 复测结果⑦）。
- v1.0（2026-09-11）——创建。归档 `openapi.json` 快照（提交 `68d40ae`，6 端点 / 13 schema），补 2 条主链路的实测请求响应结构、与技术方案 §5 的逐条差异、快照未表达的 5 项内容（含新发现缺陷 N-3）。闭环设计门 **A3-4**，登记为 [CR-019](../../变更日志/变更台账.md#cr-019)。**设计门出口标准第 2 项仍为 ❌**（A3-5 ER 图与数据量级预估未做）。
