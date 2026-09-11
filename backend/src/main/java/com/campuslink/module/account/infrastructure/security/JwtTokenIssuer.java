package com.campuslink.module.account.infrastructure.security;

import com.campuslink.module.account.domain.gateway.TokenIssuer;
import com.campuslink.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 适配器：JWT 签发（security 包）适配为 TokenIssuer 端口 */
@Component
@RequiredArgsConstructor
public class JwtTokenIssuer implements TokenIssuer {

    private final JwtService jwtService;

    @Override
    public String issue(Long userId, String role) {
        return jwtService.issue(userId, role);
    }

    @Override
    public long ttlSeconds() {
        return jwtService.ttlSeconds();
    }
}
