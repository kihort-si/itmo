package com.greenhouse.repository;

import com.greenhouse.entity.MeasurementEntity;
import com.greenhouse.entity.ModuleEntity;
import com.greenhouse.entity.PortEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeasurementRepository extends JpaRepository<MeasurementEntity, Long> {
    List<MeasurementEntity> findByModule(ModuleEntity module);

    Optional<MeasurementEntity> findFirstByModuleAndPortOrderByCreatedAtDesc(ModuleEntity module, PortEntity port);
}
