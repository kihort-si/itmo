package ru.itmo.tpo.util;

import org.junit.jupiter.params.provider.Arguments;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class TestCsvData {
    private TestCsvData() {
    }

    public static Stream<Arguments> fromResource(String resourcePath) {
        InputStream input = TestCsvData.class.getClassLoader().getResourceAsStream(resourcePath);
        if (input == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        List<Arguments> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                String[] parts = trimmed.split(";");
                if (parts.length < 2) {
                    throw new IllegalArgumentException("Invalid row in " + resourcePath + ": " + trimmed);
                }
                rows.add(Arguments.of(parse(parts[0]), parse(parts[1])));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV test data: " + resourcePath, e);
        }

        return rows.stream();
    }

    private static double parse(String token) {
        String normalized = token.trim().replace(',', '.');
        if ("NaN".equalsIgnoreCase(normalized)) {
            return Double.NaN;
        }
        return Double.parseDouble(normalized);
    }
}
