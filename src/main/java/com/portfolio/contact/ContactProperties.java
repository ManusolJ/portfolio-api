package com.portfolio.contact;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Sender and recipient of contact-form mail (`app.mail.*`). */
@Validated
@ConfigurationProperties("app.mail")
public record ContactProperties(
    @Email @NotBlank String from,
    @Email @NotBlank String contactTo
) {}
