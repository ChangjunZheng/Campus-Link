package com.campuslink.security;

import com.campuslink.common.result.ApiError;
import com.campuslink.common.result.ResultCode;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 过滤器链内的错误出口（CR-031）：鉴权失败发生在 DispatcherServlet 之前，
 * 那时 {@code GlobalExceptionHandler} 还没轮到——若不在此处写响应，客户端会拿到
 * 默认入口点的空 403（无 JSON 体、与项目错误契约不符）。
 *
 * <p>因此本类是"所有 ≥400 响应只能由 {@code GlobalExceptionHandler} 产出"这条约定的**唯一例外**，
 * 且只产两种既有错误码，错误体仍走 {@link ApiError}：
 * 未登录 → {@code 401 / 4001}，已登录但无权 → {@code 403 / 4002}。
 * 不记日志：二者都是客户端错误，与业务错误的处理档位一致（记日志会污染 5xx 监控）。
 */
@Component
@RequiredArgsConstructor
public class ApiErrorSecurityHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        write(response, ResultCode.NOT_LOGGED_IN);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        write(response, ResultCode.FORBIDDEN);
    }

    private void write(HttpServletResponse response, ResultCode resultCode) throws IOException {
        byte[] body = objectMapper.writeValueAsBytes(ApiError.of(resultCode));
        response.setStatus(resultCode.getHttpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setContentLength(body.length);
        response.getOutputStream().write(body);
    }
}
