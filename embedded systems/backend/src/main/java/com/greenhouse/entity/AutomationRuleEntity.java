package com.greenhouse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;


@Entity
@Table(name = "automation_rules")
@Getter
@Setter
public class AutomationRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_module_id", nullable = false)
    private ModuleEntity sourceModule;

    @Column(name = "source_port_id", nullable = false)
    private Integer sourcePortId;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    private ConditionType conditionType;

    @Column(name = "threshold", nullable = false)
    private Double threshold;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_module_id", nullable = false)
    private ModuleEntity targetModule;

    @Column(name = "target_port_id", nullable = false)
    private Integer targetPortId;

    @Column(name = "action_level", nullable = false)
    private Integer actionLevel;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public AutomationRuleEntity() {
    }

    public AutomationRuleEntity(String name,
                                 ModuleEntity sourceModule, Integer sourcePortId,
                                 ConditionType conditionType, Double threshold,
                                 ModuleEntity targetModule, Integer targetPortId,
                                 Integer actionLevel, Boolean enabled) {
        this.name = name;
        this.sourceModule = sourceModule;
        this.sourcePortId = sourcePortId;
        this.conditionType = conditionType;
        this.threshold = threshold;
        this.targetModule = targetModule;
        this.targetPortId = targetPortId;
        this.actionLevel = actionLevel;
        this.enabled = enabled;
    }
}
