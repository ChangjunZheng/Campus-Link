# Campus-Link Sprint 2 计划（MVP · 论坛最小闭环）

| 文档信息 | 内容 |
|---------|------|
| 版本 | v1.2 |
| 状态 | **已交付**（M1~M6 全部完成；5 条验收标准**已实测通过**，逐条证据见 §5；**开工前置 B4 / B5 已由 [CR-022](../change-log.md) 推迟**，欠账未闭环） |
| 维护人 | 交付总监（AI 协作） |
| 关联阶段 | 开发（阶段四） |
| 上游依据 | [增量设计](sprint-2-design.md) · [next-steps](../next-steps.md) · [PRD](../requirements/prd.md) · [技术方案](../design/tech-design.md) |
| 最后更新 | 2026-09-12 |

> 版本号以 [docs/README.md](../README.md) 第 2 节为单一登记处，本文交叉引用不写版本号（手册 4.5）。

## 0. 本文为什么存在（先读这段）

发起人于 2026-09-12 决定**改按最小 MVP 原则推进**，并把原定为 Sprint 2 开工阻断项的 **A3-9（架构守护测试）/ A3-10（消除文档-代码不一致）推迟到 MVP 之后**。该决定登记为 [CR-022](../change-log.md)，偏离本身登记为 [W-07](../tailoring-waivers.md)。

**理由（如实记录，不含粉饰）**：截至本 Sprint 开工，项目已产出 25 份文档、21 条变更记录、11 项设计门行动项，而后端只有 `module/account` 一个上下文、前端 3 个真实页面 + 6 个占位页——**流程的复杂度已远超代码量，且治理工作不再带来可见的产品价值**。继续按原节奏补 A3-9 / A3-10 约需 1 天，但对"用户能否发帖"零贡献。

**代价（同样如实记录）**：推迟 A3-9 意味着四层依赖方向**仍无机器强制**，本 Sprint 新增的 `module/forum` 将照人工自查落地，若写错结构错误会被后续上下文复制（[W-02](../tailoring-waivers.md) 风险②的固有敞口，**不因推迟而消失**）；[W-02](../tailoring-waivers.md) 保持开放、不得关闭。

## 1. 目标与验收标准

**目标（一句话）**：**一个人能在版块里发帖，别人能在列表里看到它、点进详情、回帖，刷新后数据还在。**

**范围（IN）——4 项**

| 需求 | 裁剪后的范围 | 说明 |
|---|---|---|
| F-FORUM-001（部分） | 版块列表接口 + 版块页接真实数据 | 6 版块已由 `V2__seed_boards.sql` 种入，**无需新迁移** |
| F-FORUM-002（部分） | 发帖：标题 + Markdown 正文 | **不含标签**；`content_html` 按 ADR-005 在**发布时服务端渲染落库** |
| F-FORUM-003（部分） | 帖子列表：版块内 + 全站最新，分页 | **不含热门**（依赖 ADR-006 定时任务，不在本 Sprint） |
| F-FORUM-004（部分） | 帖子详情 + 楼层回复（平铺） | **不含引用回复**（`quoted_reply_id` 暂不使用） |

**明确不做（OUT）——保持占位页，不在本 Sprint 触碰**

标签（`tags` / `post_tags` 两表本 Sprint 不使用）· 点赞收藏（F-FORUM-005）· 采纳最佳答案（F-QA-001）· 编辑帖子（F-FORUM-007）· 删除帖子（F-FORUM-006）· 站内搜索（F-FORUM-008）· 通知中心（F-SOC-001）· 他人主页（F-ACC-002 剩余部分）· 管理后台（F-ADMIN-001~003）· 机审与举报（F-SAFE-001~003）· 账号注销（F-ACC-003）· 热榜定时任务（ADR-006）· UI 稿相关补偿措施（W-03）

> 上面这份"不做清单"和"做清单"**同等重要**——它挡住的是范围蔓延，不是偷工减料。

**验收标准（5 条，替代 Sprint 1 的 7 项出口自查）**——逐条证据见 **§5 验收记录**

