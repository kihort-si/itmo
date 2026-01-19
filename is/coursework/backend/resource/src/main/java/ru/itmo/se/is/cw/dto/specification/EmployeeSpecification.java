package ru.itmo.se.is.cw.dto.specification;

import jakarta.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.itmo.se.is.cw.dto.filter.EmployeeFilter;
import ru.itmo.se.is.cw.model.EmployeeEntity;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public final class EmployeeSpecification {

    public static Specification<EmployeeEntity> byFilter(EmployeeFilter f) {
        return ((root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            if (f.getRole() != null) {
                p.add(cb.equal(root.get("account").get("role"), f.getRole()));
            }

            return cb.and(p.toArray(new Predicate[0]));
        });
    }
}
