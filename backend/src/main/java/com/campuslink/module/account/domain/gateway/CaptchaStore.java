package com.campuslink.module.account.domain.gateway;

import java.time.Duration;
import java.util.Optional;

/**
 * 出站端口：验证码限流计数与验证码存取（Redis 适配器实现）。
 * key 由应用层以目标的 HMAC 哈希传入，明文邮箱 / 手机号不进入缓存。
 */
public interface CaptchaStore {

    /** 重发间隔槽位：占用成功返回 true（60s 内重复请求返回 false） */
    boolean tryAcquireSendSlot(String targetKey, Duration interval);

    /** 当日发送计数自增（实现负责首次自增时设置当日过期），返回自增后的值 */
    long incrementDailyCount(String targetKey);

    void saveCode(String targetKey, String code, Duration ttl);

    Optional<String> loadCode(String targetKey);

    void deleteCode(String targetKey);
}
