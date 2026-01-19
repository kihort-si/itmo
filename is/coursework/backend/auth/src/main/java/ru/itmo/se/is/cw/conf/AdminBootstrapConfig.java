package ru.itmo.se.is.cw.conf;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.itmo.se.is.cw.service.AdminInitService;

@Configuration
@RequiredArgsConstructor
public class AdminBootstrapConfig {

    private final AdminInitService adminInitService;

    @Bean
    ApplicationRunner createAdmin(AdminInitService adminInitService) {
        return args -> adminInitService.ensureAdminExists();
    }
}

