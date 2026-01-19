package ru.itmo.se.is.cw.mapper;

import org.mapstruct.*;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.ClientRegistrationRequestDto;
import ru.itmo.se.is.cw.dto.ClientResponseDto;
import ru.itmo.se.is.cw.dto.ClientUpdateRequestDto;
import ru.itmo.se.is.cw.model.ClientEntity;

@Mapper(config = MapperConfig.class)
public interface ClientMapper {
    ClientResponseDto toDto(ClientEntity entity);

    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person.firstName", source = "firstName")
    @Mapping(target = "person.lastName", source = "lastName")
    ClientEntity toEntity(ClientRegistrationRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "person.firstName", source = "firstName")
    @Mapping(target = "person.lastName", source = "lastName")
    @Mapping(target = "person.id", ignore = true)
    void updateEntity(ClientUpdateRequestDto request, @MappingTarget ClientEntity entity);
}
