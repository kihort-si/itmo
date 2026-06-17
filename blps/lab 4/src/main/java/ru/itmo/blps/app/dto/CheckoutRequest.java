package ru.itmo.blps.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.itmo.blps.app.models.enums.FulfillmentMethod;
import ru.itmo.blps.app.models.enums.PaymentMethod;

public record CheckoutRequest(
        @NotNull(message = "Тип выдачи обязателен")
        FulfillmentMethod fulfillmentMethod,

        @NotNull(message = "Способ оплаты обязателен")
        PaymentMethod paymentMethod,

        @Size(max = 32, message = "Промокод слишком длинный")
        String promoCode,

        Long shopId,

        @Size(max = 255, message = "Адрес доставки слишком длинный")
        String deliveryAddress
) {
}
