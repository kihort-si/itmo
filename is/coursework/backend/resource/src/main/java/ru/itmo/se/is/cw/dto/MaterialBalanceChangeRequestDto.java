package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на изменение баланса материала")
public class MaterialBalanceChangeRequestDto {

    @Schema(description = "Значение (>= 0)",
            example = "5.0")
    private Double amount;
}
