package com.campuslink.common.exception;

import com.campuslink.common.result.ResultCode;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final transient ResultCode code;

    public ApiException(ResultCode code) {
        super(code.getMessage());
        this.code = code;
    }
}