- [x] A1 **发帖可用**：登录用户发帖后，帖子出现在对应版块列表首位
- [x] A2 **链路完整**：列表 → 详情 → 回帖，回帖后楼层数 +1 且刷新后仍在
- [x] A3 **权限正确**：未登录可看列表与详情；点"发帖"/"回帖"跳登录页，登录后回到原页
- [x] A4 **XSS 防线不破**：正文含 `<script>` / `onerror` 等载荷时，详情页不执行且内容正常显示（`MarkdownRendererTest` 全绿）
- [x] A5 **构建与真机**：`mvn verify` 全绿 + `npm run build` 通过 + **浏览器真机起栈实测**（不是只跑单测）

> 验收标准从 7 项压到 5 项，是有意为之：手册 3.4 的覆盖率门槛与静态扫描在单人项目下**当前无法度量**（见 [W-05](../tailoring-waivers.md)），继续列为"未满足"只会让每个 Sprint 都收不了尾。**本 Sprint 不因此声称提测准入门已通过。**

## 2. 页面与接口（最终契约以[增量设计](sprint-2-design.md)为准）

**页面**：`/board/:code` 接真实列表 · `/publish` 发帖（需登录）· `/post/:id` 详情 + 楼层 + 回帖框（回帖需登录）· `/` 全站最新

**接口（6 个，全部为新增）**——已核实现有后端只有 `module/account` 的 6 个账号端点（[api 快照](../design/api/README.md)），**版块与帖子相关端点一个都不存在**，故本 Sprint 的 6 个端点全部新写。
**契约细节（参数名、请求/响应形状、分页口径）以 [增量设计](sprint-2-design.md) §3 为准**，本表只列方法与鉴权。

| 方法 | 路径 | 鉴权 |
|---|---|---|
| GET | `/api/v1/boards` | 公开 |
| GET | `/api/v1/posts?boardCode=&page=&size=` | 公开 |
| POST | `/api/v1/posts` | 需登录 |
| GET | `/api/v1/posts/{id}` | 公开 |
| GET | `/api/v1/posts/{id}/replies?page=&size=` | 公开 |
| POST | `/api/v1/posts/{id}/replies` | 需登录 |

## 3. 任务清单

| # | 任务 | 产出 | 状态 |
|---|------|------|------|
| M1 | 增量设计（**含范围裁剪，原 M1 增量 PRD 已合并入内**——理由见 [sprint-2-design.md](sprint-2-design.md) §0） | `docs/development/sprint-2-design.md` | ✅ **已完成（已按 §8 记录 8 项落码偏差）** |
| M2 | `account` 侧只读方法（批量查昵称，跨上下文调用的唯一入口） | `AccountApplicationService#nicknamesOf` + 单测 | ✅ **已完成**（签名与增量设计的差异见 §8 P-8） |
| M3 | `module/forum` 四层骨架 + Mapper / DO / Converter / Repository | `module/forum/**` | ✅ **已完成**（§2.1 六条分层自检实测通过，证据见增量设计 §8） |
| M4 | 6 个端点（4 公开 + **2 个受保护，须手写鉴权 + 各配 1 个 401 单测**） | 3 个 Controller + Vo + application 服务 | ✅ **已完成**（`PostControllerAuthTest` / `ReplyControllerAuthTest` 各 3 例，真机 401 已实测） |
| M5 | 前端 3 页接真实数据 | 版块列表 / 发帖 / 详情回帖 | ✅ **已完成**（前端改动文件清单见 §8 P-6：增量设计 §2 只覆盖后端） |
| M6 | 契约同步（快照再生成 + 技术方案 §5）+ 5 条验收标准实测 | `openapi.json` / §5 / 验收记录 | ✅ **已完成**（快照 12 端点 / 30 schema；验收记录见本文 §5） |

> **实现依据以 [sprint-2-design.md](sprint-2-design.md) 为准**——该文件给出四层文件清单、6 个端点的请求/响应形状、统一分页口径（`page/size`，**与技术方案 §5 旧的 `pageNum/pageSize` 不同，落码后须同步 §5**）、楼层号与并发策略、以及 §2.1 的六条人工分层自检。

## 4. 开工时的三条人工自查（A3-9 推迟的直接后果）——**执行结果**

