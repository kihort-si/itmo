package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Материал в заявке на закупку")
public class PurchaseOrderMaterialDto {
    @Schema(description = "Уникальный идентификатор материала", example = "301")
    private Long materialId;

    @Schema(description = "Количество материала", example = "10.0")
    private Double amount;

    @Schema(description = "Цена за единицу", example = "500.0")
    private Double priceForUnit;

    @Schema(description = "Поставщик", example = "ООО 'Лесной мир'")
    private String supplier;

    @Schema(description = "Реальное количество полученного материала", example = "8.5", nullable = true)
    private Double realAmount;
}
