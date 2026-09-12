# 技术方案 v0.3 / v0.4 补充设计评审（追认）纪要

| 文档信息 | 内容 |
|---------|------|
| 版本 | v1.5 |
| 状态 | **已签署**（发起人 Fonzo / 2026-09-11，见 [CR-020](../change-log.md)）；结论 **②有条件追认**，追认范围 **A + B + C**——[设计门](gate-3-design.md) **A3-2 已闭环**，所附三条件生效为 **A3-9 / A3-10 / A3-11**。<br>v1.3（2026-09-12）为 [CR-021](../change-log.md) 的**状态回填**：N-1 / N-2 / N-3 已闭环、A3-9 范围含 N-4、A3-10 余量收窄——**未改动任何评审意见（含 F-1 ~ F-7）与 §6 任何签署行**，追认结论与三条件不变。<br>**v1.4（2026-09-12）为 [CR-022](../change-log.md) 的截止点回填**：条件 1 / 2（A3-9 / A3-10）的截止点由"Sprint 2 开工前（阻断）"改为"**MVP 验收后、且不晚于阶段四收尾**"（偏离登记为 [W-07](../tailoring-waivers.md)）——**三条件本身一字未改、§6 签署行一字未改、F-1 ~ F-7 意见一字未改**；条件 1 / 2 **仍是未闭环的条件**，只是截止点后移。<br>**v1.5（2026-09-12）为 Sprint 2 MVP 交付后的状态回填**：**推迟的截止点"MVP 验收后"已经到达**——条件 1（A3-9）/ 条件 2（A3-10）**仍未开工**，条件 3（A3-11）**触发条件已达成**（`module/forum` 已合入）、复评结论尚未产出。**三条件全部仍为未闭环；"截止点到达"不等于"条件已满足"**，**§6 签署行一字未改** |
| 维护人 | 技术负责人（发起人兼任） |
| 最后更新 | 2026-09-12 |
| 对应行动项 | [设计门纪要](gate-3-design.md) **A3-2**：v0.3 / v0.4 补充设计评审或发起人书面确认（含 ADR-012 已落地实现的追认） |
| 评审对象 | [技术方案](../design/tech-design.md) v0.3（[CR-007](../change-log.md)）与 v0.4（[CR-008](../change-log.md)）；v0.4 已落地的 `backend/src/main/java/com/campuslink/module/account/` 全部实现 |
| 依据 | [流程手册](../process-handbook.md) 3.3 节（设计评审门）、4.1 节（评审结论与行动项闭环）、4.2 节（变更管理）、5.1 节（裁剪）；[W-02](../tailoring-waivers.md) 补偿措施① |
| 关联记录 | [设计门纪要](gate-3-design.md) · [变更台账](../change-log.md) · [裁剪与让步放行记录](../tailoring-waivers.md) |

## 0. 本文用途与效力边界

**用途**：A3-2 要求"补充设计评审**或**发起人书面确认"。本文是这两者的合并载体——前半部分（§1~§3）是评审材料与架构评审意见，后半部分（§4~§6）是供发起人签署的书面确认页。签署后 A3-2 闭环。

**效力边界（必须先讲清楚，否则本文会成为下一处"看起来已评审"的假证据）**：

1. **形式上仍是单人评审**。手册第 2 节要求每道门 ≥ 2 名不同角色，本文由 AI 协作会话出材料、发起人一人签署，该偏离登记在 [W-01](../tailoring-waivers.md)，**本文不消除它**，只是把 W-02 补偿措施①（"AI 协作会话出一份架构评审意见并归档"）落实；
2. **这是追认，不是事前评审**。v0.3 于 2026-08-31、v0.4 于 2026-09-07 落地，本文写于 2026-09-11——**代码先于评审存在**。追认能恢复的是"决策有据可查、缺陷已被第二双眼睛看过"，不能恢复"评审拦住了错误设计"这一事前价值；
3. **证据等级**：§2 的全部事实均为 2026-09-11 对照仓库实际状态与真实构建/运行结果核实，可复现（每项给出复现方式）；§1 的"当时理由"取自文档自述的变更记录与 CR 台账，**无同期会议凭证**；
4. **一处不可复现的证据缺口**：v0.4 重构**前**的代码状态**不在 git 历史中**——首次提交 `837a22f` 晚于重构（2026-09-11 vs 2026-09-07），重构前的旧包只存在于 `backend/.refactor-backup-auth-roster-user.zip`（已被 `.gitignore` 排除，仍在仓库工作区）。因此"重构是否改变了行为"只能用**当前**测试结果与端点清单佐证，无法做前后 diff 比对。该缺口本身是 [W-06](../tailoring-waivers.md) 所记版本控制缺失的直接后果。

## 1. 追认范围

### 1.1 范围 A · v0.3（2026-08-31，[CR-007](../change-log.md)）技术栈升级

| # | 变更点 | 当时理由（文档自述） | 2026-09-11 核实结果 |
|---|-------|-------------------|-------------------|
| A-1 | JDK 17 → **21** | 升级至当前主流稳定线 | ✅ `pom.xml` `<java.version>21</java.version>`；CI workflow 亦为 Java 21 |
| A-2 | Spring Boot 3.2 → **4.1.1**（Spring Framework 7 / Security 7） | 同上 | ✅ parent `4.1.1`；`mvn verify` 通过 |
| A-3 | starter 更名 `spring-boot-starter-web` → **`-webmvc`** | Boot 4 模块化拆分 | ✅ pom 中为 `spring-boot-starter-webmvc`，并有行内注释说明 |
| A-4 | MyBatis-Plus 改用官方 **`mybatis-plus-spring-boot4-starter` 3.5.17** + 配套 `mybatis-plus-jsqlparser` | Boot 4 需用官方 boot4 starter；分页拦截器需 jsqlparser 模块 | ✅ 两个 artifact 均在 pom，版本属性 `3.5.17`；分页拦截器在 `config/MybatisPlusConfig` |
| A-5 | springdoc **3.1.0**、jjwt **0.13.0**、jsoup **1.23.2**、flexmark **0.64.8** | 适配 Boot 4 | ✅ 与 pom 版本属性逐项一致 |
| A-6 | 业务代码零改动兼容 | v0.3 变更记录自述 | 🟡 **无法验证**——重构前代码不在 git 历史（§0 第 4 条）。可间接佐证：当前对外契约与技术方案 §5 一致（§2.4） |

