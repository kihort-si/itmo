package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Schema(description = "Сообщение в диалоге")
public class MessageResponseDto {
    @Schema(description = "Уникальный идентификатор сообщения", example = "1101")
    private Long id;

    @Schema(description = "Уникальный идентификатор связанного диалога", example = "1001")
    private Long conversationId;

    @Schema(description = "Уникальный идентификатор автора сообщения", example = "123")
    private Long authorId;

    @Schema(description = "Содержание сообщения", example = "Добрый день! Когда будет готов заказ?")
    private String content;

    @Schema(description = "Дата и время отправки сообщения", example = "2023-10-20T10:00:00Z")
    private ZonedDateTime sentAt;

    @Schema(description = "Список идентификаторов файлов-вложений", example = "[201, 202]")
    private List<Long> attachmentFileIds;
}
