package ru.itmo.blps.app.camunda.workers;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.itmo.blps.app.dto.CheckoutRequest;
import ru.itmo.blps.app.dto.OrderResponse;
import ru.itmo.blps.app.models.enums.FulfillmentMethod;
import ru.itmo.blps.app.models.enums.PaymentMethod;
import ru.itmo.blps.app.services.OrderService;

@Component
public class CheckoutTaskHandler implements ExternalTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(CheckoutTaskHandler.class);

    private final OrderService orderService;

    public CheckoutTaskHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public String topic() {
        return "checkout";
    }

    @Override
    public Map<String, Object> handle(Map<String, Object> vars) throws Exception {
        Long customerId         = toLong(vars.get("customerId"));
        String promoCode        = (String) vars.get("promoCode");
        String fulfillmentStr   = (String) vars.get("fulfillmentMethod");
        String paymentStr       = (String) vars.get("paymentMethod");
        Long shopId             = toLong(vars.get("shopId"));
        String deliveryAddress  = (String) vars.get("deliveryAddress");

        if (customerId == null) throw new IllegalArgumentException("customerId is required");
        if (fulfillmentStr == null) throw new IllegalArgumentException("fulfillmentMethod is required");
        if (paymentStr == null)     throw new IllegalArgumentException("paymentMethod is required");

        FulfillmentMethod fulfillment = FulfillmentMethod.valueOf(fulfillmentStr);
        PaymentMethod payment         = PaymentMethod.valueOf(paymentStr);

        CheckoutRequest request = new CheckoutRequest(fulfillment, payment, promoCode, shopId, deliveryAddress);
        log.info("checkout-worker: customerId={} fulfillment={} payment={}", customerId, fulfillment, payment);

        OrderResponse order = orderService.checkout(customerId, request);

        Map<String, Object> out = new HashMap<>();
        out.put("orderId", order.id());
        out.put("paid", order.paid());
        if (order.deliveryEta() != null) {
            out.put("deliveryEta", order.deliveryEta());
        }
        return out;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        try { return Long.parseLong(value.toString()); } catch (NumberFormatException e) { return null; }
    }
}
