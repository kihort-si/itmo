package ru.itmo.se.is.cw.dto.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.itmo.se.is.cw.dto.filter.PurchaseOrderFilter;
import ru.itmo.se.is.cw.model.PurchaseOrderEntity;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public final class PurchaseOrderSpecification {

    public static Specification<PurchaseOrderEntity> byFilter(PurchaseOrderFilter f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            if (f == null) {
                return cb.conjunction();
            }

            if (f.getSupplyManagerId() != null) {
                p.add(cb.equal(root.get("supplyManager").get("id"), f.getSupplyManagerId()));
            }

            if (f.getCreatedFrom() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("createdAt"), f.getCreatedFrom()));
            }
            if (f.getCreatedTo() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("createdAt"), f.getCreatedTo()));
            }

            boolean needStatusJoin =
                    f.getCurrentStatusId() != null ||
                            f.getStatus() != null ||
                            f.getWithoutCurrentStatus() != null;

            var statusJoin = needStatusJoin ? root.join("currentStatus", JoinType.LEFT) : null;

            if (f.getCurrentStatusId() != null) {
                p.add(cb.equal(statusJoin.get("id"), f.getCurrentStatusId()));
            }

            if (f.getStatus() != null) {
                p.add(cb.equal(statusJoin.get("status"), f.getStatus()));
            }

            if (f.getWithoutCurrentStatus() != null) {
                if (f.getWithoutCurrentStatus()) {
                    p.add(cb.isNull(root.get("currentStatus")));
                } else {
                    p.add(cb.isNotNull(root.get("currentStatus")));
                }
            }

            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
