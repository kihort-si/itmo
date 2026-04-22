package ru.itmo.blps.app.dto;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        Long unitPrice,
        Long lineAmount
) {
}
