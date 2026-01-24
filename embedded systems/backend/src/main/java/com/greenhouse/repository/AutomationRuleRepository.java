package com.greenhouse.repository;

import com.greenhouse.entity.AutomationRuleEntity;
import com.greenhouse.entity.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutomationRuleRepository extends JpaRepository<AutomationRuleEntity, Long> {

    List<AutomationRuleEntity> findByEnabled(Boolean enabled);

    List<AutomationRuleEntity> findBySourceModule(ModuleEntity sourceModule);

    List<AutomationRuleEntity> findByTargetModule(ModuleEntity targetModule);

    void deleteBySourceModule(ModuleEntity module);

    void deleteByTargetModule(ModuleEntity module);
}
