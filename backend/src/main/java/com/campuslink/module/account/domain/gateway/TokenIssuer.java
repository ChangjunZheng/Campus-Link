package com.campuslink.module.account.domain.gateway;

/**
 * 出站端口：登录态令牌签发。JWT 细节由基础设施适配器实现，
 * 未来替换为 opaque token / refresh token 机制时不影响用例编排。
 */
public interface TokenIssuer {

    String issue(Long userId, String role);

    long ttlSeconds();
}
