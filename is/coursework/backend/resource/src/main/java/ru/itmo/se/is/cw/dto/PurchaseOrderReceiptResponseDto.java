package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Приход по заявке на закупку")
public class PurchaseOrderReceiptResponseDto {
    @Schema(description = "Уникальный идентификатор прихода", example = "1401")
    private Long id;

    @Schema(description = "Уникальный идентификатор связанной заявки", example = "1301")
    private Long purchaseOrderId;

    @Schema(description = "Уникальный идентификатор работника склада", example = "101")
    private Long warehouseWorkerId;

    @Schema(description = "Номер накладной", example = "INV-2023-12345")
    private String invoiceNumber;

    @Schema(description = "Дата и время прихода", example = "2023-10-20T10:00:00Z")
    private ZonedDateTime receiptedAt;
}
