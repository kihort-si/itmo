package ru.itmo.is.transaction;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import ru.itmo.is.storage.MinioFileStorage;

import java.util.function.Supplier;
import java.util.logging.Logger;

@Dependent
public class MinioParticipant implements TwoPhaseCommitParticipant {

    private static final Logger logger = Logger.getLogger(MinioParticipant.class.getName());

    @Inject
    private MinioFileStorage minioFileStorage;

    private Supplier<Void> minioOperation;
    private String objectKey;
    private byte[] content;
    private boolean isDeleteOperation = false;
    private boolean prepared = false;

    public void setSaveOperation(String objectKey, byte[] content) {
        logger.info("MinioParticipant.setSaveOperation called with objectKey: " + objectKey);
        this.objectKey = objectKey;
        this.content = content;
        this.isDeleteOperation = false;
        this.minioOperation = () -> {
            try {
                logger.info("MinioParticipant: Saving to MinIO with objectKey: " + objectKey);
                minioFileStorage.save(objectKey, content);
                logger.info("MinioParticipant: Successfully saved to MinIO with objectKey: " + objectKey);
            } catch (Exception e) {
                logger.severe("Error saving to MinIO: " + e.getMessage());
                if (e.getCause() != null) {
                    logger.severe("Cause: " + e.getCause().getMessage());
                }
                throw new RuntimeException("Failed to save to MinIO: " + e.getMessage(), e);
            }
            return null;
        };
    }

    @Override
    public boolean prepare() throws Exception {
        try {
            prepared = true;
            return true;
        } catch (Exception e) {
            prepared = false;
            throw e;
        }
    }

    @Override
    public void commit() throws Exception {
        if (!prepared) {
            throw new IllegalStateException("Cannot commit: participant not prepared");
        }
        
        try {
            if (minioOperation != null) {
                minioOperation.get();
            }
        } catch (RuntimeException e) {
            logger.severe("Failed to commit MinIO operation: " + e.getMessage());
            if (e.getCause() != null) {
                logger.severe("Root cause: " + e.getCause().getMessage());
                throw new Exception("Failed to commit MinIO operation: " + e.getCause().getMessage(), e.getCause());
            }
            throw new Exception("Failed to commit MinIO operation: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.severe("Failed to commit MinIO operation: " + e.getMessage());
            throw new Exception("Failed to commit MinIO operation: " + e.getMessage(), e);
        }
    }

    @Override
    public void rollback() throws Exception {
        try {
            if (!isDeleteOperation && objectKey != null) {
                try {
                    minioFileStorage.delete(objectKey);
                } catch (Exception e) {
                    logger.warning("Failed to delete file during rollback: " + e.getMessage());
                }
            }
        } finally {
            reset();
        }
    }

    public void cleanup() {
        reset();
    }

    private void reset() {
        prepared = false;
        minioOperation = null;
        objectKey = null;
        content = null;
        isDeleteOperation = false;
    }
}

