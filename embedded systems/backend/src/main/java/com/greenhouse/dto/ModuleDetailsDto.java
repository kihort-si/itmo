package com.greenhouse.dto;

import java.time.Instant;
import java.util.List;

public record ModuleDetailsDto(
        Long id,
        Integer moduleUid,
        String name,
        String baseUrl,
        Instant lastSeen,
        List<PortDto> ports,
        List<DriverDto> drivers,
        List<BindingDto> bindings
) {
}
