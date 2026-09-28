package com.portfolio.monitor;

import org.springframework.stereotype.Repository;

import org.springframework.jdbc.core.simple.JdbcClient;

/** Stores probe outcomes: one row per check, plus a running daily rollup. */
@Repository
public class ProbeRepository {

    private static final String INSERT_CHECK =
        """
            INSERT INTO service_check (service, checked_at, up, latency_ms, status_code)
            VALUES (:service, now(), :up, :latencyMs, :statusCode)
            """;

    private static final String UPSERT_DAY =
        """
            INSERT INTO service_day (service, day, checks, ups, latency_sum)
            VALUES (:service, current_date, 1, :up, :latencyMs)
            ON CONFLICT (service, day) DO UPDATE
            SET checks = service_day.checks + 1,
                ups = service_day.ups + EXCLUDED.ups,
                latency_sum = service_day.latency_sum + EXCLUDED.latency_sum
            """;

    private static final String DELETE_OLD_CHECKS =
        """
            DELETE FROM service_check
            WHERE checked_at < now() - interval '24 hours'
            """;

    private final JdbcClient db;

    public ProbeRepository(JdbcClient db) {
        this.db = db;
    }

    public void record(String service, ProbeResult result) {
        db.sql(INSERT_CHECK)
            .param("service", service)
            .param("up", result.up())
            .param("latencyMs", result.latencyMs())
            .param("statusCode", result.statusCode())
            .update();

        db.sql(UPSERT_DAY)
            .param("service", service)
            .param("up", result.up() ? 1 : 0)
            .param("latencyMs", result.latencyMs())
            .update();
    }

    public int pruneChecksOlderThanADay() {
        return db.sql(DELETE_OLD_CHECKS).update();
    }
}
