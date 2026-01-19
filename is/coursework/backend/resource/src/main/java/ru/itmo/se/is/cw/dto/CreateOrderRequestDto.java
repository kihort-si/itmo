package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание заказа")
public class CreateOrderRequestDto {
    @Schema(description = "Уникальный идентификатор связанной заявки", example = "801")
    private Long clientApplicationId;

    @Schema(description = "Цена заказа", example = "150000.0", nullable = true)
    private Double price;
}
