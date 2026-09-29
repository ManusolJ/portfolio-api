package com.portfolio.monitor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Probes every configured service and stores each outcome. */
@Service
public class MonitorService {

    private static final Logger log = LoggerFactory.getLogger(MonitorService.class);

    private final ProbeService probeService;
    private final ProbeRepository probeRepository;
    private final MonitorProperties monitorProperties;
    private final TransactionTemplate transactionTemplate;

    public MonitorService(
        ProbeService probeService,
        ProbeRepository probeRepository,
        MonitorProperties monitorProperties,
        TransactionTemplate transactionTemplate
    ) {
        this.probeService = probeService;
        this.probeRepository = probeRepository;
        this.monitorProperties = monitorProperties;
        this.transactionTemplate = transactionTemplate;
    }

    public void runChecks() {
        for (MonitorProperties.Service service : monitorProperties.services()) {
            try {
                ProbeResult result = probeService.probe(service.url());

                transactionTemplate.executeWithoutResult(status -> {
                    probeRepository.record(service.name(), result);
                    recordTransition(service.name(), result);
                });
            } catch (RuntimeException ex) {
                log.warn("Could not record a check for {}: {}", service.name(), ex.getMessage());
            }
        }
    }

    private void recordTransition(String service, ProbeResult result) {
        if (result.up()) {
            if (probeRepository.closeOpenIncident(service) > 0) {
                log.info("{} is reachable again", service);
            }

            return;
        }

        if (probeRepository.openIncident(service, result.statusCode()) > 0) {
            log.warn("{} went down (status {})", service, result.statusCode());
        }
    }

    public void pruneOldChecks() {
        log.info("Pruned {} checks older than a day", probeRepository.pruneChecksOlderThanADay());
    }
}
