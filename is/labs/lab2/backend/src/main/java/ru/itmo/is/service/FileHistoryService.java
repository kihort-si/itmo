package ru.itmo.is.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.itmo.is.model.File;
import ru.itmo.is.repository.UploadFileRepository;

@ApplicationScoped
public class FileHistoryService {

    @Inject
    private UploadFileRepository uploadFileRepository;

    @Transactional(jakarta.transaction.Transactional.TxType.REQUIRES_NEW)
    public void saveFileHistory(File file) {
        try {
            File fileToSave = new File();
            fileToSave.setFilename(file.getFilename());
            fileToSave.setSize(file.getSize());
            fileToSave.setSuccess(file.isSuccess());
            fileToSave.setCreationDate(file.getCreationDate());
            fileToSave.setObjectsCount(file.getObjectsCount());
            
            uploadFileRepository.SaveFile(fileToSave);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось сохранить историю файла: " + e.getMessage(), e);
        }
    }
}
