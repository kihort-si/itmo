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
        entityManager.persist(file);
        entityManager.flush();
    }

    public List<File> getAllFiles() {
        TypedQuery<File> query = entityManager.createQuery(
            "SELECT f FROM File f", File.class);
        return query.getResultList();
    }
}
