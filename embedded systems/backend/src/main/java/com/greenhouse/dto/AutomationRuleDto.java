package com.greenhouse.dto;

import java.time.Instant;

public record AutomationRuleDto(
        Long id,
        String name,
        Long sourceModuleId,
        Integer sourcePortId,
        String condition,
        Double threshold,
        Long targetModuleId,
        Integer targetPortId,
        Integer actionLevel,
        Boolean enabled,
        Instant createdAt
) {}
