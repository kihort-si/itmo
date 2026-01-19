package ru.itmo.se.is.cw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.ConversationParticipantEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipantEntity, Long> {
    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);

    Optional<ConversationParticipantEntity> findByConversationIdAndUserId(Long conversationId, Long userId);

    List<ConversationParticipantEntity> findByConversationIdOrderByJoinedAtAsc(Long conversationId);
}