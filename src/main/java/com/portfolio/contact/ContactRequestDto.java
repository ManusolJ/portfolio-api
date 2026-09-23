package com.portfolio.contact;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Body of {@code POST /api/v1/contact}. */
public record ContactRequestDto(
    @Email @NotBlank @Size(max = 255) String email,
    @NotBlank @Size(min = 2, max = 100) String name,
    @NotBlank @Size(min = 2, max = 150) String subject,
    @NotBlank @Size(min = 10, max = 2000) String message
) {

}
