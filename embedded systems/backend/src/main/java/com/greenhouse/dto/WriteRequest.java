package com.greenhouse.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WriteRequest(
        @NotNull(message = "level is required")
        @Min(value = 0, message = "level must be between 0 and 255")
        @Max(value = 255, message = "level must be between 0 and 255")
        Integer level
) {
}
