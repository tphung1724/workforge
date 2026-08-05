package io.github.tphung1724.workforge.identity.domain.valueobject;

import io.github.tphung1724.workforge.identity.domain.exception.InvalidEmailException;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Email implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final String value;

    public Email(final String value) {
        this.value = normalizeAndValidate(value);
    }

    public static Email of(final String value) {
        return new Email(value);
    }

    public String value() {
        return value;
    }

    public boolean sameValueAs(final Email other) {
        return this.equals(other);
    }

    private static String normalizeAndValidate(final String value) {
        if (value == null) {
            throw new InvalidEmailException();
        }

        final String normalized = value.trim().toLowerCase(Locale.ROOT);

        if (normalized.isEmpty()) {
            throw new InvalidEmailException();
        }

        if (normalized.length() > 254) {
            throw new InvalidEmailException();
        }

        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new InvalidEmailException();
        }

        return normalized;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Email other)) {
            return false;
        }

        return Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
