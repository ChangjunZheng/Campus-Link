package com.campuslink.module.forum.application;

import com.campuslink.config.AppProperties;
import com.campuslink.module.forum.application.cmd.ForumResults.HotRefreshResult;
import com.campuslink.module.forum.domain.gateway.PostRepository;
import com.campuslink.module.forum.domain.model.HotScoreInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 热榜刷新用例（ADR-006）：锁死编排口径——窗口起点由配置推导、候选按 id 游标分批不漏不重、
 * 写回走定向 UPDATE（**不整行回写**，否则会覆盖刷新期间并发变化的互动计数）、窗口外置零恰一次。
 *
 * <p>公式本身的性质在 {@code HotScorePolicyTest}；这里只验"配置真的驱动了算分"与"编排没走偏"。
 */
@ExtendWith(MockitoExtension.class)
class HotRankApplicationServiceTest {

    @Mock
    private PostRepository postRepository;

    private AppProperties props;
    private HotRankApplicationService service;

    @BeforeEach
    void setUp() {
        props = new AppProperties();
        props.getHot().setBatchSize(2);
        props.getHot().setWindowDays(30);
        service = new HotRankApplicationService(postRepository, props);
    }

    @Test
    @DisplayName("窗口起点 = now - window-days，候选查询与窗口外置零共用同一个起点")
    void windowStartIsDerivedFromConfig() {
        when(postRepository.findHotCandidates(any(), anyInt(), any())).thenReturn(List.of());
        Instant before = Instant.now();

        service.refresh();

        Instant after = Instant.now();
        ArgumentCaptor<Instant> candidateSince = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> resetSince = ArgumentCaptor.forClass(Instant.class);
        verify(postRepository).findHotCandidates(candidateSince.capture(), eq(2), isNull());
        verify(postRepository).resetHotScoresBefore(resetSince.capture());

        assertThat(candidateSince.getValue())
                .isBetween(before.minus(30, ChronoUnit.DAYS), after.minus(30, ChronoUnit.DAYS));
        assertThat(resetSince.getValue()).as("两处必须同源，否则置零范围与刷新范围不一致")
                .isEqualTo(candidateSince.getValue());
    }

    @Test
    @DisplayName("候选多于 batchSize 时按 id 游标分批推进：不漏、不重、批数正确")
    void candidatesArePagedByIdCursor() {
        Instant createdAt = Instant.now();
        when(postRepository.findHotCandidates(any(), eq(2), isNull()))
                .thenReturn(List.of(new HotScoreInput(1L, 0, 0, 0, createdAt),
                        new HotScoreInput(2L, 0, 0, 0, createdAt)));
        when(postRepository.findHotCandidates(any(), eq(2), eq(2L)))
                .thenReturn(List.of(new HotScoreInput(3L, 0, 0, 0, createdAt)));

        HotRefreshResult result = service.refresh();

        assertThat(result.refreshed()).isEqualTo(3);
        verify(postRepository, times(3)).updateHotScore(anyLong(), anyDouble());
        verify(postRepository).updateHotScore(eq(1L), anyDouble());
        verify(postRepository).updateHotScore(eq(2L), anyDouble());
        verify(postRepository).updateHotScore(eq(3L), anyDouble());
    }

    @Test
    @DisplayName("空候选即收工：一次都不写，但仍执行窗口外置零")
    void emptyCandidatesStillResetOutOfWindow() {
        when(postRepository.findHotCandidates(any(), anyInt(), any())).thenReturn(List.of());

        HotRefreshResult result = service.refresh();

        assertThat(result.refreshed()).isZero();
        verify(postRepository, never()).updateHotScore(anyLong(), anyDouble());
        verify(postRepository, times(1)).resetHotScoresBefore(any());
    }

    @Test
    @DisplayName("写回走定向 updateHotScore，绝不整行 save（分数按公式独立复算比对）")
    void writesAreTargetedNotWholeRow() {
        Instant createdAt = Instant.now().minus(1, ChronoUnit.HOURS);
        when(postRepository.findHotCandidates(any(), anyInt(), any()))
                .thenReturn(List.of(new HotScoreInput(7L, 1, 2, 3, createdAt)));

        service.refresh();

        ArgumentCaptor<Double> score = ArgumentCaptor.forClass(Double.class);
        verify(postRepository).updateHotScore(eq(7L), score.capture());
        verify(postRepository, never()).save(any());

        // 默认权重下互动分 = 3×1 + 1×2 + 2×3 = 11，龄约 1 小时 → 11×e^-0.05。
        // 容差取 1e-3 而非精确等值：service 内部的 now 比本行的 createdAt 晚几毫秒，龄略大于 1 小时
        assertThat(score.getValue()).isCloseTo(11 * Math.exp(-0.05), within(1e-3));
    }

    @Test
    @DisplayName("权重与 λ 取自配置：改配置即改分数、不改代码（PRD Q6「待试点数据调优」）")
    void weightsAndDecayComeFromConfiguration() {
        props.getHot().setReplyWeight(1d);
        props.getHot().setLikeWeight(0d);
        props.getHot().setFavoriteWeight(0d);
        props.getHot().setDecayPerHour(0d);
        when(postRepository.findHotCandidates(any(), anyInt(), any()))
                .thenReturn(List.of(new HotScoreInput(7L, 5, 9, 9, Instant.now())));

        service.refresh();

        verify(postRepository).updateHotScore(7L, 5d);
    }

    @Test
    @DisplayName("窗口外置零的条数进入统计，供触发器记 INFO 摘要")
    void resetCountIsReported() {
        when(postRepository.findHotCandidates(any(), anyInt(), any())).thenReturn(List.of());
        when(postRepository.resetHotScoresBefore(any())).thenReturn(4);

        assertThat(service.refresh().reset()).isEqualTo(4);
    }
}
