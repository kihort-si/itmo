package ru.itmo.blps.app.process;

import jakarta.jms.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("wildfly")
public class JmsOrderProcessEventPublisher implements OrderProcessEventPublisher {
    private final JmsTemplate jmsTemplate;
    private final Queue checkoutProcessQueue;
    private final Queue deliveryProcessQueue;

    public JmsOrderProcessEventPublisher(JmsTemplate jmsTemplate,
                                         @Qualifier("checkoutProcessQueue") Queue checkoutProcessQueue,
                                         @Qualifier("deliveryProcessQueue") Queue deliveryProcessQueue) {
        this.jmsTemplate = jmsTemplate;
        this.checkoutProcessQueue = checkoutProcessQueue;
        this.deliveryProcessQueue = deliveryProcessQueue;
    }

    @Override
    public void publishCheckout(ProcessEvent event) {
        send(checkoutProcessQueue, event);
    }

    @Override
    public void publishDeliveryAssigned(ProcessEvent event) {
        send(deliveryProcessQueue, event);
    }

    private void send(Queue queue, ProcessEvent event) {
        jmsTemplate.convertAndSend(queue, toJson(event));
    }

    private String toJson(ProcessEvent event) {
        return "{"
                + "\"type\":\"" + escape(event.type().name()) + "\","
                + "\"orderId\":" + event.orderId() + ","
                + "\"customerId\":" + event.customerId() + ","
                + "\"fulfillmentMethod\":" + nullable(event.fulfillmentMethod()) + ","
                + "\"paymentMethod\":" + nullable(event.paymentMethod()) + ","
                + "\"status\":\"" + escape(event.status()) + "\","
                + "\"destination\":" + nullable(event.destination()) + ","
                + "\"createdAt\":\"" + escape(event.createdAt().toString()) + "\""
                + "}";
    }

    private String nullable(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + escape(value) + "\"";
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
