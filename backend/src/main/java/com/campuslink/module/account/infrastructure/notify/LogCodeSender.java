package com.campuslink.module.account.infrastructure.notify;

import com.campuslink.module.account.domain.gateway.CodeSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** 策略实现（开发）：验证码只打日志，不真实发送（联调用测试名册配套） */
@Slf4j
@Component
@ConditionalOnProperty(name = "campuslink.code-sender.mode", havingValue = "log", matchIfMissing = true)
public class LogCodeSender implements CodeSender {

    @Override
    public void send(String target, String code) {
        log.info("[DEV] captcha for {} => {}", target, code);
    }
}
