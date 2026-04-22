package ru.itmo.blps.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateShopRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 255) String address
) {
}
