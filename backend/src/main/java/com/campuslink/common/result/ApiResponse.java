package com.campuslink.common.result;

/**
 * 统一响应：{ code, message, data, traceId }；code = 0 表示成功（技术方案第 5 节）。
 */
public record ApiResponse<T>(int code, String message, T data, String traceId) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(ResultCode.OK.getCode(), ResultCode.OK.getMessage(), data, TraceIds.newTraceId());
    }

    public static ApiResponse<Void> ok() {
        return ok(null);
    }
}
