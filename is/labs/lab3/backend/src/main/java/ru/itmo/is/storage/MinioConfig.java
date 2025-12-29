package ru.itmo.is.storage;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

@Getter
@ApplicationScoped
public class MinioConfig {

    private static final Logger logger = Logger.getLogger(MinioConfig.class.getName());

    private String minioUrl;
    private String accessKey;
    private String secretKey;

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/microprofile-config.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            logger.warning("Failed to read microprofile-config.properties: " + e.getMessage());
        }

        minioUrl = props.getProperty("minio.url");
        if (minioUrl == null || minioUrl.isEmpty()) {
            minioUrl = System.getProperty("minio.url");
        }
        if (minioUrl == null || minioUrl.isEmpty()) {
            minioUrl = System.getenv("MINIO_URL");
        }
        if (minioUrl == null || minioUrl.isEmpty() || !minioUrl.startsWith("http")) {
            minioUrl = "http://localhost:9004";
            logger.info("MinioConfig: Using default URL: " + minioUrl);
        } else {
            logger.info("MinioConfig: Using URL from config: " + minioUrl);
        }

        accessKey = props.getProperty("minio.accessKey");
        if (accessKey == null || accessKey.isEmpty()) {
            accessKey = System.getProperty("minio.accessKey");
        }
        if (accessKey == null || accessKey.isEmpty()) {
            accessKey = System.getenv("MINIO_ACCESS_KEY");
        }
        if (accessKey == null || accessKey.isEmpty() || accessKey.startsWith("http")) {
            accessKey = "minioadmin";
            logger.info("MinioConfig: Using default accessKey");
        }

        secretKey = props.getProperty("minio.secretKey");
        if (secretKey == null || secretKey.isEmpty()) {
            secretKey = System.getProperty("minio.secretKey");
        }
        if (secretKey == null || secretKey.isEmpty()) {
            secretKey = System.getenv("MINIO_SECRET_KEY");
        }
        if (secretKey == null || secretKey.isEmpty() || secretKey.startsWith("http")) {
            secretKey = "minioadmin";
            logger.info("MinioConfig: Using default secretKey");
        }

        logger.info("MinioConfig initialized. URL: " + minioUrl + ", AccessKey: " + accessKey);
    }
}

