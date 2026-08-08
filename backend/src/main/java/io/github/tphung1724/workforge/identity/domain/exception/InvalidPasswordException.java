package io.github.tphung1724.workforge.identity.domain.exception;

public final class InvalidPasswordException extends DomainException {

    public InvalidPasswordException() {
        super(IdentityErrorCode.INVALID_PASSWORD);
    }
}
