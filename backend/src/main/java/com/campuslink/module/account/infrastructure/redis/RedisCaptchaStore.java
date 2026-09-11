package com.campuslink.module.account.infrastructure.redis;

import com.campuslink.common.redis.RedisKeys;
import com.campuslink.module.account.domain.gateway.CaptchaStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/** 适配器：验证码限流与存取的 Redis 实现（key 前缀收敛在适配器内，命名空间见 RedisKeys） */
@Component
@RequiredArgsConstructor
public class RedisCaptchaStore implements CaptchaStore {

    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE.withZone(ZoneOffset.UTC);

    private final StringRedisTemplate redis;

    private static String intervalKey(String targetKey) {
        return RedisKeys.of("captcha:interval:" + targetKey);
    }

    private static String dailyCountKey(String targetKey) {
        return RedisKeys.of("captcha:count:" + targetKey + ":" + DAY.format(LocalDate.now(ZoneOffset.UTC)));
    }

    private static String codeKey(String targetKey) {
        return RedisKeys.of("captcha:code:" + targetKey);
    }

    @Override
    public boolean tryAcquireSendSlot(String targetKey, Duration interval) {
        return Boolean.TRUE.equals(redis.opsForValue()
                .setIfAbsent(intervalKey(targetKey), "1", interval));
    }

    @Override
    public long incrementDailyCount(String targetKey) {
        String dayKey = dailyCountKey(targetKey);
        Long count = redis.opsForValue().increment(dayKey);
        if (count != null && count == 1) {
            redis.expire(dayKey, Duration.ofDays(1));
        }
        return count == null ? 0 : count;
    }

    @Override
    public void saveCode(String targetKey, String code, Duration ttl) {
        redis.opsForValue().set(codeKey(targetKey), code, ttl);
    }

    @Override
    public Optional<String> loadCode(String targetKey) {
        return Optional.ofNullable(redis.opsForValue().get(codeKey(targetKey)));
    }

    @Override
    public void deleteCode(String targetKey) {
        redis.delete(codeKey(targetKey));
    }
}
