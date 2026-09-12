# Campus-Link Sprint 1 计划（地基与账号链路）

| 文档信息 | 内容 |
|---------|------|
| 版本 | v1.12 |
| 状态 | 已执行（出口自查部分未闭环） |
| 维护人 | 技术负责人（发起人兼任） |
| 关联阶段 | 开发（阶段四） |
| 上游依据 | [next-steps.md](../next-steps.md) §3 · [技术方案](../design/tech-design.md) · [PRD](../requirements/prd.md) · [变更台账](../change-log.md) |
| 最后更新 | 2026-09-12 |

> 版本号以 [docs/README.md](../README.md) 第 2 节为单一登记处，本文交叉引用不写版本号（手册 4.5）。
>
> 本文件是 Sprint 1 的执行依据（next-steps §3 为整体节奏参考：8 周 / 4 Sprint，见 [CR-006](../change-log.md)）。周期：2 周。
>
> **程序说明**：Sprint 1 在阻塞项 B1 / B2 未清零、且设计门行动项未闭环的情况下启动并执行完毕，该偏离见 [W-02](../tailoring-waivers.md) / [W-04](../tailoring-waivers.md)，**已于 2026-09-11 由发起人签署"接受"**（[CR-020](../change-log.md)）——偏离自此获得书面批准，但 **W-02 不得关闭**（关闭前置为设计门 A3-9 架构守护测试），W-04 补偿措施③（真实名册到位后关闭 bypass 回归）仍未执行。

## 目标与出口标准

**目标**：地基与账号链路——能用学号 + 姓名 + 邮箱验证码注册成功，能登录，能看到个人主页；发一段含 `<script>` 的 Markdown 不产生 XSS。

**出口自查**
- [x] 注册链路联调通过（学籍核验 → 验证码 → 注册 → 登录 → `GET /users/me`）——**2026-09-11 首次真机跑通**（本机原生 MySQL 8 + Redis，后端 8088，经 Vite 5173 代理）：`2023001/张三` 核验返回票据 → 注册成功（`id=1`、`verified=true`）→ 登录返回 JWT → `/users/me` 返回昵称"张三"；验证码重发限流（`2002`/429）亦按设计生效。（**注**：`2023001` 是当时的 7 位测试学号，同日 [CR-013](../change-log.md) 起学号须为 9 位，该号已被格式校验挡下，现用名册见下方"测试名册"表。）**首次启动时暴露并修复 2 个启动级缺陷**（见下方 v1.6 变更记录）
- [x] XSS 用例回归全绿（`MarkdownRendererTest` 6 用例）
- [x] `mvn verify` 与 `npm run build` 本机通过（2026-08-30：10/10 单测全绿，前端 1622 模块构建成功；2026-09-07 DDD 重构后 14/14 全绿；2026-09-12 [CR-021](../change-log.md) 后 **62/62** 全绿——新增 `GlobalExceptionHandlerTest` 9 个、`OpenApiErrorResponseCustomizerTest` 5 个、`AdminRosterControllerAuthTest` 3 个，**均为纯单测，CI 仍起不了完整栈**）
- [ ] **单测覆盖率达标（手册 3.4：≥60%，核心模块 ≥80%）——当前不可测量**：`backend/pom.xml` 未接入 jacoco 或任何覆盖率插件，见 [W-05](../tailoring-waivers.md) 补偿措施 ②
- [ ] CI 全绿——✅ **已具备判定条件**：远程已配置，`backend-ci` / `frontend-ci` 随首次推送在 GitHub Actions **首跑成功**（[CR-011](../change-log.md)）。本项仍有保留条件：**静态扫描未接入**，故"全绿"尚未覆盖手册 3.4 要求的扫描环节
- [ ] Code Review 完成（发起人 / 第二角色）——手册要求至少 1 名同行批准，单人项目须以确定形式闭环，见 [W-05](../tailoring-waivers.md) 补偿措施 ④
- [ ] 静态扫描无新增阻断级问题（手册 3.4 出口标准）——**未接入**，见 [W-05](../tailoring-waivers.md)

