package io.github.tphung1724.workforge.common.exception.business;

import io.github.tphung1724.workforge.common.exception.base.BaseException;
import io.github.tphung1724.workforge.common.exception.error.ErrorCode;

public class BusinessException extends BaseException {

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
