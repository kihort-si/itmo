package ru.itmo.blps.app.camunda.workers;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.itmo.blps.app.services.OrderService;

@Component
public class CourierPickedUpTaskHandler implements ExternalTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(CourierPickedUpTaskHandler.class);

    private final OrderService orderService;

    public CourierPickedUpTaskHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public String topic() {
        return "courier-picked-up";
    }

    @Override
    public Map<String, Object> handle(Map<String, Object> vars) throws Exception {
        Long orderId   = toLong(vars.get("orderId"));
        Long courierId = toLong(vars.get("courierId"));
        if (orderId == null)   throw new IllegalArgumentException("orderId is required");
        if (courierId == null) throw new IllegalArgumentException("courierId is required");

        log.info("courier-picked-up-worker: orderId={} courierId={}", orderId, courierId);
        orderService.markCourierPickedUpFromStore(orderId, courierId);
        return Map.of();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        try { return Long.parseLong(value.toString()); } catch (NumberFormatException e) { return null; }
    }
}
