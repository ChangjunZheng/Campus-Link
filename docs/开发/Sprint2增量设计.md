# Campus-Link Sprint 2 增量设计（MVP · 论坛最小闭环）

| 文档信息 | 内容 |
|---------|------|
| 版本 | v1.1 |
| 状态 | 已生效（实现依据）；**M2~M5 已按本文落码**，实施偏差集中登记于 §8 |
| 维护人 | 技术负责人（发起人兼任） |
| 关联阶段 | 开发（阶段四） |
| 上游依据 | [Sprint 2 计划（MVP）](sprint-2.md) · [技术方案 §2.4 / §5](../design/tech-design.md) · [PRD](../requirements/prd.md)（F-FORUM-001~004）· [API 契约快照](../design/api/README.md) |
| 下游记录 | [CR-022](../change-log.md) 的后续实施记录（落码与验收结果）· [Sprint 2 计划](sprint-2.md) §6 验收记录 |
| 最后更新 | 2026-09-12 |

> 版本号以 [docs/README.md](../README.md) 第 2 节为单一登记处，本文交叉引用不写版本号（手册 4.5）。

## 0. 本文为什么只有一份（没有单独出增量 PRD）

按 [sprint-2.md](sprint-2.md) §3 原计划，M1（增量 PRD）与 M2（增量设计）是两份产出。**实际只出本文一份，这是有意的决定**，理由：

- **需求侧没有新东西可写**。F-FORUM-001~004 已在 [PRD](../requirements/prd.md) 中定义并被评审过，本 Sprint 做的是**它们的子集**（去掉标签、热门、引用回复）。再写一份 PRD 只是把"做哪几条"抄一遍，属于 [W-07](../tailoring-waivers.md) 所记"流程复杂度已远超代码量"的成因本身；
- **需求侧的裁剪已在 [sprint-2.md](sprint-2.md) §1 写清**——IN 4 项、OUT 13 项、验收标准 5 条，该文件即本 Sprint 的需求基线；
- 本文只写**代码里看不出来的决策**：分层归属、契约形状、并发与一致性、任务分解。能从既有代码抄的部分不复述。

> **不代表需求评审被绕过**：[PRD](../requirements/prd.md) 仍是需求基线且为"有条件通过 · 行动项未闭环"，[W-01](../tailoring-waivers.md)（单人评审）偏离依旧存在。本文只是**不为本 Sprint 单独再造一份需求文档**。

## 1. 已核实的前置事实（写代码前不必再查）

| 项 | 事实 | 核实方式 |
|---|---|---|
| 数据库 | `V1__init_schema.sql` 已建 **12 张表**，含 `boards` / `posts` / `replies`；`V2__seed_boards.sql` 已种入 **6 个版块** | 读迁移文件 |
| **迁移** | **本 Sprint 新增迁移 0 个**——所需的表、索引、外键全部就位 | 同上 |
| 已有端点 | 后端**只有 6 个端点，全属 `module/account`**（`verify-student` / `captcha` / `register` / `login` / `admin/roster/import` / `users/me`） | grep `@*Mapping` + 快照 |
| **论坛端点** | **一个都不存在**，本 Sprint 的 6 个端点**全部新写** | 同上 |
| 关键索引 | `posts`：`idx_posts_board_list(board_id,status,is_deleted,created_at)`、`idx_posts_latest(status,is_deleted,created_at)`；`replies`：`idx_replies_post_floor(post_id,floor_no)` | 读 DDL |
| 帖子正文 | `content_html` 在**发布时**服务端渲染落库，**请求时零渲染**（ADR-005） | DDL 注释 + 技术方案 |
| 统一响应 | `ApiResponse{code,message,data,traceId}`；错误体 `ApiError{code,message,traceId}` | 读源码 |
| 错误码 | **3xxx 段已预留给"版块与帖子"**：目前只有 `NOT_FOUND(3001,404)`。本 Sprint 复用 `NOT_FOUND` 与 `INVALID_PARAM(1001)`，**不新增错误码** | 读 `ResultCode` |

## 2. 后端 `module/forum` 结构（照 `module/account` 抄，四层）

