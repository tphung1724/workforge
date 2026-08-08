package io.github.tphung1724.workforge.identity.application.result;

import io.github.tphung1724.workforge.identity.domain.enums.RoleType;
import io.github.tphung1724.workforge.identity.domain.enums.UserStatus;
import io.github.tphung1724.workforge.identity.domain.valueobject.UserId;

import java.util.Objects;

public record RegisterResult(
    UserId userId,
    String email,
    String fullName,
    UserStatus status,
    RoleType role,
    boolean emailVerified
) {
    public RegisterResult {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(fullName, "fullName must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(role, "role must not be null");
    }
}
