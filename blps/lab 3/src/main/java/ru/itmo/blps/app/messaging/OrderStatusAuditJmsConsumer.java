package ru.itmo.blps.app.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ru.itmo.blps.audit.OrderStatusAuditEntry;
import ru.itmo.blps.audit.OrderStatusAuditRepository;

@Component
@ConditionalOnProperty(prefix = "app.messaging.order-status", name = "enabled", havingValue = "true")
public class OrderStatusAuditJmsConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusAuditJmsConsumer.class);

    private final OrderStatusAuditRepository orderStatusAuditRepository;
    private final TransactionTemplate auditTransactionTemplate;

    public OrderStatusAuditJmsConsumer(
            OrderStatusAuditRepository orderStatusAuditRepository,
            @Qualifier("auditTransactionManager") PlatformTransactionManager auditTransactionManager) {
        this.orderStatusAuditRepository = orderStatusAuditRepository;
        this.auditTransactionTemplate = new TransactionTemplate(auditTransactionManager);
    }

    @JmsListener(
            destination = "${app.messaging.order-status.topic-name:OrderStatusEvents}",
            containerFactory = "orderStatusTopicListenerContainerFactory"
    )
    public void onMessage(OrderStatusEventMessage message) {
        try {
            auditTransactionTemplate.executeWithoutResult(s -> {
                if (orderStatusAuditRepository.existsById(message.eventId())) {
                    return;
                }
                orderStatusAuditRepository.save(new OrderStatusAuditEntry(
                        message.eventId(),
                        message.orderId(),
                        message.status(),
                        message.paid(),
                        message.occurredAt()
                ));
            });
        } catch (Exception e) {
            log.error("Audit consumer failed, will rollback JMS session for redelivery", e);
            throw new IllegalStateException(e);
        }
    }
}
