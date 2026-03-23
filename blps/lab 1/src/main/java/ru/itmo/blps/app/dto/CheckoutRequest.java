package ru.itmo.blps.app.dto;

import jakarta.validation.constraints.NotNull;
import ru.itmo.blps.app.models.enums.FulfillmentMethod;
import ru.itmo.blps.app.models.enums.PaymentMethod;

public record CheckoutRequest(
        @NotNull(message = "Тип выдачи обязателен")
        FulfillmentMethod fulfillmentMethod,

        @NotNull(message = "Способ оплаты обязателен")
        PaymentMethod paymentMethod,

        String promoCode,

        Long shopId,

        String deliveryAddress
) {
}

