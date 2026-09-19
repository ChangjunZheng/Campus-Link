-- ============================================================================
-- V3 · "我的内容"两个列表的支撑索引（F-ACC-007b / F-ACC-007c，CR-071）
--
-- 覆盖的两条读路径（谓词 + 排序完全一致）：
--   posts   : WHERE author_id = ? AND is_deleted = 0            ORDER BY created_at DESC, id DESC
--   replies : WHERE author_id = ? AND is_deleted = 0（JOIN posts 判父帖可见性） 同上
--
-- ⚠️ 列序与增量 PRD §4.3 007b 的建议**不同**（该处写的是照抄 idx_posts_board_list 的
--    (author_id, status, is_deleted, created_at)），理由：本 CR 的两条读路径都不按 status 过滤——
--    帖子侧要让 REMOVED 对作者本人可见、楼层侧的 status 判定发生在父帖上。把不进谓词的列
--    卡在中间，索引只用到 author_id 一列，ORDER BY 退化成 filesort。偏差已登记在实施方案 §4。
-- 末列不写 id：InnoDB 二级索引自动追加主键，(…, created_at) 的反向扫描即覆盖 created_at DESC, id DESC。
--
-- 诚实定位：试点量级（基线 §4：注册 ≥1 万、帖 ≥10 万）下单用户最多几百帖，**不建也不会出事**；
-- 本迁移是"一次顺带、不给未来留慢查询"，不是阻塞项。
-- fk_posts_author / fk_replies_author（外键自动建的单列索引）自此对这两条查询冗余，但**不删**：
-- DROP 需要再一次迁移，且 MySQL 会为外键约束把单列索引再建回来。
-- ============================================================================

ALTER TABLE posts   ADD KEY idx_posts_author_list   (author_id, is_deleted, created_at);
ALTER TABLE replies ADD KEY idx_replies_author_list (author_id, is_deleted, created_at);
