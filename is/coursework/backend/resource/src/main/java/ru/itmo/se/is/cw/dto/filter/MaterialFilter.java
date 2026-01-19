package ru.itmo.se.is.cw.dto.filter;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Фильтр по материалам")
public class MaterialFilter {

    @Schema(description = "Поиск по названию (подстрока, без учета регистра)", example = "фанера", nullable = true)
    private String name;

    @Schema(description = "Единица измерения (точное совпадение, без учета регистра)", example = "м2", nullable = true)
    private String unitOfMeasure;

    @Schema(description = "Минимальный порог заказа (orderPoint) (включительно)", example = "10.00", nullable = true)
    private BigDecimal orderPointFrom;

    @Schema(description = "Максимальный порог заказа (orderPoint) (включительно)", example = "100.00", nullable = true)
    private BigDecimal orderPointTo;

    @Schema(description = "Минимальный текущий остаток (currentBalance.balance) (включительно)", example = "0.00", nullable = true)
    private BigDecimal currentBalanceFrom;

    @Schema(description = "Максимальный текущий остаток (currentBalance.balance) (включительно)", example = "500.00", nullable = true)
    private BigDecimal currentBalanceTo;

    @Schema(description = "Только материалы ниже порога заказа (balance < orderPoint)", example = "true", nullable = true)
    private Boolean belowOrderPoint;

    @Schema(description = "Материалы без текущего баланса (currentBalance is null)", example = "false", nullable = true)
    private Boolean withoutCurrentBalance;
}
