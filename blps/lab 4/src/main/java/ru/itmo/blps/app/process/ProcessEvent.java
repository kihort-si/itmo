package ru.itmo.blps.app.process;

import java.time.OffsetDateTime;

public record ProcessEvent(
        ProcessEventType type,
        Long orderId,
        Long customerId,
        String fulfillmentMethod,
        String paymentMethod,
        String status,
        String destination,
        OffsetDateTime createdAt
) {
}
