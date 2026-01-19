package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.AccountRequestDto;
import ru.itmo.se.is.cw.dto.AccountResponseDto;
import ru.itmo.se.is.cw.model.AccountEntity;

@Mapper(config = MapperConfig.class)
public interface AccountMapper {
    @Mapping(target = "accountId", source = "id")
    AccountResponseDto toDto(AccountEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "password", ignore = true)
    AccountEntity toEntity(AccountRequestDto dto);
}
