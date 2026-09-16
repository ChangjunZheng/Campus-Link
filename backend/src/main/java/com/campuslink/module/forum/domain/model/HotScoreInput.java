package com.campuslink.module.forum.domain.model;

import java.time.Instant;

/**
 * 热榜算分入参（ADR-006）：算分只需要 5 个字段，故用独立不可变载体而非 {@link Post} 聚合。
 *
 * <p>刻意不塞进 {@code Post}：{@code hot_score} 是**派生的排序权重**，不是帖子聚合的不变量
 * （帖子自身的一致性规则不依赖它）；且 {@code Post} 未建模 {@code favoriteCount}，
 * 为算分给聚合加字段会污染聚合语义。计数为 null 时按 0 处理，见 {@link HotScorePolicy}。
 */
public record HotScoreInput(Long id,
                            Integer replyCount,
                            Integer likeCount,
                            Integer favoriteCount,
                            Instant createdAt) {
}
