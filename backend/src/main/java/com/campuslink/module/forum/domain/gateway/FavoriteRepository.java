package com.campuslink.module.forum.domain.gateway;

import com.campuslink.module.forum.domain.model.Post;

import java.util.Collection;
import java.util.Set;

/** 收藏仓储端口（favorites 表，主键 (user_id, post_id) 天然去重；端口定义在 domain、实现在 infrastructure，DIP） */
public interface FavoriteRepository {

    /** toggle（F-FORUM-005）：未收藏则插入并返回 true，已收藏则删除并返回 false；去重语义同 {@link LikeRepository#toggle} */
    boolean toggle(long userId, long postId);

    /** 当前用户收藏的帖子分页，按收藏时间倒序；返回帖子聚合，不可见帖（已删 / 非 PUBLISHED）由实现过滤 */
    PageResult<Post> findFavoritePosts(long userId, int page, int size);

    /** 批量查当前用户已收藏的帖子 id（详情回显 favoritedByMe 用） */
    Set<Long> findFavoritedPostIds(long userId, Collection<Long> postIds);
}
