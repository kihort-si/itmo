package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Запись о расходе материала")
public class MaterialConsumptionResponseDto {
    @Schema(description = "Уникальный идентификатор записи", example = "1501")
    private Long id;

    @Schema(description = "Уникальный идентификатор связанного заказа", example = "901")
    private Long clientOrderId;

    @Schema(description = "Уникальный идентификатор материала", example = "301")
    private Long materialId;

    @Schema(description = "Количество израсходованного материала", example = "2.0")
    private Double amount;

    @Schema(description = "Дата и время создания записи", example = "2023-10-20T10:00:00Z")
    private ZonedDateTime createdAt;
}
