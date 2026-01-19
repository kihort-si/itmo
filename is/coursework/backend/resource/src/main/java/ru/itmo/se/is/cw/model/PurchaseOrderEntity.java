package ru.itmo.se.is.cw.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "purchase_order")
public class PurchaseOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "current_status_id")
    private PurchaseOrderStatusEntity currentStatus;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "supply_manager_id", nullable = false)
    private EmployeeEntity supplyManager;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderMaterialEntity> materials = new ArrayList<>();

    public void addMaterial(PurchaseOrderMaterialEntity m) {
        materials.add(m);
        m.setPurchaseOrder(this);
    }

    public void clearMaterials() {
        for (PurchaseOrderMaterialEntity m : materials) {
            m.setPurchaseOrder(null);
        }
        materials.clear();
    }
}