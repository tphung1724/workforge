package io.github.tphung1724.workforge.identity.domain.valueobject;

import io.github.tphung1724.workforge.identity.domain.exception.InvalidPasswordException;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public final class Password implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 255;

    private final String hashedValue;

    public Password(final String hashedValue) {
        this.hashedValue = validate(hashedValue);
    }

    public static Password of(final String hashedValue) {
        return new Password(hashedValue);
    }

    public String value() {
        return hashedValue;
    }

    public boolean sameValueAs(final Password other) {
        return this.equals(other);
    }

    private static String validate(final String hashedValue) {
        if (hashedValue == null) {
            throw new InvalidPasswordException();
        }

        if (hashedValue.isBlank()) {
            throw new InvalidPasswordException();
        }

        if (!hashedValue.equals(hashedValue.trim())) {
            throw new InvalidPasswordException();
        }

        if (hashedValue.length() < MIN_LENGTH) {
            throw new InvalidPasswordException();
        }

        if (hashedValue.length() > MAX_LENGTH) {
            throw new InvalidPasswordException();
        }

        return hashedValue;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Password other)) {
            return false;
        }

        return Objects.equals(this.hashedValue, other.hashedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashedValue);
    }

    @Override
    public String toString() {
        return "Password[PROTECTED]";
    }
}
