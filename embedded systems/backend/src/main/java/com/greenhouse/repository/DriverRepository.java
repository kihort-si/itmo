package com.greenhouse.repository;

import com.greenhouse.entity.DriverEntity;
import com.greenhouse.entity.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<DriverEntity, Long> {
    List<DriverEntity> findByModule(ModuleEntity module);

    Optional<DriverEntity> findByModuleAndDriverId(ModuleEntity module, Integer driverId);

    void deleteByModule(ModuleEntity module);
}
