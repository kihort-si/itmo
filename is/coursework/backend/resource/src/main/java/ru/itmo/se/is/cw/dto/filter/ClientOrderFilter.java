package ru.itmo.se.is.cw.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.itmo.se.is.cw.model.value.ClientOrderStatus;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Фильтр по заказам")
public class ClientOrderFilter {

    @Schema(description = "Фильтр по статусу", example = "IN_PROGRESS", nullable = true)
    private ClientOrderStatus status;

    @Schema(description = "Фильтр по клиенту (nullable). Для CLIENT будет игнорироваться и заменяться на текущего клиента.",
            example = "456", nullable = true)
    private Long clientId;

    @Schema(description = "Фильтр по менеджеру", example = "101", nullable = true)
    private Long managerId;

    @Schema(description = "Создан после (включительно)", example = "2023-10-01T00:00:00Z", nullable = true)
    private ZonedDateTime createdFrom;

    @Schema(description = "Создан до (включительно)", example = "2023-10-31T23:59:59Z", nullable = true)
    private ZonedDateTime createdTo;
}
