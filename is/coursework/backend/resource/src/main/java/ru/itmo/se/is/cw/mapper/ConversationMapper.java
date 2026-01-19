package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.ConversationResponseDto;
import ru.itmo.se.is.cw.model.ConversationEntity;

@Mapper(config = MapperConfig.class)
public interface ConversationMapper {

    @Mapping(target = "orderId", source = "order.id")
    ConversationResponseDto toDto(ConversationEntity entity);
}
