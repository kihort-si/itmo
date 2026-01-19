package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.itmo.se.is.cw.model.value.ProductionTaskStatus;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Производственная задача")
public class ProductionTaskResponseDto {
    @Schema(description = "Уникальный идентификатор задачи", example = "1201")
    private Long id;

    @Schema(description = "Уникальный идентификатор связанного заказа", example = "901")
    private Long clientOrderId;

    @Schema(description = "Статус задачи")
    private ProductionTaskStatus status;

    @Schema(description = "Уникальный идентификатор оператора CNC (если назначен)", example = "101", nullable = true)
    private Long cncOperatorId;

    @Schema(description = "Дата и время начала задачи", example = "2023-10-20T10:00:00Z", nullable = true)
    private ZonedDateTime startedAt;

    @Schema(description = "Дата и время завершения задачи", example = "2023-10-20T10:00:00Z", nullable = true)
    private ZonedDateTime finishedAt;

    @Schema(description = "Дата и время создания задачи", example = "2023-10-20T10:00:00Z")
    private ZonedDateTime createdAt;
}