### 1.2 范围 B · v0.4（2026-09-07，[CR-008](../change-log.md)）账号上下文 DDD 四层重构 + 前端构建优化

| # | 变更点 | 当时理由 | 2026-09-11 核实结果 |
|---|-------|---------|-------------------|
| B-1 | `auth` / `roster` / `user` 三模块 → **`account` 限界上下文** | 事务脚本式 Service 难以承载版块/问答的复杂规则与领域事件（ADR-012 背景栏） | ✅ `module/account/` 存在，`module/` 下无 auth/roster/user 残留；技术方案 §3 模块表已同步（[CR-017](../change-log.md)） |
| B-2 | 上下文内 **DDD 四层**：domain / application / infrastructure / web | ADR-012 结论 | ✅ 四层目录齐备，文件数 18 / 5 / 18 / 4（§2.2） |
| B-3 | **端口-适配器（DIP）**：9 个出站端口定义在 domain，实现在 infrastructure | 同上 | ✅ 端口数与名称逐项核实（§2.3） |
| B-4 | 策略：`CodeSender`（log/mail）、`RosterGateway`（bypass/DB 条件装配） | 同上 | ✅ `infrastructure/notify/{LogCodeSender,MailCodeSender}`、`infrastructure/memory/InMemoryTestRosterGateway` 与 `infrastructure/persistence/RosterGatewayDbImpl` 并存 |
| B-5 | 工厂方法 `Account.registered`、观察者 `AccountRegisteredEvent`（AFTER_COMMIT 审计）、防腐层 `AccountConverter`、仓储 `AccountRepositoryImpl`、门面 `AccountApplicationService` | 同上 | ✅ 对应类均存在；`infrastructure/listener/RegistrationAuditListener` 承担事件消费 |
| B-6 | **对外 API 契约不变** | ADR-012 结论 | ✅ 6 个端点逐一核实（§2.4） |
| B-7 | 前端：Element Plus 全量引入 → **按需自动引入**（主包 1056KB → 271KB） | 构建体积 | 🟡 配置已核实（`unplugin-auto-import` + `unplugin-vue-components` + `ElementPlusResolver`）；**体积数字未复测**——本会话未执行 `npm run build`，1056/271KB 属文档自述 |
| B-8 | 前端：`constants/boards.ts` 去重、`useCountdown` 组合式函数、`ApiError` 统一错误模型、`requiresAuth` 路由守卫 | 同上 | ✅ 四个文件/机制均存在（`src/constants/boards.ts`、`src/composables/useCountdown.ts`、`src/api/client.ts`、`src/router/index.ts`） |

### 1.3 范围 C · 建议一并追认的同域后续修订

只追认 v0.3 / v0.4 会留下一个空档：**当前真正生效的设计是 v0.5 + 六个 CR 的叠加态**，其中若干条同样从未评审，且都改动了 v0.3/v0.4 所确立的工程结构或账号上下文实现。建议发起人在同一次签署中一并追认，避免下次再补一轮：

| CR | 内容 | 与 v0.3/v0.4 的关系 |
|----|------|-------------------|
| [CR-012](../change-log.md) | 开发期改本机原生 MySQL/Redis；端口 8080 → 8088 | 改 v0.3 所定运行环境的落地方式（技术方案 §8） |
| [CR-013](../change-log.md) | 学号 9 位 + 姓名中外名称校验、dev 固定验证码、dev-only 管理员种子 | 改 v0.4 `account` 上下文的领域不变量（`StudentId`）与输入契约（`VerifyStudentCommand`），并牵动 PRD F-ACC-004 |
| [CR-014](../change-log.md) | 引入 Flyway 管理 DDL + PyMySQL 数据脚本规范，删除 `backend/sql/` | 改 v0.3 技术栈（新增 2 个依赖）与数据模型落地方式 |
| [CR-015](../change-log.md) | Redis 键统一前缀 `campuslink:`（`common/redis/RedisKeys`） | 改 v0.4 infrastructure 层三个适配器的键构造；**前缀只在 infrastructure 补**属对 ADR-012 分层的一次具体诠释 |
| [CR-016](../change-log.md) | 唯一键冲突 → 领域异常 `AccountConflictException` + 端口契约显式化 | **直接扩充 v0.4 的端口契约**（`AccountRepository.save` 的 `@throws`），是 ADR-012 范式的第一次实战延伸 |
| [CR-017](../change-log.md) | 修正技术方案 D-1~D-7 | 使文档与 v0.4 实现对齐；**纯文档，不改设计决策** |

> 范围 C 的追认不等于免除各自的 CR 登记义务——六条均已在台账中登记并写了影响评估。此处追认的只是"这些改动未经设计评审即实施"这一程序偏离。

## 2. 核实证据（2026-09-11，每项均可复现）

### 2.1 构建与测试

复现：`cd backend && set "JAVA_HOME=<JDK21>" && mvn verify`

- 结果：**BUILD SUCCESS（exit 0）**，**45 个测试全绿，0 失败 0 错误 0 跳过**，分布于 7 个测试类：

| 测试类 | 用例数 | 覆盖对象 |
|-------|:-----:|---------|
| `AccountCommandsValidationTest` | 20 | application 层 Command 校验（学号 9 位、中外姓名、邮箱、昵称） |
| `MarkdownRendererTest` | 6 | `common/markdown` XSS 白名单回归（论坛安全生命线） |
| `AccountDomainModelTest` | 5 | domain 层聚合与值对象不变量 |
| `CryptoServiceTest` | 4 | `common/crypto` AES-GCM / HMAC |
| `RedisKeyNamespaceTest` | 4 | infrastructure 层三个 Redis 适配器的键前缀 |
| `AccountApplicationServiceRegisterTest` | 3 | application 层注册用例（含 2 个冲突映射） |
| `AccountRepositoryImplConflictTest` | 3 | infrastructure 层唯一键冲突翻译 |

