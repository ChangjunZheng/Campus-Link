# Campus-Link Backend

Campus-Link（重庆工程学院计算机专业学生交流论坛）服务端。技术栈见[技术方案](../docs/design/tech-design.md)（版本号登记于 [docs/README.md](../docs/README.md) 第 2 节）：
Spring Boot 4.1 + Java 21 + MyBatis-Plus 3.5.17（Boot4 starter）+ MySQL 8 + Redis 7。

## 本地启动

前置：**JDK 21**（构建前 `set "JAVA_HOME=D:\develop\Java\jdk-21"`）、Docker（或本地已有 MySQL 8 / Redis 7）。

```bash
# 1. 启动 MySQL 8 + Redis 7（首次启动自动执行 sql/01_schema.sql 与 02_seed_boards.sql）
docker compose -f docker-compose.dev.yml up -d

# 2. 配置环境变量（开发默认值已在 application.yml，可跳过；或复制 .env.example 后按需导出）
cp .env.example .env

# 3. 启动应用
mvn spring-boot:run

# 4. 验证
# - 健康检查：http://localhost:8080/actuator/health
# - OpenAPI：http://localhost:8080/api/docs （UI: /api/docs/swagger-ui.html）
```

## Sprint 1 已实现

- 统一响应 `ApiResponse{code,message,data,traceId}`、错误码分段（1xxx 通用 / 2xxx 账号 / 21xx 学籍 / 4xxx 权限 / 5xxx 安全）、全局异常处理
- **Markdown 渲染服务**（`common/markdown`，flexmark + jsoup 白名单，服务端单点净化防 XSS）+ XSS 用例回归测试
- 学籍核验（F-ACC-004，bypass 模式内置测试名册）+ 验证码注册 / 登录（F-ACC-001）
- JWT 鉴权骨架（Spring Security + jjwt，登录态解析；接口权限收紧在 Sprint 2+）
- 个人主页最小版 `GET /api/v1/users/me`（F-ACC-002）
- 学籍名册 CSV 导入（superadmin，写审计日志）

## 开发联调说明

- **测试名册**（`app.roster.bypass=true` 时生效）：学号 `2023001/2023002/2023003/2024001`，姓名 `张三/李四/王五/赵六`；核验通过后返回一次性票据用于注册
- **验证码**：`CODE_SENDER_MODE=log` 时验证码只打在应用日志里，不真实发送
- **生产红线**：`APP_ROSTER_BYPASS=false`、JWT / 加密密钥全部覆盖、名册真实导入——均为上线检查清单项

## 常用命令

```bash
mvn verify            # 编译 + 单测
mvn spring-boot:run   # 本地运行
```