> [ADR-012](../design/tech-design.md) 要求四层单向依赖：`web` / `infrastructure` → `application` → `domain`；**端口定义在 domain、实现在 infrastructure（DIP）**；跨上下文只调对方 `application`。
> ⚠️ **A3-9 未落地，这套规则没有测试保护（[W-07](../tailoring-waivers.md)）**——下面是逐层对照，写完请按 [sprint-2.md](sprint-2.md) §4 第 1 条**逐行核对包名与 import**。

```
module/forum/
├── domain/
│   ├── model/         Board.java · Post.java · Reply.java · BoardType.java · PostStatus.java
│   ├── gateway/       BoardRepository.java · PostRepository.java · ReplyRepository.java   ← 端口（接口）
│   ├── exception/     PostNotFoundException.java · BoardNotFoundException.java
│   └── service/       (本 Sprint 无领域服务——发帖/回帖无跨聚合不变量，逻辑留在 application)
├── application/
│   ├── ForumQueryApplicationService.java    查：版块列表 / 帖子列表 / 帖子详情 / 回复分页
│   ├── PostApplicationService.java          写：发帖
│   ├── ReplyApplicationService.java         写：回帖（含 floor_no 分配与 reply_count 自增）
│   └── cmd/  PublishPostCommand.java · PublishReplyCommand.java
├── infrastructure/
│   └── persistence/
│       ├── mapper/    BoardMapper.java · PostMapper.java · ReplyMapper.java      ← 必须在 .mapper 包（F-3 教训）
│       ├── BoardDO.java · PostDO.java · ReplyDO.java
│       ├── BoardConverter.java · PostConverter.java · ReplyConverter.java
│       └── BoardRepositoryImpl.java · PostRepositoryImpl.java · ReplyRepositoryImpl.java
└── web/
    ├── BoardController.java     GET /api/v1/boards
    ├── PostController.java      GET/POST /api/v1/posts · GET /api/v1/posts/{id}
    ├── ReplyController.java     GET/POST /api/v1/posts/{id}/replies
    └── vo/  BoardVo.java · PostSummaryVo.java · PostDetailVo.java · ReplyVo.java · PageVo.java
```

**跨上下文调用**：`forum` 需要作者昵称（`users.nickname`）。按 ADR-012 **只允许调 `account` 的 application 服务**——本 Sprint 的落地方式见 §4.3（**不直接注入 `UserMapper`**）。

### 2.1 分层自检清单（A3-9 缺位期的替代，逐条打勾）

- [ ] `domain/` 下**没有任何** Spring / MyBatis / Jackson import（**不要像 F-1 那样在 domain 挂 `@Service`**）
- [ ] `domain/gateway/` 里只有接口，**没有实现**；实现全在 `infrastructure/`
- [ ] `infrastructure/` 与 `web/` 都只 `import ...application...` 与 `...domain...`，**`domain` 不 import 它们任何一个**
- [ ] 新增 Mapper **放在 `infrastructure/persistence/mapper/` 包内**（F-3：`AuditMapper` 就是因为在包外才被迫给 `@MapperScan` 加通配）
- [ ] `web/` 里**不出现** Mapper / DO / Converter 类型
- [ ] 没有 `import com.campuslink.module.account.infrastructure...`（跨上下文只能碰 `account.application`）

## 3. 接口契约（6 个端点，全部新增）

分页统一：**入参 `page`（从 1 起，默认 1）+ `size`（默认 20，上限 100）**；出参 `PageVo{ list, total, page, size }`。
> ⚠️ 技术方案 §5 旧表用的是 `pageNum / pageSize`。**本设计统一用 `page / size`**，因为前端 `client.ts` 与既有代码都用短名，且本 Sprint 只有 4 个分页端点、改一次成本最低。**落码后须把 §5 表同步为此口径**（否则就是新的 F-4 类文档-代码不一致）。

### 3.1 `GET /api/v1/boards` — 版块列表（公开）

```
200 { code:0, data:[ { code:"qna", name:"技术问答", description:"…", type:"QUESTION", sort:1 } ] }
```
只返回 `enabled=1` 的版块，按 `sort` 升序。**不加分页**（固定 6 条）。

### 3.2 `GET /api/v1/posts` — 帖子列表（公开）

