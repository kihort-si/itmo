package ru.itmo.se.is.cw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.ClientOrderEntity;
import ru.itmo.se.is.cw.model.value.ClientOrderStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientOrderRepository extends JpaRepository<ClientOrderEntity, Long>, JpaSpecificationExecutor<ClientOrderEntity> {

    @Modifying
    @Query(value = "call p_update_client_order_status_and_set_current(:orderId, :status)", nativeQuery = true)
    void updateStatusAndSetCurrent(@Param("orderId") Long orderId, @Param("status") String status);

    boolean existsByCurrentStatusStatusIn(List<ClientOrderStatus> statuses);

    Optional<ClientOrderEntity> findFirstByCurrentStatusStatusOrderByCreatedAtAsc(ClientOrderStatus status);

}
