package com.campuslink.module.forum.infrastructure.redis;

import com.campuslink.common.redis.RedisKeys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 定时任务锁适配器：纳入 {@code RedisKeyNamespaceTest}（CR-015）的同一断言范式——
 * 调用方只给逻辑键，{@code campuslink:} 前缀必须由适配器补，防止新增适配器时漏加前缀。
 */
@ExtendWith(MockitoExtension.class)
class RedisScheduledLockTest {

    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> ops;

    @Test
    @DisplayName("锁键带 campuslink: 前缀，SET NX 的值与 TTL 原样传递")
    void lockKeyIsNamespacedAndTtlPassedThrough() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);

        boolean acquired = new RedisScheduledLock(redis)
                .tryAcquire("hot:refresh:lock", Duration.ofSeconds(300));

        assertThat(acquired).isTrue();
        verify(ops).setIfAbsent(RedisKeys.of("hot:refresh:lock"), "1", Duration.ofSeconds(300));
        assertThat(RedisKeys.of("hot:refresh:lock")).isEqualTo("campuslink:hot:refresh:lock");
    }

    @Test
    @DisplayName("Redis 回 null（异常或集群重定向）视为未取得锁，不抛 NPE")
    void nullReplyMeansNotAcquired() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(null);

        assertThat(new RedisScheduledLock(redis)
                .tryAcquire("hot:refresh:lock", Duration.ofSeconds(300))).isFalse();
    }
}
