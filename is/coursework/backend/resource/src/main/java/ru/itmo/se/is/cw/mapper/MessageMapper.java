package ru.itmo.se.is.cw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.se.is.cw.conf.MapperConfig;
import ru.itmo.se.is.cw.dto.MessageResponseDto;
import ru.itmo.se.is.cw.model.MessageEntity;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = MapperConfig.class)
public interface MessageMapper {

    @Mapping(target = "conversationId", source = "conversationParticipant.conversation.id")
    @Mapping(target = "authorId", source = "conversationParticipant.userId")
    @Mapping(target = "attachmentFileIds", expression = "java(mapAttachmentFileIds(entity))")
    MessageResponseDto toDto(MessageEntity entity);

    default List<Long> mapAttachmentFileIds(MessageEntity entity) {
        if (entity.getAttachments() == null || entity.getAttachments().isEmpty()) {
            return List.of();
        }
        return entity.getAttachments().stream()
                .map(attachment -> attachment.getFile().getId())
                .collect(Collectors.toList());
    }
}
