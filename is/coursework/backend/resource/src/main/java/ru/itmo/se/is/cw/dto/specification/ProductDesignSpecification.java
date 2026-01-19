package ru.itmo.se.is.cw.dto.specification;

import jakarta.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.itmo.se.is.cw.dto.filter.ProductDesignFilter;
import ru.itmo.se.is.cw.model.ProductDesignEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@NoArgsConstructor
public final class ProductDesignSpecification {

    public static Specification<ProductDesignEntity> byFilter(ProductDesignFilter f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            if (f == null) {
                return cb.conjunction();
            }

            if (f.getConstructorId() != null) {
                p.add(cb.equal(root.get("constructor").get("id"), f.getConstructorId()));
            }

            if (f.getProductName() != null && !f.getProductName().isBlank()) {
                String pattern = "%" + f.getProductName().trim().toLowerCase(Locale.ROOT) + "%";
                p.add(cb.like(cb.lower(root.get("productName")), pattern));
            }

            if (f.getCreatedFrom() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("createdAt"), f.getCreatedFrom()));
            }
            if (f.getCreatedTo() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("createdAt"), f.getCreatedTo()));
            }

            if (f.getUpdatedFrom() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), f.getUpdatedFrom()));
            }
            if (f.getUpdatedTo() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("updatedAt"), f.getUpdatedTo()));
            }

            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
