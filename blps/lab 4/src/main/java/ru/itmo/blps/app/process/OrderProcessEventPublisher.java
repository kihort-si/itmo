package ru.itmo.blps.app.process;

public interface OrderProcessEventPublisher {
    void publishCheckout(ProcessEvent event);
    void publishDeliveryAssigned(ProcessEvent event);
}
