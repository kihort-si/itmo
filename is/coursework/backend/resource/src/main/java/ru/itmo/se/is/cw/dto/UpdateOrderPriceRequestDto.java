package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление цены заказа")
public class UpdateOrderPriceRequestDto {
    @Schema(description = "Новая цена", example = "145000.0")
    private Double price;
}