- 与 v0.4 当时的 14/14 相比增至 45，增量来自 CR-013（+20 校验）、CR-015（+4）、CR-016（+5）及注册用例扩充。

### 2.2 分层结构实测

复现：`find backend/src/main/java/com/campuslink -type d`

```
module/account/
├── domain/          18 个文件
│   ├── model/       Account, AccountRole, AccountStatus, EmailAddress, StudentId, StudentRecord
│   ├── service/     StudentVerificationService
│   ├── gateway/     9 个出站端口（见 §2.3）
│   ├── event/       AccountRegisteredEvent
│   └── exception/   AccountConflictException
├── application/      5 个文件  AccountApplicationService, CaptchaService,
│                              RosterImportApplicationService, cmd/{AccountCommands, RosterImportResult}
├── infrastructure/  18 个文件  codec / listener / memory / notify / persistence(+mapper) / redis / security / seed
└── web/              4 个文件  AccountController, AdminRosterController, UserController, UserVo
```

上下文外：`common/` 13 个文件（result / exception / markdown / audit / crypto / redis）、`config/` 4 个、顶层 `security/` 2 个（`JwtService`、`JwtAuthenticationFilter`）。

### 2.3 端口清单（DIP 核实）

domain 层 `gateway/` 共 **9 个端口**，与 ADR-012 描述一致：`AccountRepository`、`RosterRepository`、`RosterGateway`、`CaptchaStore`、`VerificationTicketStore`、`CodeSender`、`RateLimitGateway`、`SensitiveCodec`、`TokenIssuer`。每个端口的实现均在 `infrastructure/` 下（如 `TokenIssuer` ← `infrastructure/security/JwtTokenIssuer`，`SensitiveCodec` ← `infrastructure/codec/AesGcmHmacCodec`）。

### 2.4 对外契约实测

复现：`grep -rn "RequestMapping|PostMapping|GetMapping" backend/src/main/java/com/campuslink/module/account/web/`

| 端点 | 所在 Controller | 技术方案 §5 清单 |
|------|---------------|:--------------:|
| `POST /api/v1/auth/verify-student` | `AccountController` | ✅ |
| `POST /api/v1/auth/captcha` | `AccountController` | ✅ |
| `POST /api/v1/auth/register` | `AccountController` | ✅ |
| `POST /api/v1/auth/login` | `AccountController` | ✅ |
| `POST /api/v1/admin/roster/import` | `AdminRosterController` | ✅ |
| `GET /api/v1/users/me` | `UserController` | ✅ |

**B-6"对外 API 契约不变"成立**。路径前缀在重构后由 `auth` / `roster` / `user` 三个 Controller 收敛为按上下文组织的三个 Controller，URL 未变。

### 2.5 运行期证据（本会话真机冒烟，非单测）

栈：本机原生 MySQL 8.0.43（库 `campuslink`，Flyway 迁移 V1/V2 已应用）+ Redis 8.6.2 + 后端 8088 + 前端 5173。

| 场景 | 结果 |
|------|------|
| 重复学号注册 | `400 / 2101`（与"学号不存在""姓名不匹配"同码同提示），事务回滚干净、无孤儿行 |
| 未占用学号注册 | `200`，正常落库 |
| 管理员登录 | `admin@campuslink.local` → 角色 `SUPERADMIN` |
| 名册导入 | `POST /api/v1/admin/roster/import` → `inserted: 1` |
| 权限边界 | 匿名 / 普通用户调用后台接口 → `403 / 4002` |
| Redis 键 | 全部带 `campuslink:` 前缀；改造前残留的裸键已清理 |
| 浏览器链路 | `http://localhost:5173/login` 学籍核验 → 验证码 → 注册 → 登录 → 顶栏显示昵称，跑通 |

### 2.6 依赖版本实测

`pom.xml` 与技术方案 §2.2 / A-1~A-5 逐项一致（见 §1.1 核实列）。新增于 CR-014：`spring-boot-starter-flyway` + `org.flywaydb:flyway-mysql`（Flyway 12.4.0 由 Boot 4.1.1 统一管理）。

## 3. 架构评审意见

> 本节是 [W-02](../tailoring-waivers.md) 补偿措施①要求的"一份架构评审意见"。意见针对**范式本身能否安全地复制到后续 8 个上下文**，而不只是"account 上下文能不能跑"。

### 3.1 认可的部分（有证据支撑）

1. **依赖方向真的守住了**。grep 核实：`domain/` 无任何指向 `application` / `infrastructure` / `web` 的 import；`application/` 无任何指向 `infrastructure` / `web` 的 import。这不是"文档写了四层"，是代码里可验证的单向依赖；
2. **领域事件保持纯净**。`AccountRegisteredEvent` 只 import `java.time.Instant`，Spring 的 `ApplicationEventPublisher` 留在 application 层、`@TransactionalEventListener` 留在 infrastructure 层——事件模型没被框架污染，这是很多 DDD 落地会失守的点；
3. **端口粒度合适，没有过度抽象**。9 个端口全部对应真实的外部依赖（库、缓存、邮件、加密、令牌、名册），没有出现"为了模式而模式"的空接口；`RosterGateway` 的 bypass/DB 双实现用条件装配互斥，是把"开发便利开关"隔离在 infrastructure 的正确做法；
4. **CR-016 证明范式能承接真实缺陷修复**。重复学号问题没有在 application 层 `catch DuplicateKeyException`（那会让应用层依赖 JDBC 异常类型），而是新增领域异常 + 在端口契约上声明 `@throws` + 适配器翻译——**范式的第一次实战延伸方向正确**；
5. **防枚举策略在实现层一致**。学号冲突统一到 `2101`、邮箱冲突单独 `2004`、格式违规走 `1001` 字段级提示，三者的区分理由（名册数据不可区分 / 邮箱非名册数据 / 格式错误不泄露名册信息）站得住，且已在真机验证。

### 3.2 发现的问题

