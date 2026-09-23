package com.portfolio.monitor;

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

@Service
public class ProbeService {

    private final RestClient restClient;

    public ProbeService(RestClient restClient) {
        this.restClient = restClient;
    }

    public ProbeResult probe(String url) {
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
