package ru.itmo.se.is.cw.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Фильтр по каталогу продуктов")
public class ProductCatalogFilter {

    @Schema(description = "Идентификатор дизайна продукта", example = "15")
    private Long productDesignId;

    @Schema(description = "Название (частичное совпадение, без учета регистра)", example = "стол")
    private String name;

    @Schema(description = "Описание (частичное совпадение, без учета регистра)", example = "деревян")
    private String description;

    @Schema(description = "Категория (частичное совпадение, без учета регистра)", example = "kitchen")
    private String category;

    @Schema(description = "Цена от", example = "1000.00")
    private BigDecimal priceMin;

    @Schema(description = "Цена до", example = "5000.00")
    private BigDecimal priceMax;

    @Schema(description = "Минимальное количество от", example = "1")
    private Short minimalAmountMin;

    @Schema(description = "Минимальное количество до", example = "20")
    private Short minimalAmountMax;
}
