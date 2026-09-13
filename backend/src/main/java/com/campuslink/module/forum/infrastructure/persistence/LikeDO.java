package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * likes 表数据对象（MyBatis-Plus）。主键为 (user_id, target_type, target_id) 复合键、无自增 id，
 * 去重靠数据库唯一约束（F-FORUM-005）；本类属基础设施，不进入领域层。
 */
@Data
@TableName("likes")
public class LikeDO {

    private Long userId;
    private String targetType;
    private Long targetId;
    private java.time.Instant createdAt;
}
