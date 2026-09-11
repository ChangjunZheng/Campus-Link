package com.campuslink.module.account.domain.gateway;

import java.time.Duration;
import java.util.Optional;

/**
 * 出站端口：一次性核验票据存取（技术方案 4.3）。
 * payload 由应用层先加密再托管（票据内容不含明文学号）；消费后立即失效。
 */
public interface VerificationTicketStore {

    /** 签发票据，返回票据 id */
    String issue(String payload, Duration ttl);

    /** 消费票据（取后即焚），不存在 / 已过期返回 empty */
    Optional<String> consume(String ticket);
}
