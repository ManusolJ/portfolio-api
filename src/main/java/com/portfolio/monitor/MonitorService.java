package com.portfolio.monitor;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Probes every configured service and stores each outcome. */
@Service
public class MonitorService {

    private static final Logger log = LoggerFactory.getLogger(MonitorService.class);

    private final ProbeService probeService;
    private final ProbeRepository probeRepository;
    private final MonitorProperties monitorProperties;

    public MonitorService(
        ProbeService probeService,
        ProbeRepository probeRepository,
        MonitorProperties monitorProperties) {
        this.probeService = probeService;
        this.probeRepository = probeRepository;
        this.monitorProperties = monitorProperties;
    }

    public void runChecks() {
        for (MonitorProperties.Service service : monitorProperties.services()) {
            try {
                probeRepository.record(service.name(), probeService.probe(service.url()));
            } catch (RuntimeException ex) {
                log.warn("Could not record a check for {}: {}", service.name(), ex.getMessage());
            }
        }
    }

    public void pruneOldChecks() {
        log.info("Pruned {} checks older than a day", probeRepository.pruneChecksOlderThanADay());
    }
}
