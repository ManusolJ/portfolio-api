package com.portfolio.ratelimiter;

import org.springframework.validation.annotation.Validated;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Duration;

/** Size and refill rate of each client's token bucket (`app.rate-limit.*`). */
@Validated
@ConfigurationProperties("app.rate-limit")
public record RateLimitProperties(
    @Positive int amount,
    @Positive int capacity,
    @Positive long maxBuckets,
    @NotNull Duration refillInterval
) {
}
