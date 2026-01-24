package com.greenhouse.dto;

import com.greenhouse.entity.AutomationRuleEntity;
import com.greenhouse.entity.BindingEntity;
import com.greenhouse.entity.ConditionType;
import com.greenhouse.entity.DriverEntity;
import com.greenhouse.entity.MeasurementEntity;
import com.greenhouse.entity.ModuleEntity;
import com.greenhouse.entity.PortEntity;

import java.util.List;
import java.util.stream.Collectors;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static ModuleSummaryDto toSummary(ModuleEntity module) {
        return new ModuleSummaryDto(
                module.getId(),
                module.getModuleUid(),
                module.getName(),
                module.getBaseUrl(),
                module.getLastSeen()
        );
    }

    public static ModuleDetailsDto toDetails(ModuleEntity module,
                                             List<PortEntity> ports,
                                             List<DriverEntity> drivers,
                                             List<BindingEntity> bindings) {
        List<PortDto> portDtos = ports.stream()
                .map(DtoMapper::toPort)
                .collect(Collectors.toList());
        List<DriverDto> driverDtos = drivers.stream()
                .map(DtoMapper::toDriver)
                .collect(Collectors.toList());
        List<BindingDto> bindingDtos = bindings.stream()
                .map(DtoMapper::toBinding)
                .collect(Collectors.toList());

        return new ModuleDetailsDto(
                module.getId(),
                module.getModuleUid(),
                module.getName(),
                module.getBaseUrl(),
                module.getLastSeen(),
                portDtos,
                driverDtos,
                bindingDtos
        );
    }

    public static PortDto toPort(PortEntity port) {
        return new PortDto(port.getPortId(), port.getType());
    }

    public static DriverDto toDriver(DriverEntity driver) {
        return new DriverDto(driver.getDriverId(), driver.getName(), driver.getType());
    }

    public static BindingDto toBinding(BindingEntity binding) {
        return new BindingDto(
                binding.getPort().getPortId(),
                binding.getDriver().getDriverId(),
                binding.getDriver().getName(),
                binding.getCreatedAt()
        );
    }

    public static MeasurementDto toMeasurement(MeasurementEntity measurement) {
        return new MeasurementDto(
                measurement.getPort().getPortId(),
                measurement.getDriver() != null ? measurement.getDriver().getDriverId() : null,
                measurement.getValue(),
                measurement.getCreatedAt()
        );
    }

    public static AutomationRuleDto toAutomationRule(AutomationRuleEntity rule) {
        return new AutomationRuleDto(
                rule.getId(),
                rule.getName(),
                rule.getSourceModule().getId(),
                rule.getSourcePortId(),
                conditionToString(rule.getConditionType()),
                rule.getThreshold(),
                rule.getTargetModule().getId(),
                rule.getTargetPortId(),
                rule.getActionLevel(),
                rule.getEnabled(),
                rule.getCreatedAt()
        );
    }

    public static ConditionType stringToCondition(String condition) {
        return switch (condition.toLowerCase()) {
            case "gt" -> ConditionType.GT;
            case "gte" -> ConditionType.GTE;
            case "lt" -> ConditionType.LT;
            case "lte" -> ConditionType.LTE;
            case "eq" -> ConditionType.EQ;
            default -> throw new IllegalArgumentException("Unknown condition: " + condition);
        };
    }

    public static String conditionToString(ConditionType type) {
        return switch (type) {
            case GT -> "gt";
            case GTE -> "gte";
            case LT -> "lt";
            case LTE -> "lte";
            case EQ -> "eq";
        };
    }
}
