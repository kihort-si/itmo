package com.greenhouse.service;

import com.greenhouse.client.EspClient;
import com.greenhouse.client.dto.BindResponse;
import com.greenhouse.client.dto.DriversResponse;
import com.greenhouse.client.dto.InfoResponse;
import com.greenhouse.client.dto.PortsResponse;
import com.greenhouse.client.dto.ReadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Optional;


@Service
public class EspModuleService {

    private final EspClient espClient;

    public EspModuleService(EspClient espClient) {
        this.espClient = espClient;
    }

    public InfoResponse getInfo(String baseUrl) throws HttpStatusCodeException {
        return espClient.getInfo(baseUrl);
    }

    public PortsResponse getPorts(String baseUrl) throws HttpStatusCodeException {
        return espClient.getPorts(baseUrl);
    }

    public DriversResponse getDrivers(String baseUrl) throws HttpStatusCodeException {
        return espClient.getDrivers(baseUrl);
    }

    public Optional<BindResponse> getBinding(String baseUrl, int portId) throws HttpStatusCodeException {
        return espClient.getBind(baseUrl, portId);
    }

    public void bind(String baseUrl, int portId, int driverId) throws HttpStatusCodeException {
        espClient.bind(baseUrl, portId, driverId);
    }

    public ReadResponse read(String baseUrl, int portId) throws HttpStatusCodeException {
        return espClient.readValue(baseUrl, portId);
    }

    public void write(String baseUrl, int portId, int level) throws HttpStatusCodeException {
        espClient.writeValue(baseUrl, portId, level);
    }
}
