package com.greenhouse.repository;

import com.greenhouse.entity.ModuleEntity;
import com.greenhouse.entity.PortEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortRepository extends JpaRepository<PortEntity, Long> {
    List<PortEntity> findByModule(ModuleEntity module);

    Optional<PortEntity> findByModuleAndPortId(ModuleEntity module, Integer portId);

    void deleteByModule(ModuleEntity module);
}
