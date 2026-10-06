package com.portfolio.telemetry;

import org.springframework.validation.annotation.Validated;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.time.Duration;

/** Where to read the host's vitals and how often (`app.telemetry.*`). */
@Validated
@ConfigurationProperties("app.telemetry")
public record TelemetryProperties(
    @NotNull Boolean enabled,
    @NotNull Duration interval,
    @NotBlank String procPath,
    @NotBlank String diskPath,
    @NotBlank String pruneCron,
    @NotBlank String devicePath,
    String thermalSensor
) {
}
