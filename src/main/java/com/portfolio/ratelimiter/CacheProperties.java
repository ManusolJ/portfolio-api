package com.portfolio.ratelimiter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;

/** Bounds on the in-memory caches (`app.cache.*`). */
@Validated
@ConfigurationProperties("app.cache")
public record CacheProperties(
    @Positive long maxBuckets
) {

}
