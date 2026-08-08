package io.github.tphung1724.workforge.identity.application.command;

import java.util.Objects;

public record RegisterCommand(
    String email,
    String plainPassword,
    String fullName
) {

    public RegisterCommand {
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(plainPassword, "plainPassword must not be null");
        Objects.requireNonNull(fullName, "fullName must not be null");
    }
}
