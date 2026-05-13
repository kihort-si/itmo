package ru.itmo.blps.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.itmo.blps.app.security.dto.RegisterCustomerRequest;
import ru.itmo.blps.app.security.dto.RegisterShopAssistantRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CatalogFullTest {

    @LocalServerPort
    private int port;

    private WebTestClient client;

    private String buyerUsername;
    private String assistantUsername;

    @BeforeEach
    void setUp() throws IOException {
        buyerUsername = "buyer" + UUID.randomUUID().toString().substring(0, 8);
        assistantUsername = "anna" + UUID.randomUUID().toString().substring(0, 8);
        Files.deleteIfExists(Path.of("./build/test-users/users.xml"));
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        client.post()
                .uri("/api/auth/register/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new RegisterCustomerRequest(
                        "Покупатель",
                        buyerUsername + "@test.ru",
                        buyerUsername,
                        "secret123"))
                .exchange()
                .expectStatus().isOk();

        client.post()
                .uri("/api/auth/register/shop-assistant")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new RegisterShopAssistantRequest("Анна", 1L, assistantUsername, "secret123"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldReturnPagedProductsForAuthenticatedCustomer() {
        client.get()
                .uri("/api/catalog/products?page=0&size=5&q=iphone")
                .header(HttpHeaders.AUTHORIZATION, basic(buyerUsername, "secret123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(5);
    }

    @Test
    void shouldAllowAssistantToCreateProduct() {
        client.post()
                .uri("/api/catalog/products")
                .header(HttpHeaders.AUTHORIZATION, basic(assistantUsername, "secret123"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Тестовый смартфон",
                          "description": "Новый товар для проверки прав доступа",
                          "price": 19990,
                          "stock": 7
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Тестовый смартфон");
    }

    private String basic(String username, String password) {
        return "Basic " + java.util.Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
