package ru.itmo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.itmo.config.ApplicationFactory;
import ru.itmo.config.ApplicationSettings;
import ru.itmo.config.Database;
import ru.itmo.repository.SqliteUserRepository;
import ru.itmo.repository.UserRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppIntegrationTest {
    private static final String TEST_SECRET = "test-secret-that-is-at-least-32-characters-long";
    private static final String LOGIN = "student";
    private static final String PASSWORD = "StrongPass123";

    @TempDir
    Path temporaryDirectory;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Javalin app;
    private String baseUrl;
    private Path databasePath;

    @BeforeEach
    void startServer() throws Exception {
        databasePath = temporaryDirectory.resolve("test.db");
        ApplicationSettings settings = new ApplicationSettings(0, databasePath, TEST_SECRET);
        app = ApplicationFactory.create(settings).start(settings.port());
        baseUrl = "http://127.0.0.1:" + app.port();
    }

    @AfterEach
    void stopServer() {
        if (app != null) {
            app.stop();
        }
    }

    @Test
    void fullAuthenticationFlowProtectsDataAndStoresOnlyPasswordHash() throws Exception {
        HttpResponse<String> anonymous = getData(null);
        assertEquals(401, anonymous.statusCode());

        HttpResponse<String> registration = post("/auth/register", credentials(LOGIN, PASSWORD));
        assertEquals(201, registration.statusCode());

        UserRepository userRepository = new SqliteUserRepository(new Database(databasePath));
        String storedHash = userRepository.findByLogin(LOGIN).orElseThrow().passwordHash();
        assertNotEquals(PASSWORD, storedHash);
        assertTrue(storedHash.startsWith("$2"));

        HttpResponse<String> badPassword = post("/auth/login", credentials(LOGIN, "WrongPassword123"));
        assertEquals(401, badPassword.statusCode());

        HttpResponse<String> sqlInjection = post(
                "/auth/login",
                credentials("' OR 1=1 --", "WrongPassword123")
        );
        assertEquals(401, sqlInjection.statusCode());

        HttpResponse<String> login = post("/auth/login", credentials(LOGIN, PASSWORD));
        assertEquals(200, login.statusCode());
        JsonNode loginJson = objectMapper.readTree(login.body());
        String token = loginJson.get("accessToken").asText();
        assertFalse(token.isBlank());
        assertEquals("Bearer", loginJson.get("tokenType").asText());

        HttpResponse<String> authorized = getData(token);
        assertEquals(200, authorized.statusCode());
        assertEquals(LOGIN, objectMapper.readTree(authorized.body()).get("requestedBy").asText());
        assertEquals("nosniff", authorized.headers().firstValue("X-Content-Type-Options").orElseThrow());

        assertEquals(401, getData("not-a-jwt").statusCode());
    }

    @Test
    void registrationValidatesInputAndRejectsDuplicateLogin() throws Exception {
        assertEquals(400, post("/auth/register", credentials("<script>", PASSWORD)).statusCode());
        assertEquals(400, post("/auth/register", credentials("student", "short")).statusCode());
        assertEquals(201, post("/auth/register", credentials(LOGIN, PASSWORD)).statusCode());
        assertEquals(409, post("/auth/register", credentials(LOGIN, PASSWORD)).statusCode());
    }

    private HttpResponse<String> getData(String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + "/api/data")).GET();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String credentials(String login, String password) throws Exception {
        return objectMapper.writeValueAsString(new Credentials(login, password));
    }

    private record Credentials(String login, String password) {
    }
}
