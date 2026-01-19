package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.PurchaseOrderRequestDto;
import ru.itmo.se.is.cw.dto.PurchaseOrderResponseDto;
import ru.itmo.se.is.cw.model.EmployeeEntity;
import ru.itmo.se.is.cw.model.PurchaseOrderEntity;

@Mapper(config = MapperConfig.class, uses = PurchaseOrderMaterialMapper.class)
public interface PurchaseOrderMapper {

    @Mapping(target = "supplyManagerId", source = "supplyManager.id")
    @Mapping(target = "supplyManagerFirstName", source = "supplyManager.person.firstName")
    @Mapping(target = "supplyManagerLastName", source = "supplyManager.person.lastName")
    @Mapping(target = "status", source = "currentStatus.status")
    PurchaseOrderResponseDto toDto(PurchaseOrderEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "materials", ignore = true)
    @Mapping(target = "supplyManager", source = "supplyManager")
    PurchaseOrderEntity toEntity(PurchaseOrderRequestDto dto, EmployeeEntity supplyManager);

}
