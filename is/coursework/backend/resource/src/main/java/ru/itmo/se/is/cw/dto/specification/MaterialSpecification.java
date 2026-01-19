package ru.itmo.se.is.cw.dto.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.itmo.se.is.cw.dto.filter.MaterialFilter;
import ru.itmo.se.is.cw.model.MaterialEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@NoArgsConstructor
public final class MaterialSpecification {

    public static Specification<MaterialEntity> byFilter(MaterialFilter f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            if (f == null) {
                return cb.conjunction();
            }

            if (f.getName() != null && !f.getName().isBlank()) {
                String pattern = "%" + f.getName().trim().toLowerCase(Locale.ROOT) + "%";
                p.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            if (f.getUnitOfMeasure() != null && !f.getUnitOfMeasure().isBlank()) {
                p.add(cb.equal(
                        cb.lower(root.get("unitOfMeasure")),
                        f.getUnitOfMeasure().trim().toLowerCase(Locale.ROOT)
                ));
            }

            if (f.getOrderPointFrom() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("orderPoint"), f.getOrderPointFrom()));
            }
            if (f.getOrderPointTo() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("orderPoint"), f.getOrderPointTo()));
            }

            boolean needBalanceJoin =
                    f.getCurrentBalanceFrom() != null ||
                            f.getCurrentBalanceTo() != null ||
                            Boolean.TRUE.equals(f.getBelowOrderPoint()) ||
                            Boolean.TRUE.equals(f.getWithoutCurrentBalance());

            var balanceJoin = needBalanceJoin ? root.join("currentBalance", JoinType.LEFT) : null;

            if (f.getCurrentBalanceFrom() != null) {
                p.add(cb.greaterThanOrEqualTo(balanceJoin.get("balance"), f.getCurrentBalanceFrom()));
            }
            if (f.getCurrentBalanceTo() != null) {
                p.add(cb.lessThanOrEqualTo(balanceJoin.get("balance"), f.getCurrentBalanceTo()));
            }

            if (f.getWithoutCurrentBalance() != null) {
                if (f.getWithoutCurrentBalance()) {
                    p.add(cb.isNull(root.get("currentBalance")));
                } else {
                    p.add(cb.isNotNull(root.get("currentBalance")));
                }
            }

            if (Boolean.TRUE.equals(f.getBelowOrderPoint())) {
                p.add(cb.and(
                        cb.isNotNull(root.get("currentBalance")),
                        cb.lessThan(balanceJoin.get("balance"), root.get("orderPoint"))
                ));
            }

            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
