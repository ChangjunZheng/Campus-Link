package com.campuslink.module.forum.domain.exception;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ResultCode;

/**
 * 领域异常：版块不存在（按 code 查不到，或该版块已停用）。
 * 错误码绑定 {@link ResultCode#NOT_FOUND}（3001 / 404），绑定方式与理由见 {@link PostNotFoundException}。
 */
public class BoardNotFoundException extends ApiException {

    public BoardNotFoundException() {
        super(ResultCode.NOT_FOUND);
    }
}
