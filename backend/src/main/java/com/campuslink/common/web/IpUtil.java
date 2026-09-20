package com.campuslink.common.web;

import jakarta.servlet.http.HttpServletRequest;

public final class IpUtil {

    private IpUtil() {
    }

    /** 取客户端 IP：优先 X-Forwarded-For 首段（Nginx 反代场景），否则 remoteAddr；request 为 null（单测直调）时回落 "unknown" */
    public static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        return request.getRemoteAddr();
    }
}
