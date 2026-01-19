package ru.itmo.se.is.cw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.FileVersionEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileVersionRepository extends JpaRepository<FileVersionEntity, Long> {
    List<FileVersionEntity> findByFileIdOrderByUploadedAtDesc(Long fileId);

    Optional<FileVersionEntity> findByIdAndFileId(Long id, Long fileId);
}
