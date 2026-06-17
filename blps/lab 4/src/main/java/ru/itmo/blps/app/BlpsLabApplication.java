package ru.itmo.blps.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.itmo.blps.app.messaging.OrderStatusMessagingProperties;

@SpringBootApplication(scanBasePackages = "ru.itmo.blps.app")
@EntityScan(basePackages = "ru.itmo.blps.app.models")
@EnableJpaRepositories(basePackages = "ru.itmo.blps.app.repositories")
@EnableScheduling
@EnableConfigurationProperties(OrderStatusMessagingProperties.class)
public class BlpsLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlpsLabApplication.class, args);
    }

}