| # | 问题 | 证据 | 严重度 | 建议处置 |
|---|------|------|:-----:|---------|
| **F-1** | **domain 层存在框架泄漏，与技术方案 §2.4 的表述冲突**：§2.4 写"`domain` … 仅 JDK 与自身，不依赖框架"、"领域层不依赖 Spring / MyBatis"，但 `domain/service/StudentVerificationService` 带 `@Service`（Spring stereotype）。Lombok（`@Getter` / `@RequiredArgsConstructor`）为编译期注解，可不计 | `grep -rn "^import" module/account/domain/` | 中 | 二选一：① 去掉 `@Service`，改由 `config/` 中的 `@Bean` 装配（保持 domain 纯净，成本约 10 分钟）；② 修订 §2.4 措辞为"不依赖 Spring 的**运行时 API 与容器抽象**，允许 stereotype 注解"。**建议 ①**——ADR-012 的卖点是 domain 可脱离框架单测，`AccountDomainModelTest` 已证明这一点值得保持 |
| **F-2** | **无任何架构守护**：pom 无 ArchUnit，测试目录无分层/边界断言。四层依赖方向、"跨上下文只调对方 application"、"端口定义在 domain"、"Mapper 只在 `infrastructure/**/mapper`" **全部只靠人工自觉**；`@MapperScan("com.campuslink.**.mapper")` 是宽松通配，写错包也能扫到 | `grep -rn archunit backend/pom.xml` → 无 | **中（对范式复制是最高风险项）** | Sprint 2 开工前加一个架构守护测试（ArchUnit 约 6 条规则，或无反射依赖的轻量包名断言），成本约半天。**这是 ADR-012 风险②"范式若有误将放大至整个后端"的唯一技术性补偿**——目前该风险只由文档约定承担 |
| **F-3** | **`@MapperScan` 实际值与文档不符**：`CampusLinkApplication` 为 `@MapperScan({"com.campuslink.**.mapper", "com.campuslink.common.audit"})`，技术方案 §2.4 与 `AGENTS.md` 均只写前者。原因是 `AuditMapper` 位于 `common/audit/` 而非 `.mapper` 包 | `grep -rn MapperScan` | 低 | 二选一：① 把 `AuditMapper` 移到 `common/audit/mapper/`，让通配规则重新成立（推荐，消除特例）；② 文档补写第二个扫描路径。无论哪种，**文档与代码当前不一致**这一事实须消除 |
| **F-4** | **前端目录树与实现不符**（§2.4 frontend 块）：列出的 `utils/`（highlight.js 初始化）**不存在**；实际存在的 `composables/`、`constants/`、`types.ts` **未列出**；`components/` 实际仅 `Placeholder.vue`（文档描述的"帖子渲染、Markdown 编辑器、楼层列表"属 Sprint 2+ 计划） | `find frontend/src -maxdepth 2` | 低 | §2.4 标题为"结构**建议**"，计划中的目录可保留，但应**标注"（计划）"与"（已实现）"**，并补上已存在的 `composables/` / `constants/`。highlight.js 尚未接入是事实（Sprint 2 帖子渲染才需要），不应写成既成结构 |
| **F-5** | **单一上下文样本不足以证明范式**：`account` 是全系统**最简单**的上下文——无复杂列表查询、无跨上下文协作、无状态机、领域事件只有一个消费方。真正会压测四层范式的是 `post`（热榜计算、楼层树、`content_html` 落库）、`qa`（采纳状态机、跨上下文通知）、`moderation`（机审 fail-closed、处置工单跨上下文） | §2.2 文件分布 + 技术方案 §3 模块表 | 中 | **不建议现在改范式**。建议把"首个新上下文（Sprint 2 的 board/post）落地后复评一次四层范式"设为条件——届时才有跨上下文调用与复杂查询的真实样本 |
| **F-6** | **测试分布不均，追认不能替代提测门指标**：45 个用例集中在 domain / application / markdown / crypto；**web 层（3 个 Controller）0 测试**、顶层 `security/`（`JwtService`、`JwtAuthenticationFilter`）0 测试、`config/` 0 测试；覆盖率因无 jacoco **不可测**（[W-05](../tailoring-waivers.md)） | `find backend/src/test -name '*.java'` → 7 个类 | 低（对 A3-2）／中（对提测门） | 本文不构成对提测准入门任何一项的满足。JWT 签发与鉴权过滤器属安全关键路径，建议 Sprint 2 内补测 |
| **F-7** | **v0.3 的升级决策缺少候选对比**：变更记录与 CR-007 的理由均为"升级至当前主流稳定线"，未评估"留在 Boot 3.2（LTS 线）"的成本收益。而 A-4 本身已经是一次该风险的实际兑现——MyBatis-Plus 必须换用专门的 boot4 starter 且分页拦截器要额外配 jsqlparser 模块；springdoc 也必须跟着换版本线 | §1.1 A-4 / A-5；[CR-007](../change-log.md) 变更评审栏"❌ 未评审" | 低 | 追认时**补记为已知技术风险**：Boot 4.1.x 为较新的大版本线，后续若某依赖（如对象存储 SDK、内容安全 SDK——两者都在 P1/阶段六才引入）无 boot4 适配版本，将没有退路。建议在引入机审 SDK 与 OSS SDK 前各做一次兼容性检查点 |

> **F-1 ~ F-7 中没有任何一项要求回退 v0.4 的重构**。四项（F-1/F-3/F-4 + F-7 的补记）是文档与代码对齐，一项（F-2）是补一个守护测试，两项（F-5/F-6）是设定后续复评点。

### 3.3 与手册 3.3 出口标准的对照

本次追认**能**与**不能**满足的出口标准，须分清：