> **提测准入门现状：上述 7 项中 3 项已满足、4 项未满足**（未满足：覆盖率不可测、CI 的静态扫描环节缺失、Code Review 未闭环、静态扫描未接入）。联调冒烟已于 2026-09-11 通过，原"CI 无远程""注册链路未验证"两项已消除。按手册 3.4 出口标准，Sprint 1 目前**仍不具备提测条件**。
>
> ⚠️ **首次真机启动（2026-09-11）暴露 2 个启动级缺陷，`mvn verify` 全绿却完全没发现**——因为 14 个单测均为纯领域单测，没有一个加载完整 Spring 上下文。这正是 W-05 所记录的"无集成测试"缺口的实证：出口自查前 5 项全绿，但应用**从未成功启动过**。

## 任务清单

| # | 任务 | 产出 | 状态 |
|---|------|------|------|
| T1 | 仓库脚手架 | backend：Spring Boot（**初版 3.2，v1.2 已升级至 4.1.1 + JDK 21，见 [CR-007](../change-log.md)**）+ Maven + application.yml + docker-compose.dev.yml（MySQL 8 + Redis 7）；frontend：Vue 3 + Vite + TS + Pinia + Element Plus | ✅ 2026-08-30 |
| T2 | 数据库 | **12 张表** + 索引 + 标题 ngram 全文索引 + 6 版块种子。原为 `backend/sql/01_schema.sql` 与 `02_seed_boards.sql`（2026-08-30 产出），**2026-09-11 随 [CR-014](../change-log.md) 转为 Flyway 迁移** `db/migration/V1__init_schema.sql` + `V2__seed_boards.sql`，`backend/sql/` 已删除 | ✅ 2026-08-30 |
| T3 | 公共层 | 统一响应 `ApiResponse`（含 traceId）、`ResultCode` 分段错误码、全局异常处理、参数校验 | ✅ 2026-08-30 |
| T4 | **安全地基：Markdown 渲染** | `common/markdown/MarkdownRenderer`（flexmark + jsoup 白名单，服务端单点净化，ADR-005）+ XSS 回归用例 | ✅ 2026-08-30 |
| T5 | 鉴权骨架 | Spring Security + JWT 过滤器（jjwt）+ 角色解析 + CORS；FilterChain 收紧列入 Sprint 2 | ✅ 2026-08-30 |
| T6 | **学籍核验 + 注册登录** | F-ACC-004（verify-student / 一次性票据 / 学号占用 / IP 小时限流 / 姓名归一化）+ F-ACC-001（captcha / register / login，Redis 限流）+ 名册 CSV 导入（写审计日志） | ✅（bypass + 内置测试名册；B1/B2 清零后导入真实名册、关闭 bypass 回归） |
| T7 | 个人主页 | `GET /api/v1/users/me` 最小版（F-ACC-002；他人主页与资料编辑在 Sprint 2） | ✅ 2026-08-30 |
| T8 | 前端骨架 | 顶栏导航（6 版块入口 / 搜索占位 / 用户菜单）、注册登录页（学籍核验两步流 + 60s 验证码倒计时）、首页与版块占位页 | ✅ 2026-08-30 |
| T9 | 构建验证 | `mvn verify`（编译 + 单测 10/10 全绿）与 `npm run build`（1622 模块）本机通过 | ✅ 2026-08-30 |

## 阻塞与依赖（对应 next-steps.md）

