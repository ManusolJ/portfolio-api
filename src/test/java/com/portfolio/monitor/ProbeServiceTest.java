package com.portfolio.monitor;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

/** Probes a scripted fake server */
class ProbeServiceTest {

    private static final String URL = "https://example.test/health";

    private ProbeService probeService;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        probeService = new ProbeService(builder.build());
    }

    @Test
    void reportsUpOn2xx() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.OK));

        ProbeResult result = probeService.probe(URL);

        assertThat(result.up()).isTrue();
        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.latencyMs()).isGreaterThanOrEqualTo(0);
        server.verify();
    }

    @Test
    void reportsDownWithStatusOnNon2xx() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        ProbeResult result = probeService.probe(URL);

        assertThat(result.up()).isFalse();
        assertThat(result.statusCode()).isEqualTo(503);
        server.verify();
    }

    @Test
    void reportsDownWithoutStatusWhenNothingAnswers() {
        server.expect(requestTo(URL)).andRespond(request -> {
            throw new IOException("Connection refused");
        });

        ProbeResult result = probeService.probe(URL);

        assertThat(result.up()).isFalse();
        assertThat(result.statusCode()).isNull();
        server.verify();
    }
}