| # | 出口标准（手册 3.3） | 本文签署后的状态 |
|---|---------------------|----------------|
| 1 | 技术选型有对比结论，无未决重大技术风险 | 🟡 **部分**：v0.4 的架构选型有 ADR-012 完整记录（背景/备选/结论）；v0.3 的升级理由偏弱（F-7）；ADR-011 部署形态与机审服务商**仍未决**（A3-8） |
| 2 | 数据模型与接口契约评审通过 | ❌ **仍不满足**：A3-4（OpenAPI 快照 / 请求响应结构）**已于同日闭环**（[CR-019](../change-log.md)，[快照与说明](../design/api/README.md)），但快照只覆盖 Sprint 1 已实现的 6 个端点；A3-5（ER 图 + 数据量级）**完全未做**。本文只核实了"契约与代码一致"，**不等于契约本身经评审**。<br>**2026-09-12 状态回填（[CR-021](../change-log.md)，不改本行结论）**：归档时实测出的 **N-1 / N-2 / N-3** 三项契约缺口（N-3 为 P2 缺陷）**已全部修复并真机复测通过**，快照已再生成（6 端点 / **14** schema，含 `ApiError` 与 `bearerAuth`）；**但本项仍为 ❌**——前两条理由未变，且本次**新登记 N-4**（契约声明了鉴权而 `SecurityConfig` 仍 `permitAll()`，**声明 ≠ 强制**，归口 A3-9）/ **N-5**（归口 A3-10）/ **N-6**（P3）。<br>**2026-09-12 MVP 交付后状态回填（[CR-022](../change-log.md) 后续实施记录，不改本行结论）**：`module/forum` 6 个端点交付，快照**再次再生成**至 **12 端点 / 30 schema**（覆盖全部已实现端点），技术方案 §5 同步补入论坛端点与 `page/size` 口径；**本项仍为 ❌**——① §5 表仍有 6 行计划项未设计实现，② A3-5 ER 图与量级预估仍未做，③ N-4 / N-5 / N-6 均未处置 |
| 3 | UI 稿走查通过 | ❌ **仍不满足**：[W-03](../tailoring-waivers.md) **已于同日由发起人签署"接受"**（[CR-020](../change-log.md)），"以 Element Plus 直接拼页面替代交互稿 + 视觉稿"的书面裁剪就此成立；但 **A3-6 仍开放**——W-03 补偿②（页面截图 + 页面清单归档 `docs/design/ui/`）未做、该目录尚不存在，补偿③（提测前逐页走查）时点未到。**裁剪获批 ≠ 走查通过**，手册 3.3 的这条出口标准依旧未满足 |
| 4 | 开发可直接依据文档开工，无需口头补充约定 | 🟡 **接近满足**：D-1（阻断项）已由 CR-017 修正；剩余 F-1 / F-3 / F-4 三处轻微不一致**已生效为行动项 A3-10**（Sprint 2 开工~~**阻断**~~项 → **截止点已于 2026-09-12 由 [CR-022](../change-log.md) 推迟**），修正后即可主张满足——**但当前仍为 🟡，因三处不一致在签署时依然存在，且截至 2026-09-12 仍未修正** |

**结论：签署本文只闭环 A3-2，设计门整体仍是"有条件通过 · 行动项未闭环"**（**A3-5 ~ A3-11 共 7 项仍开放**，其中 A3-9 / A3-10 因选 ② 而新增），[W-02](../tailoring-waivers.md) 所记"设计门行动项未闭环即启动阶段四"的偏离性质亦不因本文而消失——**W-02 已于同日由发起人签署"接受"，但仍不得关闭**：其关闭前置是 A3-9（架构守护测试）落地，而非单条 A3-2 的闭环。
>
> **→ 2026-09-12 补充（不改动上方结论）**：A3-9 / A3-10 的"Sprint 2 开工**阻断**"截止点已由 [CR-022](../change-log.md) 推迟至"MVP 验收后、且不晚于阶段四收尾"（登记为 [W-07](../tailoring-waivers.md)）。**上方结论的每一句仍然成立**——七项行动项仍是七项、仍全部开放，设计门仍是"有条件通过 · 行动项未闭环"，**W-02 仍不得关闭**；变的只是 A3-9 / A3-10 的**排队位置**。

## 4. 追认结论（供发起人三选一）

| 选项 | 含义 | 代价与后果 |
|------|------|-----------|
| **① 无条件追认** | 认可 v0.3 / v0.4 与 ADR-012 已落地实现，A3-2 直接闭环，F-1~F-7 仅作备查 | 最快；但 F-2（无架构守护）会被带入后续 8 个上下文的开发，范式错误只能靠人工发现 |
| **② 有条件追认（技术负责人建议）** | 认可现状**不回退**，A3-2 闭环，同时附三个条件并登记为新行动项：<br>**条件 1**：Sprint 2 开工前补架构守护测试（F-2）；<br>**条件 2**：Sprint 2 开工前消除 F-1 / F-3 / F-4 三处文档-代码不一致（F-7 补记为已知风险）；<br>**条件 3**：首个新上下文（board / post）落地后，对四层范式做一次复评（F-5），结论追加到本文 | 多约 1 天工作量；换来的是"范式被复制前有机器守护 + 有真实样本复评"，与 ADR-012 风险②的补偿直接对应 |
| **③ 拒绝追认** | 要求补开正式设计评审会，或回退 `module/account` 至事务脚本结构 | **不建议**：回退意味着丢弃 45 个测试中针对四层结构的部分、重写已跑通的账号链路，且 Sprint 2 停摆；补开评审会则需外部第二角色（单人项目无人可召，成本即 W-01 所述） |

> ✅ **已执行**：发起人选择 ②，三个条件已按上表约定登记为 [设计门纪要](gate-3-design.md) §7 的 **A3-9 / A3-10 / A3-11**（责任人：技术负责人；截止：条件 1、2 为 Sprint 2 开工前并标注"阻断"，条件 3 为 board / post 上下文合入后）。编制本文时**刻意不预先创建这三行**，以免发起人选择 ① 或 ③ 时留下无效行动项——现选择已定，三行生效。

## 5. 不受本文影响的开放项（避免误读为"设计门已通过"）

