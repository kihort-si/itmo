package ru.itmo.blps.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.itmo.blps.app.security.dto.AuthResponse;
import ru.itmo.blps.app.security.dto.RegisterCustomerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.itmo.blps.app.models.enums.UserRole.CUSTOMER;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RegistrationFullTest {

    @LocalServerPort
    private int port;

    private WebTestClient client;

    @BeforeEach
    void setUp() throws IOException {
        Files.deleteIfExists(Path.of("./build/test-users/users.xml"));
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void shouldRegisterCustomerAndAuthenticateViaBasic() {
        RegisterCustomerRequest request = new RegisterCustomerRequest(
                "Иван Иванов",
                "ivan@test.ru",
                "ivan",
                "secret123"
        );

        client.post()
                .uri("/api/auth/register/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .value(response -> {
                    assertThat(response.role()).isEqualTo(CUSTOMER);
                    assertThat(response.authenticationScheme()).isEqualTo("BASIC");
                    assertThat(response.participantId()).isNotNull();
                });

        client.get()
                .uri("/api/customers/me")
                .header(HttpHeaders.AUTHORIZATION, basic("ivan", "secret123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo("ivan@test.ru");
    }

    @Test
    void shouldRejectInvalidCustomerRegistration() {
        client.post()
                .uri("/api/auth/register/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"fullName\":\"\",\"email\":\"bad\",\"username\":\"ab\",\"password\":\"123\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isNotEmpty();
    }

    private String basic(String username, String password) {
        return "Basic " + java.util.Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
