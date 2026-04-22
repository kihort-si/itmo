package ru.itmo.blps.app.process;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!wildfly")
public class NoOpOrderProcessEventPublisher implements OrderProcessEventPublisher {
    @Override
    public void publishCheckout(ProcessEvent event) {
    }

    @Override
    public void publishDeliveryAssigned(ProcessEvent event) {
    }
}