- **B1 / B2 学籍名册**（**仍开放**）：当前以 `app.roster.bypass=true` + 内置测试名册（学号 **9 位**，见下方"测试名册"表；原 7 位号 `2023001` 等已随 [CR-013](../change-log.md) 的格式校验作废）开发联调；真实名册到位后由 SUPERADMIN 走 `POST /api/v1/admin/roster/import` 导入，然后**关闭 bypass 并回归注册链路**（名册表结构已按"学号,姓名[,年级[,专业]]"预留，B2 字段决议如有出入需调整导入列映射）。该回归同时是 [W-04](../tailoring-waivers.md) 补偿措施③，**签署"接受"不等于已执行**。
- ~~**B3 仓库远程**~~ **✅ 已闭环（2026-09-11，[CR-011](../change-log.md)）**：项目为根目录单一仓库（monorepo，[CR-010](../change-log.md)）；远程 `https://github.com/changjunzheng/Campus-Link` 已配置，首次提交 `837a22f` 已推送并建立 `main` 上游跟踪。GitHub Actions 工作流位于根 `.github/workflows/`（`backend-ci.yml` 跑 `mvn verify`、`frontend-ci.yml` 跑 `npm ci && npm run build`，各自以 `working-directory` 指向子目录并按 `paths` 过滤触发），**两次首跑均成功**。未配置的剩余项：`main` 分支保护与 PR 流程。
- **开发期依赖栈**：本机原生 MySQL 8 / Redis，不用 Docker；后端默认端口 **8088**（[CR-012](../change-log.md)）。
- **P1 UI 稿 → [W-03](../tailoring-waivers.md) 补偿措施**（= 设计门 **A3-6**，**仍开放**）：**2026-09-11 发起人签署"接受"裁剪**（[CR-020](../change-log.md)），确认不产出交互稿与视觉稿、直接用 Element Plus 拼页面；替代交付物为①组件与交互规范约定、②页面截图归档至 `docs/design/ui/`（**目录尚不存在**）、③提测前逐页走查——**三条均未执行**。
- **B4 / B5 Sprint 2 开工阻断项**（= 设计门 **A3-9 / A3-10**，2026-09-11 随「②有条件追认」生效）：A3-9 补架构守护测试（处置 **F-2**，四层依赖方向当前无机器强制，同时是 [W-02](../tailoring-waivers.md) 关闭前置）；A3-10 消除 **F-1 / F-3 / F-4** 三处文档-代码不一致（**涉及改代码，须另开 CR**）。**2026-09-12 更新（[CR-021](../change-log.md)）**：原"A3-10 建议与契约缺口 N-1 / N-2 / N-3 合并、约 1.5~2 天"**已被超越**——三项已由发起人指令提前单独处置完毕并真机复测通过（含 **P2 缺陷 N-3**：畸形请求体返回 500 已改回 400），故 **A3-10 余量降为约 0.5 天**；本次新登记的 **N-4**（契约声明 `bearerAuth` 但 `SecurityConfig` 仍 `permitAll()`，**声明 ≠ 强制**）归入 **A3-9**，**N-5 / N-6** 建议并入 **A3-10**，两项合计约 **1 天**。详见 [next-steps.md](../next-steps.md) §1。**→ 2026-09-12 再次更新（[CR-022](../change-log.md) / [W-07](../tailoring-waivers.md)）：两项的"Sprint 2 开工阻断"截止点已被推迟至 MVP 验收后（且不晚于阶段四收尾）——Sprint 2 已在两项未闭环时开工，执行依据改为 [sprint-2.md](sprint-2.md)。推迟 ≠ 免除：W-02 的关闭前置仍是 A3-9，且本 Sprint 新增 `module/forum` 时四层依赖方向仍只能靠人工自查。**

## 测试名册（开发联调用，仅 bypass 模式）

> 学号须为 **9 位数字**（CR-013 新增格式约束）；旧的 7 位测试号已被格式校验挡下，不再可用。

| 学号 | 姓名 |
|------|------|
| 249971346 | 张三 |
| 249971347 | 李四 |
| 249971348 | 王五 |
| 249971349 | 赵六 |

**管理员账号**（dev-only 种子，与 bypass 同门控）：`admin@campuslink.local`（`SUPERADMIN`，无学号、不走核验）。
**验证码**：默认固定 `123456`（`campuslink.captcha.fixed-code`）；留空则恢复随机 6 位并只打日志。

## 变更记录