> 三条均在落码时逐条执行，**证据已归档**，不是"打过勾"：① 依赖方向 → 增量设计 §8 末尾"两处 §2.1 自检的实测证据"（grep 实测：`domain` 无框架/基础设施依赖、`domain/gateway/` 3 接口 + 1 record 且无实现、3 个 Mapper 均在 `infrastructure/persistence/mapper/`、`web/` 无 Mapper/DO/Converter、`module.account.infrastructure` 导入数 **0**）；② 受保护端点鉴权 → 两个 Controller 各写显式判空 + 各 3 例 401 单测 + 真机匿名/非法 token 实测均 401；③ 快照重生成 → 已再生成（12 端点 / 30 schema，见 [api/README](../design/api/README.md) §2）并同步技术方案 §5 的 4 行状态与分页口径。**这三条是人工做的——机制仍无机器强制**（A3-9 / A3-10 欠账未闭环）。

1. **依赖方向人工核对**：`module/forum` 必须照 [ADR-012](../design/tech-design.md) 四层落地——web/infrastructure → application → domain 单向依赖，端口定义在 domain、实现在 infrastructure，跨上下文只调对方 application。**没有测试会拦你**，写错不会失败。**请直接照 [增量设计](sprint-2-design.md) §2 的文件清单建目录，并用其 §2.1 的六条清单逐条打勾**（该清单已把 F-1 在 domain 挂 `@Service`、F-3 Mapper 放错包这两个具体教训写成了可勾选项）。
2. **受保护端点人工确认鉴权**：`POST /posts` 与 `POST /posts/{id}/replies` 必须在 Controller 里**真的写了**登录态校验——契约上的 `@SecurityRequirement` 只是声明（[api/README](../design/api/README.md) §6.2 的 **N-4**：`SecurityConfig` 仍 `permitAll()`）。漏写会静默变成公开接口。**并照 [增量设计](sprint-2-design.md) §4.2 各补一个「匿名调用 → 401」单测**——这是唯一能把"漏写"变成"测试失败"的手段。
3. **接口契约变更后重生成快照**：本 Sprint 新增 6 个端点，合入前须按 [api/README](../design/api/README.md) §2 的命令重抓 `openapi.json`；同时把技术方案 §5 的 4 行路径由"计划项"改为"已实现"，并把分页口径由 `pageNum/pageSize` 同步为 [增量设计](sprint-2-design.md) §3 的 **`page/size`**（[增量设计](sprint-2-design.md) §6 的 **D-6**——不同步就是新的文档-代码不一致）。

## 5. 验收记录（2026-09-12，真机起栈实测）

**环境**：本机原生 MySQL 8 + Redis 7 + 后端 `8088`（`module/forum` 工作区，未提交）+ 前端 Vite `5173`（`/api` 代理到 8088）。取证时间 15:19~15:24。

**A1 发帖可用 —— ✅ 通过**

- 后端【实测】：`GET /posts?boardCode=qna` 发帖前 `total: 1` → `POST /posts` 连发两帖（`{"id":5}`、`{"id":6}`）→ 再查列表 `total: 3`，**首位为 `id=6`**（`createdAt 2026-09-12T15:19:25Z`），其后 `id=5`、`id=1` 按时间倒序。
- 全站列表【实测】：`GET /posts?page=1&size=3`（不带 `boardCode`）同样以 `id=6` 打头，共 6 帖。
- 前端【实测】：浏览器打开 `/board/qna`，首条即该帖，作者"管理员"、计数"3 回复"、时间"2026-09-12 23:19"（UTC 存储 + 本地时区展示）。

**A2 链路完整 —— ✅ 通过**

- 后端【实测】：`GET /posts/6` → `GET /posts/6/replies`（`total: 0`）→ `POST /posts/6/replies` 返回 `{"id":5,"floorNo":1}`，再发一条返回 `{"id":6,"floorNo":2}` → 重新请求 `GET /posts/6/replies` 得 `total: 2`、楼层 **#1 / #2** 依序返回；`GET /posts/6` 的 `replyCount` 由 `0` 变为 `2`。**楼层号从 1 起、与帖子正文无关**（增量设计 D-1 的口径）。
- 前端【实测】：详情页 `全部回复（2）` 显示 #1 / #2；在回复框输入 Markdown 并点"发表回复" → 列表新增 **#3 楼**、页头计数变 **"3 回复"**（回帖后 `Promise.all([loadReplies(), loadPost()])` 双刷新）。
- **"刷新后仍在"**：后端为真实 MySQL 落库（Flyway 迁移后的 `replies` 表），重新请求即重新查库；浏览器刷新后楼层仍在。

