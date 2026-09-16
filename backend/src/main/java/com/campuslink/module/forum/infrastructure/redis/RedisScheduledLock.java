package com.campuslink.module.forum.infrastructure.redis;

import com.campuslink.common.redis.RedisKeys;
import com.campuslink.module.forum.domain.gateway.ScheduledLockGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** 适配器：定时任务互斥锁的 Redis 实现（SET NX + TTL，key 前缀收敛在适配器内，命名空间见 RedisKeys） */
@Component
@RequiredArgsConstructor
public class RedisScheduledLock implements ScheduledLockGateway {

    private final StringRedisTemplate redis;

    @Override
    public boolean tryAcquire(String logicalKey, Duration ttl) {
        return Boolean.TRUE.equals(redis.opsForValue()
                .setIfAbsent(RedisKeys.of(logicalKey), "1", ttl));
    }
}
