package ru.itmo.blps.app.schedule;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.itmo.blps.app.messaging.OrderStatusLifecycleNotifier;
import ru.itmo.blps.app.models.Order;
import ru.itmo.blps.app.models.OrderItem;
import ru.itmo.blps.app.models.Product;
import ru.itmo.blps.app.models.enums.OrderStatus;
import ru.itmo.blps.app.repositories.OrderRepository;
import ru.itmo.blps.app.repositories.ProductRepository;

@Component
public class StaleOrderReaper {

    private static final Logger log = LoggerFactory.getLogger(StaleOrderReaper.class);

    private static final EnumSet<OrderStatus> TERMINAL = EnumSet.of(
            OrderStatus.COMPLETED,
            OrderStatus.CANCELLED,
            OrderStatus.REFUNDED);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final TransactionTemplate transactionTemplate;
    private final OrderStatusLifecycleNotifier orderStatusLifecycleNotifier;
    private final int staleStatusHours;

    public StaleOrderReaper(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            TransactionTemplate transactionTemplate,
            OrderStatusLifecycleNotifier orderStatusLifecycleNotifier,
            @Value("${app.orders.stale-status-hours:48}") int staleStatusHours) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.transactionTemplate = transactionTemplate;
        this.orderStatusLifecycleNotifier = orderStatusLifecycleNotifier;
        this.staleStatusHours = staleStatusHours;
    }

    /**
     * Fallback scheduler for orders NOT managed by the Camunda process engine
     * (e.g. created via legacy REST API directly, or during Camunda downtime).
     * Camunda-managed orders are cancelled by the "cancel-order" External Task
     * worker triggered by PT48H boundary timer events — no polling needed there.
     */
    @Scheduled(fixedDelayString = "${app.orders.stale-check-ms:3600000}")
    public void reapStaleOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(staleStatusHours);
        List<Order> candidates = orderRepository.findByStatusSinceBeforeAndStatusNotIn(cutoff, TERMINAL);
        for (Order snapshot : candidates) {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    Order order = orderRepository.findWithItemsById(snapshot.getId()).orElse(null);
                    if (order == null) {
                        return;
                    }
                    if (TERMINAL.contains(order.getStatus())) {
                        return;
                    }
                    if (order.getStatusSince() == null || !order.getStatusSince().isBefore(cutoff)) {
                        return;
                    }
                    restoreStock(order);
                    if (order.isPaid()) {
                        order.setStatus(OrderStatus.REFUNDED);
                        order.setPaid(false);
                    } else {
                        order.setStatus(OrderStatus.CANCELLED);
                    }
                    Order saved = orderRepository.save(order);
                    orderStatusLifecycleNotifier.publishOrderStatusChangedAfterCommit(saved);
                    log.info("Reaped stale order id={} newStatus={}", saved.getId(), saved.getStatus());
                });
            } catch (Exception e) {
                log.error("Failed to reap stale order id={}", snapshot.getId(), e);
            }
        }
    }

    private void restoreStock(Order order) {
        Map<Long, Product> toSave = new HashMap<>();
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() == null || item.getProduct().getId() == null) {
                continue;
            }
            Long id = item.getProduct().getId();
            Product managed = productRepository.findById(id).orElse(null);
            if (managed != null) {
                managed.setStock(managed.getStock() + item.getQuantity());
                toSave.put(id, managed);
            }
        }
        if (!toSave.isEmpty()) {
            productRepository.saveAll(toSave.values());
        }
    }
}
