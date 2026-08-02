package io.github.tphung1724.workforge.common.exception.system;

import io.github.tphung1724.workforge.common.exception.base.BaseException;
import io.github.tphung1724.workforge.common.exception.error.ErrorCode;

public class SystemException extends BaseException {

    public SystemException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
