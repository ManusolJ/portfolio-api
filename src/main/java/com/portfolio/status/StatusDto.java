package com.portfolio.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Everything the status panel renders, composed from stored checks, rollups and incidents. */
public record StatusDto(
    Instant generatedAt,
    List<ServiceStatus> services
) {

    /** Current state of one monitored service plus its recent history. */
    public record ServiceStatus(
        String name,
        boolean up,
        Integer latencyMs,
        Integer statusCode,
        Instant checkedAt,
        Uptime uptime,
        List<DayUptime> days,
        LastIncident lastIncident
    ) {
    }

    /** Uptime over the usual windows, as a percentage of the checks that were expected. */
    public record Uptime(
        double last7Days,
        double last30Days,
        double last90Days
    ) {
    }

    /** One bar: uptime for a day, and whether the monitor itself missed part of it. */
    public record DayUptime(
        LocalDate day,
        double uptime,
        Integer avgLatencyMs,
        boolean hasGap
    ) {
    }

    /** Most recent outage; ongoing when endedAt is null. */
    public record LastIncident(
        Instant startedAt,
        Instant endedAt,
        Integer statusCode,
        boolean ongoing
    ) {
    }
}