| 类别 | 开放项 |
|------|-------|
| 设计门行动项 | **A3-2 已于 2026-09-11 随本文签署闭环**。仍开放 **7 项**：A3-5（ER 图 + 数据量级预估）· A3-6（UI 稿走查；W-03 书面裁剪已签署，但页面截图归档与走查未做）· A3-7（技术方案文末检查清单剩余 6 项；**其"Sprint 2 开工前"截止点已随 [CR-022](../change-log.md) 失效**）· A3-8（部署形态 ADR-011 + 机审服务商选型）· **A3-9**（架构守护测试，条件 1；**2026-09-12 起范围含 N-4**）· **A3-10**（消除 F-1 / F-3 / F-4，条件 2，涉改代码须另开 CR；**余量于 2026-09-12 收窄至约 0.5 天**，见下行）· **A3-11**（board / post 合入后复评四层范式，条件 3）。（A3-4 已于同日随 [CR-019](../change-log.md) 闭环）<br>**⚠️ 2026-09-12 截止点变更（[CR-022](../change-log.md) / [W-07](../tailoring-waivers.md)）：条件 1、条件 2 对应的 A3-9 / A3-10 由"Sprint 2 开工前（阻断）"改为"MVP 验收后、且不晚于阶段四收尾"**——发起人决定改按最小 MVP 推进，**Sprint 2 在两项未闭环时开工**。<br>**这不改变本文的评审结论与追认效力**：条件 1、2 仍是未闭环的条件，**只是截止点后移**；**A3-9 仍是 [W-02](../tailoring-waivers.md) 的关闭前置**（推迟 ≠ 免除）；**条件 3（A3-11）的触发点反而更近**——MVP 一交付即触发，**不得再延期**。<br>**⚠️ 2026-09-12 MVP 交付后（同上 CR-022 后续实施记录）**：推迟的截止点**已经到达**——**条件 1 / 2 仍未开工**（`SecurityConfig` 一行未动、F-1 / F-3 / F-4 一行未改），**条件 3 的触发条件已达成**（`module/forum` 已合入，见 [sprint-2.md](../development/sprint-2.md) §5），**复评结论尚未产出**。三条件**全部仍为未闭环** ——**"截止点到达"不等于"条件已满足"** |
| 接口契约缺口 | ~~**N-1 / N-2 / N-3**~~ **✅ 已于 2026-09-12 全部闭环**（[CR-021](../change-log.md)）：错误响应与 `bearerAuth` 已进入契约、畸形请求体由 **500 / `9999`** 改回 **400 / `1001`** 并降日志档位（**P2 缺陷关闭**），7 个错误响应真机复测通过、快照已再生成。**原"建议与 A3-10 合并为同一个 CR"的处置已被超越**——发起人指令提前单独处置，故 A3-10 只剩 F-1 / F-3 / F-4（约 0.5 天）。<br>**本次新登记三项，仍开放**：**N-4** 🔴（契约声明了 `bearerAuth` 但 `SecurityConfig` 仍 `permitAll()`，**声明 ≠ 强制**，与 F-2 同根因，归口 **A3-9**）· **N-5**（技术方案 §5 鉴权表述与实现机制不符，归口 **A3-10**）· **N-6**（名册 CSV 表头识别只认中文，**P3**，未修）。逐项见 [api/README.md](../design/api/README.md) §6.2 |
| 流程偏离 | [W-01 ~ W-06](../tailoring-waivers.md) **已于 2026-09-11 全部签署**（[CR-020](../change-log.md)）：W-01 ~ W-05 签"接受"、**W-06 签"接受"并正式关闭**。**但 W-02 与 W-05 仍为开放状态、不得关闭**——W-02 的关闭前置是 A3-9 落地，W-05 的关闭前置是 jacoco / 静态扫描 / 书面 Code Review 三项补齐。各条**未做的补偿措施不因签署而自动完成**（W-03 ①②、W-04 ③、W-05 ②③④、W-06 ②③） |
| 提测准入门 | 覆盖率不可测（无 jacoco）· 静态扫描未接入 · Code Review 未闭环 · CI 缺扫描环节（4 项未满足） |
| 外部依赖 | **B1 / B2 学籍名册**仍未清零——真实名册到位后须做一次"关闭 `app.roster.bypass` 的全链路回归"；届时 `RosterGatewayDbImpl` 路径才是首次被真实数据验证（本次冒烟走的是 bypass 与 DB 名册导入两条路径，但名册数据为测试数据） |
| 生产红线 | `app.roster.bypass=false`、`campuslink.captcha.fixed-code` 留空、密钥全覆盖、审计日志、机审 fail-closed——均须由上线检查清单拦截，**当前无技术强制**（CR-013 遗留敞口③） |

## 6. 签署

> 按 [W-01](../tailoring-waivers.md) 补偿措施①"每道门检查清单由发起人**逐角色分别签署**并注明兼任身份"，以下四行由同一人分别以不同角色签署，形式上保留多角色视角。

**发起人决定**（三选一，见 §4）：

☐ ① 无条件追认　　☑ **② 有条件追认**（附条件：**条件 1** Sprint 2 开工前补架构守护测试，机器强制 ADR-012 四层依赖方向与跨上下文调用规则（处置 F-2）；**条件 2** Sprint 2 开工前消除 F-1 / F-3 / F-4 三处文档-代码不一致，F-7 补记为已知风险、不要求整改；**条件 3** 首个新上下文 board / post 落地后对四层范式做一次复评（处置 F-5），结论追加到本文）　　☐ ③ 拒绝追认，处置方式：＿＿＿＿＿＿＿＿＿＿

> 三个条件已按 §4 表末约定登记为[设计门纪要](gate-3-design.md) §7 的 **A3-9 / A3-10 / A3-11**（责任人：技术负责人；截止：A3-9 / A3-10 为 Sprint 2 开工前且标注"阻断"，A3-11 为 board / post 上下文合入后）。**A3-9 同时是 [W-02](../tailoring-waivers.md) 的关闭前置**。
>
> **→ 2026-09-12 补充（不修改上方任何签署内容）**：条件 1、条件 2 的**截止点已由 [CR-022](../change-log.md) 改为"MVP 验收后、且不晚于阶段四收尾"**，偏离登记为 [W-07](../tailoring-waivers.md)——即**同一发起人在 24 小时内先设条件、后自行推翻该条件**。**上方"发起人决定"与四行签署保持原文不改**（签署效力不因截止点后移而消灭）：条件 1、2 仍是**未闭环**的条件，条件 3 的触发点未变且更近。
>
> **→ 2026-09-12 MVP 交付后再补充（不修改上方任何签署内容）**：Sprint 2 MVP 已交付并完成 A1~A5 真机验收（[sprint-2.md](../development/sprint-2.md) §5），**推迟的截止点"MVP 验收后"就此到达**。三个条件的现状：**条件 1（A3-9）** 未开工——本 Sprint 的两个受保护端点由**手写鉴权 + 各 1 个「匿名 → 401」单测**守住，N-4 的机制敞口本身未变；**条件 2（A3-10）** 未开工——F-1 / F-3 / F-4 一行未改；**条件 3（A3-11）** **触发条件已达成**，复评结论**尚未产出**，按 [CR-022](../change-log.md) 的约定**不得再延期**。**如实记载**：MVP 交付过程中，Sprint 2 计划 §4 的三条人工自查**全部执行且未发现违规**（依赖方向、两端点鉴权、快照再生成），但这**替代不了**条件 1 所要求的机器强制——**"人工兜住了"不能当作"条件已满足"**。

