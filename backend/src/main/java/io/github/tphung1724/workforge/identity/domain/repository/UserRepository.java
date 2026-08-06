package io.github.tphung1724.workforge.identity.domain.repository;

import io.github.tphung1724.workforge.identity.domain.aggregate.User;
import io.github.tphung1724.workforge.identity.domain.valueobject.Email;
import io.github.tphung1724.workforge.identity.domain.valueobject.UserId;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId userId);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    void delete(User user);
}
