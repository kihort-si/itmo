package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Материал")
public class MaterialResponseDto {
    @Schema(description = "Уникальный идентификатор материала", example = "301")
    private Long id;

    @Schema(description = "Название материала", example = "Дуб")
    private String name;

    @Schema(description = "Единица измерения", example = "м3")
    private String unitOfMeasure;

    @Schema(description = "Точка заказа", example = "10.0")
    private Double orderPoint;

    @Schema(description = "Текущий баланс", example = "15.5")
    private Double currentBalance;
}
