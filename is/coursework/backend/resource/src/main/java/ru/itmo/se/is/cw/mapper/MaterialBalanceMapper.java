package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.MaterialBalanceHistoryResponseDto;
import ru.itmo.se.is.cw.model.MaterialBalanceEntity;

@Mapper(config = MapperConfig.class)
public interface MaterialBalanceMapper {

    @Mapping(target = "balance", expression = "java(toDouble(entity.getBalance()))")
    MaterialBalanceHistoryResponseDto.BalanceEntryDto toEntry(MaterialBalanceEntity entity);

    default Double toDouble(java.math.BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
