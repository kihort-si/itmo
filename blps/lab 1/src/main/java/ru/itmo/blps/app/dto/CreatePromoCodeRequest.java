package ru.itmo.blps.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePromoCodeRequest(
        @NotBlank @Size(max = 32) String code,
        @NotNull @Min(1) @Max(90) Integer discountPercent,
        @NotNull Boolean active
) {
}
