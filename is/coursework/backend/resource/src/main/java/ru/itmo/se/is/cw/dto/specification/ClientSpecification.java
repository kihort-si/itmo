package ru.itmo.se.is.cw.dto.specification;

import jakarta.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.itmo.se.is.cw.dto.filter.ClientFilter;
import ru.itmo.se.is.cw.model.ClientEntity;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public final class ClientSpecification {

    public static Specification<ClientEntity> byFilter(ClientFilter f) {
        return (root, query, cb) -> {
            if (f == null) return cb.conjunction();

            List<Predicate> p = new ArrayList<>();

            if (hasText(f.getEmail())) {
                p.add(cb.like(
                        cb.lower(root.get("account").get("email")),
                        "%" + f.getEmail().toLowerCase() + "%"
                ));
            }

            if (hasText(f.getUsername())) {
                p.add(cb.like(
                        cb.lower(root.get("account").get("username")),
                        "%" + f.getUsername().toLowerCase() + "%"
                ));
            }

            if (hasText(f.getFirstName())) {
                p.add(cb.like(
                        cb.lower(root.get("firstName")),
                        "%" + f.getFirstName().toLowerCase() + "%"
                ));
            }

            if (hasText(f.getLastName())) {
                p.add(cb.like(
                        cb.lower(root.get("lastName")),
                        "%" + f.getLastName().toLowerCase() + "%"
                ));
            }

            if (hasText(f.getPhoneNumber())) {
                p.add(cb.like(
                        cb.lower(root.get("phoneNumber")),
                        "%" + f.getPhoneNumber().toLowerCase() + "%"
                ));
            }

            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}