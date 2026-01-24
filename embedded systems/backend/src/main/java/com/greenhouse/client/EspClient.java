package com.greenhouse.client;

import com.greenhouse.client.dto.BindResponse;
import com.greenhouse.client.dto.DriversResponse;
import com.greenhouse.client.dto.InfoResponse;
import com.greenhouse.client.dto.LevelRequest;
import com.greenhouse.client.dto.PortsResponse;
import com.greenhouse.client.dto.ReadResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;

@Component
public class EspClient {

    private final RestTemplate restTemplate;

    public EspClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public InfoResponse getInfo(String baseUrl) {
        return restTemplate.getForObject(URI.create(baseUrl + "/info"), InfoResponse.class);
    }

    public DriversResponse getDrivers(String baseUrl) {
        return restTemplate.getForObject(URI.create(baseUrl + "/drivers"), DriversResponse.class);
    }

    public PortsResponse getPorts(String baseUrl) {
        return restTemplate.getForObject(URI.create(baseUrl + "/ports"), PortsResponse.class);
    }

    public Optional<BindResponse> getBind(String baseUrl, int portId) {
        try {
            ResponseEntity<BindResponse> response = restTemplate.getForEntity(
                    URI.create(baseUrl + "/ports/" + portId + "/bind"),
                    BindResponse.class
            );
            return Optional.ofNullable(response.getBody());
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    public void bind(String baseUrl, int portId, int driverId) {
        HttpEntity<BindResponse> request = new HttpEntity<>(new BindResponse(driverId));
        restTemplate.exchange(
                URI.create(baseUrl + "/ports/" + portId + "/bind"),
                HttpMethod.PUT,
                request,
                Void.class
        );
    }

    public ReadResponse readValue(String baseUrl, int portId) {
        return restTemplate.getForObject(URI.create(baseUrl + "/ports/" + portId + "/control"), ReadResponse.class);
    }

    public void writeValue(String baseUrl, int portId, int level) {
        HttpEntity<LevelRequest> request = new HttpEntity<>(new LevelRequest(level));
        restTemplate.postForEntity(
                URI.create(baseUrl + "/ports/" + portId + "/control"),
                request,
                Void.class
        );
    }
}
