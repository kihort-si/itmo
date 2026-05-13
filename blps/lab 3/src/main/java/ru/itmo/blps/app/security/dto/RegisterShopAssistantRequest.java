package ru.itmo.blps.app.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterShopAssistantRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull Long shopId,
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 6, max = 72) String password
) {
}
