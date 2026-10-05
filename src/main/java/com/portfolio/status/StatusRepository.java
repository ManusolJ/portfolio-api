package com.portfolio.status;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.Optional;

/** Reads what the monitor has already stored; never probes anything. */
@Repository
public class StatusRepository {

    private static final String LATEST_CHECKS =
        """
            SELECT DISTINCT ON (service) service, up, latency_ms, status_code, checked_at
            FROM service_check
            ORDER BY service, checked_at DESC
            """;

    private static final String LAST_90_DAYS_OF_SERVICE =
        """
            SELECT service, day, checks, ups, latency_sum
            FROM service_day
            WHERE day > current_date - 90
            ORDER BY service, day
            """;

    private static final String LATEST_INCIDENTS =
        """
            SELECT DISTINCT ON (service) id, service, started_at, ended_at, status_code
            FROM incident
            ORDER BY service, started_at DESC
            """;

    private static final String LATEST_HOST_SAMPLE =
        """
            SELECT sampled_at, cpu_percent, mem_used_bytes, mem_total_bytes,
                   disk_used_bytes, disk_total_bytes, load_1m AS load1m, uptime_seconds, temp_celsius
            FROM telemetry_sample
            ORDER BY sampled_at DESC
            LIMIT 1
            """;

    private static final String HOST_HISTORY =
        """
            SELECT date_bin('5 minutes', sampled_at, TIMESTAMPTZ '2000-01-01') AS bucket,
                   avg(cpu_percent) AS cpu_percent,
                   avg(mem_used_bytes)::bigint AS mem_used_bytes
            FROM telemetry_sample
            WHERE sampled_at > now() - interval '24 hours'
            GROUP BY bucket
            ORDER BY bucket
            """;

    private final JdbcClient db;

    public StatusRepository(JdbcClient db) {
        this.db = db;
    }

    public List<ServiceCheck> getLatestChecks() {
        return db.sql(LATEST_CHECKS).query(ServiceCheck.class).list();
    }

    public List<ServiceDay> getServiceDays() {
        return db.sql(LAST_90_DAYS_OF_SERVICE).query(ServiceDay.class).list();
    }

    public Optional<HostSample> getLatestHostSample() {
        return db.sql(LATEST_HOST_SAMPLE).query(HostSample.class).optional();
    }

    public List<HostPoint> getHostHistory() {
        return db.sql(HOST_HISTORY).query(HostPoint.class).list();
    }

    public List<Incident> getLatestIncidents() {
        return db.sql(LATEST_INCIDENTS).query(Incident.class).list();
    }
}
