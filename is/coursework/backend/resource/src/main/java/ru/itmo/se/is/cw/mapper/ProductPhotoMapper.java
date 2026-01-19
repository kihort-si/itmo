package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.ProductPhotoDto;
import ru.itmo.se.is.cw.model.FileEntity;
import ru.itmo.se.is.cw.model.ProductCatalogEntity;
import ru.itmo.se.is.cw.model.ProductPhotoEntity;

@Mapper(config = MapperConfig.class)
public interface ProductPhotoMapper {
    @Mapping(target = "fileId", source = "file.id")
    ProductPhotoDto toDto(ProductPhotoEntity entity);

    @Mapping(target = "file", source = "file")
    @Mapping(target = "productCatalog", source = "productCatalog")
    @Mapping(target = "id", ignore = true)
    ProductPhotoEntity toEntity(FileEntity file, ProductCatalogEntity productCatalog);
}
