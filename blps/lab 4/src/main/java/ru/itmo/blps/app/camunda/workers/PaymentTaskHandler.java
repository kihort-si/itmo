package ru.itmo.blps.app.camunda.workers;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.itmo.blps.app.services.OrderService;

@Component
public class PaymentTaskHandler implements ExternalTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentTaskHandler.class);

    private final OrderService orderService;

    public PaymentTaskHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public String topic() {
        return "payment";
    }

    @Override
    public Map<String, Object> handle(Map<String, Object> vars) throws Exception {
        Long orderId = toLong(vars.get("orderId"));
        if (orderId == null) throw new IllegalArgumentException("orderId is required");

        log.info("payment-worker: orderId={}", orderId);

        orderService.confirmOnlinePaymentIfNeeded(orderId);

        return Map.of("paid", true);
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        try { return Long.parseLong(value.toString()); } catch (NumberFormatException e) { return null; }
    }
}
