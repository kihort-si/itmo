package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Запрос на создание/обновление дизайна продукта")
public class ProductDesignRequestDto {
    @Schema(description = "Название продукта", example = "Кухонный стол - Проект Beta")
    private String productName;

    @Schema(description = "Список идентификаторов файлов дизайна", example = "[201, 202]")
    private List<Long> fileIds;

    @Schema(description = "Список требуемых материалов")
    private List<RequiredMaterialDto> requiredMaterials;
}
