package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.ProductCatalogRequestDto;
import ru.itmo.se.is.cw.dto.ProductCatalogResponseDto;
import ru.itmo.se.is.cw.model.ProductCatalogEntity;

import java.math.BigDecimal;

@Mapper(
        config = MapperConfig.class,
        uses = {ProductPhotoMapper.class}
)
public interface ProductCatalogMapper {
    @Mapping(target = "productDesignId", source = "productDesign.id")
    @Mapping(target = "price", expression = "java(toDouble(entity.getPrice()))")
    @Mapping(target = "photos", source = "photos")
    ProductCatalogResponseDto toDto(ProductCatalogEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productDesign", ignore = true)
    @Mapping(target = "price", expression = "java(toBigDecimal(request.getPrice()))")
    @Mapping(target = "minimalAmount", expression = "java(toShort(request.getMinimalAmount()))")
    @Mapping(target = "photos", ignore = true)
    ProductCatalogEntity toEntity(ProductCatalogRequestDto request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productDesign", ignore = true)
    @Mapping(target = "price", expression = "java(toBigDecimal(request.getPrice()))")
    @Mapping(target = "minimalAmount", expression = "java(toShort(request.getMinimalAmount()))")
    @Mapping(target = "photos", ignore = true)
    void updateEntity(@MappingTarget ProductCatalogEntity entity, ProductCatalogRequestDto request);

    default Double toDouble(BigDecimal v) {
        return v == null ? null : v.doubleValue();
    }

    default BigDecimal toBigDecimal(Double v) {
        return v == null ? null : BigDecimal.valueOf(v);
    }

    default Short toShort(Integer v) {
        if (v == null) return null;
        return (short) (int) v;
    }
}
