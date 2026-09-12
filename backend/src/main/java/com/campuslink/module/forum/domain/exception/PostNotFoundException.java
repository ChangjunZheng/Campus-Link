package com.campuslink.module.forum.domain.exception;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ResultCode;

/**
 * 领域异常：帖子不可读——不存在 / 已删除 / 非 PUBLISHED **三种情况不区分**，
 * 统一提示可防止按 id 探测帖子是否存在（与学籍核验的防名册枚举同思路）。
 *
 * <p>错误码在构造函数内绑定 {@link ResultCode#NOT_FOUND}（3001 / 404）——沿用既有分层惯例
 * （见 account 的 {@code StudentRecord} 引用 common）：domain 用领域词汇表达失败，
 * ResultCode 仍是错误响应的单点来源，全局出口 {@code GlobalExceptionHandler} 无需改动。
 *
 * <p>不携带 postId：业务错误按约定不记日志，带上下文既无用又多一处可泄漏面。
 */
public class PostNotFoundException extends ApiException {

    public PostNotFoundException() {
        super(ResultCode.NOT_FOUND);
    }
}