| 参数 | 必填 | 说明 |
|---|---|---|
| `boardCode` | 否 | 传则按版块过滤，不传则**全站最新**；传了不存在的 code → `404 / 3001` |
| `page` / `size` | 否 | 见上 |

```
200 { code:0, data:{ list:[ { id, boardCode, boardName, title, authorNickname, replyCount, likeCount, createdAt, summary } ], total, page, size } }
```
- **排序固定 `created_at DESC`**（对应 `idx_posts_latest` / `idx_posts_board_list`）；**本 Sprint 不做热门**；
- 过滤条件固定 `status='PUBLISHED' AND is_deleted=0`；
- `summary`：`content_md` 去 Markdown 记号后的前 **120 字**（纯文本，**由服务端截断**，前端不做兜底截断）。

### 3.3 `POST /api/v1/posts` — 发帖（**需登录**）

```
请求 { boardCode:"qna", title:"…", contentMd:"…" }
200  { code:0, data:{ id: 123 } }          ← 只回 id，前端跳 /post/123
```
校验：`title` 1~100 字（DDL 上限）、`contentMd` 非空且 ≤ **50000** 字符（防止 MEDIUMTEXT 被滥用）。
服务端：`contentHtml = MarkdownRenderer.render(contentMd)` **落库**；`type` 取版块的 `type`；`status='PUBLISHED'`。
**鉴权见 §4.2——必须真的写校验，不能只挂 `@SecurityRequirement`。**

### 3.4 `GET /api/v1/posts/{id}` — 帖子详情（公开）

```
200 { code:0, data:{ id, boardCode, boardName, title, contentHtml, authorNickname, replyCount, likeCount, createdAt, accepted:false } }
404 → 404 / 3001（不存在、已删、或 status != PUBLISHED）
```
返回 `contentHtml`（**已渲染好的 HTML，前端用 `v-html`**）。XSS 防线在 `MarkdownRenderer` 的白名单，**不在前端**。

### 3.5 `GET /api/v1/posts/{id}/replies` — 楼层列表（公开）

```
200 { code:0, data:{ list:[ { id, floorNo, contentHtml, authorNickname, createdAt } ], total, page, size } }
```
按 `floor_no ASC`（对应 `idx_replies_post_floor`）。

### 3.6 `POST /api/v1/posts/{id}/replies` — 回帖（**需登录**）

```
请求 { contentMd:"…" }
200  { code:0, data:{ id: 456, floorNo: 7 } }
```

## 4. 需要专门交代的四处

### 4.1 楼层号 `floor_no` 与 `reply_count` 的一致性（本 Sprint 唯一的并发点）

`floor_no` 按 post 内自增。朴素写法 `select max(floor_no)+1` 在并发回帖下会**重号**（`replies` 表**没有** `uk(post_id, floor_no)` 唯一约束，重号不会被数据库拦下）。

本 Sprint 采用**「乐观重试 + 计数同事务」**，不引入分布式锁：

1. 在一个 `@Transactional` 内：`UPDATE posts SET reply_count = reply_count + 1 WHERE id = ?`（**行锁先行**）→ 再读回 `reply_count` 作为本次 `floor_no`；
2. 若同一 post 并发回帖，第二步的行锁天然串行化，`floor_no` 不会重号；
3. `floor_no` 与 `reply_count` 由**同一个递增**派生，两者天然一致；
4. **已知取舍**：`floor_no` 从第 2 楼开始（`reply_count` 先自增到 1），**与常见论坛"1 楼=楼主"的习惯不同**——本 Sprint 采用"**楼层 = 回复序号**"，1 楼即第一条回复，**详情页顶部帖子本体不占楼层号**。**该口径必须在前端与 PRD 表述一致**，否则是新的文档-代码不一致。

> 为什么不做唯一约束 + 重试：加 `uk(post_id, floor_no)` 需**新增迁移**（与"本 Sprint 零迁移"冲突），而乐观重试在单人内测规模（日均新帖 ≥30）下完全够用。**该取舍登记在本文 §6。**

### 4.2 鉴权：两个受保护端点**必须手写校验**（N-4）

