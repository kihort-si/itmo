package ru.itmo.blps.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity
) {
}
