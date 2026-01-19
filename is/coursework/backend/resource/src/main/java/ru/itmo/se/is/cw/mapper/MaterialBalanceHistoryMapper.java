package ru.itmo.se.is.cw.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.MaterialBalanceHistoryResponseDto;
import ru.itmo.se.is.cw.model.MaterialBalanceEntity;
import ru.itmo.se.is.cw.model.MaterialConsumptionEntity;
import ru.itmo.se.is.cw.model.MaterialEntity;

import java.util.List;

@Mapper(
        config = MapperConfig.class,
        uses = {MaterialMapper.class, MaterialBalanceMapper.class, MaterialConsumptionMapper.class}
)
public interface MaterialBalanceHistoryMapper {

    @Mapping(target = "material", source = "material")
    @Mapping(target = "balances", source = "balances")
    @Mapping(target = "consumptions", source = "consumptions")
    MaterialBalanceHistoryResponseDto toDto(
            MaterialEntity material,
            List<MaterialBalanceEntity> balances,
            List<MaterialConsumptionEntity> consumptions
    );
}
