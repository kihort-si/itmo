package ru.itmo.blps.app.process;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(OrderProcessEventPublisher.class)
public class NoOpOrderProcessEventPublisher implements OrderProcessEventPublisher {
    @Override
    public void publishCheckout(ProcessEvent event) {
    }

    @Override
    public void publishDeliveryAssigned(ProcessEvent event) {
    }
}
