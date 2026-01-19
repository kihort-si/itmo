package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Schema(description = "Дизайн продукта")
public class ProductDesignResponseDto {
    @Schema(description = "Уникальный идентификатор дизайна", example = "601")
    private Long id;

    @Schema(description = "Уникальный идентификатор конструктора (если назначен)", example = "101", nullable = true)
    private Long constructorId;

    @Schema(description = "Название продукта", example = "Кухонный стол - Проект Alpha")
    private String productName;

    @Schema(description = "Дата и время создания дизайна", example = "2023-10-20T10:00:00Z")
    private ZonedDateTime createdAt;

    @Schema(description = "Дата и время последнего обновления дизайна", example = "2023-10-20T10:00:00Z")
    private ZonedDateTime updatedAt;

    @Schema(description = "Список файлов дизайна")
    private List<FileMetadataResponseDto> files;

    @Schema(description = "Список требуемых материалов")
    private List<RequiredMaterialDto> requiredMaterials;
}