`config/SecurityConfig` 仍是 `anyRequest().permitAll()`（**N-4 未闭环**），因此：

- `POST /posts`、`POST /posts/{id}/replies` 的 Controller **必须**有 `UserController.me` 里那句同样的判断：

```java
if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
    throw new ApiException(ResultCode.NOT_LOGGED_IN);
}
```

- **两个端点各补一个单测**「匿名调用 → 401 / 4001」（照 `AdminRosterControllerAuthTest` 的写法）；
- 注解只用于契约：类上 `@SecurityRequirement(name = ApiDocs.BEARER_AUTH)`，`@ErrorCodes({ResultCode.NOT_LOGGED_IN, ...})`。

> **不要**顺手在 `SecurityConfig` 里改成按路径授权——那会**同时**改变 4 个公开端点的行为，属 §5 明列的"不做"，且会触发契约快照大改。N-4 的处置归 **A3-9**。

### 4.3 作者昵称：跨上下文读 `account`，不碰对方数据表

列表/详情要显示作者昵称。**不允许** `forum` 直接注入 `UserMapper`（越层 + 跨上下文越界）。

本 Sprint 采用**「批量查昵称 + 应用层拼装」**：
1. 在 `account` 的 `application` 层**新增一个只读方法**（如 `AccountApplicationService#nicknamesOf(Collection<Long> ids) → Map<Long,String>`）；
2. `forum` 的 `application` **注入该 application 服务**（符合 ADR-012 的"跨上下文只调对方 application"）；
3. 一页最多 20 条，**一次批量查**，不做 N+1；
4. 昵称缺失（用户注销等）回落到 `"已注销用户"`。

> ⚠️ 这是本设计**唯一触碰 `account` 模块**的地方，改动仅"新增一个只读方法"，**不改任何既有行为**，故不另行开 CR，登记在本文与 [CR-022](../change-log.md) 的后续实施记录中。

### 4.4 契约快照与文末检查清单

- 落码后按 [api/README](../design/api/README.md) §2 重生成 `openapi.json`（6 → **12 个端点**）；
- 技术方案 §5 接口表同步：`/boards`、`/posts`、`/posts/{id}`、`/posts/{id}/replies` **从"计划项"转为"已实现"**，分页口径改为 `page/size`；
- 快照仍**不会**包含 `1002` / `1003` / `1004`（OpenAPI 表达极限，见 api/README §6.3）。

## 5. 本 Sprint 不做（与 [sprint-2.md](sprint-2.md) §1 的 OUT 一致，此处只列**容易被顺手做掉**的）

标签（`tags` / `post_tags`）· 热门与 `hot_score`（ADR-006 定时任务）· 点赞收藏 · 采纳最佳答案 · 编辑 / 删除帖子 · 引用回复（`quoted_reply_id`）· 搜索 · 通知 · 机审 · 改 `SecurityConfig` 授权规则 · 新增错误码 · 新增 Flyway 迁移。

## 6. 已登记的取舍与风险（如实）

| # | 项 | 内容 |
|---|---|---|
| D-1 | `floor_no` 口径 | "**楼层 = 回复序号**"（第一条回复为 1 楼，帖子本体不占楼层号），与部分论坛"楼主=1 楼"习惯不同。**前端与 PRD 须与此一致** |
| D-2 | 并发策略 | 用 `posts.reply_count` 行锁派生 `floor_no`，未加 `uk(post_id, floor_no)` 唯一约束（避免新增迁移）。**代价**：绕过 application 层直接写 `replies` 表仍可能造成不一致——本 Sprint 无此路径 |
| D-3 | 作者昵称 | 跨上下文调 `account.application` 新增的只读方法；**`account` 模块被改动（仅新增）** |
| D-4 | 🔴 **N-4** | 两个受保护端点的鉴权靠**人工手写**，漏写不会失败、会静默变公开接口。补偿：§4.2 的两个单测 + [sprint-2.md](sprint-2.md) §4 第 2 条自查 |
| D-5 | ⚠️ **A3-9 缺位** | 四层依赖方向**无机器强制**，`AuthUnit` 尚未引入。补偿：§2.1 六条自检清单（人工逐条核对） |
| D-6 | 分页参数改名 | 由技术方案 §5 的 `pageNum/pageSize` 改为 `page/size`，**须同步 §5 否则产生新的文档-代码不一致** |
| D-7 | 本 Sprint 无增量 PRD | 见 §0。**不构成本 Sprint 已通过需求评审**——PRD 侧仍是"有条件通过 · 行动项未闭环" |

