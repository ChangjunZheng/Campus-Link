package com.campuslink.common.web;

/**
 * OpenAPI 契约里共用的名称常量。放在 common 而非 config，
 * 使 web 层不必反向依赖组装根（composition root）。
 */
public final class ApiDocs {

    /** JWT 鉴权方案名；受保护的操作以 {@code @SecurityRequirement(name = BEARER_AUTH)} 引用 */
    public static final String BEARER_AUTH = "bearerAuth";

    private ApiDocs() {
    }
}
