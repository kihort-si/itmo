package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Запрос на создание/обновление товара каталога")
public class ProductCatalogRequestDto {
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

    @Schema(
            description = "Список идентификаторов файлов фотографий товара",
            example = "[9001, 9002, 9003]"
    )
    private List<Long> photoFileIds;
}
