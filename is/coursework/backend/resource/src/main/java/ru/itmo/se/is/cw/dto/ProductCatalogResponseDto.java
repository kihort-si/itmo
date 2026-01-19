package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Элемент каталога товаров")
public class ProductCatalogResponseDto {
    @Schema(description = "Уникальный идентификатор товара", example = "501")
    private Long id;

    @Schema(description = "Название товара", example = "Кухонный стол")
    private String name;

    @Schema(description = "Описание товара", example = "Стол из натурального дерева")
    private String description;

    @Schema(description = "Уникальный идентификатор связанного дизайна", example = "601")
    private Long productDesignId;

    @Schema(description = "Цена товара", example = "15000.0")
    private Double price;

    @Schema(description = "Минимальное количество для заказа", example = "1")
    private Integer minimalAmount;

    @Schema(description = "Категория товара", example = "Мебель")
    private String category;

    @Schema(description = "Список фотографий товара")
    private List<ProductPhotoDto> photos;
}
