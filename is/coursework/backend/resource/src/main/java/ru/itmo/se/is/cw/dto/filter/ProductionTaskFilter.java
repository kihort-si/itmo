package ru.itmo.se.is.cw.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Фильтр по производственным задачам")
public class ProductionTaskFilter {

    @Schema(description = "Идентификатор заказа клиента (nullable)", example = "1001", nullable = true)
    private Long clientOrderId;

    @Schema(description = "Текущий статус задачи (nullable)", example = "3", nullable = true)
    private Long currentStatusId;

    @Schema(description = "Оператор ЧПУ (nullable)", example = "42", nullable = true)
    private Long cncOperatorId;

    @Schema(description = "Начата после (включительно)", example = "2023-10-01T00:00:00Z", nullable = true)
    private ZonedDateTime startedFrom;

    @Schema(description = "Начата до (включительно)", example = "2023-10-31T23:59:59Z", nullable = true)
    private ZonedDateTime startedTo;

    @Schema(description = "Завершена после (включительно)", example = "2023-10-01T00:00:00Z", nullable = true)
    private ZonedDateTime finishedFrom;

    @Schema(description = "Завершена до (включительно)", example = "2023-10-31T23:59:59Z", nullable = true)
    private ZonedDateTime finishedTo;

    @Schema(description = "Создана после (включительно)", example = "2023-10-01T00:00:00Z", nullable = true)
    private ZonedDateTime createdFrom;

    @Schema(description = "Создана до (включительно)", example = "2023-10-31T23:59:59Z", nullable = true)
    private ZonedDateTime createdTo;
}

