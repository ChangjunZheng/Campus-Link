package com.campuslink.module.forum.domain.gateway;

import com.campuslink.module.forum.domain.model.LikeTargetType;

import java.util.Collection;
import java.util.Set;

/** 点赞仓储端口（likes 表，主键 (user_id, target_type, target_id) 天然去重；端口定义在 domain、实现在 infrastructure，DIP） */
public interface LikeRepository {

    /**
     * toggle（F-FORUM-005）：未赞则插入并返回 true（调用方随后 +1），已赞则删除并返回 false（调用方随后 -1）。
     * 去重由主键唯一约束兜底——并发双击时后到者撞键走删除分支，不会重复计数。
     */
    boolean toggle(long userId, LikeTargetType targetType, long targetId);

    /** 批量查当前用户已赞目标 id（详情 / 楼层列表回显 likedByMe 用） */
    Set<Long> findLikedTargetIds(long userId, LikeTargetType targetType, Collection<Long> targetIds);
}
