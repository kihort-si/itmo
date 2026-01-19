package ru.itmo.se.is.cw.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Фильтр по дизайнам продуктов")
public class ProductDesignFilter {

    @Schema(description = "Фильтр по идентификатору конструктора (nullable)", example = "101", nullable = true)
    private Long constructorId;

    @Schema(description = "Фильтр по названию продукта (подстрока, без учета регистра)", example = "стол", nullable = true)
    private String productName;

    @Schema(description = "Создан после (включительно)", example = "2023-10-01T00:00:00Z", nullable = true)
    private ZonedDateTime createdFrom;

    @Schema(description = "Создан до (включительно)", example = "2023-10-31T23:59:59Z", nullable = true)
    private ZonedDateTime createdTo;

    @Schema(description = "Обновлён после (включительно)", example = "2023-10-01T00:00:00Z", nullable = true)
    private ZonedDateTime updatedFrom;

    @Schema(description = "Обновлён до (включительно)", example = "2023-10-31T23:59:59Z", nullable = true)
    private ZonedDateTime updatedTo;
}