- **v1.12（2026-09-12）**——**随 [CR-021](../change-log.md)（契约缺口 N-1 / N-2 / N-3 修复）同步本文，Sprint 1 的任务范围与完成情况零改动**：① "阻塞与依赖"B4 / B5 条——原"A3-10 建议与契约缺口 N-1 / N-2 / N-3 合并为一个 CR、约 1.5~2 天"**已被超越**（三项由发起人指令提前单独处置完毕并真机复测通过，其中 **P2 缺陷 N-3** 畸形请求体返回 500 已改回 400 / `1001`），**A3-10 余量降为约 0.5 天**；**A3-9 追加本次新登记的 N-4**（契约已声明 `bearerAuth` 但 `SecurityConfig` 仍 `permitAll()`，**声明 ≠ 强制**，与 F-2 同根因），**A3-10 建议并入 N-5 / N-6**，两项合计约 **1 天**；② 出口自查 `mvn verify` 条补记 2026-09-12 的单测数 **62/62**（较 DDD 重构后的 14 个新增 48 个，本次 CR-021 贡献 17 个：`GlobalExceptionHandlerTest` 9 + `OpenApiErrorResponseCustomizerTest` 5 + `AdminRosterControllerAuthTest` 3），并保留"**均为纯单测、CI 起不了完整栈**"的限定——该项**不改变**"覆盖率不可测""静态扫描未接入"两条未满足结论，**提测准入门仍是 7 项中 4 项未满足**。**B4 / B5 仍为 Sprint 2 开工阻断项，本文其余结论未变。**
- v1.11（2026-09-11）——**编辑性修订 + 随治理签署（[CR-020](../change-log.md)）同步，任务范围与完成情况零改动**：① 🔴 **修正过期事实**——"阻塞与依赖"B1/B2 条仍写内置测试名册为 7 位学号 `2023001/2023002/2023003/2024001`，与 [CR-013](../change-log.md) 的 9 位格式约束及本文下方"测试名册"表（`249971346`~`249971349`）自相矛盾，已改为指向下表；② 出口自查第 1 项的 `2023001/张三` 补注说明其为**当时**的测试号、现已作废（该处是 2026-09-11 真机冒烟的历史记录，不改写事实只加限定）；③ **P1 UI 稿条改写**——[W-03](../tailoring-waivers.md) 已由发起人签署"接受"，裁剪正式获批（不产出交互稿 / 视觉稿），改为记录其三条替代补偿措施**均未执行**（= 设计门 A3-6 仍开放）；④ **新增 B4 / B5 条**——「②有条件追认」所附条件 1 / 2 生效为设计门 **A3-9（架构守护测试，处置 F-2，W-02 关闭前置）/ A3-10（消除 F-1 / F-3 / F-4，涉及改代码须另开 CR）**，二者为 **Sprint 2 开工阻断项**；⑤ B1/B2 条补记"关闭 bypass 全链路回归"即 W-04 补偿措施③，**签署接受不等于已执行**。

- v1.10（2026-09-11）——**CR-016：唯一键冲突翻译为业务错误**（关闭 v1.7 第 8 条登记的待办）：
  1. 新增领域异常 `AccountConflictException`（`Field{EMAIL, STUDENT_ID}`）；**`AccountRepository.save` 端口契约显式化**——适配器负责把数据库唯一键冲突翻译为领域异常，应用层不感知 JDBC / MyBatis 异常类型（保持 DIP）；
  2. `AccountRepositoryImpl` 按唯一索引名（`uk_users_student_id_hash` / `uk_users_email_hash`）识别冲突字段；**未识别的约束原样抛出**，不猜测业务语义；
  3. `register` 映射：**学号冲突 → `2101`（与其它核验失败同码同提示）**、邮箱冲突 → `2004`；
  4. 🔴 **更正 v1.7 第 8 条的表述**：该处写"应映射为'该学籍已注册'"——**这是错的**。PRD F-ACC-004 要求"学号不存在 / 姓名不匹配 / 学号已注册"返回**同一提示**；若为重复学号单独提示"已注册"，等于可据提示差异枚举出哪些学号在名册中且被占用。故改为统一 `2101`，仅邮箱保留可区分提示（邮箱非名册数据）；
  5. ✅ **实测**：重复学号注册由 `500/9999 系统繁忙` 变为 `400/2101 学籍信息校验未通过`；日志无 `unhandled exception`；事务回滚干净（冲突未留孤儿行，`users` 表计数不变）；未占用学号正常注册成功（`id=6`）；
  6. **单测 40 → 45**：新增应用层映射 2 例（`AccountApplicationServiceRegisterTest`）+ 适配器翻译 3 例（`AccountRepositoryImplConflictTest`，含"未识别约束原样抛出"）。

