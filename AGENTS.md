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

## 项目状态速览（快照 2026-09-16；每次收尾更新，细节看驾驶舱）

- **阶段四开发中**：Sprint 1 ✅（账号链路，T1~T9）；Sprint 2 ✅（论坛最小 MVP，M1~M6、A1~A5 真机验收通过）；**Sprint 3 五项功能已全部交付**（[CR-043](docs/变更日志/变更台账.md) 采纳最佳答案 · [CR-048](docs/变更日志/变更台账.md) 点赞收藏 · [CR-049](docs/变更日志/变更台账.md) 站内搜索 · [CR-050](docs/变更日志/变更台账.md) 通知中心 · [CR-058](docs/变更日志/变更台账.md) 热榜定时任务 + `sort=hot`）；⚠️ **CR-058 只是 F-FORUM-003 双排序的后端一半**——前端首页「最新 / 热门」切换 UI **尚未做**（该轮前端由并发会话占用），不得声称双排序已上线；**无 `Sprint3计划.md`**——Sprint 3 以各 CR 的[实施方案](docs/开发/实施方案/说明与模板.md)为执行依据；
- **B4 / B5（= A3-9 / A3-10）已于 2026-09-12 由 [CR-028](docs/变更日志/变更台账.md) 完成**：ArchUnit `ArchitectureGuardTest`（初版 10 用例，[CR-031](docs/变更日志/变更台账.md) 增至 11）把 ADR-012 四层规则**首次变为机器强制**（规则吸收复评 R-1 / R-2 / R-3；R-1 裁决：禁止 web 注入 domain 端口，`UserController` 已改经 application）；**N-4 闭环**（每个映射方法必须显式 `@PublicEndpoint` 或 `@SecurityRequirement`，受保护者必须调用 `CurrentUser` 统一入口）；F-1 / F-3 / F-4 与 N-5 / N-6 同批处置；单测 87 → **107** 全绿。**同日 [CR-031](docs/变更日志/变更台账.md) 补上框架级路径鉴权**（消除当时登记的"`SecurityConfig` 仍 `permitAll()`、路径级拦截未做"残留）：`EndpointAuthorizationManager` 在过滤器链按端点注解裁决（`module` 端点未声明即 fail-closed），401 / 403 出口沿用 `ApiError` 外壳，单测 **107 → 117** 全绿、真机 4 个受保护端点匿名 / 非法 token 一律 401；**W-02 / W-07 的关闭前置已成立，状态为"可关闭、待发起人签署"（任何文档不代签）**；
- **CR-032 之后（2026-09-13 ~ 09-16）的收敛概述**（逐条见 [台账](docs/变更日志/变更台账.md) §1，本文不重复）：设计门 **A3-5（ER 图与量级预估）/ A3-7（检查清单）已闭环**，**开放项只剩 A3-6（UI 走查，仅剩补偿③ 逐页走查）与 A3-8（部署与机审选型，须发起人决策）**；除 Sprint 3 五个功能外，另有全库时间戳时区口径修复（[CR-051](docs/变更日志/变更台账.md)，闭环台账外问题 L-6）、前端界面美化与优化（[CR-052](docs/变更日志/变更台账.md) / [CR-055](docs/变更日志/变更台账.md)）、移动端验证暂缓（[CR-053](docs/变更日志/变更台账.md)）、学籍名册模拟数据 45 名 + 开发测试账号清单（[CR-054](docs/变更日志/变更台账.md)）、**[CR-058](docs/变更日志/变更台账.md) 热榜定时任务（ADR-006）——本仓首次引入 `@Scheduled` / `@EnableScheduling`**。**单测基线已由 117 升至 185**（[CR-058](docs/变更日志/变更台账.md) 新增 21 例：算分策略 8 / 热榜用例 6 / 触发器 3 / Redis 锁 2 / `sort` 校验 2）；契约快照仍为 **21 端点 / 40 schema**，最近一次再生成为 [CR-058](docs/变更日志/变更台账.md) 的 v1.10（**规模零变化**，只给 `GET /api/v1/posts` 加了一个 `sort` 查询参数；上一次规模变化是 [CR-050](docs/变更日志/变更台账.md) 的 v1.9）；
- **流程已降密度（[CR-029](docs/变更日志/变更台账.md)）**：CR 自 CR-030 起改一行式登记；**项目状态只维护 change-log §1 + 进度驾驶舱两处**；取消凭证回填仪式（凭证 = commit message 写 CR 号）；AI 遵守下方「汇报约定」；
- **唯一外部依赖**：B1 / B2 学籍名册——**真实名册仍未到位，但自 [CR-054](docs/变更日志/变更台账.md)（2026-09-15）起已不再阻塞开发**：库内已有 45 名全合成学生（批次 `dev-sim-45`）+ 45 个开发测试账号，`bypass=false` 下的注册链路可实测可复现（清单见「常用命令」段）；**正式导入路径与生产红线一条未放宽**（`bypass` 生产必须 `false`、dev 固定验证码生产必须留空）；
- **提测准入门 7 项中 4 项未满足**（单测覆盖率不可测 / **CI 缺静态扫描环节** / Code Review 未闭环 / 静态扫描未接入——7 项清单见 [`Sprint1计划.md`](docs/开发/Sprint1计划.md) §出口自查）；让步放行 W-01 ~ W-07 中**仅 W-06 已关闭**，**W-02 / W-07 关闭前置已成立（[CR-028](docs/变更日志/变更台账.md)）但待发起人签署、不得代签**；⚠️ **UI 走查不在这 7 项内**——它属设计门 **A3-6**（[W-03](docs/流程偏离记录.md) 补偿③），两笔欠账不要混算；
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
| [`docs/设计/UI规范/界面规范.md`](docs/设计/UI规范/界面规范.md) · [`前端界面素材选型.md`](docs/设计/UI规范/前端界面素材选型.md) | 新增 / 修改前端页面前：令牌表、组件与交互约定（W-03 补偿①落点）；**要引入任何外部素材（组件库 / 图标 / 字体 / 插画 / 动效）前，先查选型文档 §3「禁区汇总」**——多数素材已被规范排除，均带"想用先改规范"的解禁路径 |
| [`docs/开发/Sprint2计划.md`](docs/开发/Sprint2计划.md) · [`docs/开发/Sprint2增量设计.md`](docs/开发/Sprint2增量设计.md) | 做论坛相关任务时：IN/OUT 范围、六端点契约、三条人工自查（§4）、落码偏差（§8 P-1~P-8） |
| [`docs/开发/实施方案/说明与模板.md`](docs/开发/实施方案/说明与模板.md) | 要做「够大」的改动前：先按模板写 `CR-xxx-主题.md` 并**停下等确认**（门槛见「实施方案约定」） |
| [`docs/需求/产品需求文档-*.md`](docs/需求/产品需求文档.md) | ⚠️ **6 份增量 PRD 草案**（[CR-033](docs/变更日志/变更台账.md) / [CR-036](docs/变更日志/变更台账.md)~[CR-039](docs/变更日志/变更台账.md) / [CR-042](docs/变更日志/变更台账.md)）**全部"待发起人评审、通过前不得开工"**——个人主页 `/u/:id`、头像体系、账号类型与身份体系等能力都压在这些草案上，**按草案动手前先确认其状态** |
| [`docs/设计/数据模型ER图与量级预估.md`](docs/设计/数据模型ER图与量级预估.md) | 要查表结构 / 字段 / 量级时（**以 V1 迁移 12 张表为唯一事实源**，[CR-035](docs/变更日志/变更台账.md)） |
| [`docs/开发/开发测试账号清单.md`](docs/开发/开发测试账号清单.md) | 需要开发测试账号、做批量登录核验时（45 个合成账号，[CR-054](docs/变更日志/变更台账.md)） |
| [`docs/评审/gate-*.md`](docs/评审/立项门纪要.md) · [`docs/评审/技术方案追认纪要.md`](docs/评审/技术方案追认纪要.md) · [`docs/评审/部署与机审选型决策材料.md`](docs/评审/部署与机审选型决策材料.md) | 追溯评审结论与行动项（A1-*/A2-*/A3-* 状态、F-1~F-7、R-1~R-3）；最后一份是 A3-8 的决策备料（**只备料、不代决策**） |
| [`docs/需求/产品需求文档.md`](docs/需求/产品需求文档.md) | 需求基线（学籍核验 F-ACC-004 为 P0） |

