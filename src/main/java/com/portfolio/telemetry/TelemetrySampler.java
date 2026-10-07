package com.portfolio.telemetry;

import org.springframework.stereotype.Component;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Samples the host on a fixed cadence and stores the result. */
@Component
@ConditionalOnProperty(name = "app.telemetry.enabled", havingValue = "true")
public class TelemetrySampler {

    private static final Logger log = LoggerFactory.getLogger(TelemetrySampler.class);

    private final HostMetricsReader hostMetricsReader;
    private final TelemetryRepository telemetryRepository;

    public TelemetrySampler(HostMetricsReader hostMetricsReader, TelemetryRepository telemetryRepository) {
        this.hostMetricsReader = hostMetricsReader;
        this.telemetryRepository = telemetryRepository;
    }

    @Scheduled(fixedRateString = "${app.telemetry.interval}")
    public void sampleHost() {
        try {
            telemetryRepository.record(hostMetricsReader.read());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException ex) {
            log.warn("Could not sample the host: {}", ex.getMessage());
        }
    }

    @Scheduled(cron = "${app.telemetry.prune-cron}")
    public void pruneOldSamples() {
        log.info("Pruned {} samples older than a week", telemetryRepository.pruneSamplesOlderThanAWeek());
    }
}
