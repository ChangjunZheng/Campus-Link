# Campus-Link Backend

Campus-Link（重庆工程学院计算机专业学生交流论坛）服务端。技术栈见[技术方案](../docs/设计/技术方案.md)（版本号登记于 [docs/README.md](../docs/README.md) 第 2 节）：
Spring Boot 4.1 + Java 21 + MyBatis-Plus 3.5.17（Boot4 starter）+ MySQL 8 + Redis 7。

## 本地启动

前置：**JDK 21**（构建前 `set "JAVA_HOME=D:\develop\Java\jdk-21"`）、本机已运行的 **MySQL 8** 与 **Redis**。
> 开发期不使用 Docker（[CR-011](../docs/变更日志/变更台账.md#cr-011)）；`docker-compose.dev.yml` 保留，供发布阶段（阶段六）启用。

```bash
# 1. 准备依赖服务（本机原生，非 Docker）
#    - MySQL 8：127.0.0.1:3306，库 campuslink
#      仅首次需要建库（库必须先存在，Flyway 才能连上；建库属基础设施引导，不归 Flyway 管）：
#      mysql -h127.0.0.1 -uroot -p -e "CREATE DATABASE IF NOT EXISTS campuslink \
#        DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
#      ⚠️ 不再手工导入建表 SQL：表结构与 6 版块种子由 Flyway 在启动时自动执行
#    - Redis：127.0.0.1:6379（无密码）

# 2. 配置环境变量（开发默认值已在 application.yml，可跳过；或复制 .env.example 后按需导出）
cp .env.example .env

# 3. 启动应用（默认端口 8088，可用 SERVER_PORT 覆盖）
#    启动过程中 Flyway 会校验并执行 db/migration 下未应用的迁移，日志形如：
#    "Migrating schema `campuslink` to version "1 - init schema"" / "Successfully applied N migrations"
mvn spring-boot:run

# 4. 验证
# - 健康检查：http://localhost:8088/actuator/health
# - OpenAPI：http://localhost:8088/api/docs （UI: /api/docs/swagger-ui.html）
# - 当前库结构版本：select * from flyway_schema_history order by installed_rank;
```

## 数据库变更

结构变更与数据变更走两条轨（[CR-014](../docs/变更日志/变更台账.md#cr-014)）：

| 变更类型 | 放哪 | 怎么执行 |
|---------|------|---------|
| 表 / 列 / 索引 / 参考数据 | `src/main/resources/db/migration/V<n>__<描述>.sql` | 应用启动时 Flyway 自动执行，记录在 `flyway_schema_history` |
| 纯数据增删改 | `scripts/data/D<序号>__<描述>.py`（PyMySQL） | 手工执行，默认 dry-run，加 `--apply` 才写库 |

- **已执行的迁移不可修改**（校验和锁定）；回滚靠**新增前向迁移**，社区版无 undo。完整规范见 `src/main/resources/db/migration/README.md` 与 `scripts/README.md`。
- `docker-compose.dev.yml` **不再挂载 SQL 初始化目录**——容器内执行建表会绕过 Flyway 历史，导致 `validate` 失败。

## Sprint 1 已实现

- 统一响应 `ApiResponse{code,message,data,traceId}`、错误码分段（1xxx 通用 / 2xxx 账号 / 21xx 学籍 / 4xxx 权限 / 5xxx 安全）、全局异常处理
- **Markdown 渲染服务**（`common/markdown`，flexmark + jsoup 白名单，服务端单点净化防 XSS）+ XSS 用例回归测试
- 学籍核验（F-ACC-004，bypass 模式内置测试名册）+ 验证码注册 / 登录（F-ACC-001）
- JWT 鉴权骨架（Spring Security + jjwt，登录态解析；接口权限收紧在 Sprint 2+）
- 个人主页最小版 `GET /api/v1/users/me`（F-ACC-002）
- 学籍名册 CSV 导入（superadmin，写审计日志）

## 开发联调说明

- **测试名册**（`app.roster.bypass=true` 时生效，学号须为 **9 位数字**）：学号 `249971346/249971347/249971348/249971349`，姓名 `张三/李四/王五/赵六`；核验通过后返回一次性票据用于注册
- **管理员账号**（dev-only 种子，与 bypass 同门控）：`admin@campuslink.local`，角色 `SUPERADMIN`，**无学号、不走学籍核验**；用于调用 `POST /api/v1/admin/roster/import`
- **验证码**：默认固定为 `123456`（`campuslink.captcha.fixed-code`，启动打 WARN）；留空则恢复随机 6 位，随机码在 `CODE_SENDER_MODE=log` 时只打在应用日志里，不真实发送
- **入参格式**：学号 `^\d{9}$`；姓名限中文名（2~16 汉字，可含 `·`）或外文名（字母起头，可含空格 / `-` / `'` / `.`）；`nickname` 为 2~32 自由文本
- **生产红线**：`APP_ROSTER_BYPASS=false`、**`CAPTCHA_FIXED_CODE` 留空**（固定验证码等于取消验证码防线）、JWT / 加密密钥全部覆盖、名册真实导入——均为上线检查清单项

## 常用命令

```bash
mvn verify            # 编译 + 单测
mvn spring-boot:run   # 本地运行
```
