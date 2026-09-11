# OpenAPI 快照说明（`docs/design/api/`）

| 文档信息 | 内容 |
|---------|------|
| 版本 | v1.0 |
| 状态 | 已归档（**快照**，接口契约变更后须再生成，见 §2） |
| 维护人 | 技术负责人（发起人兼任） |
| 最后更新 | 2026-09-11 |
| 对应行动项 | [设计门纪要](../../reviews/gate-3-design.md) **A3-4**：补归档 OpenAPI 快照，或补 2~3 个主链路接口请求/响应结构（**两个备选项本文都做**） |
| 变更登记 | [CR-019](../../change-log.md#cr-019) |
| 关联记录 | [技术方案](../tech-design.md) §5 接口契约 · [PRD](../../requirements/prd.md) F-ACC-004 · [Sprint 1 计划](../../development/sprint-1.md) |

## 1. 这份文件是什么、不是什么

- **是**：`openapi.json` —— 由 springdoc 从**运行中的后端**导出的一次**冻结快照**，用于设计门归档与"文档 vs 实现"的差异比对；
- **不是**手写的接口规范：契约的权威来源永远是运行中的服务（`GET /api/docs`，Swagger UI 在 `/api/docs/swagger-ui.html`）。本文与快照一旦落后于代码，以代码为准并**立即再生成**；
- **覆盖范围仅 Sprint 1 已实现的 6 个端点**，不得读成"全部接口契约已归档"——[技术方案](../tech-design.md) §5 接口表（本次补入 `users/me` 后 **16 行**）中还有 **11 行**属 Sprint 2+ 计划项（逐条差异见 §5）。因此技术方案文末检查清单第 3 项"接口契约**完整**"**本次仍不勾选**；
- **不留多份历史副本**：本目录只有一个当前快照，历史版本由 git 承担（[CR-008](../../change-log.md#cr-008) 的教训——不该用 zip / 副本替代 git 历史）。

## 2. 快照来源与再生成

| 项 | 值 |
|---|---|
| 生成时间 | 2026-09-11 |
| 代码提交 | `68d40ae`（生成时 `backend/`、`frontend/` 工作区干净，故快照对应该提交） |
| 后端地址 | `http://localhost:8088`（开发端口，[CR-012](../../change-log.md#cr-012)）——快照 `servers` 字段即此值，**部署形态（ADR-011）确定后须重生成** |
| 生成方 | springdoc-openapi 3.1.0（`springdoc-openapi-starter-webmvc-ui`） |
| 规范版本 | OpenAPI **3.1.0**；`info.title` = `Campus-Link API`，`info.version` = `0.1.0` |
| 规模 | 3 个 tag / **6 个端点** / **13 个 schema** |
| 落盘处理 | 原始响应 5624 字节单行压缩 → 按 2 空格缩进格式化（10876 字节）。**键序保持 springdoc 原序，未排序、未删改任何字段** |
| 可复现性 | **已实测**：连续两次 `curl` 的响应字节完全一致，故格式化后的 `git diff` 可作为契约变更的可靠信号 |

**再生成命令**（在仓库根目录执行；先按 `AGENTS.md`"常用命令"起栈：本机 MySQL / Redis + `mvn spring-boot:run`）：

```bash
curl -s http://localhost:8088/api/docs -o /tmp/openapi.json
python -c "import json;d=json.load(open('/tmp/openapi.json',encoding='utf-8'));open('docs/design/api/openapi.json','w',encoding='utf-8',newline='\n').write(json.dumps(d,ensure_ascii=False,indent=2)+'\n')"
git diff --stat docs/design/api/openapi.json   # 非空即契约有变更，须同步技术方案 §5
```

**再生成触发点**（已写入 `AGENTS.md`）：① 新增 / 修改端点或请求响应模型后；② 每个 Sprint 收尾；③ 部署形态确定后（`servers` 会变）；④ N-1 / N-2 修复后（见 §6）。

## 3. 端点清单（快照实际覆盖的 6 个）

| 方法 | 路径 | tag | 摘要 | 请求体 | `data` 类型 | **实际鉴权**（快照未表达，见 N-2） |
|------|------|-----|------|--------|------------|-----------------------------------|
| POST | `/api/v1/auth/verify-student` | auth | 学籍核验（F-ACC-004），通过返回一次性票据 | `VerifyStudentCommand` | `VerifyStudentResult` | 公开（另有 IP 限流） |
| POST | `/api/v1/auth/captcha` | auth | 发送邮箱验证码（60s 重发间隔，单账号日上限 10 条） | `CaptchaCommand` | 无（`data` 省略） | 公开（另有 IP / 账号限流） |
| POST | `/api/v1/auth/register` | auth | 注册，需先通过学籍核验并携带票据 | `RegisterCommand` | `AuthResponse` | 公开 |
| POST | `/api/v1/auth/login` | auth | 登录：邮箱 + 验证码 | `LoginCommand` | `AuthResponse` | 公开 |
| GET | `/api/v1/users/me` | user | 我的主页（F-ACC-002 最小版） | — | `UserVo` | **需登录**（无 JWT → `401 / 4001`） |
| POST | `/api/v1/admin/roster/import?batch=` | admin-roster | 学籍名册 CSV 导入，body 为 CSV 文本 | `string`（CSV） | `RosterImportResult` | **需 `ROLE_SUPERADMIN`** |

> **鉴权为什么不在快照里**：`config/SecurityConfig` 为 `anyRequest().permitAll()`，授权**不由 Spring Security 规则表达**，而是 `security/JwtAuthenticationFilter` 填充 `Authentication` + Controller 内手工校验（`UserController.me` 判空抛 `4001`、`AdminRosterController.requireSuperadmin` 校验角色）。因此"哪些端点需登录"只能看上表，无法从快照推导。

## 4. 主链路请求 / 响应结构（A3-4 备选项②）

以下响应体的来源分三档，逐条标注：**【实测】**= 2026-09-11 对 `68d40ae` 运行实例抓取；**【CR-016 实测】**= 取自 [CR-016](../../change-log.md#cr-016) 当时记录的实测结果，本次未重跑；**【推演】**= 按快照 schema 与 `common/result/ResultCode` 的 httpStatus 映射写出，本次未实测。一次性凭据（票据 / JWT）已脱敏。dev 固定验证码与测试名册见 `AGENTS.md`，本文不重复登记凭据。

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

## 5. 与技术方案 §5 接口表的逐条差异

| 类别 | 端点 | 处置 |
|------|------|------|
| **§5 已列且已实现（一致）** | `POST /auth/verify-student`、`POST /auth/captcha`、`POST /auth/register`、`POST /auth/login`、`POST /admin/roster/import` | 无需动作 |
| **原 §5 未列、本次已补入** | `GET /api/v1/users/me`（F-ACC-002 最小版，需登录） | ✅ **已于本次补入技术方案 §5 接口表**（[CR-019](../../change-log.md#cr-019)）——发现即修，不留已知文档漏项 |
| **§5 已列但未实现**（Sprint 2+ 计划项，共 11 行） | `GET /boards/{code}/posts`、`GET /posts?sort=`、`POST /posts`、`GET\|DELETE /posts/{id}`、`POST /posts/{id}/replies`、`PUT /posts/{id}/accepted-reply`、`POST\|DELETE /posts/{id}/like`·`/favorite`、`GET\|PUT /notifications`、`GET /search`、`POST /reports`、`/admin/...` 处置台与工单 | 契约尚未设计；各 Sprint 开工前补 §5 并在实现后重生成快照 |
| **§5 有约定但快照无对应表达** | 分页 `?pageNum=&pageSize=`（默认 20 / 最大 100，返回 `{list,total,pageNum,pageSize}`）、时间 ISO 8601 UTC、错误码分段 | Sprint 1 端点均无分页，故快照里看不到分页模型；后续实现时须在快照中体现 |

## 6. 快照**未表达**的内容（读快照前必须知道的 5 件事）

| # | 缺口 | 事实与复现 | 处置 |
|---|------|-----------|------|
| **N-1** | **错误响应完全缺失** | 快照中 6 个端点**只声明 `200`**。springdoc 仅按 Controller 返回类型生成，而实际错误经 `common/exception/GlobalExceptionHandler` 按 `ResultCode.getHttpStatus()` 返回真实状态码：`1001`/`2101` → **400**、`2004` → **409**、`4001` → **401**、`9999` → **500**。前端无法据快照处理非 2xx 分支 | 未修。需在 Controller 补 `@ApiResponse` 声明或全局 `OperationCustomizer`，**涉及改代码 → 另开 CR** |
| **N-2** | **未声明鉴权** | `config/OpenApiConfig` 只设置了 `info`，无 `components.securitySchemes` → 快照无 bearerAuth、Swagger UI 无 Authorize 按钮、需登录的 `GET /users/me` 在快照中看起来无需鉴权（实际 401，见 §4.2） | 未修。同上，**另开 CR** |
| **N-3** | **不可解析的请求体返回 500 / 9999（缺陷，P2）** | 实测：`curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8088/api/v1/auth/verify-student -H "Content-Type: application/json" -d '{"studentId":}'` → **HTTP 500**、`{"code":9999,"message":"系统繁忙，请稍后再试"}`。原因：`HttpMessageNotReadableException` 无专用处理器，落到兜底 `@ExceptionHandler(Exception.class)`。应为 **400 / 1001**（技术方案 §5"入参格式违规 → `1001` + 字段级提示"）。**危害**：客户端错误被报成服务端错误，污染 5xx 监控与错误日志（兜底分支会 `log.error` 打全栈），且用户看到"系统繁忙"这一误导提示 | **未修**。按[流程手册](../../process-handbook.md) 4.3 定级 **P2（一般：次要功能异常，影响体验；排期修复，遗留须评审接受）**——不影响主链路（正常客户端不触发），但违反已归档的接口约定。修复量小（新增一个 `@ExceptionHandler`），**建议在 Sprint 2 开工前与 N-1 / N-2 一并处理，另开 CR** |
| — | 错误码语义不在快照 | `2101` 三态同码同提示的**防名册枚举**约定（PRD F-ACC-004）无法从 OpenAPI 表达，只体现在 §4.1 的实测响应里 | 以[技术方案](../tech-design.md) §5 与 PRD 为准 |
| — | 统一响应外壳是逐端点展开的 | 快照里是 `ApiResponseAuthResponse`、`ApiResponseUserVo` 等 5 个**具体类型**，没有统一的 `ApiResponse<T>` 泛型表达；改外壳字段会同时改动这 5 个 schema | 读 diff 时注意"一处改动、多处变化" |

> **N-1 / N-2 / N-3 均未处置**，登记于 [CR-019](../../change-log.md#cr-019) 遗留敞口。若拖到 Sprint 2 之后，前端将按"只有 200、无需鉴权"的错误契约开发大量新端点。

## 7. 变更记录

- v1.0（2026-09-11）——创建。归档 `openapi.json` 快照（提交 `68d40ae`，6 端点 / 13 schema），补 2 条主链路的实测请求响应结构、与技术方案 §5 的逐条差异、快照未表达的 5 项内容（含新发现缺陷 N-3）。闭环设计门 **A3-4**，登记为 [CR-019](../../change-log.md#cr-019)。**设计门出口标准第 2 项仍为 ❌**（A3-5 ER 图与数据量级预估未做）。
