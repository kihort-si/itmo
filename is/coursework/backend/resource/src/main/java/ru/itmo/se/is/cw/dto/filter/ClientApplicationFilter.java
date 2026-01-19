package ru.itmo.se.is.cw.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Фильтр по клиентским заявкам")
public class ClientApplicationFilter {

    @Schema(description = "Фильтр по клиенту (nullable). Для роли CLIENT будет игнорироваться и заменяться на текущего клиента.",
            example = "15", nullable = true)
    private Long clientId;

    @Schema(description = "Поиск по описанию (подстрока, без учета регистра)", example = "стол", nullable = true)
    private String description;

    @Schema(description = "Шаблонный дизайн (nullable)", example = "601", nullable = true)
    private Long templateProductDesignId;

    @Schema(description = "Создана после (включительно)", example = "2023-10-01T00:00:00Z", nullable = true)
    private ZonedDateTime createdFrom;

    @Schema(description = "Создана до (включительно)", example = "2023-10-31T23:59:59Z", nullable = true)
    private ZonedDateTime createdTo;

    @Schema(description = "Количество (amount) минимум (включительно)", example = "1", nullable = true)
    private Integer amountFrom;

    @Schema(description = "Количество (amount) максимум (включительно)", example = "10", nullable = true)
    private Integer amountTo;
}
