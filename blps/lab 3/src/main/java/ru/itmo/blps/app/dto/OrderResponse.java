package ru.itmo.blps.app.dto;

import java.time.LocalDateTime;
import java.util.List;
import ru.itmo.blps.app.models.enums.FulfillmentMethod;
import ru.itmo.blps.app.models.enums.OrderStatus;
import ru.itmo.blps.app.models.enums.PaymentMethod;

public record OrderResponse(
        Long id,
        Long customerId,
        OrderStatus status,
        FulfillmentMethod fulfillmentMethod,
        PaymentMethod paymentMethod,
        boolean paid,
        String promoCode,
        Long totalAmount,
        Long discountAmount,
        Long finalAmount,
        String deliveryAddress,
        Long shopId,
        String deliveryEta,
        String courierName,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
