package com.portfolio.status;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;

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

    public List<Incident> getLatestIncidents() {
        return db.sql(LATEST_INCIDENTS).query(Incident.class).list();
    }
}
