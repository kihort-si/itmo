package com.greenhouse.dto;

import java.time.Instant;

public record BindingDto(
        Integer portId,
        Integer driverId,
        String driverName,
        Instant createdAt
) {
}
