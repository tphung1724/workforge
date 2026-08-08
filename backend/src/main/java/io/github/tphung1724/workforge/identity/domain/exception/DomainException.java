package io.github.tphung1724.workforge.identity.domain.exception;

import java.util.Objects;

public abstract class DomainException extends RuntimeException {

    private final IdentityErrorCode errorCode;

    protected DomainException(final IdentityErrorCode errorCode) {
        super(Objects.requireNonNull(errorCode, "errorCode must not be null").getDefaultMessage());
        this.errorCode = errorCode;
    }

    public IdentityErrorCode getErrorCode() {
        return errorCode;
    }
}
