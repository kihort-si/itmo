package ru.itmo.se.is.cw.dto.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.itmo.se.is.cw.dto.filter.ClientOrderFilter;
import ru.itmo.se.is.cw.model.ClientOrderEntity;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public final class ClientOrderSpecification {

    public static Specification<ClientOrderEntity> byFilter(ClientOrderFilter f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f == null) return cb.conjunction();

            if (f.getStatus() != null) {
                var st = root.join("currentStatus", JoinType.LEFT);
                p.add(cb.equal(st.get("status"), f.getStatus()));
            }

            if (f.getClientId() != null) {
                var app = root.join("clientApplication", JoinType.INNER);
                var client = app.join("client", JoinType.INNER);
                p.add(cb.equal(client.get("id"), f.getClientId()));
            }

            if (f.getManagerId() != null) {
                p.add(cb.equal(root.get("manager").get("id"), f.getManagerId()));
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
