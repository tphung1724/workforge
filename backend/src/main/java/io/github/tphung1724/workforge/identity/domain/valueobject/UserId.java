package io.github.tphung1724.workforge.identity.domain.valueobject;

import io.github.tphung1724.workforge.identity.domain.exception.IdentityErrorCode;
import io.github.tphung1724.workforge.identity.domain.exception.InvalidUserIdException;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public final class UserId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID value;

    public UserId(final UUID value) {
        this.value = validate(value);
    }

    public static UserId from(final String value) {
        if (value == null) {
            throw new InvalidUserIdException();
        }

        final String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new InvalidUserIdException();
        }

        try {
            return new UserId(UUID.fromString(normalized));
        } catch (IllegalArgumentException ex) {
            throw new InvalidUserIdException();
        }
    }

    public static UserId from(final UUID value) {
        return new UserId(value);
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    public boolean sameValueAs(final UserId other) {
        return equals(other);
    }

    private static UUID validate(final UUID value) {
        if (value == null) {
            throw new InvalidUserIdException();
        }

        return value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof UserId other)) {
            return false;
        }

        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
