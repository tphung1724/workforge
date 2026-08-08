package io.github.tphung1724.workforge.identity.domain.exception;

public final class UserNotFoundException extends DomainException {

    public UserNotFoundException() {
        super(IdentityErrorCode.USER_NOT_FOUND);
    }
}
