package io.github.tphung1724.workforge.identity.application.service;

import io.github.tphung1724.workforge.identity.application.command.RegisterCommand;
import io.github.tphung1724.workforge.identity.application.port.in.RegisterUseCase;
import io.github.tphung1724.workforge.identity.application.port.out.PasswordEncoder;
import io.github.tphung1724.workforge.identity.application.result.RegisterResult;
import io.github.tphung1724.workforge.identity.domain.aggregate.User;
import io.github.tphung1724.workforge.identity.domain.enums.RoleType;
import io.github.tphung1724.workforge.identity.domain.exception.UserAlreadyExistsException;
import io.github.tphung1724.workforge.identity.domain.repository.UserRepository;
import io.github.tphung1724.workforge.identity.domain.valueobject.Email;
import io.github.tphung1724.workforge.identity.domain.valueobject.Password;
import io.github.tphung1724.workforge.identity.domain.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

public final class RegisterService implements RegisterUseCase {

    private static final RoleType DEFAULT_ROLE = RoleType.USER;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(
        final UserRepository userRepository,
        final PasswordEncoder passwordEncoder
    ) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder must not be null");
    }

    @Override
    public RegisterResult register(final RegisterCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        final Email email = Email.of(command.email());

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException();
        }

        final String hashedPassword = passwordEncoder.encode(command.plainPassword());
        final Password password = Password.of(hashedPassword);

        final User user = User.register(
            UserId.generate(),
            email,
            password,
            command.fullName(),
            DEFAULT_ROLE,
            Instant.now()
        );

        userRepository.save(user);

        return new RegisterResult(
            user.getId(),
            user.getEmail().value(),
            user.getFullName(),
            user.getStatus(),
            user.getRole(),
            user.isEmailVerified()
        );
    }
}
