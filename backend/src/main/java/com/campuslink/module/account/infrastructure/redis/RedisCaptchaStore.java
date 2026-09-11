package com.campuslink.module.account.infrastructure.redis;

import com.campuslink.module.account.domain.gateway.CaptchaStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/** 适配器：验证码限流与存取的 Redis 实现（key 前缀收敛在适配器内） */
@Component
@RequiredArgsConstructor
public class RedisCaptchaStore implements CaptchaStore {

    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE.withZone(ZoneOffset.UTC);

    private final StringRedisTemplate redis;

    @Override
    public boolean tryAcquireSendSlot(String targetKey, Duration interval) {
        return Boolean.TRUE.equals(redis.opsForValue()
                .setIfAbsent("captcha:interval:" + targetKey, "1", interval));
    }

    @Override
    public long incrementDailyCount(String targetKey) {
        String dayKey = "captcha:count:" + targetKey + ":" + DAY.format(LocalDate.now(ZoneOffset.UTC));
        Long count = redis.opsForValue().increment(dayKey);
        if (count != null && count == 1) {
            redis.expire(dayKey, Duration.ofDays(1));
        }
        return count == null ? 0 : count;
    }

    @Override
    public void saveCode(String targetKey, String code, Duration ttl) {
        redis.opsForValue().set("captcha:code:" + targetKey, code, ttl);
    }

    @Override
    public Optional<String> loadCode(String targetKey) {
        return Optional.ofNullable(redis.opsForValue().get("captcha:code:" + targetKey));
    }

    @Override
    public void deleteCode(String targetKey) {
        redis.delete("captcha:code:" + targetKey);
    }
}
