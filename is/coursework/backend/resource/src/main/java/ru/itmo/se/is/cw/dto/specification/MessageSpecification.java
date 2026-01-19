package ru.itmo.se.is.cw.dto.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.itmo.se.is.cw.dto.filter.MessageFilter;
import ru.itmo.se.is.cw.model.MessageEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@NoArgsConstructor
public final class MessageSpecification {

    public static Specification<MessageEntity> byFilter(MessageFilter f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f == null) return cb.conjunction();

            var partJoin = root.join("conversationParticipant", JoinType.INNER);

            if (f.getParticipantId() != null) {
                p.add(cb.equal(partJoin.get("id"), f.getParticipantId()));
            }

            if (f.getAuthorId() != null) {
                p.add(cb.equal(partJoin.get("user").get("id"), f.getAuthorId()));
            }

            if (f.getConversationId() != null) {
                p.add(cb.equal(partJoin.get("conversation").get("id"), f.getConversationId()));
            }

            if (f.getContent() != null && !f.getContent().isBlank()) {
                String pattern = "%" + f.getContent().trim().toLowerCase(Locale.ROOT) + "%";
                p.add(cb.like(cb.lower(root.get("content")), pattern));
            }

            if (f.getSentFrom() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("sentAt"), f.getSentFrom()));
            }
            if (f.getSentTo() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("sentAt"), f.getSentTo()));
            }

            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
