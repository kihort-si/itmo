package ru.itmo.blps.app.dto;

public record OrderItemResponse(
        Long id,
        String productName,
        Long unitPrice,
        Integer quantity,
        Long lineAmount
) {
}
