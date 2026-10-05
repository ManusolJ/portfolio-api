package com.portfolio.status;

import java.time.Instant;

public record Incident(
    long id,
    String service,
    Instant endedAt,
    Instant startedAt,
    Integer statusCode
) {

}
