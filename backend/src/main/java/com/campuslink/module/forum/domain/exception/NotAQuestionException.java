package com.campuslink.module.forum.domain.exception;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ResultCode;

/**
 * 领域异常：仅技术问答帖（QUESTION 版块）可采纳最佳答案（F-QA-001）。
 * 错误码在构造函数内绑定 {@link ResultCode#NOT_A_QUESTION}（3002 / 400）——
 * 沿用 {@link PostNotFoundException} 的分层惯例，全局出口无需改动。
 */
public class NotAQuestionException extends ApiException {

    public NotAQuestionException() {
        super(ResultCode.NOT_A_QUESTION);
    }
}
