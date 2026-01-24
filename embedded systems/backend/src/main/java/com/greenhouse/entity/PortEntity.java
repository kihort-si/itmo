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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ports", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"module_id", "port_id"})
})
@Getter
@Setter
public class PortEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id")
    private ModuleEntity module;

    @Column(name = "port_id", nullable = false)
    private Integer portId;

    @Column(name = "port_type", nullable = false)
    private Integer type;

    public PortEntity() {
    }

    public PortEntity(ModuleEntity module, Integer portId, Integer type) {
        this.module = module;
        this.portId = portId;
        this.type = type;
    }

}
