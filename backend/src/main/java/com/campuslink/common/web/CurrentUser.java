package com.campuslink.common.web;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ResultCode;
import org.springframework.security.core.Authentication;

/**
 * 受保护端点的统一鉴权入口（N-4 闭环，CR-028）。
 *
 * <p>背景：{@code SecurityConfig} 仍是 {@code anyRequest().permitAll()}，登录态由 Controller 主动校验，
 * 而 {@code @SecurityRequirement} 只是契约声明。历史上"新端点漏写校验"不会报错、不会让测试失败，
 * 会静默变成公开接口。本类把这段校验收敛到唯一入口，{@code ArchitectureGuardTest} 规定
 * **凡声明 {@code @SecurityRequirement} 的方法必须真的调用本类方法**——漏写即测试失败。
 *
 * <p>⚠️ 本类闭环的是"漏写鉴权"，**不是**框架级 URL 拦截：SecurityConfig 仍不拦任何路径。
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    /** 取当前登录账号 id；未登录（无 Authentication 或 principal 不是账号 id）抛 401。 */
    public static long requireId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new ApiException(ResultCode.NOT_LOGGED_IN);
        }
        return userId;
    }

    /**
     * 取当前登录账号 id 且必须持有指定权限（Spring authority 全名，如 {@code ROLE_SUPERADMIN}）。
     * 未登录 401、已登录但权限不足 403——两者必须分开（技术方案 §5）。
     */
    public static long requireRole(Authentication authentication, String authority) {
        long userId = requireId(authentication);
        boolean granted = authentication.getAuthorities().stream()
                .anyMatch(a -> authority.equals(a.getAuthority()));
        if (!granted) {
            throw new ApiException(ResultCode.FORBIDDEN);
        }
        return userId;
    }
}
