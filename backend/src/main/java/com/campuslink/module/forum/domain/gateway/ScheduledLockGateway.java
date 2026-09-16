package com.campuslink.module.forum.domain.gateway;

import java.time.Duration;

/**
 * 定时任务互斥端口（ADR-006 的"@Scheduled + Redis 锁"选型）：抢到返回 true，已被占用返回 false。
 *
 * <p>与 account 的 {@code RateLimitGateway} 同范式——端口定义在 domain、实现在 infrastructure（DIP），
 * 领域与用例层不感知 Redis。
 *
 * <p><b>锁是 advisory 而非正确性依赖</b>：热榜重算是确定性的幂等操作（同一时刻同一批数据算出同一批分数），
 * 两实例同时跑结果一致，TTL 到期导致的重复执行无害。锁的作用只是省掉重复的读写开销，
 * 因此调用方**不需要**释放锁、也不需要在拿不到锁时重试或报错。
 */
public interface ScheduledLockGateway {

    /**
     * 尝试占用一把带 TTL 的锁。
     *
     * @param logicalKey 逻辑键（不含应用前缀，前缀由适配器按 {@code RedisKeys} 统一补）
     * @param ttl        自动过期时间——不显式释放，靠 TTL 兜底，故 ttl 应略大于单轮预期耗时
     */
    boolean tryAcquire(String logicalKey, Duration ttl);
}
