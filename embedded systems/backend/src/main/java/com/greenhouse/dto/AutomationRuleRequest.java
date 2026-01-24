package com.greenhouse.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AutomationRuleRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Source module id is required")
        Long sourceModuleId,

        @NotNull(message = "Source port id is required")
        Integer sourcePortId,

        @NotBlank(message = "Condition is required")
        String condition,

        @NotNull(message = "Threshold is required")
        Double threshold,

        @NotNull(message = "Target module id is required")
        Long targetModuleId,

        @NotNull(message = "Target port id is required")
        Integer targetPortId,

        @NotNull(message = "Action level is required")
        @Min(value = 0, message = "Action level must be at least 0")
        @Max(value = 255, message = "Action level must be at most 255")
        Integer actionLevel,

        @NotNull(message = "Enabled flag is required")
        Boolean enabled
) {}
