package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Запрос на регистрацию прихода по заявке на закупку")
public class PurchaseOrderReceiptRequest {
    @Schema(description = "Номер накладной", example = "INV-2023-12345")
    private String invoiceNumber;

    @Schema(description = "Список полученных материалов")
    private List<ReceivedItem> receivedItems;

    @Data
    public static class ReceivedItem {
        @Schema(description = "Уникальный идентификатор материала", example = "301")
        private Long materialId;

        @Schema(description = "Количество полученного материала", example = "5.0")
        private Double amount;
    }
}
