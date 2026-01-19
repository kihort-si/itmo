package ru.itmo.se.is.cw.mapper;

import org.mapstruct.*;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.MaterialRequestDto;
import ru.itmo.se.is.cw.dto.MaterialResponseDto;
import ru.itmo.se.is.cw.model.MaterialEntity;

import java.math.BigDecimal;

@Mapper(config = MapperConfig.class)
public interface MaterialMapper {

    @Mapping(target = "orderPoint", expression = "java(toDouble(entity.getOrderPoint()))")
    @Mapping(target = "currentBalance", source = "currentBalance.balance", qualifiedByName = "bdToDouble")
    MaterialResponseDto toDto(MaterialEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentBalance", ignore = true)
    @Mapping(target = "orderPoint", expression = "java(toBigDecimal(dto.getOrderPoint()))")
    MaterialEntity toEntity(MaterialRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentBalance", ignore = true)
    @Mapping(target = "orderPoint", expression = "java(toBigDecimal(dto.getOrderPoint()))")
    void updateEntity(@MappingTarget MaterialEntity entity, MaterialRequestDto dto);

    default Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    default BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    @Named("bdToDouble")
    default Double bdToDouble(BigDecimal value) {
        return toDouble(value);
    }
}
