package com.portfolio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;

@Validated
@ConfigurationProperties("app.cache")
public record CacheProperties(
    @Positive long maxBuckets
) {

}
