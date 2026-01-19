package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.RequiredMaterialDto;
import ru.itmo.se.is.cw.model.MaterialEntity;
import ru.itmo.se.is.cw.model.ProductDesignEntity;
import ru.itmo.se.is.cw.model.RequiredMaterialEntity;

import java.math.BigDecimal;

@Mapper(config = MapperConfig.class)
public interface RequiredMaterialMapper {
    @Mapping(target = "materialId", source = "material.id")
    @Mapping(target = "amount", expression = "java(toDouble(entity.getAmount()))")
    RequiredMaterialDto toDto(RequiredMaterialEntity entity);

    @Mapping(target = "id", ignore = true) // обычно id генерится БД
    @Mapping(target = "material", source = "material")
    @Mapping(target = "productDesign", source = "productDesign")
    @Mapping(target = "amount", expression = "java(toBigDecimal(dto.getAmount()))")
    RequiredMaterialEntity toEntity(
            RequiredMaterialDto dto,
            MaterialEntity material,
            ProductDesignEntity productDesign
    );

    default Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    default BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}
