package com.portfolio.ratelimiter;

import org.springframework.validation.annotation.Validated;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Duration;

@Validated
@ConfigurationProperties("app.rate-limit")
public record RateLimitProperties(
    @Positive int amount,
    @Positive int capacity,
    @NotNull Duration refillInterval
) {
}
