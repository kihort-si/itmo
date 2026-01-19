package ru.itmo.se.is.cw.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.PurchaseOrderEntity;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, Long>, JpaSpecificationExecutor<PurchaseOrderEntity> {

    @Modifying
    @Query(value = "call p_update_purchase_order_status_and_set_current(:poId, :status)", nativeQuery = true)
    void updateStatusAndSetCurrent(@Param("poId") Long poId, @Param("status") String status);

}
