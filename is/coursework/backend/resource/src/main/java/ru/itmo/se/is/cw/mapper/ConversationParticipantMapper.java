package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.ConversationParticipantResponseDto;
import ru.itmo.se.is.cw.model.ConversationParticipantEntity;

@Mapper(config = MapperConfig.class)
public interface ConversationParticipantMapper {

    @Mapping(target = "conversationId", source = "conversation.id")
    ConversationParticipantResponseDto toDto(ConversationParticipantEntity entity);
}
