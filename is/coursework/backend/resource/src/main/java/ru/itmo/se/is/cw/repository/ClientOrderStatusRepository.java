package ru.itmo.se.is.cw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.ClientOrderStatusEntity;
import ru.itmo.se.is.cw.model.value.ClientOrderStatus;

import java.util.List;

@Repository
public interface ClientOrderStatusRepository extends JpaRepository<ClientOrderStatusEntity, Long> {
    List<ClientOrderStatusEntity> findByClientOrderIdOrderBySetAtAsc(Long clientOrderId);
    
    @Query("SELECT COUNT(s) > 0 FROM ClientOrderStatusEntity s WHERE s.clientOrder.id = :orderId AND s.status = :status")
    boolean existsByClientOrderIdAndStatus(@Param("orderId") Long orderId, @Param("status") ClientOrderStatus status);
    
    @Query("SELECT s.setAt FROM ClientOrderStatusEntity s WHERE s.clientOrder.id = :orderId AND s.status = :status ORDER BY s.setAt ASC")
    java.util.Optional<java.time.ZonedDateTime> findSetAtByClientOrderIdAndStatus(@Param("orderId") Long orderId, @Param("status") ClientOrderStatus status);
}
