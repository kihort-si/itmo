package ru.itmo.se.is.cw.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.MaterialConsumptionEntity;

import java.util.List;

@Repository
public interface MaterialConsumptionRepository extends JpaRepository<MaterialConsumptionEntity, Long> {
    List<MaterialConsumptionEntity> findByClientOrderIdOrderByCreatedAtDesc(Long orderId);

    List<MaterialConsumptionEntity> findByMaterialIdOrderByCreatedAtDesc(Long materialId);
}
