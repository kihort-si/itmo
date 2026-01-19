package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.ProductionTaskResponseDto;
import ru.itmo.se.is.cw.model.ProductionTaskEntity;

@Mapper(config = MapperConfig.class)
public interface ProductionTaskMapper {
    @Mapping(target = "clientOrderId", source = "clientOrder.id")
    @Mapping(target = "status", source = "currentStatus.status")
    @Mapping(target = "cncOperatorId", source = "cncOperator.id")
    ProductionTaskResponseDto toDto(ProductionTaskEntity entity);
}
