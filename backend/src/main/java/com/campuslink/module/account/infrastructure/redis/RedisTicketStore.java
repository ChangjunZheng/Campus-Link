package com.campuslink.module.account.infrastructure.redis;

import com.campuslink.module.account.domain.gateway.VerificationTicketStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/** 适配器：一次性核验票据的 Redis 实现（取后即焚） */
@Component
@RequiredArgsConstructor
public class RedisTicketStore implements VerificationTicketStore {

    private static final String KEY_PREFIX = "verify:ticket:";

    private final StringRedisTemplate redis;

    @Override
    public String issue(String payload, Duration ttl) {
        String ticket = UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(KEY_PREFIX + ticket, payload, ttl);
        return ticket;
    }

    @Override
    public Optional<String> consume(String ticket) {
        String key = KEY_PREFIX + ticket;
        String payload = redis.opsForValue().get(key);
        if (payload != null) {
            redis.delete(key);
        }
        return Optional.ofNullable(payload);
    }
}
