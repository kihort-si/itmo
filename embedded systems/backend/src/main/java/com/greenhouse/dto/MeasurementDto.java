package com.greenhouse.dto;

import java.time.Instant;

public record MeasurementDto(
        Integer portId,
        Integer driverId,
        Double value,
        Instant timestamp
) {
}
