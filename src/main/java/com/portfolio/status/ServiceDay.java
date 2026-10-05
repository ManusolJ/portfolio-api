package com.portfolio.status;

import java.time.LocalDate;

public record ServiceDay(
    int ups,
    int checks,
    LocalDate day,
    String service,
    Long latencySum
) {

}
