package com.campuslink.common.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 公开端点标记（N-4 闭环，CR-028）：显式声明"本方法无需登录即可访问"。
 *
 * <p>{@code ArchitectureGuardTest} 要求 web 层每个 HTTP 映射方法**必须且只能**声明其一：
 * 本注解（公开）或 {@code @SecurityRequirement}（受保护）。目的不是记录事实，而是**强制一次明确表态**——
 * 新增端点若两者都没写，守护测试即失败，不会再出现"忘了加鉴权 → 静默变成公开接口"。
 *
 * <p>⚠️ 本注解自 [CR-031] 起**参与运行时裁决**：{@code security/EndpointAuthorizationManager} 在过滤器链里读取它，
 * 标记者匿名放行（与 {@code @SecurityRequirement} 互斥）。它是"公开"的唯一声明锚点——
 * module 端点两者都不标即 fail-closed（要求登录），不会再有"没表态的端点默认公开"。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PublicEndpoint {
}
