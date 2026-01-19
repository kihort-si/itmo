package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.EmployeeRequestDto;
import ru.itmo.se.is.cw.dto.EmployeeResponseDto;
import ru.itmo.se.is.cw.model.EmployeeEntity;

@Mapper(config = MapperConfig.class)
public interface EmployeeMapper {

    @Mapping(target = "username", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    EmployeeResponseDto toDto(EmployeeEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "person.id", ignore = true)
    @Mapping(target = "person.firstName", source = "firstName")
    @Mapping(target = "person.lastName", source = "lastName")
    EmployeeEntity toEntity(EmployeeRequestDto request);

}
