package com.campuslink.module.forum.infrastructure.scheduling;

import com.campuslink.config.AppProperties;
import com.campuslink.module.forum.application.HotRankApplicationService;
import com.campuslink.module.forum.application.cmd.ForumResults.HotRefreshResult;
import com.campuslink.module.forum.domain.gateway.ScheduledLockGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 热榜刷新触发器（ADR-006 选型「{@code @Scheduled} + Redis 锁」的落码，CR-058）。
 *
 * <p>它是 infrastructure 的**入站适配器**——与 web 层的 Controller 同类：外部触发（这里是时钟）进来、
 * 调 application 的用例、把结果转成协议外的表达（这里是日志）。故依赖方向 infrastructure → application
 * 合规（守护测试 G1 的禁止清单不含该方向，本轮是全仓首例，适用条件已写入技术方案 §2.4）。
 *
 * <p>周期与首轮延迟走属性占位符而非 {@code AppProperties}：{@code @Scheduled} 只接受编译期常量或占位符，
 * 且这两项只被本类消费，再绑一份配置类就成了两个事实源（改了一处另一处静默不动）。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HotScoreRefreshScheduler {

    /** 锁逻辑键：应用前缀由 {@code RedisScheduledLock} 按 RedisKeys 统一补，最终键 {@code campuslink:hot:refresh:lock} */
    private static final String LOCK_KEY = "hot:refresh:lock";

    private final HotRankApplicationService hotRankApplicationService;
    private final ScheduledLockGateway scheduledLock;
    private final AppProperties props;

    @Scheduled(fixedDelayString = "${campuslink.hot.refresh-interval-ms:600000}",
            initialDelayString = "${campuslink.hot.initial-delay-ms:15000}")
    public void refreshHotScores() {
        Duration lockTtl = Duration.ofSeconds(props.getHot().getLockTtlSeconds());
        if (!scheduledLock.tryAcquire(LOCK_KEY, lockTtl)) {
            // 锁未到期（另一实例在刷，或本实例上一轮的锁还在）：重算是幂等的，跳过即可，不值得 WARN
            log.debug("热榜刷新跳过：未取得锁 {}", LOCK_KEY);
            return;
        }
        try {
            HotRefreshResult result = hotRankApplicationService.refresh();
            log.info("热榜刷新完成：写回 {} 条、置零 {} 条、耗时 {} ms",
                    result.refreshed(), result.reset(), result.elapsedMillis());
        } catch (RuntimeException e) {
            // 自己记一行带上下文的 ERROR 而不外抛：定时任务没有调用方可报错，抛出只会被 Spring 的
            // 默认错误处理器记一条无上下文的日志。本轮失败不影响下一轮（fixedDelay 从本次结束算起），
            // 期间热榜退化为"按上一次的分数排序"，不报错、不影响最新序。
            log.error("热榜刷新失败：本轮分数未更新，下一轮按 fixedDelay 自动重试", e);
        }
    }
}
