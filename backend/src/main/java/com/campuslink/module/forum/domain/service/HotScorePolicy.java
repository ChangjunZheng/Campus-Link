package com.campuslink.module.forum.domain.service;

import com.campuslink.module.forum.domain.model.HotScoreInput;

import java.time.Duration;
import java.time.Instant;

/**
 * 热榜算分策略（ADR-006）：{@code hot_score = 互动分 × exp(-λ × 小时龄)}，
 * 其中 {@code 互动分 = w_reply×回复数 + w_like×点赞数 + w_favorite×收藏数}。
 *
 * <p>本类是公式的**唯一事实源**：SQL 侧不重复表达（仓储只负责读候选与定向写回分数），
 * 故改公式只改这里且有单测守护。纯 Java、零框架依赖（守护测试 G2）；权重与 λ 由构造入参传入，
 * domain 不读 Spring 配置——装配在 application 侧按 {@code campuslink.hot.*} 完成。
 *
 * <p>权重与 λ 是**待调优参数**而非结论：PRD Q6 明写"权重与 λ 待试点数据调优"，ADR-006 只给出 λ 未给权重，
 * 所以四项全部可配、调参不改代码。
 */
public final class HotScorePolicy {

    private static final double MILLIS_PER_HOUR = 3_600_000d;

    private final double replyWeight;
    private final double likeWeight;
    private final double favoriteWeight;
    private final double decayPerHour;

    public HotScorePolicy(double replyWeight, double likeWeight, double favoriteWeight, double decayPerHour) {
        this.replyWeight = replyWeight;
        this.likeWeight = likeWeight;
        this.favoriteWeight = favoriteWeight;
        this.decayPerHour = decayPerHour;
    }

    /**
     * 算某帖在 {@code now} 时刻的热度分。
     *
     * <p>两条边界：① 互动分为 0（含计数列为 null）直接得 0 分，且**不会**因权重被配成负数而产生负分
     * （负分会把帖子排到 0 分之下，热榜语义不成立）；② 小时龄为负（{@code created_at} 晚于 {@code now}，
     * 时钟漂移或人工改数据）钳为 0，否则正指数会把分数放大到超过互动分本身。
     */
    public double scoreOf(HotScoreInput input, Instant now) {
        double interaction = replyWeight * orZero(input.replyCount())
                + likeWeight * orZero(input.likeCount())
                + favoriteWeight * orZero(input.favoriteCount());
        if (interaction <= 0d) {
            return 0d;
        }
        double ageHours = Duration.between(input.createdAt(), now).toMillis() / MILLIS_PER_HOUR;
        return interaction * Math.exp(-decayPerHour * Math.max(0d, ageHours));
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }
}
