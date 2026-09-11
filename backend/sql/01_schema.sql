-- Campus-Link 核心表 DDL（技术方案 v0.2 第 4 节）
-- MySQL 8.0 / utf8mb4 / 时间字段存 UTC（应用以 Instant 写入，连接时区 UTC）
-- 由 docker-compose.dev.yml 挂载到 /docker-entrypoint-initdb.d 首次启动自动执行；
-- 手动执行：mysql -h127.0.0.1 -ucampuslink -p < sql/01_schema.sql

CREATE DATABASE IF NOT EXISTS campuslink DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE campuslink;

-- 账号表：email/phone/学号均为敏感信息，加密存储（AES-GCM），查询用 HMAC 哈希
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    email_enc       VARCHAR(512) NULL COMMENT 'AES-GCM 加密邮箱',
    email_hash      CHAR(64)     NULL COMMENT 'HMAC-SHA256(邮箱小写)',
    phone_enc       VARCHAR(512) NULL,
    phone_hash      CHAR(64)     NULL,
    nickname        VARCHAR(64)  NOT NULL,
    avatar_url      VARCHAR(512) NULL,
    school          VARCHAR(128) NULL,
    major           VARCHAR(128) NULL COMMENT '选填',
    grade           VARCHAR(32)  NULL COMMENT '选填',
    bio             VARCHAR(512) NULL,
    role            VARCHAR(16)  NOT NULL DEFAULT 'USER' COMMENT 'USER/OPS/SUPERADMIN',
    status          VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/BANNED/DEACTIVATED',
    student_id_enc  VARCHAR(512) NULL COMMENT 'AES-GCM 加密学号',
    student_id_hash CHAR(64)     NULL COMMENT 'HMAC-SHA256(学号)，一号一账号',
    verified        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否通过学籍核验',
    anonymized      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '注销匿名化标记',
    delete_at       DATETIME     NULL COMMENT '注销冷静期截止时间',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_email_hash (email_hash),
    UNIQUE KEY uk_users_phone_hash (phone_hash),
    UNIQUE KEY uk_users_student_id_hash (student_id_hash)
) ENGINE = InnoDB COMMENT = '账号';

-- 学籍名册：学号只存 HMAC 哈希，不落明文（技术方案 4.3）；姓名明文用于容错比对，不对外暴露
CREATE TABLE IF NOT EXISTS student_roster (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id_hash CHAR(64)     NOT NULL,
    name            VARCHAR(64)  NOT NULL,
    grade           VARCHAR(32)  NULL,
    department      VARCHAR(128) NULL COMMENT '专业/院系，名册字段以 B2 决议为准',
    used_user_id    BIGINT       NULL COMMENT '被占用则回填 users.id',
    source_batch    VARCHAR(64)  NULL COMMENT '导入批次号',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_roster_student_id_hash (student_id_hash),
    KEY idx_roster_used_user (used_user_id)
) ENGINE = InnoDB COMMENT = '学籍名册';

-- 版块：MVP 固定 6 个（种子数据见 02_seed_boards.sql）
CREATE TABLE IF NOT EXISTS boards (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(32)  NOT NULL COMMENT '路由标识，如 qna',
    name        VARCHAR(64)  NOT NULL,
    description VARCHAR(512) NULL,
    type        VARCHAR(16)  NOT NULL COMMENT 'QUESTION/DISCUSSION',
    sort        INT          NOT NULL DEFAULT 0,
    enabled     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_boards_code (code)
) ENGINE = InnoDB COMMENT = '版块';

