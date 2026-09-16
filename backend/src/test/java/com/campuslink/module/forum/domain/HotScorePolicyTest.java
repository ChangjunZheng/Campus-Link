package com.campuslink.module.forum.domain;

import com.campuslink.module.forum.domain.model.HotScoreInput;
import com.campuslink.module.forum.domain.model.PostSortOrder;
import com.campuslink.module.forum.domain.service.HotScorePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * 热榜公式单测（ADR-006）：公式只有这一个实现，故这里锁死其可观测性质（衰减率 / 权重线性 / 边界钳位），
 * 而不是逐个硬编算好的期望值——权重与 λ 是待调优参数，写死期望值会让调参变成"改测试"。
 */
class HotScorePolicyTest {

    /** 与 application.yml 的 campuslink.hot 默认值同源：回复 3 / 赞 1 / 收藏 2，λ=0.05 */
    private static final HotScorePolicy DEFAULT_POLICY = new HotScorePolicy(3d, 1d, 2d, 0.05d);
    private static final Instant NOW = Instant.parse("2026-09-15T12:00:00Z");
    private static final double TOLERANCE = 1e-9;

    @Test
    @DisplayName("零互动 → 0 分（衰减项不给空互动产生残值）")
    void zeroInteractionScoresZero() {
        assertThat(DEFAULT_POLICY.scoreOf(candidate(0, 0, 0, NOW), NOW)).isZero();
        assertThat(DEFAULT_POLICY.scoreOf(candidate(0, 0, 0, NOW.minus(10, ChronoUnit.DAYS)), NOW)).isZero();
    }

    @Test
    @DisplayName("计数列为 null 按 0 处理，不抛 NPE（DO 的计数列可空）")
    void nullCountsTreatedAsZero() {
        assertThat(DEFAULT_POLICY.scoreOf(new HotScoreInput(1L, null, null, null, NOW), NOW)).isZero();
    }

    @Test
    @DisplayName("λ 生效：龄 20 小时的分 = 龄 0 的分 × e^-1（λ=0.05 × 20h = 1）")
    void decayFollowsLambda() {
        double fresh = DEFAULT_POLICY.scoreOf(candidate(1, 0, 0, NOW), NOW);
        double aged20h = DEFAULT_POLICY.scoreOf(candidate(1, 0, 0, NOW.minus(20, ChronoUnit.HOURS)), NOW);

        assertThat(fresh).isCloseTo(3d, within(TOLERANCE));
        assertThat(aged20h).isCloseTo(fresh * Math.exp(-1d), within(TOLERANCE));
        assertThat(aged20h).isLessThan(fresh);
    }

    @Test
    @DisplayName("λ=0 时不衰减（衰减率确实由入参驱动，而非写死在实现里）")
    void zeroLambdaMeansNoDecay() {
        HotScorePolicy noDecay = new HotScorePolicy(3d, 1d, 2d, 0d);
        double fresh = noDecay.scoreOf(candidate(1, 1, 1, NOW), NOW);
        double agedYear = noDecay.scoreOf(candidate(1, 1, 1, NOW.minus(365, ChronoUnit.DAYS)), NOW);

        assertThat(agedYear).isCloseTo(fresh, within(TOLERANCE));
    }

    @Test
    @DisplayName("互动分按权重线性可加，且回复权重最高在结果上可辨")
    void interactionScoreIsLinearInWeights() {
        double byReply = DEFAULT_POLICY.scoreOf(candidate(1, 0, 0, NOW), NOW);
        double byLike = DEFAULT_POLICY.scoreOf(candidate(0, 1, 0, NOW), NOW);
        double byFavorite = DEFAULT_POLICY.scoreOf(candidate(0, 0, 1, NOW), NOW);

        assertThat(byReply).isGreaterThan(byFavorite);
        assertThat(byFavorite).isGreaterThan(byLike);
        assertThat(DEFAULT_POLICY.scoreOf(candidate(1, 1, 1, NOW), NOW))
                .isCloseTo(byReply + byLike + byFavorite, within(TOLERANCE));
        assertThat(DEFAULT_POLICY.scoreOf(candidate(2, 0, 0, NOW), NOW))
                .isCloseTo(2 * byReply, within(TOLERANCE));
    }

    @Test
    @DisplayName("created_at 晚于 now（时钟漂移）→ 龄钳为 0，分数不被正指数放大")
    void futureCreatedAtIsClampedToZeroAge() {
        double future = DEFAULT_POLICY.scoreOf(candidate(1, 0, 0, NOW.plus(48, ChronoUnit.HOURS)), NOW);

        assertThat(future).isCloseTo(DEFAULT_POLICY.scoreOf(candidate(1, 0, 0, NOW), NOW), within(TOLERANCE));
    }

    @Test
    @DisplayName("极端互动分与极端龄不产生 NaN / Infinity（分数落库为 DOUBLE，非有限值会污染排序）")
    void extremeInputsStayFinite() {
        double extreme = DEFAULT_POLICY.scoreOf(
                candidate(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
                        NOW.minus(3650, ChronoUnit.DAYS)), NOW);

        assertThat(Double.isFinite(extreme)).as("分数必须是有限值").isTrue();
        assertThat(extreme).isGreaterThanOrEqualTo(0d);
    }

    @Test
    @DisplayName("sort 解析：大小写不敏感 + 去空白；null 与非法值一律 empty，不静默归一到 LATEST")
    void sortOrderParsingIsStrict() {
        assertThat(PostSortOrder.fromCode("hot")).contains(PostSortOrder.HOT);
        assertThat(PostSortOrder.fromCode("HOT")).contains(PostSortOrder.HOT);
        assertThat(PostSortOrder.fromCode("  latest  ")).contains(PostSortOrder.LATEST);
        assertThat(PostSortOrder.fromCode(null)).isEmpty();
        assertThat(PostSortOrder.fromCode("")).isEmpty();
        assertThat(PostSortOrder.fromCode(" ")).isEmpty();
        assertThat(PostSortOrder.fromCode("foo")).isEmpty();
        assertThat(PostSortOrder.fromCode("new")).isEmpty();
    }

    private static HotScoreInput candidate(int replyCount, int likeCount, int favoriteCount, Instant createdAt) {
        return new HotScoreInput(1L, replyCount, likeCount, favoriteCount, createdAt);
    }
}