**A3 权限正确 —— ✅ 通过**

- 匿名可读【实测】：`GET /posts`、`GET /boards`、`GET /posts/6`、`GET /posts/6/replies` 四个公开端点匿名调用**全部 200**。
- 受保护端点【实测】：匿名 `POST /posts`（**合法请求体**）→ **401 / `4001` 未登录**；匿名 `POST /posts/6/replies`（合法体）→ **401 / `4001`**；**伪造 token**（`Bearer not.a.real.token`）`POST /posts` → **401 / `4001`**。
- 前端【实测】：匿名访问 `/publish` → 被路由守卫拦下跳到 **`/login?redirect=/publish`**；登录成功后**回到 `/publish`**（发帖页）。匿名访问 `/post/6`：回复区替换为提示"登录后才能回帖 · 去登录"，其链接为 **`/login?redirect=/post/6`**；登录后**回到 `/post/6`**。
  - **实现与标准的措辞差异（如实说明）**：标准写"点'发帖'/'回帖'跳登录页"，实际实现是**路由守卫跳转**（`/publish` 属 `requiresAuth`）与**回复区降级为登录提示**（未登录不渲染提交框），并非点按钮才跳。两者都达成"未登录不能发/回、且登录后回到原页"的意图，故判通过。
  - **一处反直觉行为（已归档）**：**参数校验先于登录态检查**——匿名 + **非法**请求体会先被 `@Valid` 拦成 **400 / `1001`**，只有"匿名 + 合法体"才走到 **401 / `4001`**。初次取证用 `-d '{}'` 因此得到 400 而非 401，改用合法体复测得 401。已记入 [api/README](../design/api/README.md) §4.5 的 🔍 注记。

**A4 XSS 防线不破 —— ✅ 通过**

- 单测【实测】：`MarkdownRendererTest` **6 / 6 全绿**（`Tests run: 6, Failures: 0, Errors: 0`，BUILD SUCCESS）——其中含 6 个 XSS 回归用例。
- 入库渲染【实测】：发文正文 `行内脚本 <script>alert(1)</script> 与图片 <img src=x onerror=alert(2)> 与链接 [x](javascript:alert(3))`，详情接口返回的 `contentHtml` 为
  `<p>行内脚本 与图片 <img src="x"> 与链接 x</p>` —— **`<script>` 整段丢弃、`onerror` 属性被剥除、`javascript:` 协议被剥除**，无一残留。
- 前端【实测】：经 UI 发的回帖含 `<img src=x onerror=alert(1)>`，详情页把它**显示为纯文本**（DOM 中**不存在** `<img>` 元素，`onerror` 未执行、无弹窗），页面其余内容正常渲染。

**A5 构建与真机 —— ✅ 通过**

- 后端【实测】：`mvn -o verify` → **BUILD SUCCESS**，单测 **87 / 0 失败 / 0 错误**（Sprint 1 结束时为 62；本 Sprint 新增论坛相关 6 个测试类 22 例 + 既有账号用例）。论坛测试类：`ForumQueryApplicationServiceTest` 8、`PostApplicationServiceTest` 2、`ReplyApplicationServiceTest` 3、`ForumPublicReadTest` 3、`PostControllerAuthTest` 3、`ReplyControllerAuthTest` 3。
- 前端【实测】：`npm run build` → `✓ built in 5.00s`，产物含 `BoardView` / `PublishView` / `PostDetailView` / `forum` 分包。
- 真机【实测】：A1~A4 的**浏览器取证均在上文这套真实栈上完成**（本机原生 MySQL / Redis + 后端 + Vite），不是只跑单测。

**本轮未覆盖（如实列出，避免"全绿"被误读为"全都验过"）**