**追认范围勾选**：☑ **范围 A**（v0.3 / CR-007）　☑ **范围 B**（v0.4 / CR-008）　☑ **范围 C**（CR-012 ~ CR-017，见 §1.3）

| 角色 | 姓名 | 签署 | 日期 | 意见 |
|------|------|------|------|------|
| 项目发起人 | **Fonzo** | ☑ 已签署 | 2026-09-11 | 选 **②有条件追认**，范围 A + B + C 全部追认。认可 v0.3 / v0.4 与 ADR-012 的现状、**不回退** `module/account` 四层结构；A3-2 就此闭环。本人知悉并确认：本文是**事后追认**，不恢复事前评审的拦截价值，v0.4 的 DDD 重构仍属"先改代码、后补文档"；亦知悉 §0 效力边界——形式上仍是单人评审，[W-01](../tailoring-waivers.md) 所记偏离**不因本文消除**。三个条件作为 Sprint 2 开工阻断项接受，排期影响（约 1 天，与 N-1~N-3 合计 1.5~2 天）已在 [CR-020](../change-log.md) 影响评估中登记。 |
| 架构师 / 技术负责人（发起人兼任） | **Fonzo** | ☑ 已签署 | 2026-09-11 | §3.1 认可的五项（四层落地一致、DIP 端口方向正确、敏感字段加密与哈希分离、Markdown 白名单单一出口、Redis 键命名空间）经 §2 证据核实成立，同意追认。**但 F-2 是本范式的致命敞口**：`@MapperScan("com.campuslink.**.mapper")` 为宽松通配、写错包也能扫到，四层依赖方向与"跨上下文只调对方 application"当前**零机器强制**，只靠 AGENTS.md 的人工自查——范式在被复制到 board / post / qa 之前必须有守护测试，故**条件 1（A3-9）不可降级为"建议"**。F-5（范式仅被一个上下文验证）由条件 3（A3-11）承接。 |
| 开发负责人（发起人兼任） | **Fonzo** | ☑ 已签署 | 2026-09-11 | 现状不回退，`module/account` 已跑通的账号链路（注册 / 学籍核验 / 登录 / 个人主页）保留。条件 2（A3-10）中 **F-1 去掉 domain 层 `@Service`** 与 **F-3 将 `AuditMapper` 移入 `.mapper` 包**属**改代码**，按"先登记后实施"须另开 CR；F-4（前端目录树列了不存在的 `utils/`、漏了 `composables/` 与 `constants/`）为纯文档修正。建议与 N-1 / N-2 / N-3 三项契约缺口**合并为同一个 CR** 实施，避免重复起栈验证。 |
| 测试负责人（发起人兼任） | **Fonzo** | ☑ 已签署 | 2026-09-11 | §2.1 的 `mvn verify` **45/45 全绿**与 §2.5 的 7 场景真机冒烟已由本人复核，证据可复现。如实标注三点测试侧缺口：① **覆盖率不可测**（无 jacoco），"核心模块 80%"无法主张；② **无架构守护测试**，四层依赖方向被破坏时现有 45 个单测**不会失败**——这正是 A3-9 要补的；③ [W-05](../tailoring-waivers.md) 所记 Code Review 未闭环，本人是唯一的测试角色，无独立验证视角。 |

**签署后的联动动作**（由项目经理执行，不需再次确认）：

1. ✅ [设计门纪要](gate-3-design.md) A3-2 置 ☑ 并链接本文；因选 ②，已同时新增 A3-9 / A3-10 / A3-11；
2. ✅ [技术方案](../design/tech-design.md) 头部"评审状态"改为如实描述（v0.3 / v0.4 已于本文追认，形式为单人书面确认，非评审会）；
3. ✅ [W-02](../tailoring-waivers.md) 补偿措施①标记已执行并链接本文；
4. ✅ [docs/README.md](../README.md) §1 阶段门状态表与 §4 同步；
5. ⬜ **待执行**：条件 2 涉及改代码（F-1 去 `@Service`、F-3 移 `AuditMapper`），按"先登记后实施"**须新开 CR**——本次签署**不构成实施授权**，仅确立了 A3-10 这一行动项。

> 材料编制：Qoder 协作会话，2026-09-11。§2 全部事实经对照仓库文件、真实构建输出与真机运行结果核实；§3 意见基于同一批证据，非推测。**本文已于 2026-09-11 由发起人签署，产生流程效力：A3-2 闭环，A3-9 / A3-10 / A3-11 生效**。签署前的"不产生任何流程效力"声明就此失效；§1 ~ §5 的评审材料与意见**未因签署而改动一个字**。

## 7. 变更记录

