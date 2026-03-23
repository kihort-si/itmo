package ru.itmo.blps.app.dto;

import jakarta.validation.constraints.Size;
import ru.itmo.blps.app.models.enums.FulfillmentMethod;
import ru.itmo.blps.app.models.enums.PaymentMethod;

public record UpdateOrderRequest(
        @Size(max = 32) String promoCode,
        FulfillmentMethod fulfillmentMethod,
        PaymentMethod paymentMethod,
        Boolean payOnline,
        Long shopId,
        @Size(max = 255) String deliveryAddress
) {
}
