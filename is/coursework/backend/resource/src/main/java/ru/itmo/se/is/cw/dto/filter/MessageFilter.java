package ru.itmo.se.is.cw.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Фильтр по сообщениям")
public class MessageFilter {

    @Schema(description = "Идентификатор диалога (nullable)", example = "1001", nullable = true)
    private Long conversationId;

    @Schema(description = "Идентификатор автора (account.id) (nullable)", example = "123", nullable = true)
    private Long authorId;

    @Schema(description = "Поиск по содержимому (подстрока, без учета регистра)", example = "готов", nullable = true)
    private String content;

    @Schema(description = "Отправлено после (включительно)", example = "2023-10-01T00:00:00Z", nullable = true)
    private ZonedDateTime sentFrom;

    @Schema(description = "Отправлено до (включительно)", example = "2023-10-31T23:59:59Z", nullable = true)
    private ZonedDateTime sentTo;

    @Schema(description = "Идентификатор участника диалога (conversation_participant.id) (nullable)",
            example = "1701", nullable = true)
    private Long participantId;
}
