package com.campuslink.module.account.infrastructure.redis;

import com.campuslink.module.account.domain.gateway.RateLimitGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** 适配器：通用计数限流的 Redis 实现（INCR + 首次过期） */
@Component
@RequiredArgsConstructor
public class RedisRateLimitAdapter implements RateLimitGateway {

    private final StringRedisTemplate redis;

    @Override
    public long hitAndCount(String key, Duration window) {
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, window);
        }
        return count == null ? 0 : count;
    }
}