- v1.0（2026-09-11）——创建。响应设计门行动项 A3-2 与 W-02 补偿措施①，提供 v0.3（CR-007）/ v0.4（CR-008）的追认范围、可复现核实证据、架构评审意见（认可 5 项 + 问题 F-1~F-7）、三选一结论建议与逐角色签署页。登记于 [CR-018](../change-log.md)。
- v1.1（2026-09-11）——**事实回填，不改动任何评审意见**：同日 [CR-019](../change-log.md) 闭环了设计门 **A3-4**（OpenAPI 快照归档），故 ① §3.3 出口标准第 2 行改为"A3-4 已闭环、A3-5 未做"（**结论仍为 ❌ 未满足**）；② §3.3 末尾结论句的开放范围由"A3-4 ~ A3-8"收窄为"A3-5 ~ A3-8"；③ §5 开放项表移除 A3-4，并**新增一行"接口契约缺口 N-1 / N-2 / N-3"**——这是归档快照时实测出的新事实（N-3 为 **P2 缺陷**：畸形请求体返回 500 / `9999`），须另开 CR 修复。追认范围、§2 证据、F-1~F-7 意见、§4 三选一建议与 §6 签署页**均未改动**，本文仍**待签署**。
- v1.2（2026-09-11）——**发起人签署，本文产生流程效力（[CR-020](../change-log.md)）**：① §6 发起人决定勾选 **②有条件追认**（三个条件原文写入附条件栏）、追认范围勾选 **A + B + C**、四行角色签署由 Fonzo 以兼任身份分别填写并各出具该角色视角的意见；"签署后的联动动作"1 ~ 4 标记 ✅ 已执行、第 5 项标记 ⬜ 待执行（**签署不构成改代码的实施授权**，仅确立 A3-10）；② 头部状态改为"已签署"并注明结论与 A3-2 闭环；③ §3.3 出口标准第 3 行据实更新（W-03 已签署、书面裁剪成立，但 **A3-6 仍开放**，裁剪获批 ≠ 走查通过，**结论仍为 ❌**）、第 4 行注明剩余三处不一致已生效为 **A3-10**（**仍为 🟡**）、结论句开放范围由"A3-5 ~ A3-8"扩为"**A3-5 ~ A3-11 共 7 项**"并补记 W-02 已签署但不得关闭（关闭前置为 A3-9）；④ §4 表末说明改为"✅ 已执行"（三行已在设计门 §7 创建）；⑤ §5 开放项表的"设计门行动项"与"流程偏离"两行据签署结果重写。**§1 追认范围、§2 核实证据、§3.1 认可五项、§3.2 问题 F-1 ~ F-7 与 §4 三选一建议均未改动一个字**——签署只填写了结论，未回头修饰意见。
- **v1.3（2026-09-12）**——**[CR-021](../change-log.md) 状态回填（N-1 / N-2 / N-3 闭环），不改动任何评审意见与签署内容**：① §3.3 出口标准第 2 行——原列的不满足理由中"归档时实测出 N-1 / N-2 / N-3"已过期，改为单列一段"**2026-09-12 状态回填**"，记录三项**已全部修复并真机复测通过**、快照再生成为 6 端点 / **14** schema；**本行结论维持 ❌**（前两条理由未变：仅覆盖 6 端点、A3-5 ER 图未做；第三条换成本次新登记的 **N-4 / N-5 / N-6**）；② §5 开放项表——"**接口契约缺口**"行由开放改为 **✅ 已闭环**，并如实记载**原"与 A3-10 合并为同一个 CR"的处置建议已被超越**（发起人指令提前单独处置，A3-10 只剩 F-1 / F-3 / F-4），同时列出新登记的 **N-4** 🔴 / **N-5** / **N-6** 及其归口；"设计门行动项"行补记 **A3-9 范围含 N-4**、**A3-10 余量收窄至约 0.5 天**；③ 头部版本与最后更新。**未改动**：§1 追认范围、§2 核实证据、§3.1 认可五项、**§3.2 问题 F-1 ~ F-7**、§4 三选一建议、§6 全部签署行与"签署后的联动动作"（第 5 项**仍为 ⬜ 待执行**——A3-10 的 F-1 / F-3 / F-4 代码改动一行未动）。**追认结论与三个条件不变，A3-9 / A3-10 仍为 Sprint 2 开工阻断项，设计门仍不得视为通过。**
- **v1.4（2026-09-12）**——**[CR-022](../change-log.md) 截止点回填（条件 1 / 2 的 Sprint 2 前置被推迟）**，**属状态回填，不含任何新评审意见**：① 头部版本与状态栏据实回填；② §5 开放项表的"设计门行动项"行由"A3-9 / A3-10 为 Sprint 2 开工**阻断**"改为**截止点已改**，并补一段强调"**不改变本文评审结论与追认效力**（条件 1 / 2 仍是未闭环的条件，只是截止点后移；**A3-9 仍是 W-02 的关闭前置**；条件 3 的触发点更近、**不得再延期**）"；③ §6 签署页在四行签署之后**追加一段说明**（不改动任何原有文字）：条件 1 / 2 截止点已由 [CR-022](../change-log.md) 改为"MVP 验收后、且不晚于阶段四收尾"，偏离登记为 [W-07](../tailoring-waivers.md)，并如实指出"**同一发起人在 24 小时内先设条件、后自行推翻该条件**"，同时明确**签署效力不因截止点后移而消灭**。**§1 追认范围、§2 核实证据、§3.1 认可五项、§3.2 问题 F-1 ~ F-7、§4 三选一建议与 §6 四行签署全部一字未改。**
- **v1.5（2026-09-12）**——**Sprint 2 MVP 交付后的状态回填**（[CR-022](../change-log.md) 后续实施记录 / [sprint-2.md](../development/sprint-2.md) §5），**属状态回填，不含任何新评审意见**：① 头部版本与状态栏据实回填；② §3.3 出口标准第 2 行补记快照已再生成至 **12 端点 / 30 schema**（**本行结论维持 ❌**，三条不满足理由据实换新）；③ §5 开放项表的"设计门行动项"行补记——**推迟的截止点已经到达**（条件 1 / 2 仍未开工、条件 3 触发条件已达成但复评未产出），并写明"**'截止点到达'不等于'条件已满足'**"；④ §6 签署页再追加一段说明（同上，不改动任何原有文字），如实指出 Sprint 2 §4 的三条人工自查**全部执行且未发现违规**，但"**人工兜住了**"**替代不了**条件 1 要求的机器强制。**§1 追认范围、§2 核实证据、§3.1 认可五项、§3.2 问题 F-1 ~ F-7、§4 三选一建议与 §6 四行签署全部一字未改**；条件 1（A3-9）仍是 [W-02](../tailoring-waivers.md) 的关闭前置，**未因 MVP 交付而免除**。
