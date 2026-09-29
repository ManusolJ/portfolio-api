package com.portfolio.monitor;

import org.springframework.validation.annotation.Validated;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.time.Duration;

/** Services to probe and how often (`app.monitor.*`) */
@Validated
@ConfigurationProperties("app.monitor")
public record MonitorProperties(
    @NotNull Boolean enabled,
    @NotNull Duration interval,
    @NotBlank String pruneCron,
    List<@Valid Service> services
) {

    public MonitorProperties {
        services = services == null ? List.of() : List.copyOf(services);
    }

    /** Monitored service: the label shown on the panel and the URL to probe. */
    public record Service(
        @NotBlank String url,
        @NotBlank String name
    ) {
    }
}
