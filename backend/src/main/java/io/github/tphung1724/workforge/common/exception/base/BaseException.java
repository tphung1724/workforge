package io.github.tphung1724.workforge.common.exception.base;

import io.github.tphung1724.workforge.common.exception.error.ErrorCode;

public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    protected BaseException(ErrorCode errorCode, String message) {
        super(message);

        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
