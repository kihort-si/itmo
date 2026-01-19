package ru.itmo.se.is.cw.conf;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(
            @Value("${app.swagger.server-url}") String serverUrl
    ) {
        return new OpenAPI()
                .info(new Info()
                        .title("CNC Order Management – Resource API")
                        .version("1.0.0")
                        .description("REST API для подсистемы управления клиентскими заказами, конструкторскими работами, производством, складом и снабжением."))
                .servers(List.of(new Server().url(serverUrl)));
    }
}
