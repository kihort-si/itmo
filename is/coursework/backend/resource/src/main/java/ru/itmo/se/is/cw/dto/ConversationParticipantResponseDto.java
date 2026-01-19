package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Участник диалога")
public class ConversationParticipantResponseDto {
    @Schema(description = "Уникальный идентификатор участника", example = "1701")
    private Long id;

    @Schema(description = "Уникальный идентификатор связанного диалога", example = "1001")
    private Long conversationId;

    @Schema(description = "Уникальный идентификатор пользователя", example = "123")
    private Long userId;

    @Schema(description = "Дата и время присоединения к диалогу", example = "2023-10-20T10:00:00Z")
    private ZonedDateTime joinedAt;
}
