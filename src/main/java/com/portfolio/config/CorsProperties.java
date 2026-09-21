package com.portfolio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties("app.cors")
public record CorsProperties(
    @NotBlank String allowedOrigin
) {
    
}