## 常用命令

> **开发期依赖栈用本机原生服务，不用 Docker**（[CR-011](docs/变更日志/变更台账.md)）；`docker-compose.dev.yml` 保留，发布阶段（阶段六）再启用。
> Redis：`D:\Workspace\TechResources\Redis\Redis-8.6.2-Windows-x64-msys2-with-Service\redis-server.exe`（在自身目录下以 `redis.conf` 启动，监听 127.0.0.1:6379，无密码）。
> MySQL：本机 `127.0.0.1:3306`，库 `campuslink`。**表结构不用手工导入**：Flyway 在后端启动时自动执行迁移（[CR-014](docs/变更日志/变更台账.md)）。

**backend/**（构建需 **JDK 21**，本机先执行 `set "JAVA_HOME=D:\develop\Java\jdk-21"`）：

```bash
# 仅首次需要：库必须先存在，Flyway 才能连上（建库属基础设施引导，不归 Flyway 管）
mysql -h127.0.0.1 -uroot -p -e "CREATE DATABASE IF NOT EXISTS campuslink DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"

mvn verify            # 编译 + 单测（合码前必须全绿，当前基线 185 个；含架构守护测试）
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
**联调冒烟（Sprint 3 四条链路）**：① **采纳最佳答案**——发帖 / 回帖后由**楼主**在详情页点「采纳」（`POST /api/v1/posts/{id}/accept`），最佳答案楼层置顶、列表出「已采纳」标识；② **点赞收藏**——列表 / 详情页点赞与收藏 toggle，收藏页 `/favorites/mine` 可见；③ **站内搜索**——顶栏搜索框 → `/search`（`keyword` 2~50 字，`days` 取 7 / 30 / 90）；④ **通知中心**——`/notifications` 页 + 顶栏铃铛角标，`unread` 可筛选。
**热榜（[CR-058](docs/变更日志/变更台账.md#cr-058)，本仓首个定时任务）**：`posts.hot_score` 由 `@Scheduled` 定时刷新（公式 `互动分 × exp(-λ × 小时龄)`，ADR-006），读取走 `GET /api/v1/posts?sort=hot`（缺省 `latest`，非法值 `400 / 1001`）。**调参只改 `application.yml` 的 `campuslink.hot.*`，不改代码**：权重 `reply-weight=3 / like-weight=1 / favorite-weight=2`、`decay-per-hour=0.05`、`window-days=30`（窗口外置 0）、`batch-size=500`、`lock-ttl-seconds=300`（⚠️ **须 ≤ 刷新周期**——锁靠 TTL 兜底不显式释放，TTL 比周期长会把实际周期变成 TTL）。**验证时用 `HOT_REFRESH_INTERVAL_MS=20000` / `HOT_INITIAL_DELAY_MS=5000` 临时缩短周期**（默认 600000 / 15000；这两项只被 `@Scheduled` 占位符消费，**刻意不绑定到 `AppProperties`**，避免两个事实源），看 1 行 INFO「热榜刷新完成：写回 N 条、置零 M 条、耗时 X ms」；Redis 侧只应出现 `campuslink:hot:refresh:lock` 一个键。⚠️ **目前只有后端能力，前端首页「最新 / 热门」切换 UI 尚未做**（F-FORUM-003 的后端一半），页面上看不到热榜不是缺陷。
**验证码**：默认固定 `123456`（`campuslink.captcha.fixed-code`，启动打 WARN）；留空恢复随机 6 位（`CODE_SENDER_MODE=log` 下只打日志，行格式 `[DEV] captcha for <邮箱> => <6位码>`）。
**测试名册**（仅 `app.roster.bypass=true` 生效，学号须 **9 位数字**）：`249971346/张三`、`249971347/李四`、`249971348/王五`、`249971349/赵六`。
**开发测试账号（45 个，[CR-054](docs/变更日志/变更台账.md#cr-054) 已灌入本机开发库）**：清单见 [`docs/开发/开发测试账号清单.md`](docs/开发/开发测试账号清单.md)——学号保留段 `888800001`~`888800045`、邮箱 `dev-stu-01@dev.campuslink.local`~`dev-stu-45@…`、昵称 `模拟学生01`~`模拟学生45`，**全合成身份、仅限本机开发库**；登录 = **邮箱 + 验证码 `123456`**（**本系统没有密码字段**）。重建走数据轨脚本 `backend/scripts/data/D001__seed_dev_roster_45.py`（默认 dry-run，须指向 `APP_ROSTER_BYPASS=false` 的实例）；批量核验时把 `campuslink.verify.ip-hourly-limit` 用 **`VERIFY_IP_HOURLY_LIMIT=<n>`** 临时抬高（**默认仍 10 次/小时/IP、对外行为不变**）。**这不等于 B1 / B2 真实名册已到位**，后者仍是未闭环的外部依赖。
**管理员账号**（dev-only 种子，与 bypass 同门控）：`admin@campuslink.local`，任意邮箱方式 + 验证码 `123456`，角色 `SUPERADMIN`，可调用 `/api/v1/admin/roster/import`；**无学号、不走学籍核验**。
**入参格式**：学号 `^\d{9}$`；姓名限中文名（2~16 汉字，可含 `·`）或外文名（字母起头，可含空格 / `-` / `'` / `.`）；`nickname` 2~32 自由文本。

## 架构与编码约定

### 后端分层（ADR-012，强制）

- 限界上下文 `module/{account,forum,...}`，上下文内四层：`domain`（聚合根 / 值对象 / 领域服务 / **端口 gateway** / 领域事件）→ `application`（用例编排 + Command）→ `infrastructure`（MyBatis-Plus 仓储、Redis、通知等适配器）→ `web`（Controller + VO）；
- 依赖方向：**web / infrastructure → application → domain**；端口定义在 domain、实现在 infrastructure（DIP）；**跨上下文只允许调用对方 application 服务**；
- **`infrastructure` 同时承载入站适配器（[CR-058](docs/变更日志/变更台账.md) 首次出现）**：定时任务触发器（`@Scheduled`，如 `module/forum/infrastructure/scheduling/HotScoreRefreshScheduler`）放 infrastructure 并**调用同上下文的 `application` 服务**——该方向（`infrastructure → application`）**本就是上一条与守护测试 G1 放行的方向**，不是违规（`ArchitectureGuardTest` 11 例实测全绿）；但触发器里**不写业务规则**（只负责"何时跑 + 取锁 + 记日志"），算分口径只在 `domain` 的领域服务里（`HotScorePolicy` 是公式的唯一事实源，SQL 侧不重复表达）。`@EnableScheduling` 与调度线程池在 `config/SchedulingConfig`（**不在 `CampusLinkApplication`**）；
- MyBatis-Plus Mapper 统一放 `*.mapper` 包；`@MapperScan` 的值**恰为单值** `{"com.campuslink.**.mapper"}`（2026-09-12 [CR-028](docs/变更日志/变更台账.md) 将 `AuditMapper` 移入 `common/audit/mapper/` 后收窄，**并由守护测试断言"恰为单值"——再加特例即测试失败**）；
- ✅ **四层与跨上下文规则自 [CR-028](docs/变更日志/变更台账.md)（A3-9）起由 `ArchitectureGuardTest` 机器强制**（ArchUnit，**11 用例**——[CR-028](docs/变更日志/变更台账.md) 落 10 例，[CR-031](docs/变更日志/变更台账.md) 新增 G8）：分层依赖方向 / domain 不依赖框架（lombok 放行、`domain → common` 放行）/ **web 不得注入 `domain.gateway` 端口、不得调 `domain.service`（R-1），允许对 `domain.model` 与不可变载体做只读类型引用（R-2）** / 跨上下文只调对方 `application` / 端口实现必须在同上下文 `infrastructure` / Mapper 实现必须在 `..mapper..` 包 / N-4 声明一致性 / **G8：`SecurityConfig` 必须依赖 `EndpointAuthorizationManager` 且不得再调 `permitAll()`（路径级鉴权不被摘除）**。**改依赖结构前先读 [技术方案 §2.4](docs/设计/技术方案.md)（规则已与该节双向绑定，任一侧改动须同步）**；新增上下文最有效做法仍为**照抄 `module/{account,forum}` 的结构**（范式可复制性已由 A3-11 复评确认，结论见追认纪要 §8）。

### 错误响应与契约声明（[CR-021](docs/变更日志/变更台账.md) 起）

- 错误外壳 `common/result/ApiError{code,message,traceId}`（**无 `data`**，与成功外壳 `ApiResponse` 分开）；**所有 ≥400 的响应只能由 `GlobalExceptionHandler` 产出**，Controller 只 `throw new ApiException(ResultCode.X)`，不自己拼错误体；
- **HTTP 状态码不另行约定**——每个 `ResultCode` 自带 `httpStatus`，改状态码只改枚举一处；
- 契约里的错误响应由 `OpenApiErrorResponseCustomizer` 从 `ResultCode` **单点派生**：端点用项目自有注解 `@ErrorCodes({ResultCode.X, ...})` 声明业务错误码（通用 400/500 自动补，不必声明）；**鉴权声明（N-4 闭环，[CR-028](docs/变更日志/变更台账.md)；自 [CR-031](docs/变更日志/变更台账.md) 起同时是运行时拦截的唯一依据）**：`module/*/web` 下**每个 HTTP 映射方法必须显式二选一**——公开端点标 `@PublicEndpoint`、受保护端点标 `@SecurityRequirement(name = ApiDocs.BEARER_AUTH)` **且方法体真的调用 `common/web/CurrentUser`**（`requireId` / `requireRole`，统一抛 `NOT_LOGGED_IN` / `FORBIDDEN`）——漏写即守护测试失败；**`security/EndpointAuthorizationManager` 在过滤器链按同一组注解裁决**（公开放行 / 受保护要求登录 / module 端点未声明即 fail-closed），框架只区分"登录 / 未登录"，角色与资源级授权仍归业务代码；**不要加全局 security**（会把公开端点错标为需鉴权）；**禁止用 swagger 的 `@ApiResponses` 手写错误响应**（事实存两份，改错误码必然漂移）；
- **日志分档**：业务错误不记日志、框架级客户端错误记 1 行 WARN 不打全栈、只有兜底 `Exception` 记 ERROR + 全栈（客户端错误进 ERROR 会污染 5xx 监控——N-3 缺陷成因）。

### 数据与迁移

- 统一响应 `ApiResponse{code,message,data,traceId}`；错误码分段（1xxx 通用 / 2xxx 账号 / 21xx 学籍 / 3xxx 帖子 / 4xxx 权限 / 5xxx 安全机审），见技术方案 §5；分页口径 **`page` / `size`**（短名，从 1 起）；
- **结构变更一律新增 Flyway 迁移** `src/main/resources/db/migration/V<n>__<描述>.sql`（**已执行的迁移不可修改**，回滚靠新增前向迁移）；**纯数据增删走** `backend/scripts/data/D<序号>__<描述>.py`（PyMySQL，默认 dry-run）；**禁止 Hibernate 自动建表**。

### 安全

- **敏感信息**（邮箱 / 手机号 / 学号）：明文一律 AES-GCM 加密存 `*_enc`，等值查询用 HMAC 哈希 `*_hash`；任何接口不得返回 `*_enc` / `*_hash`；密钥只从环境变量读取（`APP_HASH_KEY` / `APP_CRYPT_KEY`），**源码、示例、测试不得写入可用凭据字面量**；
- **学籍核验**：三种失败（学号不存在 / 姓名不匹配 / 已注册）统一提示，防名册枚举；`app.roster.bypass` 仅限开发联调，**生产必须为 false**（上线检查清单项）；
- **Markdown 渲染唯一出口** `common/markdown/MarkdownRenderer`（flexmark + jsoup 白名单）；flexmark 扩展须**同时注册到 Parser 与 HtmlRenderer**，否则解析成功渲染为空；代码高亮由前端 `highlight.js` 完成（**已于 [CR-055](docs/变更日志/变更台账.md) 批次① 落地**：`frontend/src/utils/highlight.ts` + `src/styles/hljs.css`；**只对服务端已渲染的 `pre code` 的 `textContent` 着色，不二次渲染 Markdown、不拼接 HTML、不做前端净化兜底**——ADR-005 与此项欠账就此闭环）；任何渲染改动必须保持 `MarkdownRendererTest` 全绿；
- **Redis 键命名**：所有键必须带 `campuslink:` 前缀（走 `common/redis/RedisKeys.of(...)`）；**前缀只在 infrastructure 适配器补**，应用层只传逻辑键；新增适配器须走 `RedisKeys`（`RedisKeyNamespaceTest` 有 3 个适配器断言，无编译期强制）。

### 前端

- 页面按 PRD 5.1 清单实现；**Element Plus 按需自动引入**（unplugin-auto-import / unplugin-vue-components，勿回退全量引入）；
- **样式体系（[CR-023](docs/变更日志/变更台账.md) 建立、[CR-024](docs/变更日志/变更台账.md) 调色）**：`src/styles/tokens.css` 是**设计令牌唯一来源**——主色 `#409eff`、**正文链接色 `#1a6fc4` 独立于主色**（`#409eff` 白底 2.78:1 不适合小号文字）、字号阶梯、间距为 4 的倍数、圆角三档、0.5px 细边框、去默认阴影 / 渐变；以 `:root:root` 覆写 EP 的 `--el-*`；**改主色只改 `--cl-color-primary` 一个值，但 `--el-color-primary-rgb` 须手工同步**（链接色不派生、不随动）；`src/styles/tailwind.css` 提供工具类（`@theme inline` 映射令牌），两者在 `main.ts` 一次性引入；
- **层叠边界（强制约定，无 lint 强制）**：Tailwind v4 输出在 `@layer` 内、EP 样式不带 layer，**无层恒胜有层——EP 组件一律通过 CSS 变量调样式，自有元素才用工具类**；新增 / 修改页面前先查 [ui-guideline](docs/设计/UI规范/界面规范.md)；
- **代码高亮（[CR-055](docs/变更日志/变更台账.md) 批次① 已落地）**：`src/utils/highlight.ts` 用 `highlight.js/lib/core` 按需注册语言，`src/styles/hljs.css` 只提供 `--cl-code-*` 色板（**不整份引入 github 主题 CSS**——那含字面 hex，会破"颜色只来自令牌"）；
- ⏳ **进行中**：[CR-055](docs/变更日志/变更台账.md) 五批中**仅批次①（代码高亮）已落地**；批次② 头像占位 / ③ Tabler 图标 / ④ 中文字体分片自托管 / ⑤ 桌面走查**均尚未落地**（选型已随方案确认）——**不得声称"图标 / 字体已统一"**，`@element-plus/icons-vue` 与（未来的）Tabler 是**混用体系**；- API 统一走 `src/api/client.ts`（`ApiError` + JWT 注入 + 后端错误消息直接透出 UI）；可复用逻辑放 `src/composables/`，共享常量放 `src/constants/`。

