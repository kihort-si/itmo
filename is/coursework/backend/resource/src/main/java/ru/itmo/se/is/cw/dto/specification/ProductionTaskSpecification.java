package ru.itmo.se.is.cw.dto.specification;

import jakarta.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.itmo.se.is.cw.dto.filter.ProductionTaskFilter;
import ru.itmo.se.is.cw.model.ProductionTaskEntity;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public final class ProductionTaskSpecification {

    public static Specification<ProductionTaskEntity> byFilter(ProductionTaskFilter f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            if (f == null) {
                return cb.conjunction();
            }

            if (f.getClientOrderId() != null) {
                p.add(cb.equal(root.get("clientOrder").get("id"), f.getClientOrderId()));
            }

            if (f.getCurrentStatusId() != null) {
                p.add(cb.equal(root.get("currentStatus").get("id"), f.getCurrentStatusId()));
            }

            if (f.getCncOperatorId() != null) {
                p.add(cb.equal(root.get("cncOperator").get("id"), f.getCncOperatorId()));
            }

            if (f.getStartedFrom() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("startedAt"), f.getStartedFrom()));
            }
            if (f.getStartedTo() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("startedAt"), f.getStartedTo()));
            }

            if (f.getFinishedFrom() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("finishedAt"), f.getFinishedFrom()));
            }
            if (f.getFinishedTo() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("finishedAt"), f.getFinishedTo()));
            }

            if (f.getCreatedFrom() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("createdAt"), f.getCreatedFrom()));
            }
            if (f.getCreatedTo() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("createdAt"), f.getCreatedTo()));
            }

            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
