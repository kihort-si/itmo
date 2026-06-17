package ru.itmo.blps.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
        @Size(max = 160) String name,
        @Size(max = 3000) String description,
        @Min(1) Long price,
        @Min(0) Integer stock
) {
}