## 测试约定

- **单测**：`mvn verify` 必须全绿（**当前基线 185 个**，含 `ArchitectureGuardTest` 架构守护测试与 `EndpointAuthorizationManagerTest` 路径级鉴权矩阵）；`MarkdownRendererTest` 的 6 个 XSS 回归用例是论坛安全生命线，渲染 / 白名单改动**先补用例再改实现**；
- **受保护端点必须真的校验鉴权（N-4 已闭环，[CR-028](docs/变更日志/变更台账.md)；路径级拦截自 [CR-031](docs/变更日志/变更台账.md) 起落地）**：**每个 HTTP 映射方法必须显式标 `@PublicEndpoint` 或 `@SecurityRequirement`，且标后者者必须真的调用 `common/web/CurrentUser`（`requireId` / `requireRole`）**——`ArchitectureGuardTest` G7 会拦（漏写即测试失败）；**新增需登录的端点仍须补一个「匿名 → 401」单测**（真机 401 也要验）。⚠️ 运行时是**两层**：`security/EndpointAuthorizationManager` 在过滤器链按注解拒绝匿名（401 / 4001，框架直接产出），`CurrentUser` 在业务侧承担角色与资源级授权（403 / 4002）——**框架只区分"登录 / 未登录"，别指望它做角色判定**；
- **联调冒烟**：新链路合入前必须真实起栈（本机 MySQL / Redis + 后端 + 前端）并**浏览器实测**，不能只依赖单测；
- **结构变更后真实启动一次**确认 Flyway 迁移成功（`mvn verify` 不校验迁移可执行性，也不校验 `@MapperScan` 通配的实际扫描结果）；
- **接口契约变更必须重生成 OpenAPI 快照** `docs/设计/接口契约/openapi.json`：凡改 Controller 路径 / 方法 / 入参出参 / 校验注解、**新增端点（公开 / 受保护声明）**、`ApiResponse` / `ApiError` 外壳、`OpenApiConfig`、**`common/web/CurrentUser` 或 `PublicEndpoint`**、`ResultCode` 的错误码 / 提示语 / `getHttpStatus()` 映射、**或改了 `security/EndpointAuthorizationManager` / `SecurityConfig` 的授权口径**（[CR-031](docs/变更日志/变更台账.md) 已把 `permitAll()` 换成按端点注解授权并再生成过快照——快照的 `security` 声明不会自动跟随运行时规则，此后每次改动都须重抓核对），都须起后端后按 [api/README.md](docs/设计/接口契约/README.md) §2 命令重抓并格式化，看 `git diff`——**非空即契约已变**，同步技术方案 §5 与该 README。**快照无机器校验，漂移只能靠这条约定拦**；`1002` / `1003` / `1004` 三类响应永远进不了快照，以该 README §6.3 文字约定为准。

