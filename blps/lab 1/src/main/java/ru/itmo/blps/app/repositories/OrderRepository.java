package ru.itmo.blps.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.itmo.blps.app.models.Order;
import ru.itmo.blps.app.models.enums.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Page<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    Page<Order> findByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, OrderStatus status, Pageable pageable);
}