## 7. 任务分解（照 [sprint-2.md](sprint-2.md) §3，落到文件）

| # | 任务 | 主要产出 | 依赖 | 验收 |
|---|------|---------|------|------|
| M1 | 增量设计（本文） | `sprint-2-design.md` | — | ✅ 已完成 |
| M2 | `account` 侧只读方法 | `AccountApplicationService#nicknamesOf` + 单测 | M1 | 既有 62 个单测全绿 |
| M3 | `forum` 四层骨架 + 3 个 Mapper/DO/Converter/Repository | `module/forum/**` | M1 | **§2.1 六条自检全过** |
| M4 | 6 个端点（4 公开 + 2 受保护） | 3 个 Controller + 4 个 Vo + 3 个 application 服务 | M3 | 2 个 401 单测 + 匿名可读单测 |
| M5 | 前端 3 页接真实数据 | 版块列表 / 发帖 / 详情+回帖 | M4 | `npm run build` 通过 |
| M6 | 契约同步 + 验收 | 快照再生成、技术方案 §5、5 条验收标准实测 | M4 | [sprint-2.md](sprint-2.md) §1 的 A1~A5 |

## 8. 落码偏差记录（M2~M5 实施时与本文的差异，逐条如实登记）

> 记录原则同本项目的其他一致性记录：**偏差不隐藏、也不回头改写设计原文**——§3~§6 保持落码前的样子，差异集中登记于此，需要时由后续版本修订正文。**无一项改变 §1 的范围、§3 的契约形状（除下表中已标注的两处命名）或 §6 的取舍**。

| # | 偏差 | 实际落码 | 性质与处置 |
|---|------|---------|-----------|
| **P-1** | §3.4 写 `isAccepted` | 实际字段名为 **`accepted`**（Java record 组件名 `accepted` → Jackson 序列化即 `accepted`，无 `is` 前缀） | **文档错误，已修正 §3.4 正文**（本版唯一改动正文的偏差）。前端 `forum.ts` 已按 `accepted` 对接；[api/README](../design/api/README.md) §3.2 已加显式警示 |
| **P-2** | §3.3 / §3.4 的路径参数写 `{id}` | `ReplyController` 用类级 `@RequestMapping("/api/v1/posts/{postId}/replies")`，故回复两个端点的路径参数名为 **`{postId}`** | **非错误、不改**：两个 Controller 各自命名，OpenAPI 里如实呈现为 `/posts/{id}` 与 `/posts/{postId}/replies`。[api/README](../design/api/README.md) §3.2 已加说明，避免读者误读为两个资源 |
| **P-3** | §2 文件清单未列 `domain/gateway/PageResult` | 新增 `PageResult<T>` record（`domain/gateway` 包内）作为仓储层分页返回类型 | **合理的补充**：分页是仓储端口的能力，返回类型须定义在 domain 才不违反依赖方向（不引入 Spring Data 的 `Page`）。登记但不改 §2 |
| **P-4** | §2 文件清单未列 `application/cmd/ForumResults` | 新增 `ForumResults`（`PublishedPost` / `PublishedReply` 两个返回记录的载体） | **合理的补充**：与既有 `account` 的 `AuthResponse` 同类的用例返回类型，位置合规。登记但不改 §2 |
| **P-5** | §2 文件清单未列 `MarkdownRenderer` 的新方法 | 为生成 `summary` 新增 `common/markdown/MarkdownRenderer#toPlainSummary`（去 Markdown 记号、截 120 字） | **必要的补充**：§3.2 要求 `summary` **由服务端截断**，而 Markdown 渲染的唯一出口是 `MarkdownRenderer`（AGENTS.md 约定），故新方法只能加在那里。**该文件属 `common/`，不改任何既有渲染行为、`MarkdownRendererTest` 6 个 XSS 用例保持全绿** |
| **P-6** | §2 未列前端文件（该节只列后端） | 实际新增/改写 `frontend/src/api/forum.ts`、`utils/time.ts`、`views/PostDetailView.vue`、`views/PublishView.vue`，改写 `views/BoardView.vue`、`views/HomeView.vue`、`router/index.ts`、`App.vue` | **非偏差、说明**：§2 标题即"后端 `module/forum` 结构"，前端不在其范围。登记以便追溯 |
| **P-7** | §6 未登记"分页边界归一化的位置" | 归一化（`page<1→1`、`size<1→20`、`size>100→100`）落在 **application 层**，而非 `@Min`/`@Max` 注解校验 | **有意的实现选择**：注解校验失败会抛 `ConstraintViolationException`，若未被专用处理器接住就会落兜底分支报成 500（**即 N-3 那类缺陷**）。放在 application 层做**归一化**而非报错，既避免该类风险、又对调用方更宽容。已同步写入技术方案 §5 分页条目 |
| **P-8** | §4.3 未写明 `account` 侧方法的返回形状 | 实为 `AccountApplicationService#nicknamesOf(Collection<Long>) → Map<Long,String>`，昵称缺失回落 `"已注销用户"` | 与 §4.3 文字一致，仅补记确切签名。**`account` 模块本次改动仅"新增一个只读方法"，未改既有行为**（单测 `AccountApplicationServiceNicknameTest` 覆盖） |

