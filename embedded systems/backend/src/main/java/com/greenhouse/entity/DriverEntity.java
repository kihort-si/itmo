package com.greenhouse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(name = "drivers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"module_id", "driver_id"})
})
@Getter
@Setter
public class DriverEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id")
    private ModuleEntity module;

    @Column(name = "driver_id", nullable = false)
    private Integer driverId;

    @Column(name = "driver_name", nullable = false)
    private String name;

    @Column(name = "driver_type", nullable = false)
    private Integer type;

    public DriverEntity() {
    }

    public DriverEntity(ModuleEntity module, Integer driverId, String name, Integer type) {
        this.module = module;
        this.driverId = driverId;
        this.name = name;
        this.type = type;
    }
}
