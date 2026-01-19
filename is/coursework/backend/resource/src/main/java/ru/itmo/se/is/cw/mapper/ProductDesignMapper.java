package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.ProductDesignRequestDto;
import ru.itmo.se.is.cw.dto.ProductDesignResponseDto;
import ru.itmo.se.is.cw.model.ProductDesignEntity;

@Mapper(
        config = MapperConfig.class,
        uses = {ProductDesignFileMapper.class, RequiredMaterialMapper.class}
)
public interface ProductDesignMapper {
    @Mapping(target = "constructorId", source = "constructor.id")
    @Mapping(target = "files", source = "files")
    @Mapping(target = "requiredMaterials", source = "requiredMaterials")
    ProductDesignResponseDto toDto(ProductDesignEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "constructor", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "requiredMaterials", ignore = true)
    ProductDesignEntity toEntity(ProductDesignRequestDto request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "constructor", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "requiredMaterials", ignore = true)
    void updateEntity(@MappingTarget ProductDesignEntity entity, ProductDesignRequestDto request);
}
