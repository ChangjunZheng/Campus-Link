# OpenAPI 快照说明（`docs/design/api/`）

| 文档信息 | 内容 |
|---------|------|
| 版本 | v1.2 |
| 状态 | 已归档（**快照**，接口契约变更后须再生成，见 §2） |
| 维护人 | 技术负责人（发起人兼任） |
| 最后更新 | 2026-09-12 |
| 对应行动项 | [设计门纪要](../../reviews/gate-3-design.md) **A3-4**：补归档 OpenAPI 快照，或补 2~3 个主链路接口请求/响应结构（**两个备选项本文都做**） |
| 变更登记 | [CR-019](../../change-log.md#cr-019)（首次归档）· [CR-021](../../change-log.md#cr-021)（修复 N-1 / N-2 / N-3 后**再生成**：契约新增错误响应与 bearerAuth 声明） |
| 关联记录 | [技术方案](../tech-design.md) §5 接口契约 · [PRD](../../requirements/prd.md) F-ACC-004 · [Sprint 1 计划](../../development/sprint-1.md) |

## 1. 这份文件是什么、不是什么

- **是**：`openapi.json` —— 由 springdoc 从**运行中的后端**导出的一次**冻结快照**，用于设计门归档与"文档 vs 实现"的差异比对；
- **不是**手写的接口规范：契约的权威来源永远是运行中的服务（`GET /api/docs`，Swagger UI 在 `/api/docs/swagger-ui.html`）。本文与快照一旦落后于代码，以代码为准并**立即再生成**；
- **覆盖范围仅 Sprint 1 已实现的 6 个端点**，不得读成"全部接口契约已归档"——[技术方案](../tech-design.md) §5 接口表（本次补入 `users/me` 后 **16 行**）中还有 **11 行**属 Sprint 2+ 计划项（逐条差异见 §5）。因此技术方案文末检查清单第 3 项"接口契约**完整**"**本次仍不勾选**；
- **自 [CR-021](../../change-log.md#cr-021) 起，快照也表达错误响应与鉴权**：每个操作都声明了 4xx / 5xx 及错误体 schema `ApiError`，`components.securitySchemes.bearerAuth` 已定义，2 个受保护操作带 `security`。**但"声明了鉴权"不等于"运行时强制了鉴权"**（`SecurityConfig` 仍是 `permitAll()`，见 §6 **N-4**），另有 4 类事实快照表达不了（§6）——读快照前必须先看 §6；
- **不留多份历史副本**：本目录只有一个当前快照，历史版本由 git 承担（[CR-008](../../change-log.md#cr-008) 的教训——不该用 zip / 副本替代 git 历史）。

## 2. 快照来源与再生成

| 项 | 值 |
|---|---|
| 首次归档 | 2026-09-11（[CR-019](../../change-log.md#cr-019)，源提交 `68d40ae`，6 端点 / 13 schema） |
| **本次生成时间** | **2026-09-12**（[CR-021](../../change-log.md#cr-021) 修复 N-1 / N-2 / N-3 后再生成） |
| **代码来源** | ✅ **现已核对为提交 `63ed21c`**（本快照生成时该代码尚未提交，2026-09-12 提交；核对依据与"为何无需重抓"见本行末）。生成时的基准：`HEAD` = `f19fc79`（纯文档提交，其 `backend/`、`frontend/` 与 `68d40ae` **逐字节相同**，已用 `git diff --stat 68d40ae f19fc79 -- backend frontend` 核实为空）+ CR-021 的 7 个后端文件改动与 4 个新增文件。**与 v1.0 的差别**：v1.0 能声称"快照对应某提交"，本次生成时不能（CR-021 的代码当时尚未提交），**但事后已核对为提交 `63ed21c`**——按文件时间戳核验，本快照导出（01:24:21）之后 `backend/src/main` **无任何文件被修改**，唯一变动是其后新增的测试类，而快照只由生产代码与注解生成，**故本快照确与 `63ed21c` 对应，无需重抓**（见 §2 末的可复现性结论） |
| 后端地址 | `http://localhost:8088`（开发端口，[CR-012](../../change-log.md#cr-012)）——快照 `servers` 字段即此值，**部署形态（ADR-011）确定后须重生成** |
| 生成方 | springdoc-openapi 3.1.0（`springdoc-openapi-starter-webmvc-ui`） |
| 规范版本 | OpenAPI **3.1.0**；`info.title` = `Campus-Link API`，`info.version` = `0.1.0` |
| 规模 | 3 个 tag / **6 个端点** / **14 个 schema**（新增 `ApiError`，v1.0 为 13 个） |
| 落盘处理 | 原始响应 **9108** 字节单行压缩 → 按 2 空格缩进格式化（**17545** 字节，v1.0 为 5624 → 10876）。**键序保持 springdoc 原序，未排序、未删改任何字段** |
| 可复现性 | **已重新实测**：连续两次 `curl` 的响应字节完全一致，故格式化后的 `git diff` 仍可作为契约变更的可靠信号 |
| 本次 diff 性质 | **纯新增**：已用脚本逐 schema 比对 `HEAD` 版本——13 个业务 schema 逐字节相同、6 个端点的 `200` 响应全部未变、`info` / `servers` / `paths` 键集合未变；`git diff --stat` = **224 行新增 / 2 行删除**（两处删除均为结构性的 `        }` 行，非字段移除） |

**再生成命令**（在仓库根目录执行；先按 `AGENTS.md`"常用命令"起栈：本机 MySQL / Redis + `mvn spring-boot:run`）：

```bash
curl -s http://localhost:8088/api/docs -o /tmp/openapi.json
python -c "import json;d=json.load(open('/tmp/openapi.json',encoding='utf-8'));open('docs/design/api/openapi.json','w',encoding='utf-8',newline='\n').write(json.dumps(d,ensure_ascii=False,indent=2)+'\n')"
git diff --stat docs/design/api/openapi.json   # 非空即契约有变更，须同步技术方案 §5
```

**再生成触发点**（已写入 `AGENTS.md`）：① 新增 / 修改端点或请求响应模型后；② 每个 Sprint 收尾；③ 部署形态确定后（`servers` 会变）；④ ~~N-1 / N-2 修复后~~ **已完成**（[CR-021](../../change-log.md#cr-021)，2026-09-12 再生成）；⑤ **新增**：改了 `common/result/ResultCode` 的错误码、提示语或 `getHttpStatus()` 映射后——契约里的状态码与 description 全部由它派生，改它即改契约；⑥ **新增**：`SecurityConfig` 从 `permitAll()` 改为真正按路径授权后（N-4 闭环，见 §6），届时须核对快照的 `security` 声明与运行时规则一致。

## 3. 端点清单（快照实际覆盖的 6 个）

| 方法 | 路径 | tag | 摘要 | 请求体 | `data` 类型 | **快照声明的鉴权** | **运行时实际强制方式**（见 N-4） |
|------|------|-----|------|--------|------------|------------------|--------------------------------|
| POST | `/api/v1/auth/verify-student` | auth | 学籍核验（F-ACC-004），通过返回一次性票据 | `VerifyStudentCommand` | `VerifyStudentResult` | 无（公开） | 公开（另有 IP 限流） |
| POST | `/api/v1/auth/captcha` | auth | 发送邮箱验证码（60s 重发间隔，单账号日上限 10 条） | `CaptchaCommand` | 无（`data` 省略） | 无（公开） | 公开（另有 IP / 账号限流） |
| POST | `/api/v1/auth/register` | auth | 注册，需先通过学籍核验并携带票据 | `RegisterCommand` | `AuthResponse` | 无（公开） | 公开 |
| POST | `/api/v1/auth/login` | auth | 登录：邮箱 + 验证码 | `LoginCommand` | `AuthResponse` | 无（公开） | 公开 |
| GET | `/api/v1/users/me` | user | 我的主页（F-ACC-002 最小版） | — | `UserVo` | **`security: bearerAuth`** | Controller 内手工判空 → `401 / 4001` |
| POST | `/api/v1/admin/roster/import?batch=` | admin-roster | 学籍名册 CSV 导入，body 为 CSV 文本 | `string`（CSV） | `RosterImportResult` | **`security: bearerAuth`** | `AdminRosterController.requireSuperadmin` 校验角色 → `401 / 4001` 或 `403 / 4002` |

**快照中各端点声明的非 200 响应**（description 为 `提示语（错误码）`，由 `ResultCode` 派生；下表为本次抓取的实际内容）：

| 端点 | 声明的状态码 | description（错误码） |
|------|------------|---------------------|
| `POST /auth/verify-student` | 400 / 429 / 500 | 400：`1001` + `2101`；429：`2103` |
| `POST /auth/captcha` | 400 / 429 / 500 | 400：`1001`；429：`2002` + `2003` |
| `POST /auth/register` | 400 / 409 / 500 | 400：`1001` + `2001` + `2102` + `2101`；409：`2004` |
| `POST /auth/login` | 400 / 403 / 500 | 400：`1001` + `2005`；403：`2006` |
| `GET /users/me` | 401 / 404 / 500 | 401：`4001`；404：`2007`（业务码，非路由级 `1004`） |
| `POST /admin/roster/import` | 400 / 401 / 403 / 500 | 400：`1001`；401：`4001`；403：`4002` |

> ⚠️ **N-4：声明了鉴权 ≠ 运行时强制了鉴权**。CR-021 之后快照里有 `components.securitySchemes.bearerAuth`，2 个受保护操作带 `security`——但 `config/SecurityConfig` 仍是 `anyRequest().permitAll()`（带 `TODO(Sprint 2+)`），**Spring Security 不拦任何请求**。上表第 8 列的强制逻辑是 Controller 里手写的判空与角色校验，即：契约声明的鉴权与实际的鉴权由**两套互不校验的机制**分别维护，漏写一处不会被任何测试或 CI 发现。这一敞口**本次未修**，处置归口设计门 **A3-9（架构守护测试）**——它与 [追认评审](../../reviews/retro-review-design-v03-v04.md) **F-2「后端无架构守护测试」**是同一个根因（规则靠人工自查，无机器强制）。快照里 `bearerAuth` 的 description 已就地写明此事实，使契约本身不误导读者。
>
> 另需注意：**路径级错误无法按端点声明**。`405`（`1002` 方法不允许）、`404`（`1004` 无此路由）、`415`（`1003` 媒体类型不支持）由 `GlobalExceptionHandler` 统一处理，springdoc 的 `OperationCustomizer` 只能改已注册的操作，看不到"不存在的路径"，故这三类响应在快照里**完全不出现**（详见 §6）。

## 4. 主链路请求 / 响应结构（A3-4 备选项②）

以下响应体的来源分四档，逐条标注：**【实测】**= 2026-09-11 对 `68d40ae` 运行实例抓取（**均为成功响应**）；**【CR-016 实测】**= 取自 [CR-016](../../change-log.md#cr-016) 当时记录的实测结果，本次未重跑；**【CR-021 实测】**= 2026-09-12 对**修复后**的运行实例抓取，集中在 §4.4 的错误响应；**【推演】**= 按快照 schema 与 `common/result/ResultCode` 的 httpStatus 映射写出，未实测。一次性凭据（票据 / JWT）已脱敏。dev 固定验证码与测试名册见 `AGENTS.md`，本文不重复登记凭据。

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

## 5. 与技术方案 §5 接口表的逐条差异

| 类别 | 端点 | 处置 |
|------|------|------|
| **§5 已列且已实现（一致）** | `POST /auth/verify-student`、`POST /auth/captcha`、`POST /auth/register`、`POST /auth/login`、`POST /admin/roster/import` | 无需动作 |
| **原 §5 未列、本次已补入** | `GET /api/v1/users/me`（F-ACC-002 最小版，需登录） | ✅ **已于本次补入技术方案 §5 接口表**（[CR-019](../../change-log.md#cr-019)）——发现即修，不留已知文档漏项 |
| **§5 已列但未实现**（Sprint 2+ 计划项，共 11 行） | `GET /boards/{code}/posts`、`GET /posts?sort=`、`POST /posts`、`GET\|DELETE /posts/{id}`、`POST /posts/{id}/replies`、`PUT /posts/{id}/accepted-reply`、`POST\|DELETE /posts/{id}/like`·`/favorite`、`GET\|PUT /notifications`、`GET /search`、`POST /reports`、`/admin/...` 处置台与工单 | 契约尚未设计；各 Sprint 开工前补 §5 并在实现后重生成快照 |
| **§5 有约定但快照无对应表达** | 分页 `?pageNum=&pageSize=`（默认 20 / 最大 100，返回 `{list,total,pageNum,pageSize}`）、时间 ISO 8601 UTC、错误码**分段规则** | Sprint 1 端点均无分页，故快照里看不到分页模型；后续实现时须在快照中体现。**错误码自 [CR-021](../../change-log.md#cr-021) 起部分可见**：具体码与提示语出现在各响应的 description 里（§3 第二张表），但"1xxx 通用 / 2xxx 账号 / 21xx 学籍 …"这一**分段规则**OpenAPI 表达不了，仍须读技术方案 §5 |

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
| **N-4** | 🔴 **契约声明了鉴权，运行时并未按该声明强制** | `config/SecurityConfig` 仍是 `anyRequest().permitAll()`（带 `TODO(Sprint 2+)`），Spring Security 不拦任何请求；实际强制是 `JwtAuthenticationFilter` 填 `Authentication` + **各 Controller 手写判空/角色校验**（§3 第 8 列）。契约与强制由两套互不校验的机制维护，**Sprint 2 新端点漏写校验会静默成为公开接口，而快照上的 `security` 反而制造"已受保护"的错觉** | 并入设计门 **A3-9（架构守护测试）**——与[追认评审](../../reviews/retro-review-design-v03-v04.md) **F-2「后端无架构守护测试」**同根因（规则靠人工自查、无机器强制）。快照里 `bearerAuth` 的 description 已就地写明此事实，使契约本身不误导 |
| **N-5** | 技术方案 §5 的鉴权表述与实现机制不符 | §5 写"未登录可读、写操作 401"，而实现的登录态由各 Controller 自判、`SecurityConfig` 的 TODO 注明 Sprint 2+ 才收紧 | 并入 **A3-10**（同批处理 [追认评审](../../reviews/retro-review-design-v03-v04.md) F-1 / F-3 / F-4）。本次**未改 §5 表述**，只补了 1xxx 错误码清单与错误响应约定 |
| **N-6** | **名册 CSV 导入的表头识别只认中文（缺陷，P3）** | `RosterImportApplicationService` 仅当首行 `cols[0]` 含"学号"时才跳过，而 Javadoc 写的是"首行表头自动跳过"。实测用英文表头 `studentId,name` 导入，**表头行被当成一条学籍记录入库**（`name='name'`、`student_id_hash` = 字面量 `studentId` 的哈希），返回 `inserted: 2` 而非 1；导入时也**不校验学号格式**（9 位数字规则只在核验入口生效）。**危害有限**：垃圾行无法被用来注册，`verify-student` 的 `^\d{9}$` 会先挡掉；但污染名册数据、`inserted` 计数失真 | **本次不修**——不属 N-1/N-2/N-3 范围，且按"先登记后实施"。建议并入 A3-10 那一批或单独开 CR。与快照无关，登记于此只因它是本次复测发现的 |

### 6.3 OpenAPI 的固有表达极限（非缺陷，快照里**永远**看不到）

| 内容 | 为什么表达不了 | 读者该看哪里 |
|------|--------------|------------|
| 错误码语义 | `2101` 三态同码同提示的**防名册枚举**约定（PRD F-ACC-004）在 OpenAPI 里没有对应结构，只体现在 §4.1 的实测响应里 | [技术方案](../tech-design.md) §5 与 [PRD](../../requirements/prd.md) |
| 统一响应外壳是逐端点展开的 | 快照里是 `ApiResponseAuthResponse`、`ApiResponseUserVo` 等 **5 个具体类型**，没有 `ApiResponse<T>` 泛型表达；改外壳字段会同时改动这 5 个 schema | 读 diff 时注意"一处改动、多处变化" |
| **`405`（`1002` 方法不允许）与 `404`（`1004` 无此路由）** | `OperationCustomizer` 只能修改**已注册的 operation**，而"不存在的路径"根本没有 operation，"不允许的方法"也无从挂在某个 operation 上 | §4.4 的 ④⑤ 实测响应 + `GlobalExceptionHandler` |
| **`415`（`1003` 媒体类型不支持）** | springdoc 按 Controller 的 `consumes` 生成请求体媒体类型，不生成 415 响应分支 | §4.4 的 ③ 实测响应 |

> **当前状态**：N-1 / N-2 / N-3 **已闭环**（[CR-021](../../change-log.md#cr-021)，2026-09-12 真机复测通过）。前端从此可以按快照的错误响应与鉴权声明开发。**但 N-4 未闭环，且它比 N-1/N-2 更危险**：N-1/N-2 是"契约少说了"，读者会去查代码；N-4 是"契约说对了、代码没做到"，读者会信契约。Sprint 2 每加一个受保护端点，都必须人工确认 Controller 里的鉴权校验真的写了——**A3-9 落地前没有任何机器手段能替你做这件事**。

## 7. 变更记录

- **v1.2（2026-09-12）**——**补记提交凭证，无内容变更**：把"代码来源为未提交的工作区"改为**来源即提交 `63ed21c`**（CR-020 与 CR-021 合并为一次提交）。依据为**文件时间戳核验**：本快照导出（01:24:21）之后 `backend/src/main` **零改动**，唯一变动是 01:29 新增的测试类 `AdminRosterControllerAuthTest`，而快照只由生产代码与注解生成、测试类不影响其内容，故 §2 原要求的"提交后重抓一次比对"**已由该证据解除、无需执行**。快照文件本身与 §1 / §3 / §4 / §6 的实测内容**一字未改**。
- **v1.1（2026-09-12）**——配合 [CR-021](../../change-log.md#cr-021) 修复 N-1 / N-2 / N-3 后**再生成快照**（6 端点 / **14** schema，新增 `ApiError`；9108 → 17545 字节），并同步本文：**§1** 增"快照现在也表达错误响应与鉴权，但声明 ≠ 强制"一条；**§2** 换成本次的来源与规模，并**如实标注代码来源是未提交的工作区**（v1.0 能声称对应提交 `68d40ae`，本次不能），补 diff 纯新增性的脚本比对结论与两条新触发点（改 `ResultCode`、`SecurityConfig` 收紧后）；**§3** 鉴权列拆为"快照声明"与"运行时强制方式"两列，新增各端点非 200 响应清单表，并把原"鉴权为什么不在快照里"整段（已过期）改写为 **N-4**；**§4** 证据分档增【CR-021 实测】一档，新增 **§4.4 错误响应实例**（7 条实测响应体 + 与快照的对应关系 + 日志档位）；**§6** 重构为三小节：6.1 已修的三项（含修复方式与复测结论）、6.2 仍开放的 **N-4 / N-5 / N-6**、6.3 OpenAPI 固有表达极限（新增 `405`/`404`/`415` 三行），删掉"N-1/N-2/N-3 均未处置"的过期结论。**遗留**：N-4（→ A3-9）、N-5（→ A3-10）、N-6（P3，未修）；本 CR 代码已于 2026-09-12 提交为 `63ed21c`，且经文件时间戳核验快照与该提交一致（`backend/src/main` 在导出后零改动），**原要求的"提交后重抓一次"已解除**；本次**未在浏览器目视 UI 回归**（改经 Vite 代理实测 + 审阅 `client.ts`，详见 [CR-021](../../change-log.md#cr-021) 复测结果⑦）。
- v1.0（2026-09-11）——创建。归档 `openapi.json` 快照（提交 `68d40ae`，6 端点 / 13 schema），补 2 条主链路的实测请求响应结构、与技术方案 §5 的逐条差异、快照未表达的 5 项内容（含新发现缺陷 N-3）。闭环设计门 **A3-4**，登记为 [CR-019](../../change-log.md#cr-019)。**设计门出口标准第 2 项仍为 ❌**（A3-5 ER 图与数据量级预估未做）。