- v1.9（2026-09-11）——**CR-015：Redis 键统一加命名空间前缀**（关闭 v1.8 第 7 条登记的待办）：
  1. 新增 `common/redis/RedisKeys`（`PREFIX = "campuslink:"`、`of(logicalKey)`），三个适配器 `RedisCaptchaStore` / `RedisTicketStore` / `RedisRateLimitAdapter` 全部改为经它生成键；**前缀只在基础设施层补**，应用层继续只表达逻辑键（`verify:ip:<ip>`），未把 Redis 命名知识泄漏上去；
  2. 新增 `RedisKeyNamespaceTest`（4 例，Mockito 断言实际键名）防止后续适配器漏加前缀；
  3. ✅ **实测**：旧裸键名（`verify:ip:...`、`captcha:code:...`）`exists=0`，新键 `campuslink:*` 正常；并发现一个改造前残留的裸 `verify:ip:` 键（value=1、TTL 2833s）已手工清理；账号链路（核验 → 注册含票据消费 → 登录 → admin 登录）全绿，无回归。单测 36 → **40**；
  4. 说明：受影响的键**全是带 TTL 的临时键**（限流 1h / 验证码 5min / 票据 5min），故**无需数据迁移**，但改造瞬间在途的旧键会变成孤儿直到 TTL 到期。前缀取**固定常量**而非配置项，避免各环境键名漂移。
- v1.8（2026-09-11）——**CR-014：数据库变更管理改为 Flyway（DDL）+ PyMySQL（数据）**：
  1. **接入 Flyway**：`spring-boot-starter-flyway` + `flyway-mysql`（Boot 4.1.1 管理 Flyway **12.4.0**，本机仓库已缓存、可离线构建）；配置 `spring.flyway`（`locations=classpath:db/migration`、`baseline-on-migrate=false`、`clean-disabled=true`）。**实测社区版对 MySQL 8 可用**；
  2. **迁移脚本**：`backend/sql/01_schema.sql` → `V1__init_schema.sql`（12 张表，去掉 `CREATE DATABASE`/`USE`，并移除 `IF NOT EXISTS`——迁移应失败得响亮）；`02_seed_boards.sql` → `V2__seed_boards.sql`（6 版块，按"参考数据随 schema 版本化"处理）。`backend/sql/` **已删除**（内容与 git 历史双重可追溯）；
  3. **数据脚本规范**：新增 `backend/scripts/`（`README.md` 约定 + `requirements.txt`（PyMySQL 1.2.0）+ `data/_template.py`）。命名 `D<序号>__<描述>.py`；**默认 dry-run、`--apply` 才写库**；凭据只从环境变量读；事务包裹；要求幂等。`.gitignore` 已排除 `scripts/.venv/`；
  4. **`docker-compose.dev.yml` 移除 `./sql` 挂载**——容器内建表会绕过 Flyway 历史导致 `validate` 失败；
  5. ✅ **清库重建实测**（验证迁移可执行性，这是本次的关键动作）：发现并确认 **`V1` 从未被真实执行过**（原 `01_schema.sql` 一直靠手工导入）。本次 DROP 库后由 Flyway 全量执行，日志 `Successfully applied 2 migrations`，`flyway_schema_history` 记录 V1/V2 且 `success=1`；12 张业务表 + `flyway_schema_history` 齐全、6 版块中文正常、admin 种子账号已建；
  6. ✅ **功能回归**：核验 → 注册 → `/users/me`、admin 登录（`SUPERADMIN`）→ 名册导入（`inserted:1`）、匿名调用管理接口 403/4002、未注册邮箱登录统一提示 2005，全部通过；
  7. 📌 **顺带观察（未处理）**：Redis 的键**没有应用命名空间前缀**（如限流键直接是 `verify:ip:0:0:0:0:0:0:0:1`）。本机 Redis 若与其它项目共用实例，存在键冲突风险；本次因测试累积把该 IP 的限流计数打到上限（值 11 > 上限 10），是靠手工删键恢复的。建议后续给 Redis 键加统一前缀（如 `campuslink:`），登记为待办。
