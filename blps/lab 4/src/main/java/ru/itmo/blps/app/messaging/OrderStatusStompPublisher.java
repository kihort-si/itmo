package ru.itmo.blps.app.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusStompPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusStompPublisher.class);

    private final OrderStatusMessagingProperties properties;
    private final ObjectMapper objectMapper;

    public OrderStatusStompPublisher(OrderStatusMessagingProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public void publishAfterCommit(OrderStatusEventMessage message) {
        if (!properties.isEnabled()) {
            return;
        }
        try (SimpleStompClient stomp = new SimpleStompClient(
                properties.getStompHost(),
                properties.getStompPort(),
                properties.getStompLogin(),
                properties.getStompPasscode())) {
            String json = objectMapper.writeValueAsString(message);
            stomp.sendJson(properties.stompDestination(), json);
        } catch (IOException e) {
            log.error("STOMP publish failed for orderStatus eventId={}", message.eventId(), e);
        }
    }
}
