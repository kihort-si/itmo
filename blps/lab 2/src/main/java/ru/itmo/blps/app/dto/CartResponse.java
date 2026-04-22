package ru.itmo.blps.app.dto;

import java.util.List;

public record CartResponse(
        Long id,
        Long customerId,
        List<CartItemResponse> items,
        Long totalAmount
) {
}
