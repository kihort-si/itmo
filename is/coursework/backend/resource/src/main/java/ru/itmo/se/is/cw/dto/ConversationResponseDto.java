package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.itmo.se.is.cw.model.value.ConversationStatus;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Диалог по заказу")
public class ConversationResponseDto {
    @Schema(description = "Уникальный идентификатор диалога", example = "1001")
    private Long id;

    @Schema(description = "Уникальный идентификатор связанного заказа", example = "901")
    private Long orderId;

    @Schema(description = "Статус диалога", example = "ACTIVE")
    private ConversationStatus status;

    @Schema(description = "Дата и время создания диалога", example = "2023-10-20T10:00:00Z")
    private ZonedDateTime createdAt;
}
