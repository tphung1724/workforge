package io.github.tphung1724.workforge.identity.domain.exception;

public enum IdentityErrorCode {

    INVALID_USER_ID(
        "IDENTITY_001",
        "UserId is invalid."
    ),

    INVALID_EMAIL(
        "IDENTITY_002",
        "Email is invalid."
    ),

    INVALID_PASSWORD(
        "IDENTITY_003",
        "Password is invalid."
    ),

    USER_ALREADY_EXISTS(
        "IDENTITY_004",
        "User already exists."
    ),

    USER_NOT_FOUND(
        "IDENTITY_005",
        "User not found."
    );

    private final String code;
    private final String defaultMessage;

    IdentityErrorCode(
        final String code,
        final String defaultMessage
    ) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
