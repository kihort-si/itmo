package ru.itmo.blps.app.dto;

import jakarta.validation.constraints.Size;

public record UpdateShopRequest(
        @Size(max = 160) String name,
        @Size(max = 255) String address
) {
}

