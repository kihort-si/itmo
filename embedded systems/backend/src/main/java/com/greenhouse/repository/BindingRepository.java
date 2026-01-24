package com.greenhouse.repository;

import com.greenhouse.entity.BindingEntity;
import com.greenhouse.entity.ModuleEntity;
import com.greenhouse.entity.PortEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BindingRepository extends JpaRepository<BindingEntity, Long> {
    Optional<BindingEntity> findByPort(PortEntity port);

    Optional<BindingEntity> findByModuleAndPort_PortId(ModuleEntity module, Integer portId);

    List<BindingEntity> findByModule(ModuleEntity module);

    void deleteByModule(ModuleEntity module);
}
