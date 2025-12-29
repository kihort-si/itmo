package ru.itmo.is.storage;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

@Getter
@ApplicationScoped
public class StorageConfig {

    private static final Logger logger = Logger.getLogger(StorageConfig.class.getName());

    private String importsBucket;

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/microprofile-config.properties")) {
            if (is != null) {
                props.load(is);
                importsBucket = props.getProperty("storage.importsBucket");
            }
        } catch (Exception e) {
            logger.warning("Failed to read microprofile-config.properties: " + e.getMessage());
        }

        if (importsBucket == null || importsBucket.isEmpty()) {
            importsBucket = System.getProperty("storage.importsBucket");
        }
        if (importsBucket == null || importsBucket.isEmpty()) {
            importsBucket = System.getenv("STORAGE_IMPORTS_BUCKET");
        }

        if (importsBucket == null || importsBucket.isEmpty() || importsBucket.startsWith("http")) {
            importsBucket = "imports";
            logger.info("StorageConfig: Using default bucket name: " + importsBucket);
        } else {
            logger.info("StorageConfig: Using bucket name from config: " + importsBucket);
        }
    }
}

