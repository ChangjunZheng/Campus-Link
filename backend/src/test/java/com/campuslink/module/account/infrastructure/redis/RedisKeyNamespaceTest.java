package com.campuslink.module.account.infrastructure.redis;

import com.campuslink.common.redis.RedisKeys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis 键命名空间回归测试（CR-015）：所有写入 Redis 的键都必须带应用前缀。
 *
 * <p>回归背景：此前键名是裸的（如 {@code verify:ip:...}、{@code captcha:code:...}），
 * 与本机 / 共享实例上其它项目的键存在冲突风险。前缀只在 {@link RedisKeys} 定义，
 * 本测试逐个适配器断言实际键名，防止以后有人新增适配器时漏加前缀。
 */
@ExtendWith(MockitoExtension.class)
class RedisKeyNamespaceTest {

    private static final String LOGICAL_KEY = "H(target)";

    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> ops;

    @Test
    @DisplayName("RedisKeys：逻辑键统一加 campuslink: 前缀")
    void redisKeysPrefixesLogicalKey() {
        assertThat(RedisKeys.PREFIX).isEqualTo("campuslink:");
        assertThat(RedisKeys.of("captcha:code:x")).isEqualTo("campuslink:captcha:code:x");
    }

    @Test
    @DisplayName("RedisCaptchaStore：interval / count / code 键均带前缀")
    void captchaStoreKeysArePrefixed() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(ops.increment(anyString())).thenReturn(1L);
        when(ops.get(anyString())).thenReturn("123456");

        RedisCaptchaStore store = new RedisCaptchaStore(redis);
        store.tryAcquireSendSlot(LOGICAL_KEY, Duration.ofSeconds(60));
        store.incrementDailyCount(LOGICAL_KEY);
        store.saveCode(LOGICAL_KEY, "123456", Duration.ofMinutes(5));
        store.loadCode(LOGICAL_KEY);
        store.deleteCode(LOGICAL_KEY);

        verify(ops).setIfAbsent(eq("campuslink:captcha:interval:" + LOGICAL_KEY), eq("1"), any(Duration.class));
        verify(ops).set(eq("campuslink:captcha:code:" + LOGICAL_KEY), eq("123456"), any(Duration.class));
        verify(ops).get("campuslink:captcha:code:" + LOGICAL_KEY);
        verify(redis).delete("campuslink:captcha:code:" + LOGICAL_KEY);

        // 日计数键带日期后缀，用捕获校验前缀
        ArgumentCaptor<String> dayKey = ArgumentCaptor.forClass(String.class);
        verify(ops).increment(dayKey.capture());
        assertThat(dayKey.getValue()).startsWith("campuslink:captcha:count:" + LOGICAL_KEY + ":");
    }

    @Test
    @DisplayName("RedisTicketStore：票据键带前缀，取后即焚删的是同一个键")
    void ticketStoreKeysArePrefixed() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get(anyString())).thenReturn("payload");

        RedisTicketStore store = new RedisTicketStore(redis);
        String ticket = store.issue("payload", Duration.ofMinutes(5));
        store.consume(ticket);

        ArgumentCaptor<String> issuedKey = ArgumentCaptor.forClass(String.class);
        verify(ops).set(issuedKey.capture(), eq("payload"), any(Duration.class));
        assertThat(issuedKey.getValue()).isEqualTo("campuslink:verify:ticket:" + ticket);
        verify(redis).delete("campuslink:verify:ticket:" + ticket);
    }

    @Test
    @DisplayName("RedisRateLimitAdapter：调用方给逻辑键，适配器补前缀")
    void rateLimitAdapterPrefixesCallerKey() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.increment(anyString())).thenReturn(1L);

        new RedisRateLimitAdapter(redis).hitAndCount("verify:ip:1.2.3.4", Duration.ofHours(1));

        verify(ops).increment("campuslink:verify:ip:1.2.3.4");
        verify(redis).expire("campuslink:verify:ip:1.2.3.4", Duration.ofHours(1));
    }
}
