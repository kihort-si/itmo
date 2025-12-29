package ru.itmo.is.storage;

import io.minio.MinioClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import java.util.logging.Logger;

@ApplicationScoped
public class MinioClientProducer {

    private static final Logger logger = Logger.getLogger(MinioClientProducer.class.getName());

    @Inject
    MinioConfig config;

    @Produces
    @Dependent
    public MinioClient minioClient() {
        String endpoint = config.getMinioUrl();
        logger.info("Creating MinioClient with endpoint: " + endpoint);
        
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(config.getAccessKey(), config.getSecretKey())
                .build();
        
        logger.info("MinioClient created successfully");
        return client;
    }
}
