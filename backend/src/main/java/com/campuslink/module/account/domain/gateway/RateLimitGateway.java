package com.campuslink.module.account.domain.gateway;

import java.time.Duration;

/**
 * 出站端口：通用计数限流（学籍核验 IP 防爆破等，技术方案 4.3 / 7.1）。
 */
public interface RateLimitGateway {

    /** 命中计数并返回当前累计值；实现负责窗口首次命中时设置过期 */
    long hitAndCount(String key, Duration window);
}
