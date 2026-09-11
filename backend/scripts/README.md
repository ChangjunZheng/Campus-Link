# 数据变更脚本（PyMySQL）

> 本目录承担**纯数据**的增删改（补字典、修数据、清理脏数据），与**结构变更**（`src/main/resources/db/migration/` 下的 Flyway 迁移）严格分离（CR-014）。

## 为什么分两条轨

| 轨 | 工具 | 管什么 | 有版本记录吗 |
|----|------|--------|-------------|
| 结构轨 | **Flyway**（`db/migration/V*.sql`） | 建表 / 改列 / 加索引 / 参考数据 | ✅ `flyway_schema_history`，启动自动执行 |
| 数据轨 | **PyMySQL**（本目录 `D*.py`） | 业务数据的插入 / 修正 / 删除 | ❌ 无自动记录，**顺序与幂等由操作者负责** |

数据轨没有 `flyway_schema_history` 这样的自动校验，所以每条脚本必须自带**幂等判断**与**dry-run**，并由本文件记录执行情况（见下方"执行记录"）。

## 命名规范

```
D<序号>__<描述>.py        例：D001__backfill_user_grade.py
```

- `D` 前缀 + 三位递增序号 + 双下划线 + 小写英文描述，与 Flyway 的 `V/R/U` 风格一致、一眼看出先后顺序。
- 序号**递增且不可复用**；已删除的编号不回收。
- `_template.py` 是模板，不是迁移，新建脚本请复制它改名。

## 硬性约定

1. **默认 dry-run**：不加参数只**打印将要执行的 SQL 与影响行数**，不做任何写入；确认无误后加 `--apply` 才真正提交。
2. **凭据只从环境变量读**（`DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD`），**严禁硬编码**——仓库、示例、测试都不得出现可用凭据字面量（AGENTS.md 红线）。
3. **事务包裹**：全部语句在一个事务内；任一失败即整体回滚。**DDL 语句会隐式提交，不能放进数据脚本**——那是 Flyway 的职责。
4. **幂等**：用 `WHERE` 精确限定影响范围，或先查后写；重复执行的结果必须与执行一次相同。
5. **先备份**：涉及删除或大批量更新前，先确认有可回退的快照（开发期至少 `mysqldump` 一次）。

## 环境准备（一次性）

用**项目独立的虚拟环境**，不要装进系统或共享解释器：

```bash
cd backend/scripts
python -m venv .venv
.venv/Scripts/pip install -r requirements.txt     # Windows
# .venv/bin/pip install -r requirements.txt       # macOS / Linux
```

`.venv/` 已被 `.gitignore` 排除。

> ⚠️ **`requirements.txt` 必须保持纯 ASCII**（只有包名与版本号，注释用英文）。
> pip 在 Windows 上按**系统区域编码**（中文环境为 GBK）解析 requirements 文件，
> 文件里出现 UTF-8 中文注释会直接抛 `UnicodeDecodeError: 'gbk' codec can't decode ...`
> 导致安装失败。中文说明写在本 README，不要写进 `requirements.txt`（已实测踩过）。

## 执行

```bash
# 1. 先干跑（默认），确认影响范围
.venv/Scripts/python data/D001__backfill_user_grade.py

# 2. 确认无误后真正执行
.venv/Scripts/python data/D001__backfill_user_grade.py --apply
```

连远程 / 非默认库时用环境变量覆盖，例如：

```bash
DB_PORT=33061 DB_NAME=campuslink_staging \
  .venv/Scripts/python data/D001__backfill_user_grade.py --apply
```

## 执行记录（手工维护）

数据轨没有自动历史，**每次 `--apply` 后请在此登记**，避免重复执行或漏执行：

| 脚本 | 环境 | 执行时间 | 操作人 | 影响行数 | 备注 |
|------|------|---------|--------|---------|------|
| — | — | — | — | — | 尚无生产 / 共享环境的数据变更；开发期数据随 Flyway 重建 |
