package ru.itmo.blps.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.itmo.blps.app.security.AuthService;
import ru.itmo.blps.app.security.dto.RegisterCourierRequest;
import ru.itmo.blps.app.security.dto.RegisterCustomerRequest;
import ru.itmo.blps.app.security.dto.RegisterShopAssistantRequest;
import ru.itmo.blps.app.security.xml.XmlUserStore;

/**
 * Creates test users on startup if they are missing from the XML user store.
 * Runs after CamundaDeploymentService (lower precedence = runs later).
 * Safe to run multiple times: skips users that already exist.
 */
@Component
@Profile("!test")
public class SeedDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(SeedDataInitializer.class);

    private final AuthService authService;
    private final XmlUserStore xmlUserStore;

    public SeedDataInitializer(AuthService authService, XmlUserStore xmlUserStore) {
        this.authService = authService;
        this.xmlUserStore = xmlUserStore;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(100)
    public void seed() {
        seedUser("customer-1",
                () -> authService.registerCustomer(
                        new RegisterCustomerRequest("Тестовый Покупатель", "customer1@test.com", "customer-1", "password")));

        seedUser("assistant-1",
                () -> authService.registerShopAssistant(
                        new RegisterShopAssistantRequest("Иван Иванов", 1L, "assistant-1", "password")));

        seedUser("courier-1",
                () -> authService.registerCourier(
                        new RegisterCourierRequest("Пётр Петров", "courier-1", "password", "1234 567890")));
    }

    private void seedUser(String username, Runnable creator) {
        if (xmlUserStore.findByUsername(username).isPresent()) {
            return;
        }
        try {
            creator.run();
            log.info("Seed user created: {}", username);
        } catch (Exception e) {
            log.warn("Could not seed user '{}': {}", username, e.getMessage());
        }
    }
}
