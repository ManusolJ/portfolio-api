package com.portfolio.telemetry;

import org.springframework.stereotype.Repository;

import org.springframework.jdbc.core.simple.JdbcClient;

/** Stores host samples and drops the ones past the retention window. */
@Repository
public class TelemetryRepository {

    private static final String INSERT_SAMPLE =
        """
            INSERT INTO telemetry_sample (
                sampled_at, cpu_percent, mem_used_bytes, mem_total_bytes,
                disk_used_bytes, disk_total_bytes, load_1m, uptime_seconds, temp_celsius)
            VALUES (
                now(), :cpuPercent, :memUsedBytes, :memTotalBytes,
                :diskUsedBytes, :diskTotalBytes, :load1m, :uptimeSeconds, :tempCelsius)
            """;

    private static final String DELETE_OLD_SAMPLES =
        """
            DELETE FROM telemetry_sample
            WHERE sampled_at < now() - interval '7 days'
            """;

    private final JdbcClient db;

    public TelemetryRepository(JdbcClient db) {
        this.db = db;
    }

    public void record(HostMetrics metrics) {
        db.sql(INSERT_SAMPLE)
            .param("cpuPercent", metrics.cpuPercent())
            .param("memUsedBytes", metrics.memUsedBytes())
            .param("memTotalBytes", metrics.memTotalBytes())
            .param("diskUsedBytes", metrics.diskUsedBytes())
            .param("diskTotalBytes", metrics.diskTotalBytes())
            .param("load1m", metrics.load1m())
            .param("uptimeSeconds", metrics.uptimeSeconds())
            .param("tempCelsius", metrics.tempCelsius())
            .update();
    }

    public int pruneSamplesOlderThanAWeek() {
        return db.sql(DELETE_OLD_SAMPLES).update();
    }
}
