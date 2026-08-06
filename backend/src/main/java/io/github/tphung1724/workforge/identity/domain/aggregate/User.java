package io.github.tphung1724.workforge.identity.domain.aggregate;

import io.github.tphung1724.workforge.identity.domain.enums.RoleType;
import io.github.tphung1724.workforge.identity.domain.enums.UserStatus;
import io.github.tphung1724.workforge.identity.domain.valueobject.Email;
import io.github.tphung1724.workforge.identity.domain.valueobject.Password;
import io.github.tphung1724.workforge.identity.domain.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

public final class User {

    private final UserId id;

    private Email email;

    private Password password;

    private String fullName;

    private UserStatus status;

    private RoleType role;

    private boolean emailVerified;

    private Instant emailVerifiedAt;

    private Instant lastLoginAt;

    private Instant passwordChangedAt;

    private Instant createdAt;

    private Instant updatedAt;

    private User(
        final UserId id,
        final Email email,
        final Password password,
        final String fullName,
        final RoleType role,
        final Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.password = Objects.requireNonNull(password, "password must not be null");
        this.fullName = validateFullName(fullName);
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.status = UserStatus.PENDING_VERIFICATION;
        this.emailVerified = false;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = createdAt;
    }

    public static User register(
        final UserId id,
        final Email email,
        final Password password,
        final String fullName,
        final RoleType role,
        final Instant now ) {
        return new User(id, email, password, fullName, role, now);
    }

    public UserId getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public RoleType getRole() {
        return role;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void verifyEmail(final Instant verifiedAt) {
        requireNotDeleted();
        requireNotLocked();

        if (emailVerified) {
            throw new IllegalStateException("Email already verified");
        }

        this.emailVerified = true;
        this.emailVerifiedAt = Objects.requireNonNull(verifiedAt, "verifiedAt must not be null");

        if (this.status == UserStatus.PENDING_VERIFICATION) {
            this.status = UserStatus.ACTIVE;
        }

        touch(verifiedAt);
    }

    public void activate(final Instant now) {
        requireNotDeleted();

        if (!emailVerified) {
            throw new IllegalStateException("Cannot activate user before email verification");
        }

        if (this.status == UserStatus.ACTIVE) {
            throw new IllegalStateException("User is already active");
        }

        this.status = UserStatus.ACTIVE;

        touch(now);
    }

    public void suspend(final Instant now) {
        requireNotDeleted();

        if (this.status == UserStatus.SUSPENDED) {
            throw new IllegalStateException("User is already suspended");
        }

        this.status = UserStatus.SUSPENDED;

        touch(now);
    }

    public void lock(final Instant now) {
        requireNotDeleted();

        if (this.status == UserStatus.LOCKED) {
            throw new IllegalStateException("User is already locked");
        }

        this.status = UserStatus.LOCKED;

        touch(now);
    }

    public void unlock(final Instant now) {
        requireNotDeleted();
        if (this.status != UserStatus.LOCKED) {
            throw new IllegalStateException("User is not locked");
        }

        if (!emailVerified) {
            this.status = UserStatus.PENDING_VERIFICATION;
        } else {
            this.status = UserStatus.ACTIVE;
        }

        touch(now);
    }

    public void delete(final Instant now) {
        if (this.status == UserStatus.DELETED) {
            throw new IllegalStateException("User is already deleted");
        }

        this.status = UserStatus.DELETED;

        touch(now);
    }

    public void login(final Instant now) {
        requireActive();

        this.lastLoginAt = Objects.requireNonNull(now, "now must not be null");

        touch(now);
    }

    public void changePassword(
        final Password currentPassword,
        final Password newPassword,
        final Instant now ) {
        requireActive();

        Objects.requireNonNull(currentPassword, "currentPassword must not be null");
        Objects.requireNonNull(newPassword, "newPassword must not be null");

        if (!this.password.equals(currentPassword)) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (this.password.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        this.password = newPassword;
        this.passwordChangedAt = Objects.requireNonNull(now, "now must not be null");

        touch(now);
    }

    public void resetPassword(final Password newPassword, final Instant now) {
        requireNotDeleted();

        Objects.requireNonNull(newPassword, "newPassword must not be null");

        if (this.password.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        this.password = newPassword;
        this.passwordChangedAt = Objects.requireNonNull(now, "now must not be null");

        touch(now);
    }

    public void updateProfile(final String newFullName, final Instant now) {
        requireActive();
        final String normalizedName = validateFullName(newFullName);

        if (this.fullName.equals(normalizedName)) {
            throw new IllegalArgumentException("Full name is unchanged");
        }

        this.fullName = normalizedName;

        touch(now);
    }

    public void changeEmail(final Email newEmail, final Instant now) {
        requireNotDeleted();

        Objects.requireNonNull(newEmail, "newEmail must not be null");

        if (this.email.equals(newEmail)) {
            throw new IllegalArgumentException("Email is unchanged");
        }

        this.email = newEmail;
        this.emailVerified = false;
        this.emailVerifiedAt = null;
        this.status = UserStatus.PENDING_VERIFICATION;

        touch(now);
    }

    public boolean canLogin() {
        return this.status == UserStatus.ACTIVE && this.emailVerified;
    }

    public boolean hasRole(final RoleType role) {
        return this.role == role;
    }

    private void requireActive() {
        requireNotDeleted();

        if (this.status != UserStatus.ACTIVE) {
            throw new IllegalStateException("User is not active");
        }

        if (!this.emailVerified) {
            throw new IllegalStateException("Email is not verified");
        }
    }

    private void requireNotDeleted() {
        if (this.status == UserStatus.DELETED) {
            throw new IllegalStateException("User is deleted");
        }
    }

    private void requireNotLocked() {
        if (this.status == UserStatus.LOCKED) {
            throw new IllegalStateException("User is locked");
        }
    }

    private static String validateFullName(final String fullName) {
        Objects.requireNonNull(fullName, "fullName must not be null");

        final String value = fullName.trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException("Full name must not be blank");
        }

        if (value.length() > 200) {
            throw new IllegalArgumentException("Full name must not exceed 200 characters");
        }

        return value;
    }

    private void touch(final Instant now) {
        this.updatedAt = Objects.requireNonNull(now, "now must not be null");
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof User other)) {
            return false;
        }

        return id.equals(other.id);
    }

    @Override public int hashCode() {
        return id.hashCode();
    }
}
