package com.campuslink.module.forum.application;

import com.campuslink.common.crypto.CryptoService;
import com.campuslink.common.redis.RedisKeys;
import com.campuslink.module.forum.domain.gateway.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 阅读数去重计数（CR-074）：帖子详情读取时调用。同一账号（登录按用户 id、匿名按 **IP 的 HMAC 前 16 位**，
 * 明文 IP 不进缓存）对同一帖每天至多计 1 次——Redis {@code SETNX} 当日键 + 48h 过期兜底；
 * 首次命中才走 {@code PostRepository#incrementViewCount} 定向 SQL。
 *
 * <p>任何异常只记 1 行 WARN（日志分档：不打全栈，不污染 5xx），**绝不影响详情返回**——
 * 计数是增强信号而非主流程（实施方案 §2、评审建议③）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostViewCounter {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    private final StringRedisTemplate redis;
    private final PostRepository postRepository;
    private final CryptoService cryptoService;

    public void record(Long postId, Long viewerId, String ip) {
        try {
            String who = viewerId != null
                    ? "u" + viewerId
                    : "i" + cryptoService.hash(ip == null || ip.isBlank() ? "anon" : ip).substring(0, 16);
            String key = RedisKeys.of("view:post:" + postId + ":" + who + ":" + DAY.format(ZonedDateTime.now(ZoneOffset.UTC)));
            Boolean first = redis.opsForValue().setIfAbsent(key, "1", Duration.ofDays(2));
            if (Boolean.TRUE.equals(first)) {
                postRepository.incrementViewCount(postId);
            }
        } catch (Exception e) {
            log.warn("view count skipped: postId={}, reason={}", postId, e.getMessage());
        }
    }
}
