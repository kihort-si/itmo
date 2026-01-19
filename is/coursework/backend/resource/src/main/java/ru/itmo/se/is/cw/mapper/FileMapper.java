package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.FileMetadataResponseDto;
import ru.itmo.se.is.cw.model.FileEntity;

@Mapper(config = MapperConfig.class)
public interface FileMapper {
    @Mapping(target = "sizeBytes", source = "currentVersion.sizeBytes")
    FileMetadataResponseDto toDto(FileEntity entity);
}
