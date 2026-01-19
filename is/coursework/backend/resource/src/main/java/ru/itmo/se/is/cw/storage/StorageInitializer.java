package ru.itmo.se.is.cw.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.itmo.se.is.cw.conf.StorageConfigProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageInitializer {

    private final MinioClient minioClient;
    private final StorageConfigProperties properties;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        String bucketName = properties.getFileBucket();
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                log.info("Bucket '{}' not found. Creating...", bucketName);
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("Bucket '{}' created", bucketName);
            } else {
                log.info("Bucket '{}' already exists", bucketName);
            }
        } catch (Exception e) {
            log.error("Error while initializing MinIO bucket '{}'", bucketName, e);
        }
    }
}