## 汇报约定（每次 AI 会话执行）★

单人项目 + 多会话并行，用户对"AI 在干什么"必须有完整可见性：

- **开工先报计划**：任何实质工作开始前，先用 ≤3 行说明"这轮要做什么、改哪些文件"；**若本轮属于「实施方案约定」的门槛范围（见下节），先写方案、停下等确认，不要先动代码**；
- **小步汇报**：长任务拆成小步，每完成一步用一两句话报告进展；**不要一口气跑完多个批次才汇报**（CR-025 六批次连跑是被记录在案的反面教训）；
- **收尾摘要 ≤10 行**：改了什么 / 为什么 / 验证结果，并指向驾驶舱与相关文档；台账登记只报"已登记 CR-xxx"，不复述台账内容；
- **一次会话只做一个逻辑任务**，除非用户明确要求连做；
- **并行会话敏感性**：改公共文档（change-log / AGENTS.md / 驾驶舱 / docs/README）前先看当前内容是否已被其他会话更新，**用追加与加注，不做整段覆写**。

## 实施方案约定（较大改动 / Bug 修复先写方案）★（[CR-034](docs/变更日志/变更台账.md) 起）

**规则一句话**：够大就先写「可实施方案」→ **停下等用户确认** → 才动代码。

### 门槛（中等口径，满足任一即须先写方案）

