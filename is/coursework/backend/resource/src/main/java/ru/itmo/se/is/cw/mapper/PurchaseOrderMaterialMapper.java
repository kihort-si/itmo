package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.PurchaseOrderMaterialDto;
import ru.itmo.se.is.cw.model.MaterialEntity;
import ru.itmo.se.is.cw.model.PurchaseOrderEntity;
import ru.itmo.se.is.cw.model.PurchaseOrderMaterialEntity;

import java.math.BigDecimal;

@Mapper(config = MapperConfig.class)
public interface PurchaseOrderMaterialMapper {

    @Mapping(target = "materialId", source = "material.id")
    @Mapping(target = "amount", expression = "java(toDouble(entity.getAmount()))")
    @Mapping(target = "priceForUnit", expression = "java(toDouble(entity.getPriceForUnit()))")
    @Mapping(target = "realAmount", expression = "java(toDouble(entity.getRealAmount()))")
    PurchaseOrderMaterialDto toDto(PurchaseOrderMaterialEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "material", source = "material")
    @Mapping(target = "purchaseOrder", source = "purchaseOrder")
    @Mapping(target = "amount", expression = "java(toBigDecimal(dto.getAmount()))")
    @Mapping(target = "priceForUnit", expression = "java(toBigDecimal(dto.getPriceForUnit()))")
    PurchaseOrderMaterialEntity toEntity(
            PurchaseOrderMaterialDto dto,
            MaterialEntity material,
            PurchaseOrderEntity purchaseOrder
    );

    default Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    default BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}
