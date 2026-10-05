package com.portfolio.telemetry;

/** One sample of the host's vitals; tempCelsius is null when no thermal zone is configured. */
public record HostMetrics(
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
