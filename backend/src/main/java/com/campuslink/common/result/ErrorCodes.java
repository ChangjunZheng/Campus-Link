package com.campuslink.common.result;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明该端点可能返回的业务错误码；HTTP 状态与提示语由 {@link ResultCode} 推导，
 * 契约与错误码枚举同源，不在注解里手写状态码。
 *
 * <p>由 {@code config/OpenApiErrorResponseCustomizer} 读取并写入 OpenAPI 响应。
 * 通用的 400（有入参时）与 500 无需声明，定制器自动补。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ErrorCodes {

    ResultCode[] value();
}
