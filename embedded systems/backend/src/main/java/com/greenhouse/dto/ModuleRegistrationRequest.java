package com.greenhouse.dto;

import jakarta.validation.constraints.NotBlank;

public record ModuleRegistrationRequest(
        @NotBlank(message = "baseUrl is required")
        String baseUrl,
        String name
) {
}
