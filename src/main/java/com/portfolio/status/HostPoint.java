package com.portfolio.status;

import java.time.Instant;

/** One five-minute bucket of host history, averaged. */
public record HostPoint(
    Instant bucket,
    double cpuPercent,
    long memUsedBytes
) {
}
