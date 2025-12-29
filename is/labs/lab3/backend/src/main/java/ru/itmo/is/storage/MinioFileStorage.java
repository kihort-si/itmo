package ru.itmo.is.storage;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.ByteArrayInputStream;
import java.util.logging.Logger;

@ApplicationScoped
public class MinioFileStorage {
    private static final Logger logger = Logger.getLogger(MinioFileStorage.class.getName());

    @Inject
    StorageConfig storageConfig;

    @Inject
    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            String bucketName = storageConfig.getImportsBucket();
            logger.info("Initializing MinioFileStorage with bucket: " + bucketName);
            ensureBucketExists();
        } catch (Exception e) {
            logger.warning("Failed to ensure bucket exists: " + e.getMessage());
        }
    }

    private void ensureBucketExists() throws Exception {
        String bucketName = storageConfig.getImportsBucket();
        if (bucketName == null || bucketName.isEmpty()) {
            throw new IllegalStateException("Bucket name is null or empty");
        }
        logger.info("Checking bucket existence: " + bucketName);
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            logger.info("Bucket does not exist, creating: " + bucketName);
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            logger.info("Created bucket: " + bucketName);
        } else {
            logger.info("Bucket already exists: " + bucketName);
        }
    }

    public void save(String objectKey, byte[] content) throws Exception {
        try {
            String bucketName = storageConfig.getImportsBucket();
            if (bucketName == null || bucketName.isEmpty()) {
                throw new IllegalStateException("Bucket name is null or empty. Check microprofile-config.properties");
            }
            logger.info("Saving file to bucket: " + bucketName + ", objectKey: " + objectKey);
            ensureBucketExists();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(new ByteArrayInputStream(content), content.length, 10 * 1024 * 1024)
                            .contentType("application/octet-stream")
                            .build()
            );
            logger.info("Successfully saved file to MinIO: " + objectKey);
        } catch (Exception e) {
            logger.severe("Failed to save file to MinIO. Bucket: " + storageConfig.getImportsBucket() + ", Error: " + e.getMessage());
            throw new Exception("Failed to save file to MinIO: " + e.getMessage(), e);
        }
    }

    public byte[] get(String objectKey) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(storageConfig.getImportsBucket())
                        .object(objectKey)
                        .build()
        ).readAllBytes();
    }

    public void delete(String objectKey) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(storageConfig.getImportsBucket())
                        .object(objectKey)
                        .build()
        );
    }
}

