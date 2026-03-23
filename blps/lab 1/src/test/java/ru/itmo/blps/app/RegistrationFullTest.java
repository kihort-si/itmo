package ru.itmo.blps.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.itmo.blps.app.security.dto.AuthResponse;
import ru.itmo.blps.app.security.dto.RegisterCustomerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.itmo.blps.app.models.enums.UserRole.CUSTOMER;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class RegistrationFullTest {

    @LocalServerPort
    private int port;

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass");


    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void shouldRegisterCustomer() {
        RegisterCustomerRequest request = new RegisterCustomerRequest("testuser", "test@test.ru", "testusername","password123");
        client.post()
                .uri("/api/auth/register/customer")
                .bodyValue(request)
                .exchange()
                .expectHeader().
                contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(AuthResponse.class)
                .value(response ->
                        assertThat(response.role()).isEqualTo(CUSTOMER)
                );
    }

    @Test
    void shouldNotRegisterCustomer() {
        RegisterCustomerRequest request = new RegisterCustomerRequest("testuser", "test@test.ru", "testusername",null);
        client.post()
                .uri("/api/auth/register/customer")
                .bodyValue(request)
                .exchange()
                .expectHeader().
                contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isNotEmpty();
    }


}
