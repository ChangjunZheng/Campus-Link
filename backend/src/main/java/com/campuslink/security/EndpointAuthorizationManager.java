package com.campuslink.security;

import com.campuslink.common.web.PublicEndpoint;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.function.Supplier;

/**
 * 路径级鉴权（CR-031）：把"本端点是否公开"交回框架在**进入业务代码前**裁决，
 * 消除 N-4 残留的"契约上声明了受保护、框架却不拦任何路径"这一层。
 *
 * <p>裁决口径**以端点注解为单一事实来源**（不另建路径清单——两处事实必然漂移）：
 * ① 标 {@link PublicEndpoint} → 放行；
 * ② 标 {@link SecurityRequirement} → 要求已登录（未登录由 {@link ApiErrorSecurityHandler} 产出 401 / 4001）；
 * ③ 两者都未声明的 {@code com.campuslink.module.*} 端点 → **fail-closed**（同样要求已登录）——
 * 这是运行时兜底；漏写声明本身会被 {@code ArchitectureGuardTest} 在测试期拦下；
 * ④ 其余处理器（springdoc / {@code ErrorController} / actuator / 静态资源）与非映射请求 → 放行，
 * 非映射请求交回 MVC 产出既有的 404 / 405 / 415。
 *
 * <p>⚠️ 只区分"登录 / 未登录"：角色与资源级授权（如"仅作者可删"）仍由业务代码（{@code CurrentUser.requireRole}）负责。
 */
@Slf4j
@Component
public class EndpointAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    /** 未显式声明即 fail-closed 的范围：本项目业务上下文 */
    private static final String BUSINESS_PACKAGE_PREFIX = "com.campuslink.module.";

    /**
     * 按名称限定而非按类型：上下文中存在两个 {@code RequestMappingHandlerMapping} 子类
     * （MVC 的 {@code requestMappingHandlerMapping} 与 actuator 的 {@code controllerEndpointHandlerMapping}），
     * 按类型注入会因歧义抛 {@code NoUniqueBeanDefinitionException}（真机首测踩中）。
     * 用 {@link ObjectProvider} 而非 {@code @Lazy}：后者的 CGLIB 代理无法拦截 final 的
     * {@code AbstractHandlerMapping#getHandler}，延迟必须由请求期解析实现。
     */
    private final ObjectProvider<RequestMappingHandlerMapping> handlerMappingProvider;
    private final AuthorizationManager<RequestAuthorizationContext> requireAuthenticated =
            AuthenticatedAuthorizationManager.authenticated();

    public EndpointAuthorizationManager(
            @Qualifier("requestMappingHandlerMapping") ObjectProvider<RequestMappingHandlerMapping> handlerMappingProvider) {
        this.handlerMappingProvider = handlerMappingProvider;
    }

    @Override
    public AuthorizationResult authorize(Supplier<? extends Authentication> authentication, RequestAuthorizationContext context) {
        HandlerMethod handler;
        try {
            handler = resolve(context.getRequest());
        } catch (Exception e) {
            // 未知解析异常宁可拒绝也不放行：静默放行等于本层鉴权整体失效（真机首测即因 Bean 歧义静默全通）
            log.error("端点处理器解析失败，按 fail-closed 拒绝：{} {}",
                    context.getRequest().getMethod(), context.getRequest().getRequestURI(), e);
            return new AuthorizationDecision(false);
        }
        if (handler == null || !requiresAuthentication(handler)) {
            return new AuthorizationDecision(true);
        }
        return requireAuthenticated.authorize(authentication, context);
    }

    static boolean requiresAuthentication(HandlerMethod handler) {
        return requiresAuthentication(
                handler.getBeanType().getName(),
                handler.hasMethodAnnotation(PublicEndpoint.class),
                handler.hasMethodAnnotation(SecurityRequirement.class));
    }

    static boolean requiresAuthentication(String beanTypeName, boolean declaredPublic, boolean declaredProtected) {
        if (declaredPublic) {
            return false;
        }
        if (declaredProtected) {
            return true;
        }
        return beanTypeName.startsWith(BUSINESS_PACKAGE_PREFIX);
    }

    /** MVC 的"无匹配"信号：解析不到端点不等于有权访问，交回 MVC 产出 405 / 404 / 415 等既有响应 */
    private static boolean isMappingMiss(Exception e) {
        return e instanceof HttpRequestMethodNotSupportedException
                || e instanceof NoHandlerFoundException
                || e instanceof HttpMediaTypeNotSupportedException
                || e instanceof HttpMediaTypeNotAcceptableException;
    }

    private HandlerMethod resolve(HttpServletRequest request) throws Exception {
        HandlerExecutionChain chain;
        try {
            chain = handlerMappingProvider.getObject().getHandler(request);
        } catch (Exception e) {
            if (isMappingMiss(e)) {
                return null;
            }
            throw e;
        }
        return chain != null && chain.getHandler() instanceof HandlerMethod handlerMethod ? handlerMethod : null;
    }
}
