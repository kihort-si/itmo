package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.ClientApplicationRequestDto;
import ru.itmo.se.is.cw.dto.ClientApplicationResponseDto;
import ru.itmo.se.is.cw.model.ClientApplicationEntity;

@Mapper(config = MapperConfig.class)
public interface ClientApplicationMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "templateProductDesignId", source = "templateProductDesign.id")
    @Mapping(target = "amount", expression = "java(toInteger(entity.getAmount()))")
    ClientApplicationResponseDto toDto(ClientApplicationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "templateProductDesign", ignore = true)
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "amount", expression = "java(toShort(dto.getAmount()))")
    @Mapping(target = "attachments", ignore = true)
    ClientApplicationEntity toEntity(ClientApplicationRequestDto dto);

    default Integer toInteger(Short value) {
        return value == null ? null : value.intValue();
    }

    default Short toShort(Integer value) {
        return value == null ? null : value.shortValue();
    }
}
