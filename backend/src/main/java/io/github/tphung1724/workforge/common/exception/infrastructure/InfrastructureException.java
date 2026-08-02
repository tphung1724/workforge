package io.github.tphung1724.workforge.common.exception.infrastructure;

import io.github.tphung1724.workforge.common.exception.base.BaseException;
import io.github.tphung1724.workforge.common.exception.error.ErrorCode;

public class InfrastructureException extends BaseException {

    public InfrastructureException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
