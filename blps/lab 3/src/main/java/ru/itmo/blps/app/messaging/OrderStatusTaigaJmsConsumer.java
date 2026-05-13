package ru.itmo.blps.app.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.itmo.blps.app.integration.taiga.TaigaJcaInvoker;
import ru.itmo.blps.app.models.ProcessedTaigaStatusEvent;
import ru.itmo.blps.app.repositories.ProcessedTaigaStatusEventRepository;

@Component
@ConditionalOnProperty(prefix = "app.messaging.order-status", name = "enabled", havingValue = "true")
public class OrderStatusTaigaJmsConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusTaigaJmsConsumer.class);

    private static final int MAX_ATTEMPTS = 3;

    private final TaigaJcaInvoker taigaJcaInvoker;
    private final ProcessedTaigaStatusEventRepository processedTaigaStatusEventRepository;
    private final TransactionTemplate transactionTemplate;

    public OrderStatusTaigaJmsConsumer(
            TaigaJcaInvoker taigaJcaInvoker,
            ProcessedTaigaStatusEventRepository processedTaigaStatusEventRepository,
            TransactionTemplate transactionTemplate) {
        this.taigaJcaInvoker = taigaJcaInvoker;
        this.processedTaigaStatusEventRepository = processedTaigaStatusEventRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @JmsListener(
            destination = "${app.messaging.order-status.topic-name:OrderStatusEvents}",
            containerFactory = "orderStatusTopicListenerContainerFactory"
    )
    public void onMessage(OrderStatusEventMessage message) {
        try {
            if (processedTaigaStatusEventRepository.existsById(message.eventId())) {
                return;
            }
            Exception last = null;
            for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                try {
                    taigaJcaInvoker.syncOrderStatus(message.orderId(), message.status(), message.eventId());
                    transactionTemplate.executeWithoutResult(s ->
                            processedTaigaStatusEventRepository.save(new ProcessedTaigaStatusEvent(message.eventId())));
                    return;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Taiga consumer interrupted during sync", ie);
                    return;
                } catch (Exception e) {
                    last = e;
                    log.warn("Taiga sync attempt {}/{} failed for eventId={}", attempt, MAX_ATTEMPTS, message.eventId(), e);
                    try {
                        Thread.sleep(200L * attempt);
                    } catch (InterruptedException sleepInterrupt) {
                        Thread.currentThread().interrupt();
                        log.warn("Taiga consumer interrupted during backoff", sleepInterrupt);
                        return;
                    }
                }
            }
            log.error("Taiga sync abandoned after {} attempts for orderId={} eventId={}", MAX_ATTEMPTS, message.orderId(), message.eventId(), last);
        } catch (Exception e) {
            log.error("Taiga consumer failed unexpectedly", e);
            throw new IllegalStateException(e);
        }
    }
}
