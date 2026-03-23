package ru.itmo.blps.app;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.itmo.blps.app.models.Product;
import ru.itmo.blps.app.security.dto.AuthResponse;
import ru.itmo.blps.app.security.dto.RegisterCustomerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.itmo.blps.app.models.enums.UserRole.CUSTOMER;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class CatalogFullTest {

    @LocalServerPort
    private int port;

    WebTestClient client;
    private String jwt;

    @Container
    public static PostgreSQLContainer<?> postgres
            = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.username", postgres::getUsername);
    }

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        RegisterCustomerRequest request = new RegisterCustomerRequest("testuser", "test@test.ru", "testusername","password123");
        var response = client.post()
                .uri("/api/auth/register/customer")
                .bodyValue(request)
                .exchange()
                .expectHeader().
                contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(AuthResponse.class).returnResult().getResponseBody();
        jwt = response.token();
    }

    @Test
    void shouldReturnProducts() {
        client.get().uri("/api/products")
                .exchange()
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(Product.class);
    }


}
