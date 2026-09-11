# -*- coding: utf-8 -*-
"""数据变更脚本模板（复制本文件为 D<序号>__<描述>.py 后修改）。

约定（详见 backend/scripts/README.md，CR-014）：
  1. 默认 dry-run，只有显式 --apply 才写库；
  2. 凭据只从环境变量读取，严禁硬编码；
  3. 全部语句在同一事务内，失败整体回滚；
  4. 必须幂等——重复执行结果与执行一次相同；
  5. DDL 不放这里（那是 Flyway 的职责），本脚本只做纯数据增删改。

用法：
    python data/D001__xxx.py            # 干跑，打印将要执行的 SQL 与影响行数
    python data/D001__xxx.py --apply    # 真正执行
"""

import argparse
import os
import sys

import pymysql

# ---- 连接信息：只从环境变量读，默认值与 application.yml 的开发默认值一致 ----
DB_CONFIG = {
    "host": os.environ.get("DB_HOST", "127.0.0.1"),
    "port": int(os.environ.get("DB_PORT", "3306")),
    "database": os.environ.get("DB_NAME", "campuslink"),
    "user": os.environ.get("DB_USER", "campuslink"),
    "password": os.environ.get("DB_PASSWORD", "campuslink"),
    "charset": "utf8mb4",
    "autocommit": False,
}


def main() -> int:
    parser = argparse.ArgumentParser(description="数据变更脚本（默认 dry-run）")
    parser.add_argument("--apply", action="store_true", help="真正写库；不加则只干跑")
    args = parser.parse_args()

    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cur:
            # ------------------------------------------------------------------
            # 1) 幂等前置检查：先查目标行数，确认影响范围符合预期
            # ------------------------------------------------------------------
            cur.execute(
                "SELECT COUNT(*) FROM users WHERE grade IS NULL AND created_at < %s",
                ("2026-09-01",),
            )
            (pending,) = cur.fetchone()
            print(f"[检查] 待处理行数：{pending}")
            if pending == 0:
                print("[跳过] 无待处理数据，脚本已幂等结束")
                return 0

            # ------------------------------------------------------------------
            # 2) 执行语句：用 WHERE 精确限定范围，并打印影响行数
            # ------------------------------------------------------------------
            affected = cur.execute(
                "UPDATE users SET grade = %s WHERE grade IS NULL AND created_at < %s",
                ("2024级", "2026-09-01"),
            )
            print(f"[执行] UPDATE 影响 {affected} 行")

        if args.apply:
            conn.commit()
            print("[提交] 已提交")
        else:
            conn.rollback()
            print("[干跑] 已回滚（未写库）。确认无误后加 --apply 重新执行")
        return 0
    except Exception as e:  # noqa: BLE001 - 脚本入口，需保证任何异常都回滚并给出非零退出码
        conn.rollback()
        print(f"[失败] 已回滚：{e}", file=sys.stderr)
        return 1
    finally:
        conn.close()


if __name__ == "__main__":
    sys.exit(main())
