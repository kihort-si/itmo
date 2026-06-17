package ru.itmo.blps.app.camunda.workers;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.itmo.blps.app.dto.OrderResponse;
import ru.itmo.blps.app.services.OrderService;

@Component
public class AssignCourierTaskHandler implements ExternalTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(AssignCourierTaskHandler.class);

    private final OrderService orderService;

    public AssignCourierTaskHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public String topic() {
        return "assign-courier";
    }

    @Override
    public Map<String, Object> handle(Map<String, Object> vars) throws Exception {
        Long orderId     = toLong(vars.get("orderId"));
        Long assistantId = toLong(vars.get("assistantId"));
        if (orderId == null)     throw new IllegalArgumentException("orderId is required");
        if (assistantId == null) throw new IllegalArgumentException("assistantId is required");

        log.info("assign-courier-worker: orderId={} assistantId={}", orderId, assistantId);
        OrderResponse order = orderService.markReadyForDeliveryByAssistant(orderId, assistantId);

        Map<String, Object> out = new HashMap<>();
        out.put("courierName", order.courierName() != null ? order.courierName() : "");
        return out;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        try { return Long.parseLong(value.toString()); } catch (NumberFormatException e) { return null; }
    }
}
