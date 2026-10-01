package ru.itmo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

public record ApplicationSettings(int port, Path databasePath, String jwtSecret) {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationSettings.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public ApplicationSettings {
        if (port < 0 || port > 65_535) {
            throw new IllegalArgumentException("Port must be between 0 and 65535");
        }
        Objects.requireNonNull(databasePath, "Database path is required");
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 characters");
        }
    }

    public static ApplicationSettings fromEnvironment() {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));
        Path databasePath = Path.of(System.getenv().getOrDefault("DATABASE_PATH", "secure-api.db"));
        String jwtSecret = System.getenv("JWT_SECRET");

        if (jwtSecret == null || jwtSecret.isBlank()) {
            jwtSecret = generateEphemeralSecret();
            LOGGER.warn("JWT_SECRET is not set; using an ephemeral secret. Existing tokens will be invalid after restart.");
        }
        return new ApplicationSettings(port, databasePath, jwtSecret);
    }

    private static String generateEphemeralSecret() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
