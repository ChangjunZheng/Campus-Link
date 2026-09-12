package com.campuslink.common.result;

/**
 * 错误响应体：{ code, message, traceId }。
 *
 * <p>与 {@link ApiResponse} 分开定义，使 OpenAPI 中错误响应的 schema 与实际 JSON 一致——
 * 成功响应才有 data，错误响应没有。
 */
public record ApiError(int code, String message, String traceId) {

    public static ApiError of(ResultCode resultCode) {
        return of(resultCode, resultCode.getMessage());
    }

    public static ApiError of(ResultCode resultCode, String message) {
        return new ApiError(resultCode.getCode(), message, TraceIds.newTraceId());
    }
}