**两处 §2.1 自检的实测证据**（该清单是 A3-9 缺位期唯一的替代防线，故列出可复核的命令级结论）：

- `domain/` 下 import 仅见 lombok / `java.util` / `java.time` / `common` 的 `ApiException`·`ResultCode`，**无 Spring / MyBatis / Jackson**——`domain/gateway/` 内只有 3 个接口 + `PageResult` record，无实现；
- `infrastructure/persistence/mapper/` 内 3 个 Mapper（`BoardMapper` / `PostMapper` / `ReplyMapper`），**无包外 Mapper**（不重演 F-3）；`web/` 内不出现 Mapper / DO / Converter；全模块 **零** `module.account.infrastructure` import（跨上下文只经 `account.application`）。

## 9. 变更记录

- **v1.1（2026-09-12）——M2~M6 落码后的同步，范围与取舍未变**：① **修正 §3.4 的 `isAccepted` → `accepted`**（P-1，文档错误）；② 新增 **§8 落码偏差记录**（8 条：P-1 文档错误 / P-2 路径参数名 / P-3~P-5 文件清单外的合理补充 / P-6 前端文件说明 / P-7 分页归一化位置 / P-8 方法签名），并附 **§2.1 六条自检的实测证据**（grep 级结论：domain 无框架 import、Mapper 在 `.mapper` 包内、web 无持久化类型、零 `account.infrastructure` import）；③ **§1~§7 的范围、契约形状（除 P-1）、取舍与风险一律未改**——本版只做"把落码事实登记清楚"，不借机改写设计原文；④ 任务 M1~M6 的实际完成情况见 [sprint-2.md](sprint-2.md) 与 [CR-022](../change-log.md) 的后续实施记录。
- v1.0（2026-09-12）——创建（[CR-022](../change-log.md) 的后续实施产物）。**只出一份增量文档、不出单独增量 PRD**（理由见 §0，直接回应 [W-07](../tailoring-waivers.md) 所记"流程复杂度远超代码量"）；§1 核实并写明**新增迁移 0 个、论坛端点 0 个已存在**；§2 给出 `module/forum` 四层文件清单与 **§2.1 六条人工自检**（A3-9 缺位期的替代）；§3 给出 6 个端点的请求/响应形状与**统一分页口径 `page/size`**；§4 交代四处必须专门的决策——**楼层号与 `reply_count` 的一致性**（行锁派生、不加唯一约束）、**N-4 下两个受保护端点必须手写鉴权并各配一个 401 单测**、**作者昵称走 `account.application` 新增只读方法而非直连对方数据表**、**契约快照与技术方案 §5 同步**；§6 如实登记 7 条取舍与风险（含 🔴 N-4 与 ⚠️ A3-9 缺位）；§7 把任务落到具体文件。
