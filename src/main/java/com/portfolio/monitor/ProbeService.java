package com.portfolio.monitor;

import java.util.List;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

/** Records whether a service answers over HTTP, and how quickly. */
@Service
public class ProbeService {

    private static final Logger log = LoggerFactory.getLogger(ProbeService.class);

    private final RestClient restClient;
    private final ProbeRepository probeRepository;

    public ProbeService(RestClient restClient, ProbeRepository probeRepository) {
        this.restClient = restClient;
        this.probeRepository = probeRepository;
    }

    public void recordProbeResult(List<MonitorProperties.Service> serviceList) {
        if (serviceList == null || serviceList.isEmpty()) {
            throw new RuntimeException("Service list not provided");
        }

        for(MonitorProperties.Service service: serviceList) {
            try {
                probeRepository.record(service.name(), probe(service.url()));
            } catch (RuntimeException ex) {
                log.warn("Could not record a check for {}: {}", service.name(), ex.getMessage());
            }
        }
    }

    public void pruneOldRecords() {
        probeRepository.pruneChecksOlderThanADay();
    }

    private ProbeResult probe(String url) {
        long start = System.nanoTime();

        try {
            return restClient.get().uri(url).exchange((request, response) -> {
                long elapsed = (System.nanoTime() - start) / 1_000_000;
                return new ProbeResult(
                    response.getStatusCode().is2xxSuccessful(),
                    elapsed,
                    response.getStatusCode().value());
            });
        } catch (ResourceAccessException ex) {
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            return new ProbeResult(false, elapsed, null);
        }
    }
}