- **跨模块**：改动的 `module/*` 超过一个，或 `common/` 与业务模块同时改；
- **接口契约**：路径 / 方法 / 入参出参 / 校验注解 / `ApiResponse`·`ApiError` 外壳 / 错误码；
- **数据库结构**：新增 `V*.sql` 迁移、改表 / 索引 / 约束；
- **新依赖**：`pom.xml` / `package.json`（含大版本升级）；
- **安全与鉴权**：`security/`、`SecurityConfig`、`CurrentUser`、`PublicEndpoint`、审计链路；
- **Bug 修复**：根因跨层或跨模块（如表现在前端、根因在后端契约），**或修复会改变既有对外行为**（状态码 / 错误码 / 响应形状）。

### 可直接改（不写方案，但仍须按 [CR-029](docs/变更日志/变更台账.md) 轻量登记 CR）

单文件局部修改、纯样式 / 文案、文档修正、测试补充、根因单一且对外行为不变的缺陷修复。

### 形态与位置

- 目录 **`docs/开发/实施方案/`**，命名 **`CR-xxx-主题.md`**（与 CR 编号一一对应）；
- 格式照 **[说明与模板](docs/开发/实施方案/说明与模板.md)**，**一页以内**，六节：目标 / 范围（做与不做）/ 改动点（文件级）/ 步骤 / 验证 / 回滚；
- 方案是"实施前评审"的载体，**CR 编号仍登记在 [change-log](docs/变更日志/变更台账.md) §1**（[CR-029](docs/变更日志/变更台账.md) 一行式）；
- **实施完成后不回改原方案**：实际与计划的偏差追加到该方案文末「实施结果」节，保留"原计划 vs 实际"对照；
- 同一 CR 若中途改口径，**先改方案再改代码**。

