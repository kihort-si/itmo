package ru.itmo.blps.app.repositories;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.itmo.blps.app.models.Order;
import ru.itmo.blps.app.models.enums.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Page<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    Page<Order> findByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.statusSince < :cutoff AND o.status NOT IN :excluded")
    List<Order> findByStatusSinceBeforeAndStatusNotIn(
            @Param("cutoff") LocalDateTime cutoff,
            @Param("excluded") Collection<OrderStatus> excluded);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id = :id")
    Optional<Order> findWithItemsById(@Param("id") Long id);
}
