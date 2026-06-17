package ru.itmo.blps.app.camunda.workers;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.itmo.blps.app.models.enums.FulfillmentMethod;
import ru.itmo.blps.app.services.OrderService;

@Component
public class CompleteOrderTaskHandler implements ExternalTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(CompleteOrderTaskHandler.class);

    private final OrderService orderService;

    public CompleteOrderTaskHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public String topic() {
        return "complete-order";
    }

    @Override
    public Map<String, Object> handle(Map<String, Object> vars) throws Exception {
        Long orderId            = toLong(vars.get("orderId"));
        String fulfillmentStr   = (String) vars.get("fulfillmentMethod");
        if (orderId == null) throw new IllegalArgumentException("orderId is required");

        FulfillmentMethod fulfillment = fulfillmentStr != null
                ? FulfillmentMethod.valueOf(fulfillmentStr)
                : null;

        if (fulfillment == FulfillmentMethod.PICKUP) {
            Long assistantId = toLong(vars.get("assistantId"));
            if (assistantId == null) throw new IllegalArgumentException("assistantId is required for PICKUP completion");
            log.info("complete-order-worker PICKUP: orderId={} assistantId={}", orderId, assistantId);
            orderService.assistantCompletePickup(orderId, assistantId);
        } else {
            Long courierId = toLong(vars.get("courierId"));
            if (courierId == null) throw new IllegalArgumentException("courierId is required for DELIVERY completion");
            log.info("complete-order-worker DELIVERY: orderId={} courierId={}", orderId, courierId);
            orderService.courierDeliveredOrder(orderId, courierId);
        }
        return Map.of();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        try { return Long.parseLong(value.toString()); } catch (NumberFormatException e) { return null; }
    }
}
