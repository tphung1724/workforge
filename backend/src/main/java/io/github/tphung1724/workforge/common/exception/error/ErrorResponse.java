package io.github.tphung1724.workforge.common.exception.error;

import java.time.Instant;

public record ErrorResponse(
    Instant timestamp,

    int status,

    String error,

    String code,

    String message,

    String path,

    String traceId
) {
}