-- 帖子：content_html 发布时由服务端渲染落库（ADR-005），请求时零渲染
CREATE TABLE IF NOT EXISTS posts (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id          BIGINT        NOT NULL,
    author_id         BIGINT        NOT NULL,
    type              VARCHAR(16)   NOT NULL COMMENT 'QUESTION/DISCUSSION，随版块 type',
    title             VARCHAR(100)  NOT NULL,
    content_md        MEDIUMTEXT    NOT NULL,
    content_html      MEDIUMTEXT    NOT NULL,
    tags              VARCHAR(255)  NULL COMMENT '冗余展示，逗号分隔；关联见 post_tags',
    status            VARCHAR(16)   NOT NULL DEFAULT 'PUBLISHED' COMMENT 'PUBLISHED/REMOVED',
    is_deleted        TINYINT(1)    NOT NULL DEFAULT 0,
    reply_count       INT           NOT NULL DEFAULT 0,
    like_count        INT           NOT NULL DEFAULT 0,
    favorite_count    INT           NOT NULL DEFAULT 0,
    is_accepted       TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '提问帖是否已有最佳答案',
    accepted_reply_id BIGINT        NULL,
    hot_score         DOUBLE        NOT NULL DEFAULT 0 COMMENT '定时任务刷新（ADR-006）',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_posts_board_list (board_id, status, is_deleted, created_at),
    KEY idx_posts_latest (status, is_deleted, created_at),
    KEY idx_posts_hot (hot_score),
    FULLTEXT KEY ft_posts_title (title) WITH PARSER ngram,
    CONSTRAINT fk_posts_board FOREIGN KEY (board_id) REFERENCES boards (id),
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE = InnoDB COMMENT = '帖子';

CREATE TABLE IF NOT EXISTS tags (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(64) NOT NULL,
    use_count  INT         NOT NULL DEFAULT 0,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tags_name (name)
) ENGINE = InnoDB COMMENT = '标签';

CREATE TABLE IF NOT EXISTS post_tags (
    post_id BIGINT NOT NULL,
    tag_id  BIGINT NOT NULL,
    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_pt_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_pt_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
) ENGINE = InnoDB COMMENT = '帖子-标签关联';

-- 楼层回复：floor_no 按 post 内自增（发帖服务内分配）
CREATE TABLE IF NOT EXISTS replies (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id         BIGINT      NOT NULL,
    author_id       BIGINT      NOT NULL,
    floor_no        INT         NOT NULL,
    content_md      MEDIUMTEXT  NOT NULL,
    content_html    MEDIUMTEXT  NOT NULL,
    quoted_reply_id BIGINT      NULL,
    like_count      INT         NOT NULL DEFAULT 0,
    is_accepted     TINYINT(1)  NOT NULL DEFAULT 0,
    status          VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED',
    is_deleted      TINYINT(1)  NOT NULL DEFAULT 0,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_replies_post_floor (post_id, floor_no),
    CONSTRAINT fk_replies_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_replies_author FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE = InnoDB COMMENT = '楼层回复';

-- 点赞：唯一约束去重（帖子或回复）
CREATE TABLE IF NOT EXISTS likes (
    user_id     BIGINT      NOT NULL,
    target_type VARCHAR(16) NOT NULL COMMENT 'POST/REPLY',
    target_id   BIGINT      NOT NULL,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, target_type, target_id)
) ENGINE = InnoDB COMMENT = '点赞';

CREATE TABLE IF NOT EXISTS favorites (
    user_id    BIGINT   NOT NULL,
    post_id    BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, post_id),
    CONSTRAINT fk_fav_post FOREIGN KEY (post_id) REFERENCES posts (id)
) ENGINE = InnoDB COMMENT = '收藏';

CREATE TABLE IF NOT EXISTS notifications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL COMMENT '接收人',
    type        VARCHAR(16) NOT NULL COMMENT 'reply/like/favorite/accept/quote',
    actor_id    BIGINT      NOT NULL COMMENT '触发人',
    target_type VARCHAR(16) NOT NULL,
    target_id   BIGINT      NOT NULL,
    is_read     TINYINT(1)  NOT NULL DEFAULT 0,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_notifications_user (user_id, is_read, created_at)
) ENGINE = InnoDB COMMENT = '站内通知';

-- 举报工单：24h 同对象去重按 (reporter_id, target_type, target_id, created_at) 查询
CREATE TABLE IF NOT EXISTS reports (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id  BIGINT        NOT NULL,
    target_type  VARCHAR(16)   NOT NULL COMMENT 'POST/REPLY/USER',
    target_id    BIGINT        NOT NULL,
    reason       VARCHAR(32)   NOT NULL COMMENT 'ILLEGAL/HARASS/COPYRIGHT/SPAM/OTHER',
    detail       VARCHAR(1024) NULL,
    status       VARCHAR(16)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/CLOSED',
    resolution   VARCHAR(512)  NULL,
    handler_id   BIGINT        NULL,
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at    DATETIME      NULL,
    KEY idx_reports_target (target_type, target_id, created_at),
    KEY idx_reports_status (status, created_at)
) ENGINE = InnoDB COMMENT = '举报工单';

-- 审计日志：后台全操作（名册导入、内容处置等）留痕
CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id    BIGINT        NULL,
    action      VARCHAR(64)   NOT NULL,
    target_type VARCHAR(32)   NULL,
    target_id   BIGINT        NULL,
    detail      VARCHAR(2048) NULL,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_audit_actor (actor_id, created_at)
) ENGINE = InnoDB COMMENT = '审计日志';
