package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.FileMetadataResponseDto;
import ru.itmo.se.is.cw.model.FileEntity;
import ru.itmo.se.is.cw.model.ProductDesignEntity;
import ru.itmo.se.is.cw.model.ProductDesignFileEntity;

@Mapper(config = MapperConfig.class)
public interface ProductDesignFileMapper {
    @Mapping(target = "id", source = "file.id")
    @Mapping(target = "filename", source = "file.filename")
    @Mapping(target = "contentType", source = "file.contentType")
    @Mapping(target = "sizeBytes", source = "file.currentVersion.sizeBytes")
    @Mapping(target = "ownerId", source = "file.ownerId")
    @Mapping(target = "createdAt", source = "file.createdAt")
    @Mapping(target = "updatedAt", source = "file.updatedAt")
    @Mapping(target = "deletedAt", source = "file.deletedAt")
    FileMetadataResponseDto toDto(ProductDesignFileEntity entity);

    @Mapping(target = "id", ignore = true) // генерится БД
    @Mapping(target = "productDesign", source = "productDesign")
    @Mapping(target = "file", source = "file")
    @Mapping(target = "createdAt", ignore = true)
    ProductDesignFileEntity toEntity(
            ProductDesignEntity productDesign,
            FileEntity file
    );
}
