package ru.itmo.se.is.cw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.MaterialBalanceEntity;

import java.util.List;

@Repository
public interface MaterialBalanceRepository extends JpaRepository<MaterialBalanceEntity, Long> {
    List<MaterialBalanceEntity> findByMaterialIdOrderByChangedAtDesc(Long materialId);
}
