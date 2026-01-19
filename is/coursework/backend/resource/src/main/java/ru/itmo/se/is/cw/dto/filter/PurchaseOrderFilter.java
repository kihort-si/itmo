package ru.itmo.se.is.cw.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.itmo.se.is.cw.model.value.PurchaseOrderStatus;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Фильтр по закупкам (purchase orders)")
public class PurchaseOrderFilter {

    @Schema(description = "Идентификатор менеджера снабжения (nullable)", example = "42", nullable = true)
    private Long supplyManagerId;

    @Schema(description = "Идентификатор текущего статуса (purchase_order_status.id) (nullable)", example = "10", nullable = true)
    private Long currentStatusId;

    @Schema(description = "Текущий статус (enum string в БД) (nullable)", example = "CREATED", nullable = true)
    private PurchaseOrderStatus status;

    @Schema(description = "Создана после (включительно)", example = "2023-10-01T00:00:00Z", nullable = true)
    private ZonedDateTime createdFrom;

    @Schema(description = "Создана до (включительно)", example = "2023-10-31T23:59:59Z", nullable = true)
    private ZonedDateTime createdTo;

    @Schema(description = "Показать закупки без текущего статуса (currentStatus is null) (nullable)", example = "false", nullable = true)
    private Boolean withoutCurrentStatus;
}
