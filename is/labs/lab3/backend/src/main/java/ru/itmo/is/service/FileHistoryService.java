package ru.itmo.is.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import ru.itmo.is.model.File;
import ru.itmo.is.repository.UploadFileRepository;

import java.time.LocalDateTime;

@ApplicationScoped
public class FileHistoryService {

    @Inject
    private UploadFileRepository uploadFileRepository;
    
    @Inject
    private EntityManager entityManager;

    @Transactional(jakarta.transaction.Transactional.TxType.REQUIRES_NEW)
    public Long createFileHistory(String filename, String objectKey, Long size, LocalDateTime creationDate) {
        try {
            File file = new File();
            file.setFilename(filename);
            file.setObjectKey(objectKey);
            file.setSize(size);
            file.setCreationDate(creationDate);
            file.setSuccess(false);
            file.setObjectsCount(0);
            
            entityManager.persist(file);
            entityManager.flush();
            
            return file.getId();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать историю файла: " + e.getMessage(), e);
        }
    }

    @Transactional(jakarta.transaction.Transactional.TxType.REQUIRES_NEW)
    public void updateFileHistory(Long fileId, boolean success, int objectsCount) {
        try {
            File file = entityManager.find(File.class, fileId);
            if (file != null) {
                file.setSuccess(success);
                file.setObjectsCount(objectsCount);
            }
        } catch (Exception e) {
            throw new RuntimeException("Не удалось обновить историю файла: " + e.getMessage(), e);
        }
    }

    @Transactional(jakarta.transaction.Transactional.TxType.REQUIRES_NEW)
    public void saveFileHistory(File file) {
        try {
            uploadFileRepository.SaveFile(file);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось сохранить историю файла: " + e.getMessage(), e);
        }
    }

    public void saveFileHistoryInTransaction(File file) {
        try {
            uploadFileRepository.SaveFile(file);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось сохранить историю файла в текущей транзакции: " + e.getMessage(), e);
        }
    }
}
