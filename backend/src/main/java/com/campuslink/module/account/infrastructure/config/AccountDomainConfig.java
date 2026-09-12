package com.campuslink.module.account.infrastructure.config;

import com.campuslink.module.account.domain.gateway.RosterGateway;
import com.campuslink.module.account.domain.gateway.SensitiveCodec;
import com.campuslink.module.account.domain.service.StudentVerificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * account 上下文的 domain 装配点（F-1，CR-028）：domain 服务不挂 {@code @Service}，
 * 由组合根（infrastructure）负责实例化——保证 domain 层不依赖框架。
 */
@Configuration
public class AccountDomainConfig {

    @Bean
    public StudentVerificationService studentVerificationService(RosterGateway rosterGateway, SensitiveCodec codec) {
        return new StudentVerificationService(rosterGateway, codec);
    }
}