### 边界

- 用户当面明确说「直接改」时从命；
- 纯文档 / 流程类改动（如本文件、模板、README）**不写实施方案**，走 change-log 登记即可；
- 方案文件本身按「文档与流程约定」登记：**目录与模板在 `docs/README.md` §3「模板」表登记**（与既有 `docs/模板/*` 同例——**§2 只登记有版本演进的核心文档**）；**各 CR 实例按命名规则存放、不逐项登记**（避免索引随每次改动线性膨胀）。


## 工作收尾约定（每次任务完成后执行）★

1. **更新进度驾驶舱 [`docs/进度驾驶舱.html`](docs/进度驾驶舱.html)**——触发条件：本次工作改变了进度事实（新 CR / 欠账状态变化（A3-*、W-*、B-*、N-*）/ Sprint 里程碑 / 新问题 / 新业务需求）。更新内容：页头「最后更新」与数据快照日期；① 总览统计与阶段 / Sprint 状态；② 时间线新增 CR 行（**从 change-log §1 抄录**，含日期 / 标题 / 类型 / 状态 / 评审方式 / 提交凭证）；③ 问题台账状态；④ 待办与新需求。**边界**：驾驶舱只做聚合，**不得登记 change-log / next-steps / tailoring-waivers 等事实源文档中没有的新事实**；只改数据，不改结构与样式；更新前先核对 change-log 最新条目，防止驾驶舱静默过时；
2. **基线级变更确认已登记（[CR-029](docs/变更日志/变更台账.md) 起用轻量格式）**：范围 / 排期 / 技术选型 / 架构 / 工程结构的改动，实施前在 `docs/变更日志/变更台账.md` §1 登记——**一行一条**（改了什么 / 为什么 / 凭证），**不再写九行明细表与三维度影响评估**；CR 编号写入 commit message 即完成凭证对齐，**不做台账回填仪式**；
3. **展示变更摘要**：列出本次改动的文件与要点，**不自动 git commit / push**，等用户确认。

