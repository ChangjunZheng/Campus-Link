package com.campuslink.module.forum.domain.exception;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ResultCode;

/**
 * 领域异常：提问者不能采纳自己的回复（F-QA-001，PRD 验收标准）。
 * 错误码在构造函数内绑定 {@link ResultCode#CANNOT_ACCEPT_OWN_REPLY}（3003 / 400）。
 */
public class CannotAcceptOwnReplyException extends ApiException {

    public CannotAcceptOwnReplyException() {
        super(ResultCode.CANNOT_ACCEPT_OWN_REPLY);
    }
}
