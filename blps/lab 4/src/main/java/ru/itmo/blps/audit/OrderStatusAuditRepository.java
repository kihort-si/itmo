package ru.itmo.blps.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusAuditRepository extends JpaRepository<OrderStatusAuditEntry, String> {
}