## 文档与流程约定

- 阶段门未通过不得进入下一阶段；评审结论与状态由项目经理同步到 `docs/README.md` 阶段门状态表；
- **版本号只在 `docs/README.md` §2 登记**；其他文档（含本文件）交叉引用只写链接、不写版本号；
- **状态两处收口（[CR-029](docs/变更日志/变更台账.md) 起）**：项目当前状态只维护 **change-log §1 + 进度驾驶舱** 两处；next-steps / gate-* / waivers 等文档中的既有状态保留为历史，**不再新增状态同步义务**——新状态变化时只改两处；
- **流程偏离登记在 `docs/流程偏离记录.md`**（不得只在对话里说明）；评审门纪要归档在 `docs/评审/<阶段名>门纪要.md`（现行三份：`立项门纪要.md` / `需求门纪要.md` / `设计门纪要.md`）；
- **文档命名用中文（[CR-030](docs/变更日志/变更台账.md) 起，推翻原"英文小写中划线"规则）**：文档与文件夹一律用中文名（如 `docs/评审/设计门纪要.md`）；**唯一例外 `README.md`**（GitHub 目录入口约定）；目标结构：`docs/{立项,需求,设计,开发,评审,模板,变更日志}/`；
- **简洁原则**：流程文档只保留"当前结论 + 指针"，**日志式过程记录（实施补记、状态回填、凭证追踪、版本变更记录）一律写入 `docs/变更日志/`**，不在流程文档里累积；
- **批量迁移已执行（[CR-030](docs/变更日志/变更台账.md)，2026-09-12）**：中文重命名与日志迁移已一次性完成（35 个 git mv + 1582 处链接修复，校验 0 失效）——原"待 CR-028 会话结束后执行"的安排已履行，新文档一律直接用中文名；
- 新文档放入 `docs/` 对应位置并在 `docs/README.md` §2 + §3 **两处登记**（漏登记即产生文档不一致），内容用中文；
- 文档头部必须含：**版本、状态、维护人、最后更新**。

## 红线（与用户全局规则一致）

- **不自动 git commit / push**；提交前先展示变更摘要；**commit message 用中文**（保留 `feat` / `docs` / `fix` 等类型前缀，作用域与描述用中文；标题末尾写 CR 号；正文写清**「做了什么、解决了什么」**，让人不点开代码就能看懂）；monorepo 统一根目录操作；
- 删除文件 / 目录、修改 `.env` / 密钥 / 证书、`git push` / `rebase` / `reset --hard`、公开发布：**必须先征得用户同意**；
- **生产环境红线**：`app.roster.bypass=false`、`campuslink.captcha.fixed-code` 必须留空（固定验证码等同取消验证码防线）、JWT 与加密密钥全部覆盖默认值、名册导入与内容处置必须写审计日志、机审降级开关（fail-closed）不得改为跳过审核。
