package com.greenhouse.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {

    @Value("${app.http.connect-timeout:2000}")
    private long connectTimeoutMs;

    @Value("${app.http.read-timeout:3000}")
    private long readTimeoutMs;

    @Bean
    public RestTemplate restTemplate() {
        RequestConfig config = RequestConfig.custom()
                .setExpectContinueEnabled(false)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .disableContentCompression()
                .build();

        HttpComponentsClientHttpRequestFactory base =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        base.setConnectionRequestTimeout((int) connectTimeoutMs);
        base.setReadTimeout((int) readTimeoutMs);

        return new RestTemplate(new BufferingClientHttpRequestFactory(base));
    }
}
