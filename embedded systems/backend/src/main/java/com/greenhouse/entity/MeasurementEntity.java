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
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "measurements")
@Getter
@Setter
public class MeasurementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id")
    private ModuleEntity module;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "port_fk")
    private PortEntity port;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_fk")
    private DriverEntity driver;

    @Column(name = "value", nullable = false)
    private Double value;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public MeasurementEntity() {
    }

    public MeasurementEntity(ModuleEntity module, PortEntity port, DriverEntity driver, Double value) {
        this.module = module;
        this.port = port;
        this.driver = driver;
        this.value = value;
    }
}
