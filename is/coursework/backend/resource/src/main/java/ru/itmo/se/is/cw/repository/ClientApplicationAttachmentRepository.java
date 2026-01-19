package ru.itmo.se.is.cw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.ClientApplicationAttachmentEntity;

@Repository
public interface ClientApplicationAttachmentRepository extends JpaRepository<ClientApplicationAttachmentEntity, Long> {
    boolean existsByFileId(Long fileId);
}
