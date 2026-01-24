package com.greenhouse.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenhouse.client.dto.BindResponse;
import com.greenhouse.client.dto.DriversResponse;
import com.greenhouse.client.dto.EspDriver;
import com.greenhouse.client.dto.EspPort;
import com.greenhouse.client.dto.InfoResponse;
import com.greenhouse.client.dto.PortsResponse;
import com.greenhouse.client.dto.ReadResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class EspClientTest {

    private static final String BASE_URL = "http://esp-module.local";

    private EspClient espClient;
    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        espClient = new EspClient(restTemplate);
        mockServer = MockRestServiceServer.createServer(restTemplate);
        objectMapper = new ObjectMapper();
    }

    // Проверяет: получение информации о модуле (GET /info)
    @Test
    void getInfo_shouldReturnModuleId() throws Exception {
        InfoResponse expectedResponse = new InfoResponse();
        expectedResponse.setModuleId(1);

        mockServer.expect(requestTo(BASE_URL + "/info"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON));

        InfoResponse result = espClient.getInfo(BASE_URL);

        assertThat(result).isNotNull();
        assertThat(result.getModuleId()).isEqualTo(1);
        mockServer.verify();
    }

    // Проверяет: получение списка поддерживаемых драйверов (GET /drivers)
    @Test
    void getDrivers_shouldReturnSupportedDrivers() throws Exception {
        EspDriver dht22 = new EspDriver();
        dht22.setId(1);
        dht22.setName("DHT22");
        dht22.setType(0);

        EspDriver relay = new EspDriver();
        relay.setId(2);
        relay.setName("Relay");
        relay.setType(2);

        DriversResponse response = new DriversResponse();
        response.setDrivers(List.of(dht22, relay));

        mockServer.expect(requestTo(BASE_URL + "/drivers"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        DriversResponse result = espClient.getDrivers(BASE_URL);

        assertThat(result).isNotNull();
        assertThat(result.getDrivers()).hasSize(2);
        assertThat(result.getDrivers().get(0).getName()).isEqualTo("DHT22");
        assertThat(result.getDrivers().get(0).getType()).isEqualTo(0);
        assertThat(result.getDrivers().get(1).getName()).isEqualTo("Relay");
        assertThat(result.getDrivers().get(1).getType()).isEqualTo(2);
        mockServer.verify();
    }

    // Проверяет: получение списка портов (GET /ports)
    @Test
    void getPorts_shouldReturnAvailablePorts() throws Exception {
        EspPort port1 = new EspPort();
        port1.setId(1);
        port1.setType(0);

        EspPort port2 = new EspPort();
        port2.setId(2);
        port2.setType(2);

        PortsResponse response = new PortsResponse();
        response.setPorts(List.of(port1, port2));

        mockServer.expect(requestTo(BASE_URL + "/ports"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        PortsResponse result = espClient.getPorts(BASE_URL);

        assertThat(result).isNotNull();
        assertThat(result.getPorts()).hasSize(2);
        assertThat(result.getPorts().get(0).getId()).isEqualTo(1);
        assertThat(result.getPorts().get(0).getType()).isEqualTo(0);
        assertThat(result.getPorts().get(1).getId()).isEqualTo(2);
        assertThat(result.getPorts().get(1).getType()).isEqualTo(2);
        mockServer.verify();
    }

    // Проверяет: успешную привязку драйвера к порту (PUT /ports/{port}/bind)
    @Test
    void bind_shouldSuccessfullyBindDriverToPort() throws Exception {
        int portId = 1;
        int driverId = 1;

        mockServer.expect(requestTo(BASE_URL + "/ports/" + portId + "/bind"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.driverId").value(driverId))
                .andRespond(withSuccess());

        espClient.bind(BASE_URL, portId, driverId);

        mockServer.verify();
    }

    // Проверяет: ошибка 409 при несовместимости драйвера с портом
    @Test
    void bind_shouldThrowExceptionWhen_driverIncompatibleWithPort() {
        int portId = 1;
        int driverId = 2;

        mockServer.expect(requestTo(BASE_URL + "/ports/" + portId + "/bind"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> espClient.bind(BASE_URL, portId, driverId))
                .isInstanceOf(HttpClientErrorException.Conflict.class);
        mockServer.verify();
    }

    // Проверяет: ошибка 422 когда драйвер или порт не найден
    @Test
    void bind_shouldThrowExceptionWhen_driverOrPortNotFound() {
        int portId = 999;
        int driverId = 1;

        mockServer.expect(requestTo(BASE_URL + "/ports/" + portId + "/bind"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        assertThatThrownBy(() -> espClient.bind(BASE_URL, portId, driverId))
                .isInstanceOf(HttpClientErrorException.UnprocessableEntity.class);
        mockServer.verify();
    }

    // Проверяет: получение информации о привязанном драйвере (GET /ports/{port}/bind)
    @Test
    void getBind_shouldReturnBoundDriver() throws Exception {
        int portId = 1;
        BindResponse response = new BindResponse(1);

        mockServer.expect(requestTo(BASE_URL + "/ports/" + portId + "/bind"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        Optional<BindResponse> result = espClient.getBind(BASE_URL, portId);

        assertThat(result).isPresent();
        assertThat(result.get().getDriverId()).isEqualTo(1);
        mockServer.verify();
    }

    // Проверяет: поведение при запросе привязки для непривязанного порта (ожидается пустой Optional)
    @Test
    void getBind_shouldReturnEmpty_whenPortNotBound() {
        int portId = 1;

        mockServer.expect(requestTo(BASE_URL + "/ports/" + portId + "/bind"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        Optional<BindResponse> result = espClient.getBind(BASE_URL, portId);

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    // Проверяет: ошибка 422 при запросе привязки несуществующего порта
    @Test
    void getBind_shouldThrowException_whenPortNotFound() {
        int portId = 999;

        mockServer.expect(requestTo(BASE_URL + "/ports/" + portId + "/bind"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        assertThatThrownBy(() -> espClient.getBind(BASE_URL, portId))
                .isInstanceOf(HttpClientErrorException.UnprocessableEntity.class);
        mockServer.verify();
    }

    // Проверяет: чтение значения с датчика (GET /ports/{port}/control)
    @Test
    void readValue_shouldReturnSensorValue() throws Exception {
        int portId = 1;
        ReadResponse response = new ReadResponse();
        response.setValue(23.5);

        mockServer.expect(requestTo(BASE_URL + "/ports/" + portId + "/control"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        ReadResponse result = espClient.readValue(BASE_URL, portId);

        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo(23.5f);
        mockServer.verify();
    }

    // Проверяет: ошибка при чтении с непривязанного порта (400 Bad Request)
    @Test
    void readValue_shouldThrowException_whenPortNotBound() {
        int portId = 1;

        mockServer.expect(requestTo(BASE_URL + "/ports/" + portId + "/control"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> espClient.readValue(BASE_URL, portId))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
        mockServer.verify();
    }

    // Проверяет: метод чтения не поддерживается (405 Method Not Allowed)
    @Test
    void readValue_shouldThrowException_whenMethodNotSupported() {
        int portId = 2;

        mockServer.expect(requestTo(BASE_URL + "/ports/" + portId + "/control"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.METHOD_NOT_ALLOWED));

        assertThatThrownBy(() -> espClient.readValue(BASE_URL, portId))
                .isInstanceOf(HttpClientErrorException.MethodNotAllowed.class);
        mockServer.verify();
    }

    // Проверяет: успешную запись значения на выходной порт (POST /ports/{port}/control)
    @Test
    void writeValue_shouldSuccessfullySetOutputLevel() {
        int portId = 2;
        int level = 128;

        mockServer.expect(requestTo(BASE_URL + "/ports/" + portId + "/control"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.level").value(level))
                .andRespond(withSuccess());

        espClient.writeValue(BASE_URL, portId, level);

        mockServer.verify();
    }

    // Проверяет: ошибка при записи на непривязанный порт (400 Bad Request)
    @Test
    void writeValue_shouldThrowException_whenPortNotBound() {
        int portId = 2;
        int level = 255;

        mockServer.expect(requestTo(BASE_URL + "/ports/" + portId + "/control"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> espClient.writeValue(BASE_URL, portId, level))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
        mockServer.verify();
    }

    // Проверяет: метод записи не поддерживается (405 Method Not Allowed)
    @Test
    void writeValue_shouldThrowException_whenMethodNotSupported() {
        int portId = 1;

        mockServer.expect(requestTo(BASE_URL + "/ports/" + portId + "/control"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.METHOD_NOT_ALLOWED));

        assertThatThrownBy(() -> espClient.writeValue(BASE_URL, portId, 100))
                .isInstanceOf(HttpClientErrorException.MethodNotAllowed.class);
        mockServer.verify();
    }

    // Проверяет: валидация границ уровня (0 и 255)
    @Test
    void writeValue_shouldValidateLevelBoundaries() {
        int portId = 2;

        mockServer.expect(requestTo(BASE_URL + "/ports/" + portId + "/control"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.level").value(0))
                .andRespond(withSuccess());

        espClient.writeValue(BASE_URL, portId, 0);
        mockServer.verify();

        mockServer.reset();

        mockServer.expect(requestTo(BASE_URL + "/ports/" + portId + "/control"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.level").value(255))
                .andRespond(withSuccess());

        espClient.writeValue(BASE_URL, portId, 255);

        mockServer.verify();
    }

    // Проверяет: обработку серверной ошибки (500)
    @Test
    void allEndpoints_shouldHandleNetworkErrors() {
        mockServer.expect(requestTo(BASE_URL + "/info"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> espClient.getInfo(BASE_URL))
                .isInstanceOf(HttpServerErrorException.class);
        mockServer.verify();
    }
}
