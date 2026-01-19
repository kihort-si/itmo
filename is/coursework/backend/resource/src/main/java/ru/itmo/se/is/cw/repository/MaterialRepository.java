package ru.itmo.se.is.cw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.MaterialEntity;

import java.math.BigDecimal;

@Repository
public interface MaterialRepository extends JpaRepository<MaterialEntity, Long>, JpaSpecificationExecutor<MaterialEntity> {
    @Modifying
    @Query(value = "call p_update_material_balance_and_set_current(:materialId, :newBalance, :changerId)", nativeQuery = true)
    void updateBalanceAndSetCurrent(@Param("materialId") Long materialId,
                                    @Param("newBalance") BigDecimal newBalance,
                                    @Param("changerId") Long changerAccountId);
}
