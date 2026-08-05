package io.github.tphung1724.workforge.identity.domain.exception;

public final class InvalidUserIdException extends DomainException {

    public InvalidUserIdException() {
        super(IdentityErrorCode.INVALID_USER_ID);
    }
}
