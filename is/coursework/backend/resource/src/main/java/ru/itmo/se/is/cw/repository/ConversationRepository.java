package ru.itmo.se.is.cw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.ConversationEntity;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, Long> {
    Optional<ConversationEntity> findByOrderId(Long orderId);
}
