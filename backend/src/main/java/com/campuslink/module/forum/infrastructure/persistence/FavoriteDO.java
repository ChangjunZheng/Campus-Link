package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * favorites 表数据对象（MyBatis-Plus）。主键为 (user_id, post_id) 复合键、无自增 id，
 * 去重靠数据库唯一约束（F-FORUM-005）；本类属基础设施，不进入领域层。
 */
@Data
@TableName("favorites")
public class FavoriteDO {

    private Long userId;
    private Long postId;
    private java.time.Instant createdAt;
}
