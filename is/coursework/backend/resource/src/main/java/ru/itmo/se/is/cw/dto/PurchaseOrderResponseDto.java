package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.itmo.se.is.cw.model.value.PurchaseOrderStatus;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Schema(description = "Заявка на закупку")
public class PurchaseOrderResponseDto {
    @Schema(description = "Уникальный идентификатор заявки", example = "1301")
    private Long id;

    @Schema(description = "Уникальный идентификатор менеджера снабжения", example = "101")
    private Long supplyManagerId;

    @Schema(description = "Имя менеджера снабжения", example = "Иван")
    private String supplyManagerFirstName;

    @Schema(description = "Фамилия менеджера снабжения", example = "Петров")
    private String supplyManagerLastName;

    @Schema(description = "Статус заявки")
    private PurchaseOrderStatus status;

    @Schema(description = "Дата и время создания заявки", example = "2023-10-20T10:00:00Z")
    private ZonedDateTime createdAt;

    @Schema(description = "Список материалов в заявке")
    private List<PurchaseOrderMaterialDto> materials;
}