- **并发**：多用户同时发帖 / 回帖的楼层号竞争**未压测**——`reply_count` 行锁派生楼层号的正确性只有单测覆盖（增量设计 §4.1），无真实并发验证。
- **OUT 项**：`sort=hot`、标签、引用回复、编辑 / 删除等**未实现故未测**（不在本 Sprint 范围）。
- **W-03 的 UI 走查未执行**：本 Sprint 明确不做（[CR-022](../change-log.md) 的 13 项 OUT 之一），验收用的页面截图**不等于** W-03 要求的逐页走查归档，[设计门](../reviews/gate-3-design.md) 出口标准第 3 项**仍为 ❌**。
- **快照来源为未提交的工作区**：见 [api/README](../design/api/README.md) §2，提交后须按该节"提交后核对"项复核。
- **未做浏览器兼容性验证**：全部取证在单一 Chromium 内核浏览器上完成。

## 6. 变更记录

- v1.0（2026-09-12）——创建。发起人决定改按最小 MVP 推进（[CR-022](../change-log.md)）：范围锁定为"发帖 / 列表 / 详情 / 楼层回复"四项，明确 13 项不做的范围外内容；验收标准由 7 项裁为 5 项（去掉当前无法度量的覆盖率与静态扫描，**不声称提测准入门通过**）；**原 Sprint 2 开工阻断项 A3-9 / A3-10 推迟至 MVP 之后**（[W-07](../tailoring-waivers.md)），其直接后果——四层依赖方向无机器强制、契约声明不等于运行时强制——以 §4 三条人工自查承接。
- v1.1（2026-09-12）——**随 [增量设计](sprint-2-design.md) v1.0 创建同步**：① **任务清单重构**——原 M1“增量 PRD”**并入增量设计、不再单独产出**（理由见该文件 §0：需求侧无新内容，再造一份 PRD 正是 [W-07](../tailoring-waivers.md) 所记“流程复杂度远超代码量”的成因本身），任务由 5 项改为 **6 项**（M1 设计已完成 / M2 account 只读方法 / M3 forum 骨架 / M4 6 端点 / M5 前端 3 页 / M6 契约同步与验收）；② 上游依据去掉“增量 PRD”；③ §2 补一句“契约细节以增量设计 §3 为准”；④ **§4 三条人工自查全部指向增量设计的具体章节**——第 1 条指向其 §2 文件清单 + §2.1 六条分层自检（已把 F-1 在 domain 挂 `@Service`、F-3 Mapper 放错包写成可勾选项），第 2 条补“**各补一个「匿名调用 → 401」单测**”（唯一能把“漏写鉴权”变成“测试失败”的手段），第 3 条补“同步技术方案 §5 的分页口径为 `page/size`”（增量设计 §6 的 D-6）；⑤ **§1 范围、§1 的 13 项 OUT、5 条验收标准均未改动**——本版不改变本 Sprint 的范围与验收口径。
- **v1.2（2026-09-12）——交付版，M1~M6 全部完成、5 条验收标准实测通过**：① 头部状态改"**已交付**"；② **§3 任务表 M2~M6 全部置 ✅**（M1 补注"已按 §8 记录 8 项落码偏差"，M2 指向 §8 P-8、M4 注明两个 401 测试类各 3 例、M5 指向 §8 P-6、M6 注明快照规模 12 端点 / 30 schema）；③ **§1 五条验收标准全部勾选**（A1~A5）；④ **§4 标题改为"——执行结果"并新增一段汇总**，把三条人工自查的执行证据逐条指出来源（增量设计 §8 的 grep 实测、两个 401 测试类 + 真机 401、快照再生成与 §5 同步），并重申**这三条是人工做的、机制仍无机器强制**；⑤ **新增 §5 验收记录**（原有的 §5 变更记录顺延为 §6）——按 A1~A5 逐条给出真机实测证据（含具体 id / 楼层号 / HTTP 码 / 错误码 / 计数变化）、三处**如实说明**（A3 实现与标准措辞的差异、**参数校验先于登录态检查**这一反直觉行为、并发未压测）、以及一条"本轮未覆盖"清单（并发 / OUT 项 / W-03 UI 走查未执行 / 快照来源未提交 / 单浏览器内核）；⑥ **范围、13 项 OUT、5 条验收标准的文字本身未改动**——本版只填结果，不改口径。
