package io.github.tphung1724.workforge.identity.domain.exception;

public final class InvalidEmailException extends DomainException {

    public InvalidEmailException() {
        super(IdentityErrorCode.INVALID_EMAIL);
    }
}
