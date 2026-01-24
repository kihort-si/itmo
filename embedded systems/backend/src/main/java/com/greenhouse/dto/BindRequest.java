package com.greenhouse.dto;

import jakarta.validation.constraints.NotNull;

public record BindRequest(
        @NotNull(message = "driverId is required")
        Integer driverId
) {
}
