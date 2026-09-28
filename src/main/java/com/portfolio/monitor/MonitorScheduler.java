package com.portfolio.monitor;

import org.springframework.stereotype.Component;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/** Probes every configured service on a fixed cadence and records each outcome. */
@Component
@ConditionalOnProperty(name = "app.monitor.enabled", havingValue = "true")
public class MonitorScheduler {

    private final MonitorService monitorService;

    public MonitorScheduler(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @Scheduled(fixedDelayString = "${app.monitor.interval}")
    public void probeConfiguredServices() {
        monitorService.runChecks();
    }

    @Scheduled(cron = "${app.monitor.prune-cron}")
    public void pruneOldChecks() {
        monitorService.pruneOldChecks();
    }
}
