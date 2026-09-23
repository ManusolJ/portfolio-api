package com.portfolio.interceptor;

import org.springframework.stereotype.Component;

import org.springframework.web.servlet.HandlerInterceptor;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.portfolio.ratelimiter.RateLimiterService;

import java.time.Duration;

/** Refuses requests from a client that has spent its token bucket. */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final String CLIENT_IP_HEADER = "CF-Connecting-IP";
    private static final String REMAINING_HEADER = "X-Rate-Limit-Remaining";

    private final RateLimiterService rateLimiterService;

    public RateLimitInterceptor(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Bucket bucket = rateLimiterService.resolveBucket(getClientId(request));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader(REMAINING_HEADER, String.valueOf(probe.getRemainingTokens()));
            return true;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.addHeader(REMAINING_HEADER, "0");
        response.addHeader(HttpHeaders.RETRY_AFTER, String.valueOf(secondsUntilRefill(probe)));

        return false;
    }

    private long secondsUntilRefill(ConsumptionProbe probe) {
        return Math.max(1, Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds());
    }

    private String getClientId(HttpServletRequest request) {
        String clientIp = request.getHeader(CLIENT_IP_HEADER);

        if (clientIp != null && !clientIp.isBlank()) {
            return clientIp;
        }

        return request.getRemoteAddr();
    }
}
