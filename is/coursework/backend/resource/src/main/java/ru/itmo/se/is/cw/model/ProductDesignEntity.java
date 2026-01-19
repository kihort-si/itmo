package ru.itmo.se.is.cw.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "product_design")
public class ProductDesignEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "constructor_id")
    private EmployeeEntity constructor;

    @Column(name = "product_name", nullable = false, length = 50)
    private String productName;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "productDesign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductDesignFileEntity> files = new ArrayList<>();

    @OneToMany(mappedBy = "productDesign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequiredMaterialEntity> requiredMaterials = new ArrayList<>();

    public void addFile(ProductDesignFileEntity file) {
        files.add(file);
        file.setProductDesign(this);
    }

    public void addMaterial(RequiredMaterialEntity material) {
        requiredMaterials.add(material);
        material.setProductDesign(this);
    }
}