package ru.itmo.se.is.cw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.MessageEntity;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long>, JpaSpecificationExecutor<MessageEntity> {
}
