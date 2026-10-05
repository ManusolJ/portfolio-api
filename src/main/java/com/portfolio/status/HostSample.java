package com.portfolio.status;

import java.time.Instant;

/** One stored row of host telemetry. */
public record HostSample(
    Instant sampledAt,
    double cpuPercent,
    long memUsedBytes,
    long memTotalBytes,
    long diskUsedBytes,
    long diskTotalBytes,
    double load1m,
    long uptimeSeconds,
    Double tempCelsius
) {
}
