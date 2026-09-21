# CR-076 · token 过期统一退出（BUG-004，纯前端）

| 文档信息 | 内容 |
|---------|------|
| 版本 | v1.0 |
| 状态 | 已实施 |
| 维护人 | AI 会话（前端优化轮） |
| 最后更新 | 2026-09-21 |
| 关联 CR | [CR-076](../../变更日志/变更台账.md) |

## 1. 目标

修复 BUG-004（[产品优化与新增业务需求建议](../../需求/产品优化与新增业务需求建议.md) O1）：token 过期后前端仍表现为"已登录"，直到某个受保护请求返回 `401 / 4001` 才被动失败，且失败时只抛错、不清会话、不跳登录，用户卡在半登录态。目标是让过期会话在**启动 / 路由跳转 / 任意接口返回 401** 三处都被统一识别，自动清除本地会话并跳转登录页（携带回跳地址），跳转前给一次"登录已过期"提示。

## 2. 范围

- **做**：
  - `auth` store 落地并持久化后端已下发的 `expiresAtEpochSeconds`（新增 `cl_exp` localStorage 键 + `expiresAt` state），提供命令式 `hasExpired()` 判定；
  - store 初始化时若已过期则立即清本地会话（避免刷新后先渲染登录态再跳走的闪烁）；
  - 路由守卫在 `requiresAuth` 判定前命令式清除过期会话；
  - `client.ts` 新增 `setUnauthorizedHandler` 注册机制，收到 `code === 4001` 时触发（不直接 import router/store，规避 ESM 循环依赖）；
  - `main.ts` 在 pinia + router 安装后注册处理器：清会话 → `ElMessage` 提示 → 跳 `/login?redirect=<当前 fullPath>`，带去重 guard 防并发 401 多次跳转，且仅在本地存在 token 时触发（不劫持匿名浏览的公开接口 401）。
- **不做**：
  - 不做 token 静默刷新 / 续期（后端无 refresh 端点，属新功能，须另开 CR 走契约）；
  - 不做登录按钮防重复提交（`LoginView` 三按钮缺 `:loading`，违反 UI 规范 §5.3）——用户本轮未选，另议；
  - 不改任何后端代码、接口契约、数据库迁移；
  - 不改占位页视觉、不做移动端走查（用户本轮均未选）。

## 3. 改动点（文件级）

| 文件 | 动作 | 说明 |
|------|------|------|
| `frontend/src/stores/auth.ts` | 改 | 新增 `EXP_KEY='cl_exp'`、`expiresAt` state；`setSession` 增第三参 `expiresAtEpochSeconds` 并持久化；`login`/`register` 传入 `res.expiresAtEpochSeconds`；新增命令式 `hasExpired()`；`logout()` 一并清 `cl_exp`；state 初始化时过期即清本地会话 |
| `frontend/src/api/client.ts` | 改 | 新增模块级 `unauthorizedHandler` 与导出 `setUnauthorizedHandler(fn)`；`request` 内 `body.code === 4001` 时先触发 handler 再抛 `ApiError`（保持既有抛错语义不变） |
| `frontend/src/router/index.ts` | 改 | 守卫取到 `auth` 后、判 `isLoggedIn` 前，先 `if (auth.hasExpired()) auth.logout()` |
| `frontend/src/main.ts` | 改 | `use(createPinia()).use(router)` 之后调用 `setUnauthorizedHandler(...)` 注册统一退出逻辑（含去重 guard + redirect 回跳 + `ElMessage.warning`） |

## 4. 步骤

- [ ] 1. 改 `auth.ts`：落地 `expiresAt` 持久化 + `hasExpired()` + 初始化清过期会话
- [ ] 2. 改 `client.ts`：加 `setUnauthorizedHandler` 注册机制，`4001` 触发
- [ ] 3. 改 `router/index.ts`：守卫命令式清过期会话
- [ ] 4. 改 `main.ts`：注册 401 统一退出处理器（去重 + redirect + 提示）
- [ ] 5. `npm run type-check` + `npm run build` 全绿
- [ ] 6. 变更台账 §1 登记 CR-076 一行；问题清单 BUG-004 状态更新

## 5. 验证

- **单测**：前端无单测框架（既有约定），不新增；靠 type-check + build 兜底类型与编译。
- **type-check / build**：`npm run type-check` 与 `npm run build` 均应全绿、零新增告警。
- **真机（建议发起人复核）**：
  1. 登录后手动把 `localStorage.cl_exp` 改成过去的时间戳 → 刷新首页应仍是登录态显示（首页公开），但点进 `/settings` 等 `requiresAuth` 路由应被守卫清会话并跳登录页带 `redirect`；
  2. 登录后把 `cl_token` 改成非法串 → 触发任一受保护请求（如进通知中心）应弹"登录已过期"、清会话、跳登录页；
  3. 并发多个受保护请求同时 401 → 只跳一次、只提示一次（去重 guard）。
- **契约**：零改动（纯前端，不触碰 `openapi.json`）。
- **回归面**：
  - 匿名浏览公开页（首页 / 帖子详情 / 搜索）：handler 仅在本地有 token 时触发，公开接口的 401 不受影响；
  - 正常登录态：`expiresAt` 未过期时 `hasExpired()` 恒 false，守卫与请求链路行为不变；
  - `setSession` 新增第三参为可选语义（旧调用点全部同批更新），不影响资料编辑 `setUser` 路径。

## 6. 回滚

四个文件均为独立小改，`git revert` 本轮提交即可完全回退；无数据迁移、无配置项、无契约变更，回滚无需数据回补。`cl_exp` 为新增 localStorage 键，回滚后旧代码不读它、残留无害。

## 7. 实施结果（实施后追加，不回改上文）

按 §3 计划落地，四个文件全部改完、`npm run build` 两轮全绿（18.25s / 5.54s，零报错）。**与计划的偏差**：① 项目 `package.json` 无 `type-check` 脚本（只有 `dev`/`build`/`preview`，`vite build` 走 esbuild 不做类型检查），故 §5 的 `npm run type-check` 一项不适用，改以 `npm run build` + 产物核查兜底；② 已核对构建产物 `index-*.js`，`ElMessage.warning('登录已过期…')` 被解析为真实导入绑定（压缩后为局部变量调用，非遗留未定义全局），确认 unplugin-auto-import 在 `main.ts`（.ts 入口）同样生效。**实测证据**：`git diff` 四文件纯增量（`auth.ts +42 / client.ts +16 / router +4 / main.ts +26`），换行统一为 LF、UTF-8 无 BOM，与仓库既有约定一致。**遗留**：真机三场景（改 `cl_exp` 过去戳走守卫、改非法 `cl_token` 触发 401、并发 401 去重）待发起人浏览器复核；`LoginView` 三按钮缺 `:loading`（§2 明确不做，另议）。