- v1.7（2026-09-11）——**CR-013：字段校验收紧 + dev 便利项；顺带修复注册链路缺陷**：
  1. **学号限定 9 位数字**：`StudentId` 值对象加领域不变量、`VerifyStudentCommand` 加 `@Pattern`，非法输入返回 400 + 字段级提示（实测 7/8/10 位、含字母、含符号均被拒）；**姓名限定中文名或外文名**（中文 2~16 汉字可含 `·`，外文名字母起头可含空格/`-`/`'`/`.`）；`nickname` 仍为自由文本；
  2. **dev 固定验证码 `123456`**：新增 `campuslink.captcha.fixed-code`，仅显式配置时生效，配置时启动打 WARN；未配置仍为随机 6 位。**刻意与 `CODE_SENDER_MODE` 解耦**，避免"日志模式"被隐式等同"弱口令"；
  3. **dev-only 管理员种子账号**：`admin@campuslink.local`（`SUPERADMIN`、无学号、不走核验），与 bypass 同门控、幂等；`Account.provisioned(...)` 工厂 + `AccountConverter` 的 studentId 空值保护。实测：admin 登录 → `SUPERADMIN`，可导入名册（`inserted:1`）；**普通用户与匿名调用同一接口均 403/4002**；
  4. **测试名册换为 9 位**：`249971346~249971349`（旧 7 位号会被格式校验挡下），文档已同步（AGENTS.md / backend README / 本文件）；
  5. 🔴 **发现并修复注册链路缺陷（先于本次变更存在）**：`VerificationTicketStore` 的载荷是**加密后的学号**，而 `register` 直接把该**密文**当学号使用（缺 `codec.decrypt`）。后果有两层——① 落库的 `student_id_enc` 是"密文的密文"，`student_id_hash` 是 HMAC(密文)；② **因 AES-GCM 每次密文不同，`uk_users_student_id_hash` 唯一约束从未真正命中，即"一号一账号"实际失效**。已修为 `codec::decrypt`，并补回归单测 `AccountApplicationServiceRegisterTest`（断言 save 收到的聚合携带**明文**学号、名册占用用明文哈希）。修复后实测：同一学号换邮箱重复注册会撞唯一约束（修复前静默通过）；
  6. **单测 14 → 36**：新增 `AccountCommandsValidationTest`（20 例：学号/姓名格式矩阵）、`StudentId` 格式用例、注册回归 1 例；
  7. ⚠️ **更正 v1.6 的核查结论**：v1.6 记"落库核查确认邮箱以 `*_enc` + `*_hash` 存储、无明文"——该结论对邮箱成立，**但对学号不成立**（当时落库的是密文）。当时的冒烟只验证了链路返回 200，未校验持久化字段的**语义正确性**，故漏过该缺陷。这再次印证 W-05 所记的"无集成/冒烟测试"缺口：**只看响应码不足以判定链路正确**；
  8. ⏳ **未解决的健壮性缺口（登记待办）**：重复学号现在会正确撞唯一约束，但抛出的是**未处理的 `DuplicateKeyException`**，经全局兜底返回 `9999 系统繁忙`，语义上应映射为明确的业务错误（如"该学籍已注册"）；依赖名册占用（`rosterGateway.occupy`）在 **bypass 模式下恒返回 true**、不做占用记录，故开发期无法用 bypass 预演该分支，需真实名册（B1/B2）或 DB 名册实现才能验证。
- v1.6（2026-09-11）——**首次真机启动与联调，修复 2 个启动级缺陷**（缺陷修复，非基线变更）：
  1. **`@MapperScan` 路径失效（阻断启动）**：`CampusLinkApplication` 仍写 `com.campuslink.module.*.mapper`（单段通配），而 ADR-012 重构已把 mapper 移到 `module/*/infrastructure/persistence/mapper`（多出 3 段），导致 `UserMapper` / `StudentRosterMapper` 未注册为 bean，应用启动即 `APPLICATION FAILED TO START`。修为 `com.campuslink.**.mapper`（与 AGENTS.md 约定一致），保留 `com.campuslink.common.audit`。**该缺陷自 2026-09-07 重构起即存在，但应用从未被启动过，故直到今天才暴露**；
  2. **`/actuator/health` 恒为 DOWN（误导性信号）**：`MAIL_HOST` 为空但属性存在，Spring Boot 仍注册 `MailHealthIndicator` 并去连 SMTP，抛 `AuthenticationFailedException`，使健康检查整体 DOWN。而 dev 用 `CODE_SENDER_MODE=log` 根本不发信。已加 `management.health.mail.enabled: ${MAIL_HEALTH_ENABLED:false}`（切到 mail 模式时应置 true）。修复后 `/actuator/health` 返回 `UP`；
  3. **联调结果**：`2023001/张三` 学籍核验 → 票据 → 验证码（日志模式）→ 注册（`id=1`、`verified=true`）→ 登录（JWT）→ `/users/me` 返回昵称，全链路经 Vite 5173 代理跑通；验证码重发限流按设计返回 `2002`/429；落库核查确认邮箱以 `*_enc` 密文 + `*_hash` HMAC 存储、无明文，注册写审计日志 1 条。
  > **为什么测试没拦住**：`mvn verify` 的 14 个用例全是纯领域单测，无一加载完整 Spring 上下文，因此"能否启动"与"bean 是否齐全"完全不在测试覆盖内——这是 [W-05](../tailoring-waivers.md) 所指"无集成/冒烟测试"缺口的直接后果，也说明提测准入门该项不可省。
