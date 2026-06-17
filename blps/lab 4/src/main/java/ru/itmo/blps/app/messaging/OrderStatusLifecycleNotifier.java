package ru.itmo.blps.app.messaging;

import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.itmo.blps.app.models.Order;

@Component
public class OrderStatusLifecycleNotifier {

    private static final DateTimeFormatter ISO_LOCAL = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final OrderStatusStompPublisher orderStatusStompPublisher;

    public OrderStatusLifecycleNotifier(OrderStatusStompPublisher orderStatusStompPublisher) {
        this.orderStatusStompPublisher = orderStatusStompPublisher;
    }

    public void publishOrderStatusChangedAfterCommit(Order order) {
        String eventId = UUID.randomUUID().toString();
        OrderStatusEventMessage message = new OrderStatusEventMessage(
                eventId,
                order.getId(),
                order.getStatus().name(),
                order.isPaid(),
                order.getUpdatedAt() == null ? ISO_LOCAL.format(java.time.LocalDateTime.now()) : ISO_LOCAL.format(order.getUpdatedAt())
        );
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    orderStatusStompPublisher.publishAfterCommit(message);
                }
            });
        } else {
            orderStatusStompPublisher.publishAfterCommit(message);
        }
    }
}
