package ru.itmo.se.is.cw.dto.specification;

import jakarta.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.itmo.se.is.cw.dto.filter.ClientApplicationFilter;
import ru.itmo.se.is.cw.model.ClientApplicationEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@NoArgsConstructor
public final class ClientApplicationSpecification {

    public static Specification<ClientApplicationEntity> byFilter(ClientApplicationFilter f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            if (f == null) {
                return cb.conjunction();
            }

            if (f.getClientId() != null) {
                p.add(cb.equal(root.get("client").get("id"), f.getClientId()));
            }

            if (f.getDescription() != null && !f.getDescription().isBlank()) {
                String pattern = "%" + f.getDescription().trim().toLowerCase(Locale.ROOT) + "%";
                p.add(cb.like(cb.lower(root.get("description")), pattern));
            }

            if (f.getTemplateProductDesignId() != null) {
                p.add(cb.equal(root.get("templateProductDesign").get("id"), f.getTemplateProductDesignId()));
            }

            if (f.getCreatedFrom() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("createdAt"), f.getCreatedFrom()));
            }
            if (f.getCreatedTo() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("createdAt"), f.getCreatedTo()));
            }

            if (f.getAmountFrom() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("amount"), f.getAmountFrom().shortValue()));
            }
            if (f.getAmountTo() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("amount"), f.getAmountTo().shortValue()));
            }

            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