- v1.5（2026-09-11）——**因 CR-011 / CR-012 同步状态，任务范围与完成情况零改动**：① 出口自查"CI 全绿"项由"从未运行过"改为"已具备判定条件"（`backend-ci` / `frontend-ci` 首跑均成功，[CR-011](../change-log.md)），并保留"静态扫描未接入"的限定；② 提测准入门现状由"6 项中 4 项未满足"改为"3 项未满足 + 1 项待联调验证"；③ 阻塞与依赖标题去掉"均未清零"，B3 标记闭环；④ 联调步骤改为本机 MySQL / Redis（不再引用 Docker compose 起栈），新增后端端口 8088 说明（[CR-012](../change-log.md)）。
- v1.4（2026-09-11）——**编辑性修订，任务范围与完成情况零改动**（[CR-009](../change-log.md)）：① 头部"版本"字段长期停留在 v1.0、与变更记录（已到 v1.3）矛盾，本次校正；② 补手册 4.5 要求的"状态"字段；③ 上游依据去掉硬编码版本号（原写"技术方案 v0.2 / PRD v1.1"，技术方案实际已 v0.4）；④ T1 行标注 Spring Boot 3.2 → 4.1.1 升级事实；⑤ **出口自查补齐手册 3.4 要求但原先缺失的两项**——单测覆盖率门槛与静态扫描，均如实标注"当前不可执行"（`backend/pom.xml` 无 jacoco、CI 无远程），并标注提测准入门现状为 6 项中 4 项未满足，见 [W-05](../tailoring-waivers.md)；⑥ 头部补程序说明，指向 W-02 / W-04。
- v1.3（2026-09-07）——**DDD 二次开发重构**（ADR-012）：auth / roster / user 合并为 `module/account` 限界上下文四层（domain 含聚合 Account + 值对象 EmailAddress/StudentId + 9 个端口 + 领域事件；application 用例编排 + Command；infrastructure 适配器含 RosterGateway bypass/DB 双策略与注册事件 AFTER_COMMIT 审计监听；web 契约不变）。旧包文件已备份至 `backend/.refactor-backup-auth-roster-user.zip` 后删除（**该 zip 是缺少版本控制的替代产物，远程与首次提交完成后应删除，见 [W-06](../tailoring-waivers.md)**）。`mvn verify` 14/14 全绿（+4 领域单测）。前端优化：Element Plus 按需自动引入（主包 1056KB→271KB，gzip 347→100KB）、`src/constants/boards.ts` 去重、`useCountdown` 组合式函数、`ApiError` 统一错误模型、requiresAuth 路由守卫、VITE_API_TARGET 代理端口可覆盖。
- v1.2（2026-08-31）——技术栈升级（发起人决议）：JDK 17 → **21**，Spring Boot 3.2 → **4.1.1**（starter `web`→`webmvc`；MyBatis-Plus 改用官方 `mybatis-plus-spring-boot4-starter` 3.5.17，分页拦截器配套 `mybatis-plus-jsqlparser`；springdoc 3.1.0、jjwt 0.13.0、jsoup 1.23.2）。`mvn verify` 10/10 全绿，业务代码零改动兼容。
- v1.1（2026-08-30）——T9 构建验证完成：`mvn verify` 10/10 全绿、`npm run build` 通过。过程中修复四个问题并沉淀经验：① flexmark 0.64.x 表格扩展更名为 `flexmark-ext-tables`（原 ext-gfm-tables 停更于 0.50.x），ADR-005 相关依赖已修正；② 扩展须同时注册到 Parser 与 HtmlRenderer，否则节点解析成功但渲染为空；③ AES 密钥必须定长，现对密钥材料做 SHA-256 派生（`CryptoService`）；④ 数据库 DDL 中 `posts.title` 全文索引为 MySQL ngram（ADR-009），与 MySQL 8 环境配套。
- v1.0（2026-08-30）——创建；T1~T8 完成。
