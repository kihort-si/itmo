package ru.itmo.se.is.cw.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "product_catalog")
public class ProductCatalogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false, length = Integer.MAX_VALUE)
    private String description;

    @OneToOne(optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "product_design_id", nullable = false)
    private ProductDesignEntity productDesign;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "minimal_amount", nullable = false)
    private Short minimalAmount;

    @Column(name = "category", nullable = false)
    private String category;

    @OneToMany(mappedBy = "productCatalog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductPhotoEntity> photos;
}