package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание/обновление материала")
public class MaterialRequestDto {
    @Schema(description = "Название материала", example = "Дуб (обновленный)")
    private String name;

    @Schema(description = "Единица измерения", example = "м3")
    private String unitOfMeasure;

    @Schema(description = "Точка заказа", example = "12.0")
    private Double orderPoint;
}
