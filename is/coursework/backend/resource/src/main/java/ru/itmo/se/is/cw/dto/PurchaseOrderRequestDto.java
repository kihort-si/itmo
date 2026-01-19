package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Запрос на создание заявки на закупку")
public class PurchaseOrderRequestDto {
    @Schema(description = "Список материалов в заявке")
    private List<PurchaseOrderMaterialDto> materials;
}
