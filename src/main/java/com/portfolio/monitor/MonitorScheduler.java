package com.portfolio.monitor;

import org.springframework.stereotype.Component;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/** Probes every configured service on a fixed cadence and records each outcome. */
@Component
@ConditionalOnProperty(name = "app.monitor.enabled", havingValue = "true")
public class MonitorScheduler {

    private final ProbeService probeService;
    private final MonitorProperties monitorProperties;

    public MonitorScheduler(
        ProbeService probeService,
        MonitorProperties monitorProperties
    ) {
        this.probeService = probeService;
        this.monitorProperties = monitorProperties;
    }

    @Scheduled(fixedDelayString = "${app.monitor.interval}")
    public void probeConfiguredServices() {
        probeService.recordProbeResult(monitorProperties.services());
    }

    @Scheduled(cron = "${app.monitor.prune-cron}")
    public void pruneOldChecks() {
        probeService.pruneOldRecords();
    }
}
