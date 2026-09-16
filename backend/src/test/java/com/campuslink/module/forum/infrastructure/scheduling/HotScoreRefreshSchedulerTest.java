package com.campuslink.module.forum.infrastructure.scheduling;

import com.campuslink.config.AppProperties;
import com.campuslink.module.forum.application.HotRankApplicationService;
import com.campuslink.module.forum.application.cmd.ForumResults.HotRefreshResult;
import com.campuslink.module.forum.domain.gateway.ScheduledLockGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 热榜刷新触发器：锁的三条语义。定时任务没有调用方可报错，故"跳过"与"失败"都只能靠日志与不外抛来保证
 * ——一旦异常逃出方法，Spring 的调度器会记一条无上下文的日志，定位不到是哪一轮、哪个任务。
 */
@ExtendWith(MockitoExtension.class)
class HotScoreRefreshSchedulerTest {

    @Mock
    private HotRankApplicationService hotRankApplicationService;
    @Mock
    private ScheduledLockGateway scheduledLock;

    private HotScoreRefreshScheduler scheduler;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties();
        props.getHot().setLockTtlSeconds(300L);
        scheduler = new HotScoreRefreshScheduler(hotRankApplicationService, scheduledLock, props);
    }

    @Test
    @DisplayName("取到锁 → 刷新一轮；锁用逻辑键（前缀由适配器补）、TTL 取自配置")
    void refreshesOnceWhenLockAcquired() {
        when(scheduledLock.tryAcquire("hot:refresh:lock", Duration.ofSeconds(300))).thenReturn(true);
        when(hotRankApplicationService.refresh()).thenReturn(new HotRefreshResult(3, 1, 12L));

        scheduler.refreshHotScores();

        verify(hotRankApplicationService, times(1)).refresh();
    }

    @Test
    @DisplayName("未取到锁 → 一次都不刷（另一实例在跑，或上一轮的锁还没到期）")
    void skipsWhenLockNotAcquired() {
        when(scheduledLock.tryAcquire(anyString(), any(Duration.class))).thenReturn(false);

        scheduler.refreshHotScores();

        verify(hotRankApplicationService, never()).refresh();
    }

    @Test
    @DisplayName("刷新失败 → 记 ERROR 但不外抛，下一轮按 fixedDelay 照常重试")
    void refreshFailureDoesNotEscape() {
        when(scheduledLock.tryAcquire(anyString(), any(Duration.class))).thenReturn(true);
        when(hotRankApplicationService.refresh()).thenThrow(new IllegalStateException("数据库不可用"));

        assertThatCode(() -> scheduler.refreshHotScores()).doesNotThrowAnyException();
    }
}
