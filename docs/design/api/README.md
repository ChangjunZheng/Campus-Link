# OpenAPI 快照说明（`docs/design/api/`）

| 文档信息 | 内容 |
|---------|------|
| 版本 | v1.3 |
| 状态 | 已归档（**快照**，接口契约变更后须再生成，见 §2） |
| 维护人 | 技术负责人（发起人兼任） |
| 最后更新 | 2026-09-12 |
| 对应行动项 | [设计门纪要](../../reviews/gate-3-design.md) **A3-4**：补归档 OpenAPI 快照，或补 2~3 个主链路接口请求/响应结构（**两个备选项本文都做**） |
| 变更登记 | [CR-019](../../change-log.md#cr-019)（首次归档）· [CR-021](../../change-log.md#cr-021)（修复 N-1 / N-2 / N-3 后**再生成**：契约新增错误响应与 bearerAuth 声明）· [CR-022](../../change-log.md#cr-022)（Sprint 2 MVP 新增 6 个论坛端点后**再生成**：6 → **12** 端点） |
| 关联记录 | [技术方案](../tech-design.md) §5 接口契约 · [PRD](../../requirements/prd.md) F-ACC-004 · [Sprint 1 计划](../../development/sprint-1.md) · [Sprint 2 计划](../../development/sprint-2.md) · [Sprint 2 增量设计](../../development/sprint-2-design.md) |

## 1. 这份文件是什么、不是什么

- **是**：`openapi.json` —— 由 springdoc 从**运行中的后端**导出的一次**冻结快照**，用于设计门归档与"文档 vs 实现"的差异比对；
- **不是**手写的接口规范：契约的权威来源永远是运行中的服务（`GET /api/docs`，Swagger UI 在 `/api/docs/swagger-ui.html`）。本文与快照一旦落后于代码，以代码为准并**立即再生成**；
- **覆盖范围是**已实现的 **12 个端点**（账号 6 + 论坛 6），不得读成"全部接口契约已归档"——[技术方案](../tech-design.md) §5 接口表中仍有多行属后续 Sprint 计划项（逐条差异见 §5）。因此技术方案文末检查清单第 3 项"接口契约**完整**"**仍不勾选**；
- **自 [CR-021](../../change-log.md#cr-021) 起，快照也表达错误响应与鉴权**：每个操作都声明了 4xx / 5xx 及错误体 schema `ApiError`，`components.securitySchemes.bearerAuth` 已定义，**4 个**受保护操作带 `security`（`users/me`、`admin/roster/import`、`POST /posts`、`POST /posts/{postId}/replies`）。**但"声明了鉴权"不等于"运行时强制了鉴权"**（`SecurityConfig` 仍是 `permitAll()`，见 §6 **N-4**），另有 4 类事实快照表达不了（§6）——读快照前必须先看 §6；
- **不留多份历史副本**：本目录只有一个当前快照，历史版本由 git 承担（[CR-008](../../change-log.md#cr-008) 的教训——不该用 zip / 副本替代 git 历史）。

## 2. 快照来源与再生成

| 项 | 值 |
|---|---|
| 首次归档 | 2026-09-11（[CR-019](../../change-log.md#cr-019)，源提交 `68d40ae`，6 端点 / 13 schema） |
| **本次生成时间** | **2026-09-12 15:06**（[CR-022](../../change-log.md#cr-022) 的 Sprint 2 MVP 实现后**再生成**：新增 6 个论坛端点） |
| **代码来源** | ⚠️ **未提交的工作区**（`HEAD` = `4c40a1a` + 尚未提交的 `module/forum` 全部新增文件与 `account` 侧只读方法）。**与 v1.1 的差别**：v1.1 在提交后补齐了"快照对应 `63ed21c`"的核对，**本次代码尚未提交，无法声称对应任何提交**。已做的核验是**时刻核验**：快照落盘（15:06:28）晚于 `backend/src/main` 全部文件的最后修改时刻（14:51:02，`find -printf '%T@'` 取最大值），故快照确由当前源码生成、与工作区一致；**待提交后按 v1.1 同一方法补做"无改动"核对**（见 §7 遗留） |
| 后端地址 | `http://localhost:8088`（开发端口，[CR-012](../../change-log.md#cr-012)）——快照 `servers` 字段即此值，**部署形态（ADR-011）确定后须重生成** |
| 生成方 | springdoc-openapi 3.1.0（`springdoc-openapi-starter-webmvc-ui`） |
| 规范版本 | OpenAPI **3.1.0**；`info.title` = `Campus-Link API`，`info.version` = `0.1.0`（未随 Sprint 递增，见 §7 遗留） |
| 规模 | **6 个 tag** / **12 个端点** / **30 个 schema**（v1.1 为 3 tag / 6 端点 / 14 schema）。新增 16 个 schema：`BoardVo`、`PageVoPostSummaryVo`、`PostSummaryVo`、`PostDetailVo`、`PageVoReplyVo`、`ReplyVo`、`PublishedPost`、`PublishedReply`、`PublishPostCommand`、`PublishReplyCommand` 共 10 个业务类型 + 6 个 `ApiResponse*` 外壳 |
| 落盘处理 | 原始响应 **18185** 字节单行压缩 → 按 2 空格缩进格式化（**36233** 字节，v1.1 为 9108 → 17545）。**键序保持 springdoc 原序，未排序、未删改任何字段** |
| 可复现性 | **已重新实测**：连续两次 `curl` 的响应字节完全一致（均 18185 字节）；格式化后与仓库内文件**逐字节相同**（脚本比对 `MATCH`），故 `git diff` 仍可作为契约变更的可靠信号 |
| 本次 diff 性质 | **纯新增 + 路径键序变化**：已用脚本逐 schema 比对 `HEAD` 版本——14 个既有 schema **逐字节相同**、既有 6 个端点的全部响应**未变**、`info` / `servers` / `securitySchemes` 未变；`git diff --stat` = **883 行新增 / 121 行删除**，其中 121 行删除**全部**来自 `paths` 的键序重排（springdoc 按注册顺序输出，新模块插入使原有路径位置移动），无任何字段移除 |

**再生成命令**（在仓库根目录执行；先按 `AGENTS.md`"常用命令"起栈：本机 MySQL / Redis + `mvn spring-boot:run`）：

```bash
curl -s http://localhost:8088/api/docs -o /tmp/openapi.json
python -c "import json;d=json.load(open('/tmp/openapi.json',encoding='utf-8'));open('docs/design/api/openapi.json','w',encoding='utf-8',newline='\n').write(json.dumps(d,ensure_ascii=False,indent=2)+'\n')"
git diff --stat docs/design/api/openapi.json   # 非空即契约有变更，须同步技术方案 §5
```

**再生成触发点**（已写入 `AGENTS.md`）：① 新增 / 修改端点或请求响应模型后；② 每个 Sprint 收尾；③ 部署形态确定后（`servers` 会变）；④ ~~N-1 / N-2 修复后~~ **已完成**（[CR-021](../../change-log.md#cr-021)）；⑤ 改了 `common/result/ResultCode` 的错误码、提示语或 `getHttpStatus()` 映射后——契约里的状态码与 description 全部由它派生，改它即改契约；⑥ `SecurityConfig` 从 `permitAll()` 改为真正按路径授权后（N-4 闭环，见 §6），届时须核对快照的 `security` 声明与运行时规则一致；⑦ ~~Sprint 2 MVP 的 6 个论坛端点合入前~~ **已完成**（[CR-022](../../change-log.md#cr-022)，2026-09-12 再生成）。**其中 ① 是高频触发点**：Sprint 2 起每次加端点都要重抓一次，`git diff` 非空即须同步本文 §3 / §4 与技术方案 §5。

## 3. 端点清单（快照实际覆盖的 12 个）

### 3.1 账号上下文（`module/account`，6 个，Sprint 1）

| 方法 | 路径 | tag | 摘要 | 请求体 | `data` 类型 | **快照声明的鉴权** | **运行时实际强制方式**（见 N-4） |
|------|------|-----|------|--------|------------|------------------|--------------------------------|
| POST | `/api/v1/auth/verify-student` | auth | 学籍核验（F-ACC-004），通过返回一次性票据 | `VerifyStudentCommand` | `VerifyStudentResult` | 无（公开） | 公开（另有 IP 限流） |
| POST | `/api/v1/auth/captcha` | auth | 发送邮箱验证码（60s 重发间隔，单账号日上限 10 条） | `CaptchaCommand` | 无（`data` 省略） | 无（公开） | 公开（另有 IP / 账号限流） |
| POST | `/api/v1/auth/register` | auth | 注册，需先通过学籍核验并携带票据 | `RegisterCommand` | `AuthResponse` | 无（公开） | 公开 |
| POST | `/api/v1/auth/login` | auth | 登录：邮箱 + 验证码 | `LoginCommand` | `AuthResponse` | 无（公开） | 公开 |
| GET | `/api/v1/users/me` | user | 我的主页（F-ACC-002 最小版） | — | `UserVo` | **`security: bearerAuth`** | Controller 内手工判空 → `401 / 4001` |
| POST | `/api/v1/admin/roster/import?batch=` | admin-roster | 学籍名册 CSV 导入，body 为 CSV 文本 | `string`（CSV） | `RosterImportResult` | **`security: bearerAuth`** | `AdminRosterController.requireSuperadmin` 校验角色 → `401 / 4001` 或 `403 / 4002` |

### 3.2 论坛上下文（`module/forum`，6 个，Sprint 2 MVP 新增）

| 方法 | 路径 | tag | 摘要 | 请求体 | `data` 类型 | **快照声明的鉴权** | **运行时实际强制方式**（见 N-4） |
|------|------|-----|------|--------|------------|------------------|--------------------------------|
| GET | `/api/v1/boards` | board | 版块列表（6 个启用版块，按 `sort` 升序，**不分页**） | — | `List<BoardVo>` | 无（公开） | 公开 |
| GET | `/api/v1/posts?boardCode=&page=&size=` | post | 帖子列表：按 `created_at DESC`；带 `boardCode` 则版块内，不带则全站最新 | — | `PageVo<PostSummaryVo>` | 无（公开） | 公开 |
| POST | `/api/v1/posts` | post | 发帖（MVP 范围：`boardCode` + `title` + `contentMd`，不含标签） | `PublishPostCommand` | `PublishedPost` | **`security: bearerAuth`** | Controller 内手工判空 → `401 / 4001`（§4.5 ⑦ 实测） |
| GET | `/api/v1/posts/{id}` | post | 帖子详情，返回服务端渲染好的 `contentHtml` | — | `PostDetailVo` | 无（公开） | 公开 |
| GET | `/api/v1/posts/{postId}/replies?page=&size=` | reply | 楼层列表，按 `floor_no ASC` | — | `PageVo<ReplyVo>` | 无（公开） | 公开 |
| POST | `/api/v1/posts/{postId}/replies` | reply | 回帖（MVP 范围：平铺楼层，不含引用回复） | `PublishReplyCommand` | `PublishedReply` | **`security: bearerAuth`** | Controller 内手工判空 → `401 / 4001`（§4.5 ⑧ 实测） |

> ⚠️ **路径参数名不一致（真实存在）**：详情是 `/posts/{id}`（`PostController`），回复列表与回帖是 `/posts/{postId}/replies`（`ReplyController` 类级 `@RequestMapping`）。两者指同一个帖子 id，仅是**两个 Controller 各自的参数命名不同**，快照如实呈现。前端调用不受影响（URL 模板里只是占位符），但**读快照时不要以为是两个不同的资源**。属文档-代码细节差异，登记在 [增量设计](../../development/sprint-2-design.md) §8（P-2）。
>
> ⚠️ **`accepted` 而非 `isAccepted`**：[增量设计](../../development/sprint-2-design.md) §3.4 写的字段名是 `isAccepted`，**落码实际为 `accepted`**（Java record 组件名 `accepted` → Jackson 序列化即 `accepted`，无 `is` 前缀）。**以快照为准**，前端已按 `accepted` 对接；设计文档已同步修正（[增量设计](../../development/sprint-2-design.md) §8 的 P-1）。

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

> ⚠️ **N-4：声明了鉴权 ≠ 运行时强制了鉴权**。快照里有 `components.securitySchemes.bearerAuth`，**4 个**受保护操作带 `security`——但 `config/SecurityConfig` 仍是 `anyRequest().permitAll()`（带 `TODO(Sprint 2+)`），**Spring Security 不拦任何请求**。上表第 8 列的强制逻辑是 Controller 里手写的判空与角色校验，即：契约声明的鉴权与实际的鉴权由**两套互不校验的机制**分别维护，漏写一处不会被任何编译或 CI 环节发现。这一敞口**仍未修**，处置归口设计门 **A3-9（架构守护测试）**——它与 [追认评审](../../reviews/retro-review-design-v03-v04.md) **F-2「后端无架构守护测试」**是同一个根因（规则靠人工自查，无机器强制）。快照里 `bearerAuth` 的 description 已就地写明此事实，使契约本身不误导读者。
>
> **Sprint 2 的两个新受保护端点已按 §4.2 手工补齐防线**（这是本 Sprint 能在 N-4 敞口下交付的唯一依据，逐条可核）：① 两个 Controller 各写了一段显式判空（`PostController` / `ReplyController`，见 §4.5 ⑦⑧ 的真机 401 实测）；② 各配一个「匿名调用 → 401 / 4001」单测（`PostControllerAuthTest` / `ReplyControllerAuthTest`，各 3 个用例）；③ 公开端点另有 `ForumPublicReadTest` 3 个用例确认匿名可读。**这三道防线是人工加的，不是流程强制的**——A3-9 落地前，下一个上下文的受保护端点仍会静默敞开。
>
> 另需注意：**路径级错误无法按端点声明**。`405`（`1002` 方法不允许）、`404`（`1004` 无此路由）、`415`（`1003` 媒体类型不支持）由 `GlobalExceptionHandler` 统一处理，springdoc 的 `OperationCustomizer` 只能改已注册的操作，看不到"不存在的路径"，故这三类响应在快照里**完全不出现**（详见 §6）。

## 4. 主链路请求 / 响应结构（A3-4 备选项②）

以下响应体的来源分五档，逐条标注：**【实测】**= 2026-09-11 对 `68d40ae` 运行实例抓取（**均为成功响应**）；**【CR-016 实测】**= 取自 [CR-016](../../change-log.md#cr-016) 当时记录的实测结果，本次未重跑；**【CR-021 实测】**= 2026-09-12 对**修复后**的运行实例抓取，集中在 §4.4 的错误响应；**【Sprint 2 实测】**= 2026-09-12 对含 `module/forum` 的工作区运行实例抓取，集中在 §4.5；**【推演】**= 按快照 schema 与 `common/result/ResultCode` 的 httpStatus 映射写出，未实测。一次性凭据（票据 / JWT）已脱敏。dev 固定验证码与测试名册见 `AGENTS.md`，本文不重复登记凭据。

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
| `studentId` | `pattern: \d{9}`、`minLength: 1`、必填 | Jakarta Validation（[CR-013](../../change-log.md#cr-013)） |
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

链路：**版块列表 → 帖子列表 → 发帖 → 详情 → 楼层列表 → 回帖**。以下 `traceId` 均为真实抓取值（非凭据）。发帖 / 回帖的成功响应由 `admin@campuslink.local` 的 JWT 调用取得（[CR-013](../../change-log.md#cr-013) 的 dev-only 管理员账号）。

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

**⑦⑧ 两个受保护端点的匿名调用（N-4 下的人工防线，§3 有逐条交代）**

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

> 🔍 **一个必须知道的行为顺序（实测发现）**：**参数校验先于登录态校验**。Spring 在进入 Controller 方法体前就完成 `@Valid` 校验，而 N-4 下的鉴权判断写在方法体**内部**——因此**匿名 + 非法 body 的请求会先返回 `400 / 1001`，而不是 `401 / 4001`**。实测：匿名 `POST /posts` 且 body 为 `{}` → `400 / 1001`「boardCode 版块不能为空」；换成合法 body 才走到 `401 / 4001`（即 ⑦⑧）。前端据此不能把"400"当作"已登录"的证据，**判断登录态仍须看 401 或本地 token**。该顺序是框架行为，不是设计选择，故**未做调整**，仅在此如实登记。

## 5. 与技术方案 §5 接口表的逐条差异

| 类别 | 端点 | 处置 |
|------|------|------|
| **§5 已列且已实现（账号 5 行）** | `POST /auth/verify-student`、`POST /auth/captcha`、`POST /auth/register`、`POST /auth/login`、`POST /admin/roster/import` | 无需动作 |
| **§5 未列、已补入并实现** | `GET /api/v1/users/me`（[CR-019](../../change-log.md#cr-019) 补入）、`GET /api/v1/boards`、`GET /api/v1/posts/{postId}/replies`（[CR-022](../../change-log.md#cr-022) 的 MVP 范围新增，**§5 原表没有这两行**） | ✅ 技术方案 §5 已同步补入这两行 |
| **本次由"计划项"转为"已实现"（MVP 子集）** | `GET /posts`（**仅全站最新 / 版块内时间序，无 `sort=hot`**）、`POST /posts`（**无标签**）、`GET /posts/{id}`（**无 DELETE**）、`POST /posts/{id}/replies`（**无 `quotedReplyId`**） | 技术方案 §5 已标注"已实现（MVP 子集）"与未实现部分 |
| **§5 已列但仍未实现**（后续 Sprint） | `GET /boards/{code}/posts`（**已由 `GET /posts?boardCode=` 替代**，不是缺口）、`sort=hot` 与 `hot_score`（ADR-006 定时任务）、`DELETE /posts/{id}`、`PUT /posts/{id}/accepted-reply`、`POST\|DELETE /posts/{id}/like`·`/favorite`、`GET\|PUT /notifications`、`GET /search`、`POST /reports`、`/admin/...` 处置台与工单 | 契约尚未设计；各 Sprint 开工前补 §5 并在实现后重生成快照（对应 [sprint-2.md](../../development/sprint-2.md) §1 的 OUT 清单） |
| **§5 有约定但快照无对应表达** | 时间 ISO 8601 UTC、错误码**分段规则**（分页口径已随本次同步为 `page/size`，不再是差异） | **错误码自 [CR-021](../../change-log.md#cr-021) 起部分可见**：具体码与提示语出现在各响应的 description 里（§3 的非 200 表），但"1xxx 通用 / 2xxx 账号 / 21xx 学籍 …"这一**分段规则**OpenAPI 表达不了，仍须读技术方案 §5 |

## 6. 快照的已知缺口与表达极限（读快照前必须知道）

### 6.1 已由 [CR-021](../../change-log.md#cr-021) 处置的三项缺口（2026-09-12）

| # | 原缺口（[CR-019](../../change-log.md#cr-019) 归档时发现） | 修复方式 | 复测结论 |
|---|--------------------------------------------|---------|---------|
| **N-1** | **错误响应完全缺失**：6 个端点只声明 `200`。springdoc 仅按 Controller 返回类型生成，而实际错误经 `GlobalExceptionHandler` 按 `ResultCode.getHttpStatus()` 返回真实状态码，契约里完全看不到 | 新增项目自有注解 `common/result/ErrorCodes`，端点用它声明会抛的业务码；新增 `config/OpenApiErrorResponseCustomizer`（同时实现 `OperationCustomizer` 与 `OpenApiCustomizer`）统一生成 4xx / 5xx 响应，并把错误体 schema `ApiError` 注册进 `components`。**状态码与提示语一律取自 `ResultCode`，定制器不手写任何字面量** | ✅ 快照现有 `ApiError` schema，6 端点全部声明非 200 响应（逐条见 §3 第二张表）。**未采用**原计划的 swagger `@ApiResponses`——那要在每个端点重复书写 `ResultCode` 已有的状态码与提示语，同一事实存两份，改错误码必漂移 |
| **N-2** | **未声明鉴权**：`OpenApiConfig` 只设 `info`，无 `securitySchemes` → 快照无 bearerAuth、Swagger UI 无 Authorize 按钮、需登录的 `GET /users/me` 看起来是公开接口 | `OpenApiConfig` 补 `components.securitySchemes.bearerAuth`（HTTP bearer / JWT）；受保护端点加 `@SecurityRequirement`，常量集中在 `common/web/ApiDocs` | ✅ 快照有 `securitySchemes: [bearerAuth]`，`users/me` 与 `admin/roster/import` 两个操作带 `security`，Swagger UI 出现 Authorize 按钮。**但见 N-4：声明 ≠ 强制** |
| **N-3** | **不可解析的请求体返回 500 / 9999（缺陷，[手册](../../process-handbook.md) 4.3 定级 P2）**：`HttpMessageNotReadableException` 无专用处理器，落到兜底 `@ExceptionHandler(Exception.class)`，既报成服务端错误、又 `log.error` 打全栈 | 重写 `GlobalExceptionHandler`：新增 **6 个**框架异常处理器（覆盖 **8 类**异常：校验失败、不可解析请求体、缺参与类型不匹配共用一个、方法不支持、媒体类型不支持、无路由含 `NoResourceFound` 与 `NoHandlerFound` 两类），返回类型改 `ApiError`，并按"客户端错误 WARN 不打栈、业务错误不记日志、未知异常才 ERROR 打全栈"分档 | ✅ **范围比原记录更大**：改前不止畸形 JSON，共 **5 类**客户端错误都落兜底（畸形/缺失请求体 → `400/1001`、`text/plain` → `415/1003`、错方法 → `405/1002` 且带 `Allow`、无路由 → `404/1004`）。10 条探针全部符合预期，整轮复测后端日志 **ERROR 行数 = 0**、`unhandled exception` 出现 **0 次**（逐条实测响应见 §4.4） |

### 6.2 仍开放的问题（本次**未修**）

| # | 问题 | 事实 | 处置归口 |
|---|------|------|---------|
| **N-4** | 🔴 **契约声明了鉴权，运行时并未按该声明强制** | `config/SecurityConfig` 仍是 `anyRequest().permitAll()`（带 `TODO(Sprint 2+)`），Spring Security 不拦任何请求；实际强制是 `JwtAuthenticationFilter` 填 `Authentication` + **各 Controller 手写判空/角色校验**（§3 第 8 列）。契约与强制由两套互不校验的机制维护，**新端点漏写校验会静默成为公开接口，而快照上的 `security` 反而制造"已受保护"的错觉**。**Sprint 2 已按人工防线处置两个新受保护端点**（§3 的 N-4 说明段：显式判空 + 各 1 个 401 单测 + 真机 401 实测），**但这是人工加的，机制本身仍未闭环** | 并入设计门 **A3-9（架构守护测试）**——与[追认评审](../../reviews/retro-review-design-v03-v04.md) **F-2「后端无架构守护测试」**同根因（规则靠人工自查、无机器强制）。快照里 `bearerAuth` 的 description 已就地写明此事实，使契约本身不误导 |
| **N-5** | 技术方案 §5 的鉴权表述与实现机制不符 | §5 写"未登录可读、写操作 401"，而实现的登录态由各 Controller 自判、`SecurityConfig` 的 TODO 注明 Sprint 2+ 才收紧。**Sprint 2 的两个新受保护端点沿用了同一机制**（手写判空），该表述与实现的差距**又扩大了一次**（受保护端点由 2 个增至 4 个） | 并入 **A3-10**（同批处理 [追认评审](../../reviews/retro-review-design-v03-v04.md) F-1 / F-3 / F-4）。本次**未改 §5 表述**，只补了 1xxx 错误码清单与错误响应约定 |
| **N-6** | **名册 CSV 导入的表头识别只认中文（缺陷，P3）** | `RosterImportApplicationService` 仅当首行 `cols[0]` 含"学号"时才跳过，而 Javadoc 写的是"首行表头自动跳过"。实测用英文表头 `studentId,name` 导入，**表头行被当成一条学籍记录入库**（`name='name'`、`student_id_hash` = 字面量 `studentId` 的哈希），返回 `inserted: 2` 而非 1；导入时也**不校验学号格式**（9 位数字规则只在核验入口生效）。**危害有限**：垃圾行无法被用来注册，`verify-student` 的 `^\d{9}$` 会先挡掉；但污染名册数据、`inserted` 计数失真 | **本次不修**——不属 N-1/N-2/N-3 范围，且按"先登记后实施"。建议并入 A3-10 那一批或单独开 CR。与快照无关，登记于此只因它是本次复测发现的 |

### 6.3 OpenAPI 的固有表达极限（非缺陷，快照里**永远**看不到）

| 内容 | 为什么表达不了 | 读者该看哪里 |
|------|--------------|------------|
| 错误码语义 | `2101` 三态同码同提示的**防名册枚举**约定（PRD F-ACC-004）在 OpenAPI 里没有对应结构，只体现在 §4.1 的实测响应里 | [技术方案](../tech-design.md) §5 与 [PRD](../../requirements/prd.md) |
| 统一响应外壳是逐端点展开的 | 快照里是 `ApiResponseAuthResponse`、`ApiResponseUserVo` 等 **11 个具体类型**（Sprint 2 新增 6 个），没有 `ApiResponse<T>` 泛型表达；改外壳字段会同时改动这 11 个 schema | 读 diff 时注意"一处改动、多处变化" |
| **`405`（`1002` 方法不允许）与 `404`（`1004` 无此路由）** | `OperationCustomizer` 只能修改**已注册的 operation**，而"不存在的路径"根本没有 operation，"不允许的方法"也无从挂在某个 operation 上 | §4.4 的 ④⑤ 实测响应 + `GlobalExceptionHandler` |
| **`415`（`1003` 媒体类型不支持）** | springdoc 按 Controller 的 `consumes` 生成请求体媒体类型，不生成 415 响应分支 | §4.4 的 ③ 实测响应 |

> **当前状态**：N-1 / N-2 / N-3 **已闭环**（[CR-021](../../change-log.md#cr-021)，真机复测通过）。**但 N-4 未闭环，且它比 N-1/N-2 更危险**：N-1/N-2 是"契约少说了"，读者会去查代码；N-4 是"契约说对了、代码没做到"，读者会信契约。**Sprint 2 交出的答卷是"用人工防线顶住"**——两个新受保护端点各写了手写判空、各配 1 个 401 单测、真机验过 401（§3 / §4.5 ⑦⑧），所以**本轮快照里的 4 个 `security` 声明与运行时行为是一致的**；但一致性由人工维持，**每加一个受保护端点就要再确认一次**——**A3-9 落地前没有任何机器手段能替你做这件事**。

## 7. 变更记录

- **v1.3（2026-09-12）**——配合 [CR-022](../../change-log.md#cr-022) 的 Sprint 2 MVP（6 个论坛端点）**再生成快照**（**12 端点 / 30 schema**；18185 → 36233 字节），并同步本文：**§1** 覆盖范围由"6 个"改为"**12 个**（账号 6 + 论坛 6）"，受保护操作由 2 个改为 **4 个**；**§2** 换成本次的来源与规模，**如实标注代码来源仍是未提交的工作区**（与 v1.1 的差别：v1.1 事后补齐了"对应 `63ed21c`"，本次**无法**声称对应任何提交，只有"快照落盘时刻晚于 `backend/src/main` 全部 mtime"的时刻核验），新增触发点⑦与"① 是高频触发点"的提示；**§3** 拆为 3.1 账号（原表原样保留）/ **3.2 论坛**两张表，非 200 响应表新增 6 行，N-4 说明段补"Sprint 2 的三个新防线"与两条**文档-代码差异**（`{postId}` vs `{id}` 路径参数名、`accepted` vs `isAccepted`）；**§4** 证据分档增【Sprint 2 实测】一档，新增 **§4.5 论坛主链路**（11 条实测响应：6 条成功 + 5 条错误，含两个受保护端点的 401）+ 一条实测发现的**行为顺序**（参数校验先于登录态校验，故"匿名 + 非法 body"返回 400 而非 401）；**§5** 差异表重写为五类（已实现 / 已补入 / 计划项转已实现 / 仍未实现 / 无对应表达）；**§6.2** N-4 补"人工处置但机制未闭环"、N-5 补"差距又扩大一次"；**§6.3** `ApiResponse*` 具体类型 5 → **11**。**遗留**：N-4（→ A3-9，**A3-9 已被推迟至 MVP 验收后**）、N-5（→ A3-10）、N-6（P3，未修）；快照的"提交后核对"须在本轮代码提交后按 §2 方法补做；`.openapi` 的 `info.version` 仍为 `0.1.0`、**未随 Sprint 递增**，本轮未改（是否引入版本策略归 A3-8 / 部署形态讨论）。
- **v1.2（2026-09-12）**——**补记提交凭证，无内容变更**：把"代码来源为未提交的工作区"改为**来源即提交 `63ed21c`**（CR-020 与 CR-021 合并为一次提交）。依据为**文件时间戳核验**：本快照导出（01:24:21）之后 `backend/src/main` **零改动**，唯一变动是 01:29 新增的测试类 `AdminRosterControllerAuthTest`，而快照只由生产代码与注解生成、测试类不影响其内容，故 §2 原要求的"提交后重抓一次比对"**已由该证据解除、无需执行**。快照文件本身与 §1 / §3 / §4 / §6 的实测内容**一字未改**。
- **v1.1（2026-09-12）**——配合 [CR-021](../../change-log.md#cr-021) 修复 N-1 / N-2 / N-3 后**再生成快照**（6 端点 / **14** schema，新增 `ApiError`；9108 → 17545 字节），并同步本文：**§1** 增"快照现在也表达错误响应与鉴权，但声明 ≠ 强制"一条；**§2** 换成本次的来源与规模，并**如实标注代码来源是未提交的工作区**（v1.0 能声称对应提交 `68d40ae`，本次不能），补 diff 纯新增性的脚本比对结论与两条新触发点（改 `ResultCode`、`SecurityConfig` 收紧后）；**§3** 鉴权列拆为"快照声明"与"运行时强制方式"两列，新增各端点非 200 响应清单表，并把原"鉴权为什么不在快照里"整段（已过期）改写为 **N-4**；**§4** 证据分档增【CR-021 实测】一档，新增 **§4.4 错误响应实例**（7 条实测响应体 + 与快照的对应关系 + 日志档位）；**§6** 重构为三小节：6.1 已修的三项（含修复方式与复测结论）、6.2 仍开放的 **N-4 / N-5 / N-6**、6.3 OpenAPI 固有表达极限（新增 `405`/`404`/`415` 三行），删掉"N-1/N-2/N-3 均未处置"的过期结论。**遗留**：N-4（→ A3-9）、N-5（→ A3-10）、N-6（P3，未修）；本 CR 代码已于 2026-09-12 提交为 `63ed21c`，且经文件时间戳核验快照与该提交一致（`backend/src/main` 在导出后零改动），**原要求的"提交后重抓一次"已解除**；本次**未在浏览器目视 UI 回归**（改经 Vite 代理实测 + 审阅 `client.ts`，详见 [CR-021](../../change-log.md#cr-021) 复测结果⑦）。
- v1.0（2026-09-11）——创建。归档 `openapi.json` 快照（提交 `68d40ae`，6 端点 / 13 schema），补 2 条主链路的实测请求响应结构、与技术方案 §5 的逐条差异、快照未表达的 5 项内容（含新发现缺陷 N-3）。闭环设计门 **A3-4**，登记为 [CR-019](../../change-log.md#cr-019)。**设计门出口标准第 2 项仍为 ❌**（A3-5 ER 图与数据量级预估未做）。
