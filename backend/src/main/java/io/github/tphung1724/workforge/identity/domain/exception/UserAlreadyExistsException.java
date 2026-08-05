package io.github.tphung1724.workforge.identity.domain.exception;

public final class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException() {
        super(IdentityErrorCode.USER_ALREADY_EXISTS);
    }
}
