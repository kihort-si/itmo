package ru.itmo.se.is.cw.dto.specification;


import jakarta.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.itmo.se.is.cw.dto.filter.ProductCatalogFilter;
import ru.itmo.se.is.cw.model.ProductCatalogEntity;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public final class ProductCatalogSpecification {

    public static Specification<ProductCatalogEntity> byFilter(ProductCatalogFilter f) {
        return (root, query, cb) -> {
            if (f == null) return cb.conjunction();

            List<Predicate> p = new ArrayList<>();

            if (f.getProductDesignId() != null) {
                p.add(cb.equal(root.get("productDesign").get("id"), f.getProductDesignId()));
            }

            if (hasText(f.getName())) {
                p.add(cb.like(cb.lower(root.get("name")), "%" + f.getName().toLowerCase() + "%"));
            }

            if (hasText(f.getDescription())) {
                p.add(cb.like(cb.lower(root.get("description")), "%" + f.getDescription().toLowerCase() + "%"));
            }

            if (hasText(f.getCategory())) {
                p.add(cb.like(cb.lower(root.get("category")), "%" + f.getCategory().toLowerCase() + "%"));
            }

            if (f.getPriceMin() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("price"), f.getPriceMin()));
            }

            if (f.getPriceMax() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("price"), f.getPriceMax()));
            }

            if (f.getMinimalAmountMin() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("minimalAmount"), f.getMinimalAmountMin()));
            }

            if (f.getMinimalAmountMax() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("minimalAmount"), f.getMinimalAmountMax()));
            }

            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
