package ru.itmo.is.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import ru.itmo.is.model.File;

import java.util.List;

@ApplicationScoped
public class UploadFileRepository {
    @Inject
    private EntityManager entityManager;

    public void SaveFile(File file) {
        if (entityManager.contains(file)) {
            return;
        }
        
        if (file.getId() == null) {
            entityManager.persist(file);
        } else {
            File existing = entityManager.find(File.class, file.getId());
            if (existing != null) {
                existing.setFilename(file.getFilename());
                existing.setObjectKey(file.getObjectKey());
                existing.setSize(file.getSize());
                existing.setCreationDate(file.getCreationDate());
                existing.setSuccess(file.isSuccess());
                existing.setObjectsCount(file.getObjectsCount());
            } else {
                entityManager.merge(file);
            }
        }
    }

    public List<File> getAllFiles() {
        TypedQuery<File> query = entityManager.createQuery(
            "SELECT f FROM File f", File.class);
        return query.getResultList();
    }

    public File findById(Long id) {
        return entityManager.find(File.class, id);
    }
}
