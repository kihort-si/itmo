package ru.itmo.blps.app.camunda.workers;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.itmo.blps.app.services.OrderService;

@Component
public class ReadyForPickupTaskHandler implements ExternalTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(ReadyForPickupTaskHandler.class);

    private final OrderService orderService;

    public ReadyForPickupTaskHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public String topic() {
        return "ready-for-pickup";
    }

    @Override
    public Map<String, Object> handle(Map<String, Object> vars) throws Exception {
        Long orderId     = toLong(vars.get("orderId"));
        Long assistantId = toLong(vars.get("assistantId"));
        if (orderId == null)     throw new IllegalArgumentException("orderId is required");
        if (assistantId == null) throw new IllegalArgumentException("assistantId is required");

        log.info("ready-for-pickup-worker: orderId={} assistantId={}", orderId, assistantId);
        orderService.markReadyForPickupByAssistant(orderId, assistantId);
        return Map.of();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        try { return Long.parseLong(value.toString()); } catch (NumberFormatException e) { return null; }
    }
}
