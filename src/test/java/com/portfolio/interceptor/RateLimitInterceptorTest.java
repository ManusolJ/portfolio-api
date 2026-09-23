package com.portfolio.interceptor;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import com.portfolio.ratelimiter.CacheProperties;
import com.portfolio.ratelimiter.RateLimitProperties;
import com.portfolio.ratelimiter.RateLimiterService;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests the interceptor directly */
class RateLimitInterceptorTest {

    private static final int AMOUNT = 1;
    private static final int CAPACITY = 5;
    private static final int INTERVAL_REFILL = 1;

    private static final int MAX_BUCKETS = 1000;

    private static final String CLIENT_IP = "203.0.113.9";

    private RateLimitInterceptor interceptor;

    @BeforeEach
    void setUp() {
        CacheProperties cache = new CacheProperties(MAX_BUCKETS);
        RateLimitProperties limits = new RateLimitProperties(AMOUNT, CAPACITY, Duration.ofHours(INTERVAL_REFILL));

        interceptor = new RateLimitInterceptor(new RateLimiterService(limits, cache));
    }

    @Test
    void allowsRequestsUpToCapacity() {
        for (int attempt = 1; attempt <= CAPACITY; attempt++) {
            MockHttpServletResponse response = new MockHttpServletResponse();

            assertThat(interceptor.preHandle(requestFrom(CLIENT_IP), response, null)).isTrue();
            assertThat(response.getHeader("X-Rate-Limit-Remaining")).isEqualTo(String.valueOf(CAPACITY - attempt));
        }
    }

    @Test
    void refusesWithRetryAfterOnceCapacityIsSpent() {
        exhaust(CLIENT_IP);

        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(requestFrom(CLIENT_IP), response, null)).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("X-Rate-Limit-Remaining")).isEqualTo("0");
        assertThat(Long.parseLong(response.getHeader("Retry-After"))).isPositive();
    }

    @Test
    void keepsASeparateBucketPerClient() {
        exhaust(CLIENT_IP);

        assertThat(interceptor.preHandle(requestFrom("198.51.100.4"), new MockHttpServletResponse(), null))
            .isTrue();
    }

    @Test
    void fallsBackToRemoteAddressWithoutTheCloudflareHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(CLIENT_IP);

        for (int attempt = 1; attempt <= CAPACITY; attempt++) {
            assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), null)).isTrue();
        }

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), null)).isFalse();
    }

    @Test
    void ignoresASpoofedForwardedForHeader() {
        exhaust(CLIENT_IP);

        MockHttpServletRequest request = requestFrom(CLIENT_IP);
        request.addHeader("X-Forwarded-For", "1.2.3.4");

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), null)).isFalse();
    }

    private MockHttpServletRequest requestFrom(String clientIp) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/contact");
        request.addHeader("CF-Connecting-IP", clientIp);

        return request;
    }

    private void exhaust(String clientIp) {
        for (int attempt = 0; attempt < CAPACITY; attempt++) {
            interceptor.preHandle(requestFrom(clientIp), new MockHttpServletResponse(), null);
        }
    }
}
