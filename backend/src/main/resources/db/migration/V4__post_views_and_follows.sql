-- ============================================================================
-- V4 · C 档体验升级三件套的存储支撑（阅读数 / 封面图 / 关注 Feed，CR-074）
--
-- ① posts.view_count  ：帖子阅读数（详情读取时去重计数，Redis 每帖每账号每日至多 +1）
-- ② posts.cover_url   ：封面图（发帖时从正文提取首张 https 图片 URL 落冗余列，读时零成本；
--                       外链方案、零存储依赖，旧帖不回填——新帖生效，实施方案 §2）
-- ③ follows           ：关注关系（F-SOC-002 由 P1 提前，实施方案确认即范围确认）；
--                       联合主键即"一人对一人只有一行"，重复 follow 天然幂等；
--                       idx_followee 支撑"粉丝数"与未来的粉丝列表读。
-- 回滚：V5__revert_cr074.sql（drop follows 表 / drop 两列，见实施方案 §6）
-- ============================================================================

ALTER TABLE posts ADD COLUMN view_count INT NOT NULL DEFAULT 0 COMMENT '阅读数（去重计数）' AFTER like_count;
ALTER TABLE posts ADD COLUMN cover_url VARCHAR(512) NULL COMMENT '封面图（正文首张 https 图片，发帖时提取）' AFTER view_count;

CREATE TABLE IF NOT EXISTS follows (
    follower_id BIGINT NOT NULL COMMENT '关注发起人（users.id）',
    followee_id BIGINT NOT NULL COMMENT '被关注人（users.id）',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, followee_id),
    KEY idx_followee (followee_id),
    CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users (id),
    CONSTRAINT fk_follows_followee FOREIGN KEY (followee_id) REFERENCES users (id)
) ENGINE = InnoDB COMMENT = '关注关系（CR-074）';
