package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.ClientOrderResponseDto;
import ru.itmo.se.is.cw.model.ClientOrderEntity;

import java.math.BigDecimal;

@Mapper(config = MapperConfig.class)
public interface ClientOrderMapper {

    @Mapping(target = "clientApplicationId", source = "clientApplication.id")
    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "productDesignId", source = "productDesign.id")
    @Mapping(target = "status", source = "currentStatus.status")
    @Mapping(target = "price", expression = "java(toDouble(entity.getPrice()))")
    @Mapping(target = "completedAt", ignore = true)
    ClientOrderResponseDto toDto(ClientOrderEntity entity);

    default Double toDouble(BigDecimal v) {
        return v == null ? null : v.doubleValue();
    }
}
