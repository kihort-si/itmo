package com.greenhouse.repository;

import com.greenhouse.entity.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuleRepository extends JpaRepository<ModuleEntity, Long> {
    Optional<ModuleEntity> findByModuleUid(Integer moduleUid);
}
