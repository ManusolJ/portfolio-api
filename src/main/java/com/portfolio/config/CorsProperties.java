package com.portfolio.config;

import org.springframework.validation.annotation.Validated;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties("app.cors")
public record CorsProperties(
    @NotBlank String allowedOrigin
) {
    
}
