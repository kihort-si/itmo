package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Требуемый материал для дизайна")
public class RequiredMaterialDto {
    @Schema(description = "Уникальный идентификатор материала", example = "301")
    private Long materialId;

    @Schema(description = "Количество материала", example = "2.5")
    private Double amount;
}
