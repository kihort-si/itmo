package ru.itmo.blps.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 3000) String description,
        @NotNull @Min(1) Long price,
        @NotNull @Min(0) Integer stock
) {
}
