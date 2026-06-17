package ru.itmo.blps.app.camunda;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CamundaProperties.class)
public class CamundaConfig {

    @Bean("camundaRestClient")
    public RestClient camundaRestClient(CamundaProperties props) {
        return RestClient.builder()
                .baseUrl(props.restApiUrl())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
