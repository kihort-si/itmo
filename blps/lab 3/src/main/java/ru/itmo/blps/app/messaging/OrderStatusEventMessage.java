package ru.itmo.blps.app.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderStatusEventMessage(
        String eventId,
        long orderId,
        String status,
        boolean paid,
        String occurredAt
) {
}
