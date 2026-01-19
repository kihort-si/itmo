package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.itmo.se.is.cw.model.value.ClientOrderStatus;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Клиентский заказ")
public class ClientOrderResponseDto {
    @Schema(description = "Уникальный идентификатор заказа", example = "901")
    private Long id;

    @Schema(description = "Уникальный идентификатор связанной заявки", example = "801")
    private Long clientApplicationId;

    @Schema(description = "Уникальный идентификатор менеджера", example = "101")
    private Long managerId;

    @Schema(description = "Уникальный идентификатор связанного дизайна", example = "601")
    private Long productDesignId;

    @Schema(description = "Статус заказа")
    private ClientOrderStatus status;

    @Schema(description = "Цена заказа", example = "150000.0", nullable = true)
    private Double price;

    @Schema(description = "Дата и время создания заказа", example = "2023-10-20T10:00:00Z")
    private ZonedDateTime createdAt;

    @Schema(description = "Дата и время завершения заказа (установки статуса COMPLETED)", example = "2023-10-25T15:30:00Z", nullable = true)
    private ZonedDateTime completedAt;
}
