package ru.itmo.se.is.cw.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.model.ClientApplicationAttachmentEntity;
import ru.itmo.se.is.cw.model.ClientApplicationEntity;
import ru.itmo.se.is.cw.model.FileEntity;

@Mapper(config = MapperConfig.class)
public interface ClientApplicationAttachmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clientApplication", source = "application")
    @Mapping(target = "file", source = "file")
    ClientApplicationAttachmentEntity toEntity(
            ClientApplicationEntity application,
            FileEntity file
    );
}