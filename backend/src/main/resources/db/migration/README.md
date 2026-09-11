# 数据库结构变更（Flyway）

> 本目录是**结构变更（DDL）的唯一来源**。应用启动时 Flyway 自动执行未应用的迁移，并记录到 `flyway_schema_history` 表（CR-014）。

## 命名规范（企业通用）

| 前缀 | 用途 | 示例 |
|------|------|------|
| `V<版本>__<描述>.sql` | **版本化迁移**，只执行一次、顺序执行（推荐，日常只用这个） | `V1__init_schema.sql`、`V3__add_post_tags.sql` |
| `R__<描述>.sql` | **可重复迁移**，校验和变化时重新执行（适合视图 / 存储过程 / 字典数据） | `R__refresh_board_view.sql` |
| `U<版本>__<描述>.sql` | **回滚脚本**——**仅 Flyway Teams/Enterprise 支持，社区版不识别**，本项目不可用 | — |

- 版本号**递增且不可复用**：`V1` → `V2` → `V3`；已删除的编号不回收。
- 描述用小写英文 + 下划线，见名知意：`add_user_avatar`、`drop_legacy_tags`。
- 双下划线 `__` 分隔版本号与描述，**单个下划线会被解析错误**。

## 三条铁律

1. **已执行的迁移不可修改。** 内容被校验和锁定，改动会在下次启动报
   `Validate failed: Migration checksum mismatch`。要改结构？**新增 `V<n+1>__`**。
2. **回滚不是"撤销迁移文件"。** 社区版没有 undo（`U__` 是付费功能）。正确做法二选一：
   - **前向回滚**：新增一个迁移把变更反向做掉，如 `V5__drop_xxx.sql`（推荐，可审计、可重放）；
   - **人工回滚脚本**：写在下面的 `rollback-manual/` 里（**注意**：该目录不在 Flyway 扫描路径内，不会被自动执行），由发布流程按《回滚预案》手工执行。
3. **不要放 `IF NOT EXISTS`。** 迁移应"失败得响亮"——对象意外存在说明库结构与迁移历史不一致，此时应停下来排查，而不是静默跳过。

## 数据变更不放这里

纯数据增删（补字典、修数据、清理脏数据）走**独立的 PyMySQL 脚本**，见 [`backend/scripts/README.md`](../../../../../scripts/README.md)。
只有**参考数据**（如内置 6 版块，见 `V2__seed_boards.sql`）跟随 schema 版本化，才算结构迁移的一部分。

## 库的创建不在 Flyway 管辖内

JDBC 连接必须先有库，因此 `CREATE DATABASE` 是一次性**引导动作**（基础设施配置），不属于迁移：

```sql
CREATE DATABASE IF NOT EXISTS campuslink
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

建库后其余全部结构由 Flyway 从 `V1` 开始构建。

## 常用操作

```bash
# 正常：启动应用即自动迁移
mvn spring-boot:run

# 查看当前库的结构版本（由 Flyway 记录）
mysql -h127.0.0.1 -ucampuslink -p campuslink \
  -e "select installed_rank, version, description, success, installed_on
      from flyway_schema_history order by installed_rank;"

# 校验（不改库，只检查迁移文件与历史是否一致）
mvn flyway:validate      # 需配置 flyway-maven-plugin，当前未接入
```

`spring.flyway.*` 配置见 `backend/src/main/resources/application.yml`。
