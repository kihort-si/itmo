package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.MaterialConsumptionResponseDto;
import ru.itmo.se.is.cw.model.MaterialConsumptionEntity;

@Mapper(config = MapperConfig.class)
public interface MaterialConsumptionMapper {

    @Mapping(target = "clientOrderId", source = "clientOrder.id")
    @Mapping(target = "materialId", source = "material.id")
    @Mapping(target = "amount", expression = "java(toDouble(entity.getAmount()))")
    MaterialConsumptionResponseDto toDto(MaterialConsumptionEntity entity);

    default Double toDouble(java.math.BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
