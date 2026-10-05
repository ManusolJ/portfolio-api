package com.portfolio.status;

import java.time.Instant;

public record ServiceCheck(
    boolean up,
    String service,
    Instant checkedAt,
    Integer latencyMs,
    Integer statusCode
) {

}
