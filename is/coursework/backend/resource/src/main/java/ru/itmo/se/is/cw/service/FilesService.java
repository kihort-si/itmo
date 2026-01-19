package ru.itmo.se.is.cw.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.se.is.cw.conf.StorageConfigProperties;
import ru.itmo.se.is.cw.dto.FileMetadataResponseDto;
import ru.itmo.se.is.cw.dto.FileVersionResponseDto;
import ru.itmo.se.is.cw.exception.EntityNotFoundException;
import ru.itmo.se.is.cw.mapper.FileMapper;
import ru.itmo.se.is.cw.mapper.FileVersionMapper;
import ru.itmo.se.is.cw.model.FileEntity;
import ru.itmo.se.is.cw.model.FileVersionEntity;
import ru.itmo.se.is.cw.repository.FileRepository;
import ru.itmo.se.is.cw.repository.FileVersionRepository;
import ru.itmo.se.is.cw.storage.FileStorage;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FilesService {

    private final FileRepository fileRepository;
    private final FileVersionRepository fileVersionRepository;
    private final FileStorage storage;
    private final FileMapper fileMapper;
    private final FileVersionMapper fileVersionMapper;
    private final StorageConfigProperties storageProperties;
    private final EntityManager em;
    private final CurrentUserService currentUserService;

    @Transactional
    public FileMetadataResponseDto uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        Long accountId = currentUserService.getAccountId();

        FileEntity entity = new FileEntity();
        entity.setFilename(Objects.requireNonNullElse(file.getOriginalFilename(), "file"));
        entity.setContentType(Objects.requireNonNullElse(file.getContentType(), "application/octet-stream"));
        entity.setOwnerId(accountId);
        entity = fileRepository.save(entity);

        String objectKey = buildObjectKey(entity.getId(), entity.getFilename());

        try (InputStream is = file.getInputStream()) {
            storage.save(storageProperties.getFileBucket(), objectKey, is, file.getSize(), entity.getContentType());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save file to storage", e);
        }

        fileRepository.updateFileVersionAndSetCurrent(
                entity.getId(),
                storageProperties.getFileBucket(),
                objectKey,
                file.getSize(),
                entity.getContentType(),
                accountId
        );

        em.refresh(entity);
        return fileMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public FileMetadataResponseDto getFileMetadata(Long id) {
        return fileMapper.toDto(getById(id));
    }

    @Transactional(readOnly = true)
    public List<FileVersionResponseDto> getFileVersions(Long id) {
        getById(id);
        return fileVersionRepository.findByFileIdOrderByUploadedAtDesc(id)
                .stream()
                .map(fileVersionMapper::toDto)
                .toList();
    }

    @Transactional
    public FileVersionResponseDto uploadNewFileVersion(Long id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        FileEntity entity = getById(id);
        assertNotDeleted(entity);

        Long accountId = currentUserService.getAccountId();

        String contentType = Objects.requireNonNullElse(file.getContentType(), entity.getContentType());
        String objectKey = buildObjectKey(entity.getId(), Objects.requireNonNullElse(file.getOriginalFilename(), entity.getFilename()));

        try (InputStream is = file.getInputStream()) {
            storage.save(storageProperties.getFileBucket(), objectKey, is, file.getSize(), contentType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save file version to storage", e);
        }

        fileRepository.updateFileVersionAndSetCurrent(
                entity.getId(),
                storageProperties.getFileBucket(),
                objectKey,
                file.getSize(),
                contentType,
                accountId
        );

        em.refresh(entity);

        if (entity.getCurrentVersion() == null) {
            throw new IllegalStateException("Current version was not set");
        }

        return fileVersionMapper.toDto(entity.getCurrentVersion());
    }

    @Transactional(readOnly = true)
    public FileEntity getById(Long id) {
        return fileRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("File with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public InputStream downloadFileStream(Long id) {
        FileEntity file = getById(id);
        assertNotDeleted(file);

        if (file.getCurrentVersion() == null) {
            throw new IllegalStateException("File has no current version");
        }

        try {
            return storage.get(storageProperties.getFileBucket(), file.getCurrentVersion().getObjectKey());
        } catch (Exception e) {
            throw new RuntimeException("Failed to download from storage", e);
        }
    }

    @Transactional(readOnly = true)
    public InputStream downloadFileVersionStream(Long fileId, Long versionId) {
        FileEntity file = getById(fileId);
        assertNotDeleted(file);

        FileVersionEntity v = fileVersionRepository.findByIdAndFileId(versionId, fileId)
                .orElseThrow(() -> new EntityNotFoundException("FileVersion " + versionId + " for file " + fileId + " not found"));

        try {
            return storage.get(storageProperties.getFileBucket(), v.getObjectKey());
        } catch (Exception e) {
            throw new RuntimeException("Failed to download from storage", e);
        }
    }

    @Transactional
    public void softDeleteFile(Long id) {
        FileEntity file = getById(id);
        if (file.getDeletedAt() != null) {
            return;
        }
        file.setDeletedAt(ZonedDateTime.now());
        fileRepository.save(file);
    }

    private void assertNotDeleted(FileEntity file) {
        if (file.getDeletedAt() != null) {
            throw new IllegalStateException("File is deleted");
        }
    }

    private String buildObjectKey(Long fileId, String filename) {
        String safeName = (filename == null ? "file" : filename).replaceAll("[^a-zA-Z0-9._-]", "_");
        return "files/" + fileId + "/" + UUID.randomUUID() + "_" + safeName;
    }
}

